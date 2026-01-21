package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome;
import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.constants.TopicsEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.IncomingFilesetMapper;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.FinalIncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetPurgeRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFilesetSagaData;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class IncomingFilesetService {
    private final ApplicationProperties applicationProperties;
    private final IncomingFilesetRepository incomingFilesetRepository;
    private final FinalIncomingFilesetRepository finalIncomingFilesetRepository;
    private final IncomingFilesetPurgeRepository incomingFilesetPurgeRepository;
    private final MessagePublisher messagePublisher;
    private static final String EVENT_EMPTY_MSG = "Event String is empty, skipping the publish to topic :: {}";

    @Transactional(propagation = Propagation.MANDATORY)
    public IncomingFilesetEntity saveIncomingFilesetRecord(IncomingFilesetEntity currentFileset) {
        log.debug("About to save school file data for fileset: {}", currentFileset.getIncomingFilesetID());
        return this.incomingFilesetRepository.save(currentFileset);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IncomingFilesetEntity getIncomingFileset(UUID incomingFilesetID) {
        return this.incomingFilesetRepository.findById(incomingFilesetID)
                .orElseThrow(() -> new EntityNotFoundException(IncomingFilesetEntity.class, "incomingFilesetID", incomingFilesetID.toString()));
    }

    public void purgeStaleIncomingFilesetRecords() {
        final LocalDateTime oldestIncomingFilesetTimestamp = LocalDateTime.now().minusHours(this.applicationProperties.getIncomingFilesetStaleInHours());
        log.debug("Purging stale IncomingFilesets that were modified before {}.", oldestIncomingFilesetTimestamp);
        this.incomingFilesetPurgeRepository.deleteStaleWithUpdateDateBefore(oldestIncomingFilesetTimestamp);
        log.debug("Finished purging stale IncomingFilesets that were modified before {}.", oldestIncomingFilesetTimestamp);
    }

    @Async("publisherExecutor")
    public void prepareAndSendCompletedFilesetsForFurtherProcessing(final List<UUID> filesetIds) {
        List<IncomingFilesetSagaData> incomingFilesetSagaData = filesetIds
                .stream()
                .map(uuid -> IncomingFilesetSagaData.builder()
                        .incomingFilesetID(uuid)
                        .build())
                .toList();
        
        this.publishCompletedFilesetRecordsForProcessing(incomingFilesetSagaData);
    }

    public void publishCompletedFilesetRecordsForProcessing(final List<IncomingFilesetSagaData> incomingFilesetSagaData) {
        incomingFilesetSagaData.forEach(this::sendIndividualFilesetAsMessageToTopic);
    }

    private void sendIndividualFilesetAsMessageToTopic(final IncomingFilesetSagaData incomingFilesetSagaData) {
        final var eventPayload = JsonUtil.getJsonString(incomingFilesetSagaData);
        if (eventPayload.isPresent()) {
            final Event event = Event.builder().eventType(EventType.READ_COMPLETED_FILESETS_FOR_PROCESSING).eventOutcome(EventOutcome.READ_COMPLETED_FILESETS_FOR_PROCESSING_SUCCESS).eventPayload(eventPayload.get()).incomingFilesetID(String.valueOf(incomingFilesetSagaData.getIncomingFilesetID())).build();
            final var eventString = JsonUtil.getJsonString(event);
            if (eventString.isPresent()) {
                log.info("Dispatching message for completed fileset event {}", incomingFilesetSagaData.getIncomingFilesetID());
                this.messagePublisher.dispatchMessage(TopicsEnum.READ_COMPLETED_FILESETS_FROM_TOPIC.toString(), eventString.get().getBytes());
            } else {
                log.error(EVENT_EMPTY_MSG, incomingFilesetSagaData);
            }
        } else {
            log.error(EVENT_EMPTY_MSG, incomingFilesetSagaData);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void copyFilesetFromStagingToFinalAndMarkComplete(final UUID incomingFilesetID) {
        log.debug("Copying fileset from staging to final {}", incomingFilesetID);

        // 1. Copy parent fileset
        int parentsCopied = incomingFilesetRepository.copyFilesetParent(incomingFilesetID, FilesetStatus.COMPLETED.getCode());
        log.info("Filesets copied: {}", parentsCopied);

        // 2. Copy demographic students and validation issues
        int demStudentsCopied = incomingFilesetRepository.copyDemographicStudents(incomingFilesetID);
        log.info("Demographic students copied: {}", demStudentsCopied);

        int demIssuesCopied = incomingFilesetRepository.copyDemographicValidationIssues(incomingFilesetID);
        log.info("Demographic validation issues copied: {}", demIssuesCopied);

        // 3. Copy course students and validation issues
        int courseStudentsCopied = incomingFilesetRepository.copyCourseStudents(incomingFilesetID);
        log.info("Course students copied: {}", courseStudentsCopied);

        int courseIssuesCopied = incomingFilesetRepository.copyCourseValidationIssues(incomingFilesetID);
        log.info("Course validation issues copied: {}", courseIssuesCopied);

        // 4. Copy assessment students and validation issues
        int assessmentStudentsCopied = incomingFilesetRepository.copyAssessmentStudents(incomingFilesetID);
        log.info("Assessment students copied: {}", assessmentStudentsCopied);

        int assessmentIssuesCopied = incomingFilesetRepository.copyAssessmentValidationIssues(incomingFilesetID);
        log.info("Assessment validation issues copied: {}", assessmentIssuesCopied);

        // 5. Copy error fileset students
        int errorStudentsCopied = incomingFilesetRepository.copyErrorFilesetStudents(incomingFilesetID);
        log.info("Error fileset students copied: {}", errorStudentsCopied);

        // 6. Mark fileset as complete
        incomingFilesetRepository.markStagedFilesetComplete(incomingFilesetID, FilesetStatus.COMPLETED.getCode());

        log.info("Fileset copy completed successfully for {} - Total: {} demographic, {} course, {} assessment, {} errors",
                incomingFilesetID, demStudentsCopied, courseStudentsCopied, assessmentStudentsCopied, errorStudentsCopied);

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteFromStagingTables(final UUID incomingFilesetID) {
        log.debug("Deleting fileset from staging: {}", incomingFilesetID);
        this.incomingFilesetRepository.deleteByIncomingFilesetID(incomingFilesetID);
        log.debug("Deleting from staging complete for fileset: {}", incomingFilesetID);
    }
}
