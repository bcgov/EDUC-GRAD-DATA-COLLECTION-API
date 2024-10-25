package ca.bc.gov.educ.graddatacollection.api.rules;


import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradRequirementYearCodes;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentRulesProcessor;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class DemographicRulesProcessorTest extends BaseGradDataCollectionAPITest {

    @Autowired
    private DemographicStudentRulesProcessor rulesProcessor;

    @Test
    void testV101DemographicTxIDRule() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent();
        demographicStudent.setTransactionID("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.TX_ID.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.TXID_INVALID.getCode());
    }

    @Test
    void testV134DemographicSCCPCompletionDate() {
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
    void testV135DemographicSCCPCompletionDate() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent();
        demographicStudent.setGradRequirementYear(GradRequirementYearCodes.YEAR_2023.getCode());
        assertThat(demographicStudent.getGradRequirementYear()).isEqualTo(GradRequirementYearCodes.YEAR_2023.getCode());

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.SCCP_COMPLETION_DATE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_STUDENT_PROGRAM.getCode());
    }
}
