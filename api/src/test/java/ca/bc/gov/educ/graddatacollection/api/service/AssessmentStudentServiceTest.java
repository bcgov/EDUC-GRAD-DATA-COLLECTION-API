package ca.bc.gov.educ.graddatacollection.api.service;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolStudentStatus;
import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.AssessmentStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
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
    private IncomingFilesetRepository incomingFilesetRepository;
    @Autowired
    private AssessmentStudentRepository assessmentStudentRepository;

    @Autowired
    private AssessmentStudentService assessmentStudentService;

    @Test
    void getXamStudents_withSchoolId_shouldReturnList() {
        String pen = "123456789";
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var fileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        fileset.setFilesetStatusCode(FilesetStatus.COMPLETED.getCode());
        var incomingFileset = incomingFilesetRepository.save(fileset);

        AssessmentStudentEntity expected = new AssessmentStudentEntity();
        expected.setPen(pen);
        expected.setIncomingFileset(incomingFileset);
        expected.setStudentStatusCode(SchoolStudentStatus.VERIFIED.getCode());
        expected.setCreateUser("ABC");
        expected.setUpdateUser("ABC");
        List<AssessmentStudentEntity> expectedList = List.of(expected);

        assessmentStudentRepository.save(expected);

        List<AssessmentStudentEntity> result = assessmentStudentService.getXamStudents(pen, incomingFileset.getIncomingFilesetID(), incomingFileset.getSchoolID());
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
