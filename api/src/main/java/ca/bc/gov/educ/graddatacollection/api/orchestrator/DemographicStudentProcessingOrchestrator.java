package ca.bc.gov.educ.graddatacollection.api.orchestrator;

import ca.bc.gov.educ.graddatacollection.api.constants.SagaEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.TopicsEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolStudentStatus;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.model.v1.GradSagaEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.graddatacollection.api.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.service.v1.DemographicStudentService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.SagaService;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudent;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentSagaData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.EventType.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.SagaStatusEnum.IN_PROGRESS;

@Component
@Slf4j
public class DemographicStudentProcessingOrchestrator extends BaseOrchestrator<DemographicStudentSagaData> {
  private final DemographicStudentService demographicStudentService;
  private final RestUtils restUtils;

  protected DemographicStudentProcessingOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher, DemographicStudentService demographicStudentService, RestUtils restUtils) {
    super(sagaService, messagePublisher, DemographicStudentSagaData.class, SagaEnum.PROCESS_DEM_STUDENTS_SAGA.toString(), TopicsEnum.PROCESS_DEM_STUDENTS_SAGA_TOPIC.toString());
      this.demographicStudentService = demographicStudentService;
      this.restUtils = restUtils;
  }

  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
            .begin(VALIDATE_DEM_STUDENT, this::validateDEMStudentRecord)
            .step(VALIDATE_DEM_STUDENT, VALIDATE_DEM_STUDENT_SUCCESS_WITH_NO_ERROR, CREATE_OR_UPDATE_DEM_STUDENT_IN_GRAD, this::createOrUpdateDEMStudentRecordInGrad)
            .end(VALIDATE_DEM_STUDENT, VALIDATE_DEM_STUDENT_SUCCESS_WITH_ERROR, this::completeWithError)
            .or()
            .step(CREATE_OR_UPDATE_DEM_STUDENT_IN_GRAD, DEM_STUDENT_CREATED_IN_GRAD, SEND_STUDENT_ADDRESS_TO_SCHOLARSHIPS, this::sendStudentAddressToScholarships)
            .step(SEND_STUDENT_ADDRESS_TO_SCHOLARSHIPS, STUDENT_ADDRESS_UPDATED, UPDATE_DEM_STUDENT_STATUS_IN_COLLECTION, this::updateDemStudentStatus)
            .step(SEND_STUDENT_ADDRESS_TO_SCHOLARSHIPS, NO_STUDENT_ADDRESS_UPDATE_REQUIRED, UPDATE_DEM_STUDENT_STATUS_IN_COLLECTION, this::updateDemStudentStatus)
            .end(UPDATE_DEM_STUDENT_STATUS_IN_COLLECTION, DEM_STUDENT_STATUS_IN_COLLECTION_UPDATED);
  }

  public void validateDEMStudentRecord(final Event event, final GradSagaEntity saga, final DemographicStudentSagaData demographicStudentSagaData) {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(VALIDATE_DEM_STUDENT.toString());
    saga.setStatus(IN_PROGRESS.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event.EventBuilder eventBuilder = Event.builder();
    eventBuilder.sagaId(saga.getSagaId()).eventType(VALIDATE_DEM_STUDENT);

    // call For dem validation
    var validationErrors = demographicStudentService.validateStudent(UUID.fromString(demographicStudentSagaData.getDemographicStudent().getDemographicStudentID()), demographicStudentSagaData.getSchool());
    if(validationErrors.stream().anyMatch(issueValue -> issueValue.getValidationIssueSeverityCode().equalsIgnoreCase(SchoolStudentStatus.ERROR.toString()))) {
      eventBuilder.eventOutcome(VALIDATE_DEM_STUDENT_SUCCESS_WITH_ERROR);
    } else if(validationErrors.stream().anyMatch(issueValue -> issueValue.getValidationIssueSeverityCode().equalsIgnoreCase(SchoolStudentStatus.WARNING.toString()))) {
      demographicStudentService.flagErrorOnStudent(demographicStudentSagaData.getDemographicStudent());
      eventBuilder.eventOutcome(VALIDATE_DEM_STUDENT_SUCCESS_WITH_NO_ERROR);
    } else {
      eventBuilder.eventOutcome(VALIDATE_DEM_STUDENT_SUCCESS_WITH_NO_ERROR);
    }

    val nextEvent = eventBuilder.build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.debug("message sent to {} for {} Event. :: {}", this.getTopicToSubscribe(), nextEvent, saga.getSagaId());
  }

  public void createOrUpdateDEMStudentRecordInGrad(final Event event, final GradSagaEntity saga, final DemographicStudentSagaData demographicStudentSagaData) {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(CREATE_OR_UPDATE_DEM_STUDENT_IN_GRAD.toString());
    saga.setStatus(IN_PROGRESS.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    var demStudentEntity = demographicStudentService.findByID(UUID.fromString(demographicStudentSagaData.getDemographicStudent().getDemographicStudentID()));
    //Write DEM data downstream
    restUtils.writeDEMStudentRecordInGrad(demographicStudentSagaData.getDemographicStudent(), demographicStudentSagaData.getSchool(), demStudentEntity.getIncomingFileset().getReportingPeriod());

    final Event.EventBuilder eventBuilder = Event.builder();
    eventBuilder.sagaId(saga.getSagaId()).eventType(CREATE_OR_UPDATE_DEM_STUDENT_IN_GRAD);
    eventBuilder.eventOutcome(DEM_STUDENT_CREATED_IN_GRAD);
    val nextEvent = eventBuilder.build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.debug("message sent to {} for {} Event. :: {}", this.getTopicToSubscribe(), nextEvent, saga.getSagaId());
  }

  public void updateDemStudentStatus(final Event event, final GradSagaEntity saga, final DemographicStudentSagaData demographicStudentSagaData) {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(UPDATE_DEM_STUDENT_STATUS_IN_COLLECTION.toString());
    saga.setStatus(IN_PROGRESS.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    demographicStudentService.setStudentStatus(UUID.fromString(demographicStudentSagaData.getDemographicStudent().getDemographicStudentID()), SchoolStudentStatus.VERIFIED);

    final Event.EventBuilder eventBuilder = Event.builder();
    eventBuilder.sagaId(saga.getSagaId()).eventType(UPDATE_DEM_STUDENT_STATUS_IN_COLLECTION);
    eventBuilder.eventOutcome(DEM_STUDENT_STATUS_IN_COLLECTION_UPDATED);
    val nextEvent = eventBuilder.build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.debug("message sent to {} for {} Event. :: {}", this.getTopicToSubscribe(), nextEvent, saga.getSagaId());
  }

  public void sendStudentAddressToScholarships(final Event event, final GradSagaEntity saga, final DemographicStudentSagaData demographicStudentSagaData) {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(SEND_STUDENT_ADDRESS_TO_SCHOLARSHIPS.toString());
    saga.setStatus(IN_PROGRESS.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event.EventBuilder eventBuilder = Event.builder();
    eventBuilder.sagaId(saga.getSagaId()).eventType(SEND_STUDENT_ADDRESS_TO_SCHOLARSHIPS);
    
    if(isValidAddress(demographicStudentSagaData.getDemographicStudent()) && studentInGrade12orAD(demographicStudentSagaData.getDemographicStudent())) {
      Student studentApiStudent = restUtils.getStudentByPEN(UUID.randomUUID(), demographicStudentSagaData.getDemographicStudent().getPen());
      updateAddressFieldsIfNeeded(demographicStudentSagaData.getDemographicStudent());
      restUtils.writeStudentAddressToScholarships(demographicStudentSagaData.getDemographicStudent(), studentApiStudent.getStudentID());
      eventBuilder.eventOutcome(STUDENT_ADDRESS_UPDATED);
    }else{
      eventBuilder.eventOutcome(NO_STUDENT_ADDRESS_UPDATE_REQUIRED);
    }
    
    val nextEvent = eventBuilder.build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.debug("message sent to {} for {} Event. :: {}", this.getTopicToSubscribe(), nextEvent, saga.getSagaId());
  }
  
  private boolean studentInGrade12orAD(DemographicStudent student){
    return StringUtils.isNotBlank(student.getGrade()) && (student.getGrade().equals("12") || student.getGrade().equals("AD"));
  }
  
  private boolean isValidAddress(DemographicStudent student){
    return (((StringUtils.isNotBlank(student.getAddressLine1()) && !student.getAddressLine1().equalsIgnoreCase("UNKNOWN")) 
            || (StringUtils.isNotBlank(student.getAddressLine2()) && !student.getAddressLine2().equalsIgnoreCase("UNKNOWN"))) &&
            StringUtils.isNotBlank(student.getCity()) && !student.getCity().equalsIgnoreCase("UNKNOWN") &&
            StringUtils.isNotBlank(student.getProvincialCode()) &&
            StringUtils.isNotBlank(student.getCountryCode()) && 
            StringUtils.isNotBlank(student.getPostalCode()) && !student.getPostalCode().equalsIgnoreCase("UNKNOWN"));
  }

  private void updateAddressFieldsIfNeeded(DemographicStudent student) {
    if(StringUtils.isBlank(student.getAddressLine1()) &&  StringUtils.isNotBlank(student.getAddressLine2())) {
      student.setAddressLine1(student.getAddressLine2());
      student.setAddressLine2(null);
    }
  }

  private void completeWithError(final Event event, final GradSagaEntity saga, final DemographicStudentSagaData demographicStudentSagaData) {
    demographicStudentService.flagErrorOnStudent(demographicStudentSagaData.getDemographicStudent());
  }

}
