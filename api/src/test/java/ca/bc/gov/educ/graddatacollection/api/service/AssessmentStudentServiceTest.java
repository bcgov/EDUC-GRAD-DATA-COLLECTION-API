package ca.bc.gov.educ.graddatacollection.api.service;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolStudentStatus;
import ca.bc.gov.educ.graddatacollection.api.model.v1.FinalAssessmentStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.FinalAssessmentStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.FinalIncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ReportingPeriodRepository;
import ca.bc.gov.educ.graddatacollection.api.service.v1.AssessmentStudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssessmentStudentServiceTest extends BaseGradDataCollectionAPITest {

    @Autowired
    private ReportingPeriodRepository reportingPeriodRepository;
    @Autowired
    private FinalIncomingFilesetRepository incomingFilesetRepository;
    @Autowired
    private FinalAssessmentStudentRepository assessmentStudentRepository;

    @Autowired
    private AssessmentStudentService assessmentStudentService;

    @Test
    void getXamStudents_withSchoolId_shouldReturnList() {
        String pen = "123456789";
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var fileset = createMockFinalIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        fileset.setFilesetStatusCode(FilesetStatus.COMPLETED.getCode());
        var incomingFileset = incomingFilesetRepository.save(fileset);

        FinalAssessmentStudentEntity expected = new FinalAssessmentStudentEntity();
        expected.setPen(pen);
        expected.setIncomingFileset(incomingFileset);
        expected.setStudentStatusCode(SchoolStudentStatus.VERIFIED.getCode());
        expected.setCreateUser("ABC");
        expected.setUpdateUser("ABC");
        List<FinalAssessmentStudentEntity> expectedList = List.of(expected);

        assessmentStudentRepository.save(expected);

        List<FinalAssessmentStudentEntity> result = assessmentStudentService.getXamStudents(pen, incomingFileset.getIncomingFilesetID(), incomingFileset.getSchoolID());
        assertThat(result).isEqualTo(expectedList);
    }

    @Test
    void getXamStudents_withoutSchoolOrDistrict_shouldThrowIllegalArgumentException() {
        String pen = "123456789";
        UUID incomingFilesetId = UUID.randomUUID();
        UUID schoolId = null;

        assertThatThrownBy(() ->
                assessmentStudentService.getXamStudents(pen, incomingFilesetId, schoolId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("schoolID must be provided.");
    }
}
