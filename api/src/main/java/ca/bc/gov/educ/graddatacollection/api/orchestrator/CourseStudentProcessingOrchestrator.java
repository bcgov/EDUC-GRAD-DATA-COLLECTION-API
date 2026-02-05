package ca.bc.gov.educ.graddatacollection.api.orchestrator;

import ca.bc.gov.educ.graddatacollection.api.constants.SagaEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.TopicsEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolStudentStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.model.v1.GradSagaEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.graddatacollection.api.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseRulesService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseStudentService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.SagaService;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentSagaData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome.VALIDATE_COURSE_STUDENT_SUCCESS;
import static ca.bc.gov.educ.graddatacollection.api.constants.EventType.VALIDATE_COURSE_STUDENT;
import static ca.bc.gov.educ.graddatacollection.api.constants.SagaStatusEnum.IN_PROGRESS;

@Component
@Slf4j
public class CourseStudentProcessingOrchestrator extends BaseOrchestrator<CourseStudentSagaData> {
  private final CourseStudentService courseStudentService;
  private final CourseRulesService courseRulesService;

  protected CourseStudentProcessingOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher, CourseStudentService courseStudentService, CourseRulesService courseRulesService) {
    super(sagaService, messagePublisher, CourseStudentSagaData.class, SagaEnum.PROCESS_COURSE_STUDENTS_SAGA.toString(), TopicsEnum.PROCESS_COURSE_STUDENTS_SAGA_TOPIC.toString());
    this.courseStudentService = courseStudentService;
    this.courseRulesService = courseRulesService;
  }

  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
            .begin(VALIDATE_COURSE_STUDENT, this::validateCourseStudentRecordAndUpdateStatus)
            .end(VALIDATE_COURSE_STUDENT, VALIDATE_COURSE_STUDENT_SUCCESS);
  }

  public void validateCourseStudentRecordAndUpdateStatus(final Event event, final GradSagaEntity saga, final CourseStudentSagaData courseStudentSagaData) {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(VALIDATE_COURSE_STUDENT.toString());
    saga.setStatus(IN_PROGRESS.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    var student = courseStudentSagaData.getCourseStudent();
    
    final Event.EventBuilder eventBuilder = Event.builder();
    eventBuilder.sagaId(saga.getSagaId()).eventType(VALIDATE_COURSE_STUDENT);
    
    var demStudent = courseRulesService.getDemographicDataForStudent(UUID.fromString(student.getIncomingFilesetID()),student.getPen(), student.getLastName(), student.getLocalID());
    var validationErrors = courseStudentService.validateStudent(UUID.fromString(courseStudentSagaData.getCourseStudent().getCourseStudentID()), courseStudentSagaData.getSchool());
    
    if(validationErrors.stream().anyMatch(issueValue -> issueValue.getValidationIssueSeverityCode().equalsIgnoreCase(SchoolStudentStatus.ERROR.toString()))) {
      courseStudentService.setStudentStatusAndFlagErrorIfRequired(UUID.fromString(courseStudentSagaData.getCourseStudent().getCourseStudentID()), SchoolStudentStatus.ERROR, demStudent, true);
    } else {
      if(!demStudent.getStudentStatusCode().equalsIgnoreCase(SchoolStudentStatus.ERROR.getCode())) {
        var hasWarning = validationErrors.stream().anyMatch(issueValue -> issueValue.getValidationIssueSeverityCode().equalsIgnoreCase(SchoolStudentStatus.WARNING.toString()));
        courseStudentService.setStudentStatusAndFlagErrorIfRequired(UUID.fromString(courseStudentSagaData.getCourseStudent().getCourseStudentID()), SchoolStudentStatus.UPDATE_CRS, demStudent, hasWarning);
      }else{
        courseStudentService.setDemValidationErrorStudentStatusAndFlagError(UUID.fromString(courseStudentSagaData.getCourseStudent().getCourseStudentID()), SchoolStudentStatus.ERROR, demStudent, StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.PEN, CourseStudentValidationIssueTypeCode.COURSE_HAS_DEM_BLOCKING, CourseStudentValidationIssueTypeCode.COURSE_HAS_DEM_BLOCKING.getMessage(), demStudent.getUpdateUser());
      }
    } 

    eventBuilder.eventOutcome(VALIDATE_COURSE_STUDENT_SUCCESS);

    var nextEvent = eventBuilder.build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.debug("message sent to {} for {} Event. :: {}", this.getTopicToSubscribe(), nextEvent, saga.getSagaId());
  }

}
