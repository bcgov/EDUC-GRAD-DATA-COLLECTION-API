package ca.bc.gov.educ.graddatacollection.api.orchestrator;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome;
import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.AssessmentStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.model.v1.GradSagaEntity;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.*;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.AssessmentStudentSagaData;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import com.fasterxml.jackson.databind.json.JsonMapper;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.graddatacollection.api.constants.EventType.VALIDATE_ASSESSMENT_STUDENT;
import static ca.bc.gov.educ.graddatacollection.api.constants.SagaStatusEnum.COMPLETED;
import static ca.bc.gov.educ.graddatacollection.api.constants.SagaStatusEnum.IN_PROGRESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class AssessmentStudentProcessingOrchestratorTest extends BaseGradDataCollectionAPITest {

    @MockBean
    protected RestUtils restUtils;
    @Autowired
    SagaRepository sagaRepository;
    @Autowired
    SagaEventRepository sagaEventRepository;
    @Autowired
    MessagePublisher messagePublisher;
    @Autowired
    AssessmentStudentRepository assessmentStudentRepository;
    @Autowired
    IncomingFilesetRepository incomingFilesetRepository;
    @Autowired
    ReportingPeriodRepository reportingPeriodRepository;
    @Autowired
    AssessmentStudentProcessingOrchestrator assessmentStudentProcessingOrchestrator;
    @Autowired
    DemographicStudentRepository demographicStudentRepository;
    @Captor
    ArgumentCaptor<byte[]> eventCaptor;

    @BeforeEach
    public void setUp() {
        Mockito.reset(messagePublisher);
        Mockito.reset(restUtils);
        sagaEventRepository.deleteAll();
        sagaRepository.deleteAll();
        demographicStudentRepository.deleteAll();
        assessmentStudentRepository.deleteAll();
        incomingFilesetRepository.deleteAll();
        reportingPeriodRepository.deleteAll();
        JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    @SneakyThrows
    @Test
    void testHandleEvent_givenEventTypeInitiated_validateAssessmentStudentRecordWithEventOutCome_VALIDATE_ASSESSMENT_STUDENT_SUCCESS_WITH_ERROR() {
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(anyString(), anyString())).thenReturn(Optional.of(createMockSession()));
        val loadedSaga = createLoadedAssessmentStudentSaga();

        this.assessmentStudentProcessingOrchestrator.handleEvent(loadedSaga.initiatedEvent());

        final var newEvent = getLastEventSentToSagaTopic();
        assertThat(newEvent.getEventType()).isEqualTo(VALIDATE_ASSESSMENT_STUDENT);
        assertThat(newEvent.getEventOutcome()).isEqualTo(EventOutcome.VALIDATE_ASSESSMENT_STUDENT_SUCCESS_WITH_ERROR);

        val savedSagaInDB = this.sagaRepository.findById(loadedSaga.saga().getSagaId());
        assertThat(savedSagaInDB).isPresent();
        assertThat(savedSagaInDB.get().getStatus()).isEqualTo(IN_PROGRESS.toString());
        assertThat(savedSagaInDB.get().getSagaState()).isEqualTo(VALIDATE_ASSESSMENT_STUDENT.toString());
    }

    @SneakyThrows
    @Test
    void testHandleEvent_givenSessionClosedAfterXamLoad_dropsAssessmentStudentAndCompletesSagaWithoutWritingToAssessmentApi() {
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(anyString(), anyString())).thenReturn(Optional.empty());
        val loadedSaga = createLoadedAssessmentStudentSaga();

        this.assessmentStudentProcessingOrchestrator.handleEvent(loadedSaga.initiatedEvent());

        verify(this.restUtils).loadAssessmentSessionMapIfNeverLoaded();
        final var ignoredEvent = getLastEventSentToSagaTopic();
        assertThat(ignoredEvent.getEventType()).isEqualTo(VALIDATE_ASSESSMENT_STUDENT);
        assertThat(ignoredEvent.getEventOutcome()).isEqualTo(EventOutcome.ASSESSMENT_STUDENT_IGNORED_FOR_CLOSED_SESSION);
        assertThat(this.assessmentStudentRepository.findById(loadedSaga.assessmentStudentID())).isEmpty();

        this.assessmentStudentProcessingOrchestrator.handleEvent(ignoredEvent);

        assertThat(this.sagaRepository.findById(loadedSaga.saga().getSagaId()).orElseThrow().getStatus()).isEqualTo(COMPLETED.toString());
        verify(this.restUtils, never()).writeAssessmentStudentDetailInAssessmentService(any(), any(), any(), any());
    }

    @SneakyThrows
    @Test
    void testHandleEvent_givenSessionMissingFromUnloadedCacheButOpenAfterLoad_keepsAndValidatesAssessmentStudent() {
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(anyString(), anyString()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(createMockSession()));
        val loadedSaga = createLoadedAssessmentStudentSaga();

        this.assessmentStudentProcessingOrchestrator.handleEvent(loadedSaga.initiatedEvent());

        final var newEvent = getLastEventSentToSagaTopic();
        assertThat(newEvent.getEventOutcome()).isEqualTo(EventOutcome.VALIDATE_ASSESSMENT_STUDENT_SUCCESS_WITH_ERROR);
        assertThat(this.assessmentStudentRepository.findById(loadedSaga.assessmentStudentID())).isPresent();
    }

    @Test
    void testHandleEvent_givenSessionCacheLoadFails_keepsAssessmentStudentForSagaRetry() {
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(anyString(), anyString())).thenReturn(Optional.empty());
        doThrow(new GradDataCollectionAPIRuntimeException("NATS timeout")).when(this.restUtils).loadAssessmentSessionMapIfNeverLoaded();
        val loadedSaga = createLoadedAssessmentStudentSaga();
        val initiatedEvent = loadedSaga.initiatedEvent();

        assertThatThrownBy(() -> this.assessmentStudentProcessingOrchestrator.handleEvent(initiatedEvent))
                .isInstanceOf(GradDataCollectionAPIRuntimeException.class);
        assertThat(this.assessmentStudentRepository.findById(loadedSaga.assessmentStudentID())).isPresent();
    }

    private record LoadedAssessmentStudentSaga(GradSagaEntity saga, Event initiatedEvent, UUID assessmentStudentID) {}

    @SneakyThrows
    private LoadedAssessmentStudentSaga createLoadedAssessmentStudentSaga() {
        var school = this.createMockSchoolTombstone();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithCRSFile(UUID.fromString(school.getSchoolId()), reportingPeriod);
        incomingFilesetRepository.save(mockFileset);

        var assessmentStudentEntity = createMockAssessmentStudent();
        assessmentStudentEntity.setIncomingFileset(mockFileset);
        assessmentStudentEntity.setAssessmentStudentID(null);
        assessmentStudentEntity.setStudentStatusCode("LOADED");
        assessmentStudentEntity.setCreateDate(LocalDateTime.now().minusMinutes(14));
        assessmentStudentEntity.setUpdateDate(LocalDateTime.now());
        assessmentStudentEntity.setCreateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);
        assessmentStudentEntity.setUpdateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);

        assessmentStudentRepository.save(assessmentStudentEntity);

        val assessmentStudent = AssessmentStudentMapper.mapper.toAssessmentStudent(assessmentStudentEntity);
        val saga = this.createAssessmentMockSaga(assessmentStudent);
        saga.setSagaId(null);
        this.sagaRepository.save(saga);

        val sagaData = AssessmentStudentSagaData.builder().assessmentStudent(assessmentStudent).school(createMockSchoolTombstone()).build();
        val event = Event.builder()
                .sagaId(saga.getSagaId())
                .eventType(EventType.INITIATED)
                .eventOutcome(EventOutcome.INITIATE_SUCCESS)
                .eventPayload(JsonUtil.getJsonStringFromObject(sagaData)).build();
        return new LoadedAssessmentStudentSaga(saga, event, assessmentStudentEntity.getAssessmentStudentID());
    }

    @SneakyThrows
    private Event getLastEventSentToSagaTopic() {
        verify(this.messagePublisher, atLeastOnce()).dispatchMessage(eq(this.assessmentStudentProcessingOrchestrator.getTopicToSubscribe()), this.eventCaptor.capture());
        return JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    }
}
