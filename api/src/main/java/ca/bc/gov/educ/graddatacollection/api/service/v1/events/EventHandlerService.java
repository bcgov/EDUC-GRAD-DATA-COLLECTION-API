package ca.bc.gov.educ.graddatacollection.api.service.v1.events;

import ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome;
import ca.bc.gov.educ.graddatacollection.api.constants.SagaEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.SagaStatusEnum;
import ca.bc.gov.educ.graddatacollection.api.orchestrator.AssessmentStudentProcessingOrchestrator;
import ca.bc.gov.educ.graddatacollection.api.orchestrator.CourseStudentProcessingOrchestrator;
import ca.bc.gov.educ.graddatacollection.api.orchestrator.DemographicStudentProcessingOrchestrator;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.service.v1.SagaService;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradAssessmentStudentSagaData;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradCourseStudentSagaData;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradDemographicStudentSagaData;
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

  private final DemographicStudentProcessingOrchestrator demographicStudentProcessingOrchestrator;
  private final CourseStudentProcessingOrchestrator courseStudentProcessingOrchestrator;
  private final AssessmentStudentProcessingOrchestrator assessmentStudentProcessingOrchestrator;
  public static String NO_EXECUTION_MSG = "Execution is not required for this message returning EVENT is :: {}";

  @Autowired
  public EventHandlerService(final SagaService sagaService, final DemographicStudentProcessingOrchestrator demographicStudentProcessingOrchestrator, CourseStudentProcessingOrchestrator courseStudentProcessingOrchestrator, AssessmentStudentProcessingOrchestrator assessmentStudentProcessingOrchestrator) {
    this.sagaService = sagaService;
    this.demographicStudentProcessingOrchestrator = demographicStudentProcessingOrchestrator;
    this.courseStudentProcessingOrchestrator = courseStudentProcessingOrchestrator;
      this.assessmentStudentProcessingOrchestrator = assessmentStudentProcessingOrchestrator;
  }

  @Transactional(propagation = REQUIRES_NEW)
  public void handleProcessDemStudentsEvent(final Event event) throws JsonProcessingException {
    if (event.getEventOutcome() == EventOutcome.READ_DEM_STUDENTS_FOR_PROCESSING_SUCCESS) {
      final GradDemographicStudentSagaData sagaData = JsonUtil.getJsonObjectFromString(GradDemographicStudentSagaData.class, event.getEventPayload());
      final var sagaOptional = this.getSagaService().findByDemographicStudentIDAndIncomingFilesetIDAndSagaNameAndStatusNot(UUID.fromString(sagaData.getDemographicStudent().getDemographicStudentID()), UUID.fromString(sagaData.getDemographicStudent().getIncomingFilesetID()), SagaEnum.PROCESS_DEM_STUDENTS_SAGA.toString(), SagaStatusEnum.COMPLETED.toString());
      if (sagaOptional.isPresent()) { // possible duplicate message.
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
      final GradCourseStudentSagaData sagaData = JsonUtil.getJsonObjectFromString(GradCourseStudentSagaData.class, event.getEventPayload());
      final var sagaOptional = this.getSagaService().findByCourseStudentIDAndIncomingFilesetIDAndSagaNameAndStatusNot(UUID.fromString(sagaData.getCourseStudent().getCourseStudentID()), UUID.fromString(sagaData.getCourseStudent().getIncomingFilesetID()), SagaEnum.PROCESS_COURSE_STUDENTS_SAGA.toString(), SagaStatusEnum.COMPLETED.toString());
      if (sagaOptional.isPresent()) { // possible duplicate message.
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
      final GradAssessmentStudentSagaData sagaData = JsonUtil.getJsonObjectFromString(GradAssessmentStudentSagaData.class, event.getEventPayload());
      final var sagaOptional = this.getSagaService().findByAssessmentStudentIDAndIncomingFilesetIDAndSagaNameAndStatusNot(UUID.fromString(sagaData.getAssessmentStudent().getAssessmentStudentID()), UUID.fromString(sagaData.getAssessmentStudent().getIncomingFilesetID()), SagaEnum.PROCESS_ASSESSMENT_STUDENTS_SAGA.toString(), SagaStatusEnum.COMPLETED.toString());
      if (sagaOptional.isPresent()) { // possible duplicate message.
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
}
