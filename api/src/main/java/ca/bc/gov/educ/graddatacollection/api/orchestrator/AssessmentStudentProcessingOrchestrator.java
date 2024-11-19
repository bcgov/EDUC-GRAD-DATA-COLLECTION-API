package ca.bc.gov.educ.graddatacollection.api.orchestrator;

import ca.bc.gov.educ.graddatacollection.api.constants.SagaEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.TopicsEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolStudentStatus;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.model.v1.GradSagaEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.graddatacollection.api.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.graddatacollection.api.service.v1.AssessmentStudentService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.SagaService;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.AssessmentStudentSagaData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.EventType.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.SagaStatusEnum.IN_PROGRESS;

@Component
@Slf4j
public class AssessmentStudentProcessingOrchestrator extends BaseOrchestrator<AssessmentStudentSagaData> {
  private final AssessmentStudentService assessmentStudentService;

  protected AssessmentStudentProcessingOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher, AssessmentStudentService assessmentStudentService) {
    super(sagaService, messagePublisher, AssessmentStudentSagaData.class, SagaEnum.PROCESS_ASSESSMENT_STUDENTS_SAGA.toString(), TopicsEnum.PROCESS_ASSESSMENT_STUDENTS_SAGA_TOPIC.toString());
      this.assessmentStudentService = assessmentStudentService;
  }

  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
      .begin(VALIDATE_ASSESSMENT_STUDENT, this::validateCourseStudentRecord)
      .step(VALIDATE_ASSESSMENT_STUDENT, VALIDATE_ASSESSMENT_STUDENT_SUCCESS_WITH_NO_ERROR, CREATE_ASSESSMENT_STUDENT_IN_GRAD, this::createCourseStudentRecordInGrad)
      .end(VALIDATE_ASSESSMENT_STUDENT, VALIDATE_ASSESSMENT_STUDENT_SUCCESS_WITH_ERROR, this::completeWithError)
      .or()
      .step(CREATE_ASSESSMENT_STUDENT_IN_GRAD, ASSESSMENT_STUDENT_CREATED_IN_GRAD, UPDATE_ASSESSMENT_STUDENT_STATUS_IN_COLLECTION, this::updateCourseStudentStatus)
      .end(UPDATE_ASSESSMENT_STUDENT_STATUS_IN_COLLECTION, COURSE_ASSESSMENT_STATUS_IN_COLLECTION_UPDATED);

  }

  public void validateCourseStudentRecord(final Event event, final GradSagaEntity saga, final AssessmentStudentSagaData assessmentStudentSagaData) {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(VALIDATE_ASSESSMENT_STUDENT.toString());
    saga.setStatus(IN_PROGRESS.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event.EventBuilder eventBuilder = Event.builder();
    eventBuilder.sagaId(saga.getSagaId()).eventType(VALIDATE_ASSESSMENT_STUDENT);

    var validationErrors = assessmentStudentService.validateStudent(UUID.fromString(assessmentStudentSagaData.getAssessmentStudent().getAssessmentStudentID()), assessmentStudentSagaData.getSchool());
    if(validationErrors.stream().anyMatch(issueValue -> issueValue.getValidationIssueSeverityCode().equalsIgnoreCase(SchoolStudentStatus.ERROR.toString()))) {
      eventBuilder.eventOutcome(VALIDATE_ASSESSMENT_STUDENT_SUCCESS_WITH_ERROR);
    } else {
      eventBuilder.eventOutcome(VALIDATE_ASSESSMENT_STUDENT_SUCCESS_WITH_NO_ERROR);
    }

    val nextEvent = eventBuilder.build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.debug("message sent to {} for {} Event. :: {}", this.getTopicToSubscribe(), nextEvent, saga.getSagaId());
  }

  public void createCourseStudentRecordInGrad(final Event event, final GradSagaEntity saga, final AssessmentStudentSagaData assessmentStudentSagaData) {
    //TODO
  }

  public void updateCourseStudentStatus(final Event event, final GradSagaEntity saga, final AssessmentStudentSagaData assessmentStudentSagaData) {
    //TODO
  }

  private void completeWithError(final Event event, final GradSagaEntity saga, final AssessmentStudentSagaData assessmentStudentSagaData) {
    assessmentStudentService.flagErrorOnStudent(assessmentStudentSagaData.getAssessmentStudent());
  }

}
