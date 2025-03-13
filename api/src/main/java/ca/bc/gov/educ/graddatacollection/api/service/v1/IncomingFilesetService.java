package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class IncomingFilesetService {
    private final ApplicationProperties applicationProperties;
    private final IncomingFilesetRepository incomingFilesetRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public IncomingFilesetEntity saveIncomingFilesetRecord(IncomingFilesetEntity currentFileset) {
        log.debug("About to save school file data for fileset: {}", currentFileset.getIncomingFilesetID());
        return this.incomingFilesetRepository.save(currentFileset);
    }

    public void purgeStaleIncomingFilesetRecords() {
        final LocalDateTime oldestIncomingFilesetTimestamp = LocalDateTime.now().minusHours(this.applicationProperties.getIncomingFilesetStaleInHours());
        log.debug("Purging stale IncomingFilesets that were modified before {}.", oldestIncomingFilesetTimestamp);
        this.incomingFilesetRepository.deleteStaleWithUpdateDateBefore(oldestIncomingFilesetTimestamp);
        log.debug("Finished purging stale IncomingFilesets that were modified before {}.", oldestIncomingFilesetTimestamp);
    }

    public IncomingFilesetEntity getErrorFilesetStudent(String pen, UUID incomingFilesetId, UUID schoolID, UUID districtID) {
        Optional<IncomingFilesetEntity> optionalIncomingFilesetEntity = Optional.empty();

        if (incomingFilesetId != null) {
            if (schoolID != null) {
                optionalIncomingFilesetEntity = incomingFilesetRepository.findIncomingFilesetEntityByIncomingFilesetIDAndSchoolIDAndFilesetStatusCode(incomingFilesetId, schoolID, FilesetStatus.COMPLETED.getCode());
            } else if (districtID != null) {
                optionalIncomingFilesetEntity = incomingFilesetRepository.findIncomingFilesetEntityByIncomingFilesetIDAndDistrictIDAndFilesetStatusCode(incomingFilesetId, districtID, FilesetStatus.COMPLETED.getCode());
            }
        } else {
            if (schoolID != null) {
                optionalIncomingFilesetEntity = incomingFilesetRepository
                        .findFirstBySchoolIDAndFilesetStatusCodeAndDemographicStudentEntities_PenAndSchoolIDAndFilesetStatusCodeAndCourseStudentEntities_PenAndSchoolIDAndFilesetStatusCodeAndAssessmentStudentEntities_PenOrderByCreateDateDesc(schoolID, FilesetStatus.COMPLETED.getCode(), pen, schoolID, FilesetStatus.COMPLETED.getCode(), pen, schoolID, FilesetStatus.COMPLETED.getCode(), pen);
            } else if (districtID != null) {
                optionalIncomingFilesetEntity = incomingFilesetRepository
                        .findFirstByDistrictIDAndFilesetStatusCodeAndDemographicStudentEntities_PenAndDistrictIDAndFilesetStatusCodeAndCourseStudentEntities_PenAndDistrictIDAndFilesetStatusCodeAndAssessmentStudentEntities_PenOrderByCreateDateDesc(districtID, FilesetStatus.COMPLETED.getCode(), pen, districtID, FilesetStatus.COMPLETED.getCode(), pen, districtID, FilesetStatus.COMPLETED.getCode(), pen);
            } else {
                throw new IllegalArgumentException("Either schoolID or districtID must be provided.");
            }
        }

        String incomingFilesetIdString = (incomingFilesetId != null) ? incomingFilesetId.toString() : "null";
        String incomingSchoolIdString = (schoolID != null) ? schoolID.toString() : "null";
        String incomingDistrictIdString = (districtID != null) ? districtID.toString() : "null";
        return optionalIncomingFilesetEntity.orElseThrow(() -> new EntityNotFoundException(IncomingFilesetEntity.class, "pen: ", pen, "incomingFilesetId: ", incomingFilesetIdString, "incomingSchoolId: ", incomingSchoolIdString,  "incomingDistrictId: ", incomingDistrictIdString)
        );
    }
}
