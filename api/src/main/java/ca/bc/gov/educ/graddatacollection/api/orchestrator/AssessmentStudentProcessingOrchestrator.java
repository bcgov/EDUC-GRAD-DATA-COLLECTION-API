package ca.bc.gov.educ.graddatacollection.api.orchestrator;

import ca.bc.gov.educ.graddatacollection.api.constants.SagaEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.TopicsEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolStudentStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.model.v1.GradSagaEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.graddatacollection.api.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.service.v1.AssessmentRulesService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.AssessmentStudentService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.SagaService;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.AssessmentStudentSagaData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.EventType.PROCESS_ASSESSMENT_STUDENT_IN_ASSESSMENT_SERVICE;
import static ca.bc.gov.educ.graddatacollection.api.constants.EventType.VALIDATE_ASSESSMENT_STUDENT;
import static ca.bc.gov.educ.graddatacollection.api.constants.SagaStatusEnum.IN_PROGRESS;

@Component
@Slf4j
public class AssessmentStudentProcessingOrchestrator extends BaseOrchestrator<AssessmentStudentSagaData> {
  private final AssessmentStudentService assessmentStudentService;
  private final AssessmentRulesService assessmentRulesService;
  private final RestUtils restUtils;

  protected AssessmentStudentProcessingOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher, AssessmentStudentService assessmentStudentService, AssessmentRulesService assessmentRulesService, RestUtils restUtils) {
    super(sagaService, messagePublisher, AssessmentStudentSagaData.class, SagaEnum.PROCESS_ASSESSMENT_STUDENTS_SAGA.toString(), TopicsEnum.PROCESS_ASSESSMENT_STUDENTS_SAGA_TOPIC.toString());
    this.assessmentStudentService = assessmentStudentService;
    this.assessmentRulesService = assessmentRulesService;
    this.restUtils = restUtils;
  }

  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
            .begin(VALIDATE_ASSESSMENT_STUDENT, this::validateAssessmentStudentRecord)
            .step(VALIDATE_ASSESSMENT_STUDENT, VALIDATE_ASSESSMENT_STUDENT_SUCCESS_WITH_NO_ERROR, PROCESS_ASSESSMENT_STUDENT_IN_ASSESSMENT_SERVICE, this::writeAssessmentStudentRecordInAssessmentService)
            .end(VALIDATE_ASSESSMENT_STUDENT, VALIDATE_ASSESSMENT_STUDENT_SUCCESS_WITH_ERROR, this::completeWithError)
            .or()
            .end(PROCESS_ASSESSMENT_STUDENT_IN_ASSESSMENT_SERVICE, ASSESSMENT_STUDENT_REGISTRATION_PROCESSED);
  }

  public void validateAssessmentStudentRecord(final Event event, final GradSagaEntity saga, final AssessmentStudentSagaData assessmentStudentSagaData) {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(VALIDATE_ASSESSMENT_STUDENT.toString());
    saga.setStatus(IN_PROGRESS.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    var student = assessmentStudentSagaData.getAssessmentStudent();

    final Event.EventBuilder eventBuilder = Event.builder();
    eventBuilder.sagaId(saga.getSagaId()).eventType(VALIDATE_ASSESSMENT_STUDENT);
    var demStudent = assessmentRulesService.getDemographicDataForStudent(UUID.fromString(student.getIncomingFilesetID()),student.getPen(), student.getLastName(), student.getLocalID());
    
    var validationErrors = assessmentStudentService.validateStudent(UUID.fromString(assessmentStudentSagaData.getAssessmentStudent().getAssessmentStudentID()), assessmentStudentSagaData.getSchool());
    if(validationErrors.stream().anyMatch(issueValue -> issueValue.getValidationIssueSeverityCode().equalsIgnoreCase(SchoolStudentStatus.ERROR.toString()))) {
      assessmentStudentService.setStudentStatusAndFlagErrorIfRequired(UUID.fromString(assessmentStudentSagaData.getAssessmentStudent().getAssessmentStudentID()), SchoolStudentStatus.ERROR, demStudent, true);
      eventBuilder.eventOutcome(VALIDATE_ASSESSMENT_STUDENT_SUCCESS_WITH_ERROR);
    } else {
      if(!demStudent.getStudentStatusCode().equalsIgnoreCase(SchoolStudentStatus.ERROR.getCode())) {
        var hasWarning = validationErrors.stream().anyMatch(issueValue -> issueValue.getValidationIssueSeverityCode().equalsIgnoreCase(SchoolStudentStatus.WARNING.toString()));
        assessmentStudentService.setStudentStatusAndFlagErrorIfRequired(UUID.fromString(assessmentStudentSagaData.getAssessmentStudent().getAssessmentStudentID()), SchoolStudentStatus.VERIFIED, demStudent, hasWarning);
        eventBuilder.eventOutcome(VALIDATE_ASSESSMENT_STUDENT_SUCCESS_WITH_NO_ERROR);
      } else {
        assessmentStudentService.setDemValidationErrorAndStudentStatusAndFlagError(UUID.fromString(assessmentStudentSagaData.getAssessmentStudent().getAssessmentStudentID()), SchoolStudentStatus.ERROR, demStudent, StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.PEN, AssessmentStudentValidationIssueTypeCode.ASSESSMENT_HAS_DEM_BLOCKING, AssessmentStudentValidationIssueTypeCode.ASSESSMENT_HAS_DEM_BLOCKING.getMessage());
        eventBuilder.eventOutcome(VALIDATE_ASSESSMENT_STUDENT_SUCCESS_WITH_ERROR);
      }
    } 

    val nextEvent = eventBuilder.build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.debug("message sent to {} for {} Event. :: {}", this.getTopicToSubscribe(), nextEvent, saga.getSagaId());
  }

  public void writeAssessmentStudentRecordInAssessmentService(final Event event, final GradSagaEntity saga, final AssessmentStudentSagaData assessmentStudentSagaData) {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(PROCESS_ASSESSMENT_STUDENT_IN_ASSESSMENT_SERVICE.toString());
    saga.setStatus(IN_PROGRESS.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    var student = assessmentStudentSagaData.getAssessmentStudent();

    var demStudent = assessmentRulesService.getDemographicDataForStudent(UUID.fromString(student.getIncomingFilesetID()),student.getPen(), student.getLastName(), student.getLocalID());

    final Event.EventBuilder eventBuilder = Event.builder();
    eventBuilder.sagaId(saga.getSagaId()).eventType(PROCESS_ASSESSMENT_STUDENT_IN_ASSESSMENT_SERVICE);

    var assessmentID = assessmentRulesService.getAssessmentID(student.getCourseYear(), student.getCourseMonth(), student.getCourseCode());
    restUtils.writeAssessmentStudentDetailInAssessmentService(assessmentStudentSagaData.getAssessmentStudent(), assessmentID, assessmentStudentSagaData.getSchool(), demStudent.getGrade());
    eventBuilder.eventOutcome(ASSESSMENT_STUDENT_REGISTRATION_PROCESSED);
    
    val nextEvent = eventBuilder.build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.debug("message sent to {} for {} Event. :: {}", this.getTopicToSubscribe(), nextEvent, saga.getSagaId());
  }

  private void completeWithError(final Event event, final GradSagaEntity saga, final AssessmentStudentSagaData assessmentStudentSagaData) {
    //Do nothing here - we already flagged an error
  }

}
