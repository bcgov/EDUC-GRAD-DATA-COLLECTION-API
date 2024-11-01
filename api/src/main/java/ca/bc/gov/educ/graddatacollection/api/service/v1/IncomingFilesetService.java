package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolStudentStatus;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.AssessmentStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.CourseStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.FileUploadCounts;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.FileUploadSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class IncomingFilesetService {

    private final IncomingFilesetRepository incomingFilesetRepository;
    private final DemographicStudentRepository demographicStudentRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final AssessmentStudentRepository assessmentStudentRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public IncomingFilesetEntity saveIncomingFilesetRecord(IncomingFilesetEntity currentFileset) {
        log.debug("About to save school file data for fileset: {}", currentFileset.getIncomingFilesetID());
        return this.incomingFilesetRepository.save(currentFileset);
    }

    public FileUploadSummary getSummaryOfFilesBeingProcessed(UUID schoolID) {
        var fileSetEntityOptional = incomingFilesetRepository.findBySchoolID(schoolID);
        FileUploadSummary fileUploadSummary = new FileUploadSummary();
        if(fileSetEntityOptional.isPresent()) {
            var fileSetEntity = fileSetEntityOptional.get();
            fileUploadSummary.setSchoolID(String.valueOf(fileSetEntity.getSchoolID()));
            List<FileUploadCounts> counts = new ArrayList<>();
            if(fileSetEntity.getDemFileStatusCode().equalsIgnoreCase(FilesetStatus.LOADED.getCode())) {
                counts.add(getSummaryOfDEMFilesBeingProcessed(fileSetEntity));
            }
            if(fileSetEntity.getCrsFileStatusCode().equalsIgnoreCase(FilesetStatus.LOADED.getCode())) {
                counts.add(getSummaryOfCRSFilesBeingProcessed(fileSetEntity));
            }
            if(fileSetEntity.getXamFileStatusCode().equalsIgnoreCase(FilesetStatus.LOADED.getCode())) {
                counts.add(getSummaryOfXAMFilesBeingProcessed(fileSetEntity));
            }
            fileUploadSummary.setCounts(counts);
        }
        return fileUploadSummary;
    }

    public FileUploadCounts getSummaryOfDEMFilesBeingProcessed(IncomingFilesetEntity fileSetEntity) {
        FileUploadCounts uploadCount = new FileUploadCounts();
        uploadCount.setFileName(fileSetEntity.getDemFileName());
        uploadCount.setUploadDate(String.valueOf(fileSetEntity.getDemFileUploadDate()));

        var demTotalCount = demographicStudentRepository.countByIncomingFileset_IncomingFilesetID(fileSetEntity.getIncomingFilesetID());
        var demLoadedCount = demographicStudentRepository.countByStudentStatusCodeAndAndIncomingFileset_IncomingFilesetID(SchoolStudentStatus.LOADED.getCode(), fileSetEntity.getIncomingFilesetID());
        uploadCount.setPercentageStudentsProcessed(String.valueOf(getPercentageOfStudentsProcessed((demTotalCount - demLoadedCount), demTotalCount)));
        return uploadCount;
    }

    public FileUploadCounts getSummaryOfCRSFilesBeingProcessed(IncomingFilesetEntity fileSetEntity) {
        FileUploadCounts uploadCount = new FileUploadCounts();
        uploadCount.setFileName(fileSetEntity.getCrsFileName());
        uploadCount.setUploadDate(String.valueOf(fileSetEntity.getCrsFileUploadDate()));

        var courseTotalCount = courseStudentRepository.countByIncomingFileset_IncomingFilesetID(fileSetEntity.getIncomingFilesetID());
        var courseLoadedCount = courseStudentRepository.countByStudentStatusCodeAndAndIncomingFileset_IncomingFilesetID(SchoolStudentStatus.LOADED.getCode(), fileSetEntity.getIncomingFilesetID());
        uploadCount.setPercentageStudentsProcessed(String.valueOf(getPercentageOfStudentsProcessed((courseTotalCount - courseLoadedCount), courseTotalCount)));
        return uploadCount;
    }

    public FileUploadCounts getSummaryOfXAMFilesBeingProcessed(IncomingFilesetEntity fileSetEntity) {
        FileUploadCounts uploadCount = new FileUploadCounts();
        uploadCount.setFileName(fileSetEntity.getXamFileName());
        uploadCount.setUploadDate(String.valueOf(fileSetEntity.getXamFileUploadDate()));

        var assessmentTotalCount = assessmentStudentRepository.countByIncomingFileset_IncomingFilesetID(fileSetEntity.getIncomingFilesetID());
        var assessmentLoadedCount = assessmentStudentRepository.countByStudentStatusCodeAndIncomingFileset_IncomingFilesetID(SchoolStudentStatus.LOADED.getCode(), fileSetEntity.getIncomingFilesetID());
        uploadCount.setPercentageStudentsProcessed(String.valueOf(getPercentageOfStudentsProcessed((assessmentTotalCount - assessmentLoadedCount), assessmentTotalCount)));
        return uploadCount;
    }

    private int getPercentageOfStudentsProcessed(long totalProcessed, long totalStudents) {
        return (int) Math.floor((double) totalProcessed / totalStudents * 100);
    }
}
