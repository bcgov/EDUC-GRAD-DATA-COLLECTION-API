package ca.bc.gov.educ.graddatacollection.api.orchestrator;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome;
import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.DemographicStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.IncomingFilesetMapper;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.*;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.InstituteStatusEvent;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.School;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFilesetSagaData;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.graddatacollection.api.constants.EventType.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.SagaStatusEnum.IN_PROGRESS;
import static ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class CompletedFilesetProcessingOrchestratorTest extends BaseGradDataCollectionAPITest {

    @MockBean
    protected RestUtils restUtils;
    @Autowired
    SagaRepository sagaRepository;
    @Autowired
    SagaEventRepository sagaEventRepository;
    @Autowired
    MessagePublisher messagePublisher;
    @Autowired
    DemographicStudentRepository demographicStudentRepository;
    @Autowired
    CourseStudentRepository courseStudentRepository;
    @Autowired
    IncomingFilesetRepository incomingFilesetRepository;
    @Autowired
    CompletedFilesetProcessingOrchestrator completedFilesetProcessingOrchestrator;
    @Autowired
    ErrorFilesetStudentRepository errorFilesetStudentRepository;
    @Captor
    ArgumentCaptor<byte[]> eventCaptor;
    @Autowired
    private ReportingPeriodRepository reportingPeriodRepository;

    @BeforeEach
    void setUp() {
        Mockito.reset(messagePublisher);
        Mockito.reset(restUtils);
        sagaEventRepository.deleteAll();
        sagaRepository.deleteAll();
        courseStudentRepository.deleteAll();
        demographicStudentRepository.deleteAll();
        errorFilesetStudentRepository.deleteAll();
        incomingFilesetRepository.deleteAll();
        reportingPeriodRepository.deleteAll();
    }

    @SneakyThrows
    @Test
    void testHandleEvent_givenEventTypeInitiated_updateIncomingFilesetStatus() {
        var mockReportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(mockReportingPeriod);
        incomingFilesetRepository.save(mockFileset);

        var mockDemStudent = createMockDemographicStudent(mockFileset);

        val demographicStudent = DemographicStudentMapper.mapper.toDemographicStudent(mockDemStudent);
        val fileset = IncomingFilesetMapper.mapper.toStructure(mockFileset);
        val saga = createCompletedFilesetMockSaga(fileset, demographicStudent);
        saga.setSagaId(null);
        sagaRepository.save(saga);

        val sagaData = IncomingFilesetSagaData.builder().incomingFileset(fileset).demographicStudent(demographicStudent).build();
        val event = Event.builder()
                .sagaId(saga.getSagaId())
                .eventType(EventType.INITIATED)
                .eventOutcome(EventOutcome.INITIATE_SUCCESS)
                .eventPayload(JsonUtil.getJsonStringFromObject(sagaData)).build();
        completedFilesetProcessingOrchestrator.handleEvent(event);

        verify(messagePublisher, atMost(2)).dispatchMessage(eq(completedFilesetProcessingOrchestrator.getTopicToSubscribe()), eventCaptor.capture());
        final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
        assertThat(newEvent.getEventType()).isEqualTo(UPDATE_COMPLETED_FILESET_STATUS);
        assertThat(newEvent.getEventOutcome()).isEqualTo(EventOutcome.COMPLETED_FILESET_STATUS_UPDATED);

        val savedSagaInDB = sagaRepository.findById(saga.getSagaId());
        assertThat(savedSagaInDB).isPresent();
        assertThat(savedSagaInDB.get().getStatus()).isEqualTo(IN_PROGRESS.toString());
        assertThat(savedSagaInDB.get().getSagaState()).isEqualTo(UPDATE_COMPLETED_FILESET_STATUS.toString());
    }

    @SneakyThrows
    @Test
    void testHandleEvent_givenEventTypeUpdateVendorCode_updateRequired() {
        var mockSchool = createMockSchoolTombstone();
        mockSchool.setVendorSourceSystemCode("MYED");
        var mockReportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithDEMFile(UUID.fromString(mockSchool.getSchoolId()), mockReportingPeriod);
        var mockDemStudent = createMockDemographicStudent(mockFileset);

        incomingFilesetRepository.save(mockFileset);
        demographicStudentRepository.save(mockDemStudent);

        val demographicStudent = DemographicStudentMapper.mapper.toDemographicStudent(mockDemStudent);
        val fileset = IncomingFilesetMapper.mapper.toStructure(mockFileset);
        val saga = createCompletedFilesetMockSaga(fileset, demographicStudent);
        saga.setSagaId(null);
        sagaRepository.save(saga);

        val sagaData = IncomingFilesetSagaData.builder().incomingFileset(fileset).demographicStudent(demographicStudent).build();

        var school = createMockSchool();
        school.setVendorSourceSystemCode("MYED");
        when(restUtils.getSchoolFromSchoolID(any(), any())).thenReturn(school);

        var mockInstituteStatusEvent = new InstituteStatusEvent();
        mockInstituteStatusEvent.setEventOutcome(EventOutcome.SCHOOL_UPDATED.toString());
        when(restUtils.updateSchool(any(School.class), any(UUID.class))).thenReturn(mockInstituteStatusEvent);

        val event = Event.builder()
                .sagaId(saga.getSagaId())
                .eventType(UPDATE_COMPLETED_FILESET_STATUS)
                .eventOutcome(EventOutcome.COMPLETED_FILESET_STATUS_UPDATED)
                .eventPayload(JsonUtil.getJsonStringFromObject(sagaData)).build();
        completedFilesetProcessingOrchestrator.handleEvent(event);

        verify(messagePublisher, atMost(2)).dispatchMessage(eq(completedFilesetProcessingOrchestrator.getTopicToSubscribe()), eventCaptor.capture());
        final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
        assertThat(newEvent.getEventType()).isEqualTo(UPDATE_COMPLETED_FILESET_STATUS_AND_SOURCE_SYSTEM_VENDOR_CODE_REQUIRED);
        assertThat(newEvent.getEventOutcome()).isEqualTo(EventOutcome.COMPLETED_FILESET_STATUS_AND_SOURCE_SYSTEM_VENDOR_CODE_UPDATED);

        val savedSagaInDB = sagaRepository.findById(saga.getSagaId());
        assertThat(savedSagaInDB).isPresent();
        assertThat(savedSagaInDB.get().getStatus()).isEqualTo(IN_PROGRESS.toString());
        assertThat(savedSagaInDB.get().getSagaState()).isEqualTo(CHECK_SOURCE_SYSTEM_VENDOR_CODE_IN_INSTITUTE_AND_UPDATE_IF_REQUIRED.toString());

        assertThat(school.getVendorSourceSystemCode()).isEqualTo(demographicStudent.getVendorID());
    }

    @SneakyThrows
    @Test
    void testCheckVendorSourceSystemCode_whenSchoolIsNull_shouldNotUpdateVendorCode() {
        var mockReportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithDEMFile(UUID.randomUUID(), mockReportingPeriod);
        incomingFilesetRepository.save(mockFileset);
        var mockDemStudent = createMockDemographicStudent(mockFileset);
        mockDemStudent.setVendorID("M");

        val demographicStudent = DemographicStudentMapper.mapper.toDemographicStudent(mockDemStudent);
        val fileset = IncomingFilesetMapper.mapper.toStructure(mockFileset);
        val saga = createCompletedFilesetMockSaga(fileset, demographicStudent);
        saga.setSagaId(null);
        sagaRepository.save(saga);

        val sagaData = IncomingFilesetSagaData.builder().incomingFileset(fileset).demographicStudent(demographicStudent).build();

        when(restUtils.getSchoolFromSchoolID(any(UUID.class), any(UUID.class))).thenReturn(null);

        val event = Event.builder()
                .sagaId(saga.getSagaId())
                .eventType(UPDATE_COMPLETED_FILESET_STATUS)
                .eventOutcome(EventOutcome.COMPLETED_FILESET_STATUS_UPDATED)
                .eventPayload(JsonUtil.getJsonStringFromObject(sagaData)).build();

        completedFilesetProcessingOrchestrator.handleEvent(event);

        verify(messagePublisher, atMost(2)).dispatchMessage(eq(completedFilesetProcessingOrchestrator.getTopicToSubscribe()), eventCaptor.capture());
        final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
        assertThat(newEvent.getEventType()).isEqualTo(UPDATE_COMPLETED_FILESET_STATUS_AND_SOURCE_SYSTEM_VENDOR_CODE_NOT_REQUIRED);
        assertThat(newEvent.getEventOutcome()).isEqualTo(COMPLETED_FILESET_STATUS_UPDATED_SOURCE_SYSTEM_VENDOR_CODE_DOES_NOT_NEED_UPDATE);

        verify(restUtils, never()).updateSchool(any(School.class), any(UUID.class));
    }

    @SneakyThrows
    @Test
    void testCheckVendorSourceSystemCode_whenVendorIdIsM_andSchoolVendorCodeNotMYED_shouldUpdateToMYED() {
        var mockReportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithDEMFile(UUID.randomUUID(), mockReportingPeriod);
        incomingFilesetRepository.save(mockFileset);
        var mockDemStudent = createMockDemographicStudent(mockFileset);
        mockDemStudent.setVendorID("M");

        val demographicStudent = DemographicStudentMapper.mapper.toDemographicStudent(mockDemStudent);
        val fileset = IncomingFilesetMapper.mapper.toStructure(mockFileset);
        val saga = createCompletedFilesetMockSaga(fileset, demographicStudent);
        saga.setSagaId(null);
        sagaRepository.save(saga);

        val sagaData = IncomingFilesetSagaData.builder().incomingFileset(fileset).demographicStudent(demographicStudent).build();

        School school = createMockSchool();
        school.setVendorSourceSystemCode("OTHER");
        when(restUtils.getSchoolFromSchoolID(any(UUID.class), any(UUID.class))).thenReturn(school);

        InstituteStatusEvent successResponse = new InstituteStatusEvent();
        successResponse.setEventOutcome(SCHOOL_UPDATED.toString());
        when(restUtils.updateSchool(any(School.class), any(UUID.class))).thenReturn(successResponse);

        val event = Event.builder()
                .sagaId(saga.getSagaId())
                .eventType(UPDATE_COMPLETED_FILESET_STATUS)
                .eventOutcome(EventOutcome.COMPLETED_FILESET_STATUS_UPDATED)
                .eventPayload(JsonUtil.getJsonStringFromObject(sagaData)).build();

        completedFilesetProcessingOrchestrator.handleEvent(event);

        verify(messagePublisher, atMost(2)).dispatchMessage(eq(completedFilesetProcessingOrchestrator.getTopicToSubscribe()), eventCaptor.capture());
        final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
        assertThat(newEvent.getEventType()).isEqualTo(UPDATE_COMPLETED_FILESET_STATUS_AND_SOURCE_SYSTEM_VENDOR_CODE_REQUIRED);
        assertThat(newEvent.getEventOutcome()).isEqualTo(COMPLETED_FILESET_STATUS_AND_SOURCE_SYSTEM_VENDOR_CODE_UPDATED);

        ArgumentCaptor<School> schoolCaptor = ArgumentCaptor.forClass(School.class);
        verify(restUtils).updateSchool(schoolCaptor.capture(), any(UUID.class));
        assertThat(schoolCaptor.getValue().getVendorSourceSystemCode()).isEqualTo("MYED");
    }

    @SneakyThrows
    @Test
    void testCheckVendorSourceSystemCode_whenVendorIdIsNotM_andSchoolVendorCodeIsMYED_shouldUpdateToOTHER() {
        var mockReportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithDEMFile(UUID.randomUUID(), mockReportingPeriod);
        incomingFilesetRepository.save(mockFileset);
        var mockDemStudent = createMockDemographicStudent(mockFileset);
        mockDemStudent.setVendorID("ABC");

        val demographicStudent = DemographicStudentMapper.mapper.toDemographicStudent(mockDemStudent);
        val fileset = IncomingFilesetMapper.mapper.toStructure(mockFileset);
        val saga = createCompletedFilesetMockSaga(fileset, demographicStudent);
        saga.setSagaId(null);
        sagaRepository.save(saga);

        val sagaData = IncomingFilesetSagaData.builder().incomingFileset(fileset).demographicStudent(demographicStudent).build();

        School school = createMockSchool();
        school.setVendorSourceSystemCode("MYED");
        when(restUtils.getSchoolFromSchoolID(any(UUID.class), any(UUID.class))).thenReturn(school);

        InstituteStatusEvent successResponse = new InstituteStatusEvent();
        successResponse.setEventOutcome(SCHOOL_UPDATED.toString());
        when(restUtils.updateSchool(any(School.class), any(UUID.class))).thenReturn(successResponse);

        val event = Event.builder()
                .sagaId(saga.getSagaId())
                .eventType(UPDATE_COMPLETED_FILESET_STATUS)
                .eventOutcome(EventOutcome.COMPLETED_FILESET_STATUS_UPDATED)
                .eventPayload(JsonUtil.getJsonStringFromObject(sagaData)).build();

        completedFilesetProcessingOrchestrator.handleEvent(event);

        verify(messagePublisher, atMost(2)).dispatchMessage(eq(completedFilesetProcessingOrchestrator.getTopicToSubscribe()), eventCaptor.capture());
        final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
        assertThat(newEvent.getEventType()).isEqualTo(UPDATE_COMPLETED_FILESET_STATUS_AND_SOURCE_SYSTEM_VENDOR_CODE_REQUIRED);
        assertThat(newEvent.getEventOutcome()).isEqualTo(COMPLETED_FILESET_STATUS_AND_SOURCE_SYSTEM_VENDOR_CODE_UPDATED);

        ArgumentCaptor<School> schoolCaptor = ArgumentCaptor.forClass(School.class);
        verify(restUtils).updateSchool(schoolCaptor.capture(), any(UUID.class));
        assertThat(schoolCaptor.getValue().getVendorSourceSystemCode()).isEqualTo("OTHER");
    }

    @SneakyThrows
    @Test
    void testCheckVendorSourceSystemCode_whenVendorIdIsM_andSchoolVendorCodeIsMYED_shouldNotUpdate() {
        var mockReportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithDEMFile(UUID.randomUUID(), mockReportingPeriod);
        incomingFilesetRepository.save(mockFileset);
        var mockDemStudent = createMockDemographicStudent(mockFileset);
        mockDemStudent.setVendorID("M");

        val demographicStudent = DemographicStudentMapper.mapper.toDemographicStudent(mockDemStudent);
        val fileset = IncomingFilesetMapper.mapper.toStructure(mockFileset);
        val saga = createCompletedFilesetMockSaga(fileset, demographicStudent);
        saga.setSagaId(null);
        sagaRepository.save(saga);

        val sagaData = IncomingFilesetSagaData.builder().incomingFileset(fileset).demographicStudent(demographicStudent).build();

        School school = createMockSchool();
        school.setVendorSourceSystemCode("MYED");
        when(restUtils.getSchoolFromSchoolID(any(UUID.class), any(UUID.class))).thenReturn(school);

        val event = Event.builder()
                .sagaId(saga.getSagaId())
                .eventType(UPDATE_COMPLETED_FILESET_STATUS)
                .eventOutcome(EventOutcome.COMPLETED_FILESET_STATUS_UPDATED)
                .eventPayload(JsonUtil.getJsonStringFromObject(sagaData)).build();

        completedFilesetProcessingOrchestrator.handleEvent(event);

        verify(messagePublisher, atMost(2)).dispatchMessage(eq(completedFilesetProcessingOrchestrator.getTopicToSubscribe()), eventCaptor.capture());
        final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
        assertThat(newEvent.getEventType()).isEqualTo(UPDATE_COMPLETED_FILESET_STATUS_AND_SOURCE_SYSTEM_VENDOR_CODE_NOT_REQUIRED);
        assertThat(newEvent.getEventOutcome()).isEqualTo(COMPLETED_FILESET_STATUS_UPDATED_SOURCE_SYSTEM_VENDOR_CODE_DOES_NOT_NEED_UPDATE);

        verify(restUtils, never()).updateSchool(any(School.class), any(UUID.class));
    }

    @SneakyThrows
    @Test
    void testCheckVendorSourceSystemCode_whenVendorIdIsNotM_andSchoolVendorCodeIsNotMYED_shouldNotUpdate() {
        var mockReportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithDEMFile(UUID.randomUUID(), mockReportingPeriod);
        incomingFilesetRepository.save(mockFileset);
        var mockDemStudent = createMockDemographicStudent(mockFileset);
        mockDemStudent.setVendorID("ABC");

        val demographicStudent = DemographicStudentMapper.mapper.toDemographicStudent(mockDemStudent);
        val fileset = IncomingFilesetMapper.mapper.toStructure(mockFileset);
        val saga = createCompletedFilesetMockSaga(fileset, demographicStudent);
        saga.setSagaId(null);
        sagaRepository.save(saga);

        val sagaData = IncomingFilesetSagaData.builder().incomingFileset(fileset).demographicStudent(demographicStudent).build();

        School school = createMockSchool();
        school.setVendorSourceSystemCode("OTHER");
        when(restUtils.getSchoolFromSchoolID(any(UUID.class), any(UUID.class))).thenReturn(school);

        val event = Event.builder()
                .sagaId(saga.getSagaId())
                .eventType(UPDATE_COMPLETED_FILESET_STATUS)
                .eventOutcome(EventOutcome.COMPLETED_FILESET_STATUS_UPDATED)
                .eventPayload(JsonUtil.getJsonStringFromObject(sagaData)).build();

        completedFilesetProcessingOrchestrator.handleEvent(event);

        verify(messagePublisher, atMost(2)).dispatchMessage(eq(completedFilesetProcessingOrchestrator.getTopicToSubscribe()), eventCaptor.capture());
        final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
        assertThat(newEvent.getEventType()).isEqualTo(UPDATE_COMPLETED_FILESET_STATUS_AND_SOURCE_SYSTEM_VENDOR_CODE_NOT_REQUIRED);
        assertThat(newEvent.getEventOutcome()).isEqualTo(COMPLETED_FILESET_STATUS_UPDATED_SOURCE_SYSTEM_VENDOR_CODE_DOES_NOT_NEED_UPDATE);

        verify(restUtils, never()).updateSchool(any(School.class), any(UUID.class));
    }

    @SneakyThrows
    @Test
    void testCheckVendorSourceSystemCode_whenUpdateFails_shouldThrowException() {
        var mockReportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithDEMFile(UUID.randomUUID(), mockReportingPeriod);
        incomingFilesetRepository.save(mockFileset);
        var mockDemStudent = createMockDemographicStudent(mockFileset);
        mockDemStudent.setVendorID("M");

        val demographicStudent = DemographicStudentMapper.mapper.toDemographicStudent(mockDemStudent);
        val fileset = IncomingFilesetMapper.mapper.toStructure(mockFileset);
        val saga = createCompletedFilesetMockSaga(fileset, demographicStudent);
        saga.setSagaId(null);
        sagaRepository.save(saga);

        val sagaData = IncomingFilesetSagaData.builder().incomingFileset(fileset).demographicStudent(demographicStudent).build();

        School school = createMockSchool();
        school.setVendorSourceSystemCode("OTHER");
        when(restUtils.getSchoolFromSchoolID(any(UUID.class), any(UUID.class))).thenReturn(school);

        InstituteStatusEvent failureResponse = new InstituteStatusEvent();
        failureResponse.setEventOutcome("SCHOOL_UPDATE_FAILED");
        when(restUtils.updateSchool(any(School.class), any(UUID.class))).thenReturn(failureResponse);

        val event = Event.builder()
                .sagaId(saga.getSagaId())
                .eventType(UPDATE_COMPLETED_FILESET_STATUS)
                .eventOutcome(EventOutcome.COMPLETED_FILESET_STATUS_UPDATED)
                .eventPayload(JsonUtil.getJsonStringFromObject(sagaData)).build();

        assertThrows(GradDataCollectionAPIRuntimeException.class, () ->
                completedFilesetProcessingOrchestrator.handleEvent(event)
        );

        verify(restUtils).updateSchool(any(School.class), any(UUID.class));
    }
}
