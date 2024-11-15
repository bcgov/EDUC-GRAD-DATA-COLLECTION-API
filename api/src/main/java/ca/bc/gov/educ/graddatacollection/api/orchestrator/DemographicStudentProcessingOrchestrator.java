package ca.bc.gov.educ.graddatacollection.api.orchestrator;

import ca.bc.gov.educ.graddatacollection.api.constants.SagaEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.TopicsEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolStudentStatus;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.model.v1.GradSagaEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.graddatacollection.api.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.graddatacollection.api.service.v1.DemographicStudentService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.SagaService;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradDemographicStudentSagaData;
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
public class DemographicStudentProcessingOrchestrator extends BaseOrchestrator<GradDemographicStudentSagaData> {
  private final DemographicStudentService demographicStudentService;

  protected DemographicStudentProcessingOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher, DemographicStudentService demographicStudentService) {
    super(sagaService, messagePublisher, GradDemographicStudentSagaData.class, SagaEnum.PROCESS_DEM_STUDENTS_SAGA.toString(), TopicsEnum.PROCESS_DEM_STUDENTS_SAGA_TOPIC.toString());
      this.demographicStudentService = demographicStudentService;
  }

  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
      .begin(VALIDATE_DEM_STUDENT, this::validateDEMStudentRecord)
      .step(VALIDATE_DEM_STUDENT, VALIDATE_DEM_STUDENT_SUCCESS_WITH_NO_ERROR, CREATE_DEM_STUDENT_IN_GRAD, this::createDEMStudentRecordInGrad)
      .end(VALIDATE_DEM_STUDENT, VALIDATE_DEM_STUDENT_SUCCESS_WITH_ERROR, this::completeWithError)
      .or()
      .step(CREATE_DEM_STUDENT_IN_GRAD, DEM_STUDENT_CREATED_IN_GRAD, UPDATE_DEM_STUDENT_STATUS_IN_COLLECTION, this::updateDemStudentStatus)
      .end(UPDATE_DEM_STUDENT_STATUS_IN_COLLECTION, DEM_STUDENT_STATUS_IN_COLLECTION_UPDATED);

  }

  public void validateDEMStudentRecord(final Event event, final GradSagaEntity saga, final GradDemographicStudentSagaData gradDemographicStudentSagaData) {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(VALIDATE_DEM_STUDENT.toString());
    saga.setStatus(IN_PROGRESS.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event.EventBuilder eventBuilder = Event.builder();
    eventBuilder.sagaId(saga.getSagaId()).eventType(VALIDATE_DEM_STUDENT);

    // call For dem validation
    var validationErrors = demographicStudentService.validateStudent(UUID.fromString(gradDemographicStudentSagaData.getDemographicStudent().getDemographicStudentID()), gradDemographicStudentSagaData.getSchool());
    if(validationErrors.stream().anyMatch(issueValue -> issueValue.getValidationIssueSeverityCode().equalsIgnoreCase(SchoolStudentStatus.ERROR.toString()))) {
      eventBuilder.eventOutcome(VALIDATE_DEM_STUDENT_SUCCESS_WITH_ERROR);
    } else {
      eventBuilder.eventOutcome(VALIDATE_DEM_STUDENT_SUCCESS_WITH_NO_ERROR);
    }

    val nextEvent = eventBuilder.build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.debug("message sent to {} for {} Event. :: {}", this.getTopicToSubscribe(), nextEvent, saga.getSagaId());
  }

  public void createDEMStudentRecordInGrad(final Event event, final GradSagaEntity saga, final GradDemographicStudentSagaData gradDemographicStudentSagaData) {
    //TODO
  }

  public void updateDemStudentStatus(final Event event, final GradSagaEntity saga, final GradDemographicStudentSagaData gradDemographicStudentSagaData) {
    //TODO
  }

  private void completeWithError(final Event event, final GradSagaEntity saga, final GradDemographicStudentSagaData gradDemographicStudentSagaData) {
    demographicStudentService.flagErrorOnStudent(gradDemographicStudentSagaData.getDemographicStudent());
  }

}
