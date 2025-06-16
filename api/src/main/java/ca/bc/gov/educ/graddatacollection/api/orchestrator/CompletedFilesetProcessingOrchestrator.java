package ca.bc.gov.educ.graddatacollection.api.orchestrator;

import ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome;
import ca.bc.gov.educ.graddatacollection.api.constants.SagaEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.TopicsEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.model.v1.GradSagaEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.graddatacollection.api.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.service.v1.IncomingFilesetService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.SagaService;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.InstituteStatusEvent;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFilesetSagaData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.EventType.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.SagaStatusEnum.IN_PROGRESS;

@Component
@Slf4j
public class CompletedFilesetProcessingOrchestrator extends BaseOrchestrator<IncomingFilesetSagaData> {
    private final IncomingFilesetService incomingFilesetService;
    private final RestUtils restUtils;

    protected CompletedFilesetProcessingOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher, IncomingFilesetService incomingFilesetService, RestUtils restUtils) {
        super(sagaService, messagePublisher, IncomingFilesetSagaData.class, SagaEnum.PROCESS_COMPLETED_FILESETS_SAGA.toString(), TopicsEnum.PROCESS_COMPLETED_FILESETS_SAGA_TOPIC.toString());
        this.incomingFilesetService = incomingFilesetService;
        this.restUtils = restUtils;
    }

    @Override
    public void populateStepsToExecuteMap() {
        this.stepBuilder()
                .begin(UPDATE_COMPLETED_FILESET_STATUS_AND_VENDOR_CODE_IF_REQUIRED, this::updateIncomingFilesetStatus)
                .step(UPDATE_COMPLETED_FILESET_STATUS_AND_VENDOR_CODE_IF_REQUIRED, COMPLETED_FILESET_STATUS_UPDATED, CHECK_VENDOR_CODE_IN_INSTITUE_AND_UPDATE_IF_REQUIRED, this::checkVendorCodeInInstituteAndUpdateVendorCodeIfRequired)
                .end(UPDATE_COMPLETED_FILESET_STATUS_AND_VENDOR_CODE_REQUIRED, COMPLETED_FILESET_STATUS_AND_VENDOR_CODE_UPDATED, this::echoVendorCodeUpdated)
                .or()
                .end(UPDATE_COMPLETED_FILESET_STATUS_AND_VENDOR_CODE_NOT_REQUIRED, COMPLETED_FILESET_STATUS_UPDATED_VENDOR_CODE_DOES_NOT_NEED_UPDATE);
    }

    public void updateIncomingFilesetStatus(final Event event, final GradSagaEntity saga, final IncomingFilesetSagaData incomingFilesetSagaData) {
        final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
        saga.setSagaState(UPDATE_COMPLETED_FILESET_STATUS_AND_VENDOR_CODE_IF_REQUIRED.toString());
        saga.setStatus(IN_PROGRESS.toString());
        this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

        incomingFilesetService.setCompletedFilesetStatus(UUID.fromString(incomingFilesetSagaData.getIncomingFileset().getIncomingFilesetID()), FilesetStatus.COMPLETED);

        final Event.EventBuilder eventBuilder = Event.builder();
        eventBuilder.sagaId(saga.getSagaId()).eventType(UPDATE_COMPLETED_FILESET_STATUS_AND_VENDOR_CODE_IF_REQUIRED);
        eventBuilder.eventOutcome(COMPLETED_FILESET_STATUS_UPDATED);
        val nextEvent = eventBuilder.build();
        this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
        log.debug("message sent to {} for {} Event. :: {}", this.getTopicToSubscribe(), nextEvent, saga.getSagaId());
    }

    public void checkVendorCodeInInstituteAndUpdateVendorCodeIfRequired(final Event event, final GradSagaEntity saga, final IncomingFilesetSagaData incomingFilesetSagaData) {
        final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
        saga.setSagaState(CHECK_VENDOR_CODE_IN_INSTITUE_AND_UPDATE_IF_REQUIRED.toString());
        saga.setStatus(IN_PROGRESS.toString());
        this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

        Optional<SchoolTombstone> school = restUtils.getSchoolBySchoolID(incomingFilesetSagaData.getIncomingFileset().getSchoolID());
        final Event.EventBuilder eventBuilder = Event.builder();

        if (school.isPresent() && !school.get().getVendorCode().equalsIgnoreCase(incomingFilesetSagaData.getDemographicStudent().getVendorID())) {
            log.debug("Vendor code needs to be updated for school ID: {}. Current: {}, New: {}", incomingFilesetSagaData.getIncomingFileset().getSchoolID(), school.get().getVendorCode(), incomingFilesetSagaData.getDemographicStudent().getVendorID());

            InstituteStatusEvent response = restUtils.updateSchoolVendorCode(school.get(), incomingFilesetSagaData.getDemographicStudent().getVendorID(), UUID.randomUUID());

            if (!response.getEventOutcome().equalsIgnoreCase(EventOutcome.SCHOOL_UPDATED.toString())) {
                log.error("Update vendor code failed for school {}. Response: {}", incomingFilesetSagaData.getIncomingFileset().getSchoolID(), response);
                throw new GradDataCollectionAPIRuntimeException("Failed to update vendor code: " + response);
            }
            eventBuilder.sagaId(saga.getSagaId()).eventType(UPDATE_COMPLETED_FILESET_STATUS_AND_VENDOR_CODE_REQUIRED);
            eventBuilder.eventOutcome(COMPLETED_FILESET_STATUS_AND_VENDOR_CODE_UPDATED);
        } else {
            log.debug("Vendor code does not need to be updated for school ID: {}", incomingFilesetSagaData.getIncomingFileset().getSchoolID());
            eventBuilder.sagaId(saga.getSagaId()).eventType(UPDATE_COMPLETED_FILESET_STATUS_AND_VENDOR_CODE_REQUIRED);
            eventBuilder.eventOutcome(COMPLETED_FILESET_STATUS_UPDATED_VENDOR_CODE_DOES_NOT_NEED_UPDATE);
        }
        val nextEvent = eventBuilder.build();
        this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
        log.debug("message sent to {} for {} Event. :: {}", this.getTopicToSubscribe(), nextEvent, saga.getSagaId());
    }

    public void echoVendorCodeUpdated(final Event event, final GradSagaEntity saga, final IncomingFilesetSagaData incomingFilesetSagaData) {
        log.debug("Vendor code updated for Saga ID {}, incoming fileset saga data {}", saga.getSagaId(), incomingFilesetSagaData);
    }
}
