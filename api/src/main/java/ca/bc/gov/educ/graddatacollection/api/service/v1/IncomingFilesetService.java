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
                this.messagePublisher.dispatchMessage(TopicsEnum.READ_COMPLETED_FILESETS_FROM_TOPIC.toString(), eventString.get().getBytes());
            } else {
                log.error(EVENT_EMPTY_MSG, incomingFilesetSagaData);
            }
        } else {
            log.error(EVENT_EMPTY_MSG, incomingFilesetSagaData);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setCompletedFilesetStatus(final UUID incomingFilesetID, final FilesetStatus filesetStatus) {
        log.debug("Setting completed status for fileset: {}", incomingFilesetID);
        var incomingFilesetEntity = this.incomingFilesetRepository.findById(incomingFilesetID)
                .orElseThrow(() -> new EntityNotFoundException(IncomingFilesetEntity.class, "incomingFilesetID", incomingFilesetID.toString()));
        incomingFilesetEntity.setFilesetStatusCode(filesetStatus.getCode());
        incomingFilesetEntity.setUpdateDate(LocalDateTime.now());
        this.incomingFilesetRepository.save(incomingFilesetEntity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void copyFilesetFromStagingToFinal(final UUID incomingFilesetID) {
        log.debug("Copying fileset from staging to final {}", incomingFilesetID);

        var staged = this.incomingFilesetRepository.findById(incomingFilesetID)
                .orElseThrow(() -> new EntityNotFoundException(IncomingFilesetEntity.class, "incomingFilesetID", incomingFilesetID.toString()));

        // Map parent entity (without children)
        var finalFileset = IncomingFilesetMapper.mapper.stagedToEntity(staged);

        // Copy demographic students and their validation issues
        staged.getDemographicStudentEntities().forEach(stagedDemo -> {
            var finalDemo = IncomingFilesetMapper.mapper.stagedDemographicToEntity(stagedDemo);
            finalDemo.setIncomingFileset(finalFileset);

            // Copy validation issues for this demographic student
            stagedDemo.getDemographicStudentValidationIssueEntities().forEach(stagedIssue -> {
                var finalIssue = IncomingFilesetMapper.mapper.stagedDemographicValidationIssueToEntity(stagedIssue);
                finalIssue.setDemographicStudent(finalDemo);
                finalDemo.getDemographicStudentValidationIssueEntities().add(finalIssue);
            });

            finalFileset.getDemographicStudentEntities().add(finalDemo);
        });

        // Copy course students and their validation issues
        staged.getCourseStudentEntities().forEach(stagedCourse -> {
            var finalCourse = IncomingFilesetMapper.mapper.stagedCourseToEntity(stagedCourse);
            finalCourse.setIncomingFileset(finalFileset);

            // Copy validation issues for this course student
            stagedCourse.getCourseStudentValidationIssueEntities().forEach(stagedIssue -> {
                var finalIssue = IncomingFilesetMapper.mapper.stagedCourseValidationIssueToEntity(stagedIssue);
                finalIssue.setCourseStudent(finalCourse);
                finalCourse.getCourseStudentValidationIssueEntities().add(finalIssue);
            });

            finalFileset.getCourseStudentEntities().add(finalCourse);
        });

        // Copy assessment students and their validation issues
        staged.getAssessmentStudentEntities().forEach(stagedAssessment -> {
            var finalAssessment = IncomingFilesetMapper.mapper.stagedAssessmentToEntity(stagedAssessment);
            finalAssessment.setIncomingFileset(finalFileset);

            // Copy validation issues for this assessment student
            stagedAssessment.getAssessmentStudentValidationIssueEntities().forEach(stagedIssue -> {
                var finalIssue = IncomingFilesetMapper.mapper.stagedAssessmentValidationIssueToEntity(stagedIssue);
                finalIssue.setAssessmentStudent(finalAssessment);
                finalAssessment.getAssessmentStudentValidationIssueEntities().add(finalIssue);
            });

            finalFileset.getAssessmentStudentEntities().add(finalAssessment);
        });

        // Copy error fileset students
        staged.getErrorFilesetStudentEntities().forEach(stagedError -> {
            var finalError = IncomingFilesetMapper.mapper.stagedErrorToEntity(stagedError);
            finalError.setIncomingFileset(finalFileset);
            finalFileset.getErrorFilesetStudentEntities().add(finalError);
        });

        finalIncomingFilesetRepository.save(finalFileset);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteFromStagingTables(final UUID incomingFilesetID) {
        log.debug("Deleting fileset from staging: {}", incomingFilesetID);
        this.incomingFilesetRepository.deleteByIncomingFilesetID(incomingFilesetID);
        log.debug("Deleting from staging complete for fileset: {}", incomingFilesetID);
    }
}
