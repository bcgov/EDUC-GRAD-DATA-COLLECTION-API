package ca.bc.gov.educ.graddatacollection.api.service.v1.events;

import ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome;
import ca.bc.gov.educ.graddatacollection.api.constants.SagaEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.SagaStatusEnum;
import ca.bc.gov.educ.graddatacollection.api.orchestrator.*;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.service.v1.SagaService;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.*;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * The type Event handler service.
 */
@Service
@Slf4j
public class EventHandlerService {

  @Getter(PRIVATE)
  private final SagaService sagaService;

  private final CompletedFilesetProcessingOrchestrator completedFilesetProcessingOrchestrator;
  private final DemographicStudentProcessingOrchestrator demographicStudentProcessingOrchestrator;
  private final CourseStudentProcessingOrchestrator courseStudentProcessingOrchestrator;
  private final AssessmentStudentProcessingOrchestrator assessmentStudentProcessingOrchestrator;
  private final UpdateCourseStudentDownstreamOrchestrator updateCourseStudentDownstreamOrchestrator;
  public static final String NO_EXECUTION_MSG = "Execution is not required for this message returning EVENT is :: {}";

  @Autowired
  public EventHandlerService(final SagaService sagaService, CompletedFilesetProcessingOrchestrator completedFilesetProcessingOrchestrator, final DemographicStudentProcessingOrchestrator demographicStudentProcessingOrchestrator, CourseStudentProcessingOrchestrator courseStudentProcessingOrchestrator, AssessmentStudentProcessingOrchestrator assessmentStudentProcessingOrchestrator, UpdateCourseStudentDownstreamOrchestrator updateCourseStudentDownstreamOrchestrator) {
    this.sagaService = sagaService;
    this.completedFilesetProcessingOrchestrator = completedFilesetProcessingOrchestrator;
    this.demographicStudentProcessingOrchestrator = demographicStudentProcessingOrchestrator;
    this.courseStudentProcessingOrchestrator = courseStudentProcessingOrchestrator;
    this.assessmentStudentProcessingOrchestrator = assessmentStudentProcessingOrchestrator;
    this.updateCourseStudentDownstreamOrchestrator = updateCourseStudentDownstreamOrchestrator;
  }

  @Transactional(propagation = REQUIRES_NEW)
  public void handleProcessCompletedFilesetsEvent(final Event event) throws JsonProcessingException {
    if (event.getEventOutcome() == EventOutcome.READ_COMPLETED_FILESETS_FOR_PROCESSING_SUCCESS) {
      final IncomingFilesetSagaData sagaData = JsonUtil.getJsonObjectFromString(IncomingFilesetSagaData.class, event.getEventPayload());
      final var sagaList = this.getSagaService().findByIncomingFilesetIDAndSagaNameAndStatusNot(sagaData.getIncomingFilesetID(), SagaEnum.PROCESS_COMPLETED_FILESETS_SAGA.toString(), SagaStatusEnum.COMPLETED.toString());
      if (!sagaList.isEmpty()) { // possible duplicate message.
        log.trace(NO_EXECUTION_MSG, event);
        return;
      }
      val saga = this.completedFilesetProcessingOrchestrator
              .createSaga(event.getEventPayload(),
                      ApplicationProperties.GRAD_DATA_COLLECTION_API,
                      sagaData.getIncomingFilesetID(),
                      null,
                      null,
                      null);
      log.debug("Starting incoming fileset processing orchestrator :: {}", saga);
      this.completedFilesetProcessingOrchestrator.startSaga(saga);
    }
  }

  @Transactional(propagation = REQUIRES_NEW)
  public void handleProcessDemStudentsEvent(final Event event) throws JsonProcessingException {
    if (event.getEventOutcome() == EventOutcome.READ_DEM_STUDENTS_FOR_PROCESSING_SUCCESS) {
      final DemographicStudentSagaData sagaData = JsonUtil.getJsonObjectFromString(DemographicStudentSagaData.class, event.getEventPayload());
      final var sagaList = this.getSagaService().findByDemographicStudentIDAndIncomingFilesetIDAndSagaNameAndStatusNot(UUID.fromString(sagaData.getDemographicStudent().getDemographicStudentID()), UUID.fromString(sagaData.getDemographicStudent().getIncomingFilesetID()), SagaEnum.PROCESS_DEM_STUDENTS_SAGA.toString(), SagaStatusEnum.COMPLETED.toString());
      if (!sagaList.isEmpty()) { // possible duplicate message.
        log.trace(NO_EXECUTION_MSG, event);
        return;
      }
      val saga = this.demographicStudentProcessingOrchestrator
              .createSaga(event.getEventPayload(),
                      ApplicationProperties.GRAD_DATA_COLLECTION_API,
                      UUID.fromString(sagaData.getDemographicStudent().getIncomingFilesetID()),
                      UUID.fromString(sagaData.getDemographicStudent().getDemographicStudentID()),
                      null,
                      null);
      log.debug("Starting dem student processing orchestrator :: {}", saga);
      this.demographicStudentProcessingOrchestrator.startSaga(saga);
    }
  }

  @Transactional(propagation = REQUIRES_NEW)
  public void handleProcessCourseStudentsEvent(final Event event) throws JsonProcessingException {
    if (event.getEventOutcome() == EventOutcome.READ_COURSE_STUDENTS_FOR_PROCESSING_SUCCESS) {
      final CourseStudentSagaData sagaData = JsonUtil.getJsonObjectFromString(CourseStudentSagaData.class, event.getEventPayload());
      final var sagaList = this.getSagaService().findByCourseStudentIDAndIncomingFilesetIDAndSagaNameAndStatusNot(UUID.fromString(sagaData.getCourseStudent().getCourseStudentID()), UUID.fromString(sagaData.getCourseStudent().getIncomingFilesetID()), SagaEnum.PROCESS_COURSE_STUDENTS_SAGA.toString(), SagaStatusEnum.COMPLETED.toString());
      if (!sagaList.isEmpty()) { // possible duplicate message.
        log.trace(NO_EXECUTION_MSG, event);
        return;
      }
      val saga = this.courseStudentProcessingOrchestrator
              .createSaga(event.getEventPayload(),
                      ApplicationProperties.GRAD_DATA_COLLECTION_API,
                      UUID.fromString(sagaData.getCourseStudent().getIncomingFilesetID()),
                      null,
                      null,
                      UUID.fromString(sagaData.getCourseStudent().getCourseStudentID()));
      log.debug("Starting course student processing orchestrator :: {}", saga);
      this.courseStudentProcessingOrchestrator.startSaga(saga);
    }
  }

  @Transactional(propagation = REQUIRES_NEW)
  public void handleProcessAssessmentStudentsEvent(final Event event) throws JsonProcessingException {
    if (event.getEventOutcome() == EventOutcome.READ_ASSESSMENT_STUDENTS_FOR_PROCESSING_SUCCESS) {
      final AssessmentStudentSagaData sagaData = JsonUtil.getJsonObjectFromString(AssessmentStudentSagaData.class, event.getEventPayload());
      final var sagaList = this.getSagaService().findByAssessmentStudentIDAndIncomingFilesetIDAndSagaNameAndStatusNot(UUID.fromString(sagaData.getAssessmentStudent().getAssessmentStudentID()), UUID.fromString(sagaData.getAssessmentStudent().getIncomingFilesetID()), SagaEnum.PROCESS_ASSESSMENT_STUDENTS_SAGA.toString(), SagaStatusEnum.COMPLETED.toString());
      if (!sagaList.isEmpty()) { // possible duplicate message.
        log.trace(NO_EXECUTION_MSG, event);
        return;
      }
      val saga = this.assessmentStudentProcessingOrchestrator
              .createSaga(event.getEventPayload(),
                      ApplicationProperties.GRAD_DATA_COLLECTION_API,
                      UUID.fromString(sagaData.getAssessmentStudent().getIncomingFilesetID()),
                      null,
                      UUID.fromString(sagaData.getAssessmentStudent().getAssessmentStudentID()),
                      null);
      log.debug("Starting assessment student processing orchestrator :: {}", saga);
      this.assessmentStudentProcessingOrchestrator.startSaga(saga);
    }
  }

  @Transactional(propagation = REQUIRES_NEW)
  public void handleProcessCourseStudentsForDownstreamUpdateEvent(final Event event) throws JsonProcessingException {
    if (event.getEventOutcome() == EventOutcome.READ_COURSE_STUDENTS_FOR_DOWNSTREAM_UPDATE_SUCCESS) {
      final CourseStudentUpdate sagaData = JsonUtil.getJsonObjectFromString(CourseStudentUpdate.class, event.getEventPayload());
      final var sagaList = this.getSagaService().findByIncomingFilesetIDAndSagaNameAndStatusNot(UUID.fromString(sagaData.getIncomingFilesetID()), SagaEnum.PROCESS_COURSE_STUDENTS_FOR_DOWNSTREAM_UPDATE_SAGA.toString(), SagaStatusEnum.COMPLETED.toString());
      if (!sagaList.isEmpty()) { // possible duplicate message.
        log.trace(NO_EXECUTION_MSG, event);
        return;
      }
      val saga = this.updateCourseStudentDownstreamOrchestrator
              .createSaga(event.getEventPayload(),
                      ApplicationProperties.GRAD_DATA_COLLECTION_API,
                      UUID.fromString(sagaData.getIncomingFilesetID()),
                      null,
                      null,
                      null);
      log.debug("Starting course student processing orchestrator :: {}", saga);
      this.updateCourseStudentDownstreamOrchestrator.startSaga(saga);
    }
  }
}
