package ca.bc.gov.educ.graddatacollection.api.orchestrator;

import ca.bc.gov.educ.graddatacollection.api.constants.SagaEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.TopicsEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolStudentStatus;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.GradSagaEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.graddatacollection.api.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseRulesService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseStudentService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.SagaService;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentUpdate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.EventType.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.SagaStatusEnum.IN_PROGRESS;

@Component
@Slf4j
public class UpdateCourseStudentDownstreamOrchestrator extends BaseOrchestrator<CourseStudentUpdate> {
  private final CourseStudentService courseStudentService;
  private final CourseRulesService courseRulesService;
  private final RestUtils restUtils;

  protected UpdateCourseStudentDownstreamOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher, CourseStudentService courseStudentService, CourseRulesService courseRulesService, RestUtils restUtils) {
    super(sagaService, messagePublisher, CourseStudentUpdate.class, SagaEnum.PROCESS_COURSE_STUDENTS_FOR_DOWNSTREAM_UPDATE_SAGA.toString(), TopicsEnum.PROCESS_COURSE_STUDENTS_SAGA_FOR_DOWNSTREAM_UPDATE_TOPIC.toString());
      this.courseStudentService = courseStudentService;
      this.courseRulesService = courseRulesService;
      this.restUtils = restUtils;
  }

  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
            .begin(CREATE_COURSE_STUDENT_IN_GRAD, this::createCourseStudentRecordInGrad)
            .step(CREATE_COURSE_STUDENT_IN_GRAD, COURSE_STUDENT_CREATED_IN_GRAD, UPDATE_COURSE_STUDENT_STATUS, this::updateCourseStudentStatus)
            .end(UPDATE_COURSE_STUDENT_STATUS, COURSE_STUDENT_STATUS_UPDATED);

  }

  public void createCourseStudentRecordInGrad(final Event event, final GradSagaEntity saga, final CourseStudentUpdate courseStudentUpdate) {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(CREATE_COURSE_STUDENT_IN_GRAD.toString());
    saga.setStatus(IN_PROGRESS.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    final Event.EventBuilder eventBuilder = Event.builder();
    eventBuilder.sagaId(saga.getSagaId()).eventType(CREATE_COURSE_STUDENT_IN_GRAD);

    List<CourseStudentEntity> entities = courseRulesService.findByIncomingFilesetIDAndPenAndStudentStatusCode(UUID.fromString(courseStudentUpdate.getIncomingFilesetID()), courseStudentUpdate.getPen());
    var incomingFileset = entities.getFirst().getIncomingFileset();
    restUtils.writeCRSStudentRecordInGrad(entities, courseStudentUpdate.getPen(), incomingFileset.getSchoolID().toString(), incomingFileset.getReportingPeriod());
    eventBuilder.eventOutcome(COURSE_STUDENT_CREATED_IN_GRAD);

    val nextEvent = eventBuilder.build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.debug("message sent to {} for {} Event. :: {}", this.getTopicToSubscribe(), nextEvent, saga.getSagaId());
  }

  public void updateCourseStudentStatus(final Event event, final GradSagaEntity saga, final CourseStudentUpdate courseStudentUpdate) {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(UPDATE_COURSE_STUDENT_STATUS.toString());
    saga.setStatus(IN_PROGRESS.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    courseStudentService.updateStudentStatus(courseStudentUpdate, SchoolStudentStatus.VERIFIED);

    final Event.EventBuilder eventBuilder = Event.builder();
    eventBuilder.sagaId(saga.getSagaId()).eventType(UPDATE_COURSE_STUDENT_STATUS);
    eventBuilder.eventOutcome(COURSE_STUDENT_STATUS_UPDATED);
    val nextEvent = eventBuilder.build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.debug("message sent to {} for {} Event. :: {}", this.getTopicToSubscribe(), nextEvent, saga.getSagaId());
  }
}
