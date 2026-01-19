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
import ca.bc.gov.educ.graddatacollection.api.service.v1.DemographicStudentService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.IncomingFilesetService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.SagaService;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.InstituteStatusEvent;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.School;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFilesetSagaData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.EventType.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.SagaStatusEnum.IN_PROGRESS;

@Component
@Slf4j
public class CompletedFilesetProcessingOrchestrator extends BaseOrchestrator<IncomingFilesetSagaData> {
    private final IncomingFilesetService incomingFilesetService;
    private final DemographicStudentService demographicStudentService;
    private final RestUtils restUtils;

    protected CompletedFilesetProcessingOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher, IncomingFilesetService incomingFilesetService, DemographicStudentService demographicStudentService, RestUtils restUtils) {
        super(sagaService, messagePublisher, IncomingFilesetSagaData.class, SagaEnum.PROCESS_COMPLETED_FILESETS_SAGA.toString(), TopicsEnum.PROCESS_COMPLETED_FILESETS_SAGA_TOPIC.toString());
        this.incomingFilesetService = incomingFilesetService;
        this.demographicStudentService = demographicStudentService;
        this.restUtils = restUtils;
    }

    @Override
    public void populateStepsToExecuteMap() {
        this.stepBuilder()
                .begin(UPDATE_COMPLETED_FILESET_STATUS, this::updateCompletedFilesetStatus)
                .step(UPDATE_COMPLETED_FILESET_STATUS, COMPLETED_FILESET_STATUS_UPDATED, COPY_FILESET_FROM_STAGING_TO_FINAL_TABLE, this::copyFilesetToFinalTableFromStaging)
                .step(COPY_FILESET_FROM_STAGING_TO_FINAL_TABLE, COPY_FILESET_FROM_STAGING_TO_FINAL_TABLE_COMPLETE, DELETE_FILESET_FROM_STAGING_TABLE, this::deleteFilesetFromStaging)
                .step(DELETE_FILESET_FROM_STAGING_TABLE, DELETE_FILESET_FROM_STAGING_COMPLETE, CHECK_SOURCE_SYSTEM_VENDOR_CODE_IN_INSTITUTE_AND_UPDATE_IF_REQUIRED, this::checkVendorSourceSystemCodeInInstituteAndUpdateVendorCodeIfRequired)
                .end(CHECK_SOURCE_SYSTEM_VENDOR_CODE_IN_INSTITUTE_AND_UPDATE_IF_REQUIRED, COMPLETED_FILESET_STATUS_AND_SOURCE_SYSTEM_VENDOR_CODE_UPDATED, this::echoVendorSourceSystemCodeUpdated)
                .or()
                .end(CHECK_SOURCE_SYSTEM_VENDOR_CODE_IN_INSTITUTE_AND_UPDATE_IF_REQUIRED, COMPLETED_FILESET_STATUS_UPDATED_SOURCE_SYSTEM_VENDOR_CODE_DOES_NOT_NEED_UPDATE);
    }

    public void updateCompletedFilesetStatus(final Event event, final GradSagaEntity saga, final IncomingFilesetSagaData incomingFilesetSagaData) {
        final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
        saga.setSagaState(UPDATE_COMPLETED_FILESET_STATUS.toString());
        saga.setStatus(IN_PROGRESS.toString());
        this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

        incomingFilesetService.setCompletedFilesetStatus(incomingFilesetSagaData.getIncomingFilesetID(), FilesetStatus.COMPLETED);

        final Event.EventBuilder eventBuilder = Event.builder();
        eventBuilder.sagaId(saga.getSagaId()).eventType(UPDATE_COMPLETED_FILESET_STATUS);
        eventBuilder.eventOutcome(COMPLETED_FILESET_STATUS_UPDATED);
        val nextEvent = eventBuilder.build();
        this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
        log.debug("message sent to {} for {} Event. :: {}", this.getTopicToSubscribe(), nextEvent, saga.getSagaId());
    }

    public void copyFilesetToFinalTableFromStaging(final Event event, final GradSagaEntity saga, final IncomingFilesetSagaData incomingFilesetSagaData) {
        final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
        saga.setSagaState(COPY_FILESET_FROM_STAGING_TO_FINAL_TABLE.toString());
        saga.setStatus(IN_PROGRESS.toString());
        this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

        incomingFilesetService.copyFilesetFromStagingToFinal(incomingFilesetSagaData.getIncomingFilesetID());

        final Event.EventBuilder eventBuilder = Event.builder();
        eventBuilder.sagaId(saga.getSagaId()).eventType(COPY_FILESET_FROM_STAGING_TO_FINAL_TABLE);
        eventBuilder.eventOutcome(COPY_FILESET_FROM_STAGING_TO_FINAL_TABLE_COMPLETE);
        val nextEvent = eventBuilder.build();
        this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
        log.debug("message sent to {} for {} Event. :: {}", this.getTopicToSubscribe(), nextEvent, saga.getSagaId());
    }

    public void deleteFilesetFromStaging(final Event event, final GradSagaEntity saga, final IncomingFilesetSagaData incomingFilesetSagaData) {
        final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
        saga.setSagaState(DELETE_FILESET_FROM_STAGING_TABLE.toString());
        saga.setStatus(IN_PROGRESS.toString());
        this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

        incomingFilesetService.deleteFromStagingTables(incomingFilesetSagaData.getIncomingFilesetID());

        final Event.EventBuilder eventBuilder = Event.builder();
        eventBuilder.sagaId(saga.getSagaId()).eventType(DELETE_FILESET_FROM_STAGING_TABLE);
        eventBuilder.eventOutcome(DELETE_FILESET_FROM_STAGING_COMPLETE);
        val nextEvent = eventBuilder.build();
        this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
        log.debug("message sent to {} for {} Event. :: {}", this.getTopicToSubscribe(), nextEvent, saga.getSagaId());
    }


    public void checkVendorSourceSystemCodeInInstituteAndUpdateVendorCodeIfRequired(final Event event, final GradSagaEntity saga, final IncomingFilesetSagaData incomingFilesetSagaData) {
        final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
        saga.setSagaState(CHECK_SOURCE_SYSTEM_VENDOR_CODE_IN_INSTITUTE_AND_UPDATE_IF_REQUIRED.toString());
        saga.setStatus(IN_PROGRESS.toString());
        this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
        
        var incomingFileset = incomingFilesetService.getIncomingFileset(incomingFilesetSagaData.getIncomingFilesetID());

        var demStudent = demographicStudentService.getAnyDemStudentInFileset(incomingFilesetSagaData.getIncomingFilesetID());

        School school = restUtils.getSchoolFromSchoolID(incomingFileset.getSchoolID(), UUID.randomUUID());
        final Event.EventBuilder eventBuilder = Event.builder();

        if (demStudent.isPresent() && school != null && (
                (school.getVendorSourceSystemCode() == null && demStudent.get().getVendorID() != null) ||
                        ("M".equalsIgnoreCase(demStudent.get().getVendorID()) && !"MYED".equalsIgnoreCase(school.getVendorSourceSystemCode())) ||
                        (!"M".equalsIgnoreCase(demStudent.get().getVendorID()) && "MYED".equalsIgnoreCase(school.getVendorSourceSystemCode()))
        )) {
            log.debug("Vendor code needs to be updated for school ID: {}. Current: {}, New: {}", incomingFileset.getSchoolID(), school.getVendorSourceSystemCode(), demStudent.get().getVendorID());
            if ("M".equalsIgnoreCase(demStudent.get().getVendorID())) {
                school.setVendorSourceSystemCode("MYED");
            } else {
                school.setVendorSourceSystemCode("OTHER");
            }
            InstituteStatusEvent response = restUtils.updateSchool(school, UUID.randomUUID());

            if (!response.getEventOutcome().equalsIgnoreCase(EventOutcome.SCHOOL_UPDATED.toString())) {
                log.error("Update vendor code failed for school {}. Response: {}", incomingFileset.getSchoolID(), response);
                throw new GradDataCollectionAPIRuntimeException("Failed to update vendor code: " + response);
            }
            eventBuilder.sagaId(saga.getSagaId()).eventType(CHECK_SOURCE_SYSTEM_VENDOR_CODE_IN_INSTITUTE_AND_UPDATE_IF_REQUIRED);
            eventBuilder.eventOutcome(COMPLETED_FILESET_STATUS_AND_SOURCE_SYSTEM_VENDOR_CODE_UPDATED);
        } else {
            log.debug("Vendor code does not need to be updated for school ID: {}", incomingFileset.getSchoolID());
            eventBuilder.sagaId(saga.getSagaId()).eventType(CHECK_SOURCE_SYSTEM_VENDOR_CODE_IN_INSTITUTE_AND_UPDATE_IF_REQUIRED);
            eventBuilder.eventOutcome(COMPLETED_FILESET_STATUS_UPDATED_SOURCE_SYSTEM_VENDOR_CODE_DOES_NOT_NEED_UPDATE);
        }
        val nextEvent = eventBuilder.build();
        this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
        log.debug("message sent to {} for {} Event. :: {}", this.getTopicToSubscribe(), nextEvent, saga.getSagaId());
    }

    public void echoVendorSourceSystemCodeUpdated(final Event event, final GradSagaEntity saga, final IncomingFilesetSagaData incomingFilesetSagaData) {
        log.debug("Vendor code updated for Saga ID {}, incoming fileset saga data {}", saga.getSagaId(), incomingFilesetSagaData);
    }
}
