package ca.bc.gov.educ.graddatacollection.api.orchestrator;

import ca.bc.gov.educ.graddatacollection.api.constants.SagaEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.TopicsEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolStudentStatus;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.model.v1.GradSagaEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.graddatacollection.api.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseStudentService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.SagaService;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradCourseStudentSagaData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.EventType.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.SagaStatusEnum.IN_PROGRESS;

@Component
@Slf4j
public class CourseStudentProcessingOrchestrator extends BaseOrchestrator<GradCourseStudentSagaData> {
  private final CourseStudentService courseStudentService;

  protected CourseStudentProcessingOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher, CourseStudentService courseStudentService) {
    super(sagaService, messagePublisher, GradCourseStudentSagaData.class, SagaEnum.PROCESS_COURSE_STUDENTS_SAGA.toString(), TopicsEnum.PROCESS_COURSE_STUDENTS_SAGA_TOPIC.toString());
      this.courseStudentService = courseStudentService;
  }

  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
      .begin(VALIDATE_COURSE_STUDENT, this::validateCourseStudentRecord)
      .step(VALIDATE_COURSE_STUDENT, VALIDATE_COURSE_STUDENT_SUCCESS_WITH_NO_ERROR, CREATE_COURSE_STUDENT_IN_GRAD, this::createCourseStudentRecordInGrad)
      .end(VALIDATE_COURSE_STUDENT, VALIDATE_COURSE_STUDENT_SUCCESS_WITH_ERROR, this::completeWithError)
      .or()
      .step(CREATE_COURSE_STUDENT_IN_GRAD, COURSE_STUDENT_CREATED_IN_GRAD, UPDATE_COURSE_STUDENT_STATUS_IN_COLLECTION, this::updateCourseStudentStatus)
      .end(UPDATE_COURSE_STUDENT_STATUS_IN_COLLECTION, COURSE_STUDENT_STATUS_IN_COLLECTION_UPDATED);

  }

  public void validateCourseStudentRecord(final Event event, final GradSagaEntity saga, final GradCourseStudentSagaData gradCourseStudentSagaData) {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(VALIDATE_COURSE_STUDENT.toString());
    saga.setStatus(IN_PROGRESS.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event.EventBuilder eventBuilder = Event.builder();
    eventBuilder.sagaId(saga.getSagaId()).eventType(VALIDATE_COURSE_STUDENT);

    var validationErrors = courseStudentService.validateStudent(UUID.fromString(gradCourseStudentSagaData.getCourseStudent().getCourseStudentID()), gradCourseStudentSagaData.getSchool());
    if(validationErrors.stream().anyMatch(issueValue -> issueValue.getValidationIssueSeverityCode().equalsIgnoreCase(SchoolStudentStatus.ERROR.toString()))) {
      eventBuilder.eventOutcome(VALIDATE_COURSE_STUDENT_SUCCESS_WITH_ERROR);
    } else {
      eventBuilder.eventOutcome(VALIDATE_COURSE_STUDENT_SUCCESS_WITH_NO_ERROR);
    }

    val nextEvent = eventBuilder.build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.debug("message sent to {} for {} Event. :: {}", this.getTopicToSubscribe(), nextEvent, saga.getSagaId());
  }

  public void createCourseStudentRecordInGrad(final Event event, final GradSagaEntity saga, final GradCourseStudentSagaData gradCourseStudentSagaData) {
    //TODO
  }

  public void updateCourseStudentStatus(final Event event, final GradSagaEntity saga, final GradCourseStudentSagaData gradCourseStudentSagaData) {
    //TODO
  }

  private void completeWithError(final Event event, final GradSagaEntity saga, final GradCourseStudentSagaData gradCourseStudentSagaData) {
    //This is ok
  }

}
