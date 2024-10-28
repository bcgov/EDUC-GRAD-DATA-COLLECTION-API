package ca.bc.gov.educ.graddatacollection.api.rules;
import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradRequirementYearCodes;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentRulesProcessor;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.scholarships.v1.CitizenshipCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
class DemographicRulesProcessorTest extends BaseGradDataCollectionAPITest {

    @Autowired
    private DemographicStudentRulesProcessor rulesProcessor;

    @MockBean
    private RestUtils restUtils;

    @BeforeEach
    void setUp() {
        when(restUtils.getScholarshipsCitizenshipCodes()).thenReturn(
                List.of(
                        new CitizenshipCode("C", "Canadian", "Valid Citizenship Code", 1, "2020-01-01", "2099-12-31"),
                        new CitizenshipCode("O", "Other", "Valid Citizenship Code", 2, "2020-01-01", "2099-12-31"),
                        new CitizenshipCode("", "Blank", "Valid for Blank Citizenship", 3, "2020-01-01", "2099-12-31")
                )
        );
    }

    @Test
    void testV109DemographicStudentCitizenship() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent();
        demographicStudent.setCitizenship("Z");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_CITIZENSHIP_CODE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_CITIZENSHIP_CODE_INVALID.getCode());
    }

    @Test
    void testV126DemographicSCCPCompletionDate() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent();
        demographicStudent.setSchoolCertificateCompletionDate("20041312");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.SCCP_COMPLETION_DATE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getCode());

        var demographicStudent2 = createMockDemographicStudent();
        demographicStudent2.setSchoolCertificateCompletionDate("20042");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.SCCP_COMPLETION_DATE.getCode());
        assertThat(validationError3.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getCode());

        var demographicStudent3 = createMockDemographicStudent();
        demographicStudent3.setSchoolCertificateCompletionDate(null);
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent3, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.SCCP_COMPLETION_DATE.getCode());
        assertThat(validationError4.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getCode());
    }

    @Test
    void testV127DemographicSCCPCompletionDate() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent();
        demographicStudent.setGradRequirementYear(GradRequirementYearCodes.YEAR_2023.getCode());
        assertThat(demographicStudent.getGradRequirementYear()).isEqualTo(GradRequirementYearCodes.YEAR_2023.getCode());

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.SCCP_COMPLETION_DATE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_STUDENT_PROGRAM.getCode());

        var demographicStudent2 = createMockDemographicStudent();
        demographicStudent2.setSchoolCertificateCompletionDate("20050701");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));

        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.SCCP_COMPLETION_DATE.getCode());
        assertThat(validationError3.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getCode());
    }
}
