package ca.bc.gov.educ.graddatacollection.api.orchestrator;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome;
import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.DemographicStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.IncomingFilesetMapper;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.*;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
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
import static org.assertj.core.api.Assertions.assertThat;
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
        var mockSchool = createMockSchool();
        mockSchool.setVendorSourceSystemCode("MYED");
        var mockReportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithDEMFile(UUID.fromString(mockSchool.getSchoolId()), mockReportingPeriod);
        var mockDemStudent = createMockDemographicStudent(mockFileset);

        incomingFilesetRepository.save(mockFileset);

        val demographicStudent = DemographicStudentMapper.mapper.toDemographicStudent(mockDemStudent);
        val fileset = IncomingFilesetMapper.mapper.toStructure(mockFileset);
        val saga = createCompletedFilesetMockSaga(fileset, demographicStudent);
        saga.setSagaId(null);
        sagaRepository.save(saga);

        val sagaData = IncomingFilesetSagaData.builder().incomingFileset(fileset).demographicStudent(demographicStudent).build();

        var school = createMockSchool();
        school.setVendorSourceSystemCode(demographicStudent.getVendorID());
        when(restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));

        val event = Event.builder()
                .sagaId(saga.getSagaId())
                .eventType(UPDATE_COMPLETED_FILESET_STATUS)
                .eventOutcome(EventOutcome.COMPLETED_FILESET_STATUS_UPDATED)
                .eventPayload(JsonUtil.getJsonStringFromObject(sagaData)).build();
        completedFilesetProcessingOrchestrator.handleEvent(event);

        verify(messagePublisher, atMost(2)).dispatchMessage(eq(completedFilesetProcessingOrchestrator.getTopicToSubscribe()), eventCaptor.capture());
        final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
        assertThat(newEvent.getEventType()).isEqualTo(UPDATE_COMPLETED_FILESET_STATUS_AND_SOURCE_SYSTEM_VENDOR_CODE_REQUIRED);
        assertThat(newEvent.getEventOutcome()).isEqualTo(EventOutcome.COMPLETED_FILESET_STATUS_UPDATED_SOURCE_SYSTEM_VENDOR_CODE_DOES_NOT_NEED_UPDATE);

        val savedSagaInDB = sagaRepository.findById(saga.getSagaId());
        assertThat(savedSagaInDB).isPresent();
        assertThat(savedSagaInDB.get().getStatus()).isEqualTo(IN_PROGRESS.toString());
        assertThat(savedSagaInDB.get().getSagaState()).isEqualTo(CHECK_SOURCE_SYSTEM_VENDOR_CODE_IN_INSTITUTE_AND_UPDATE_IF_REQUIRED.toString());

        assertThat(school.getVendorSourceSystemCode()).isEqualTo(demographicStudent.getVendorID());
    }
}
