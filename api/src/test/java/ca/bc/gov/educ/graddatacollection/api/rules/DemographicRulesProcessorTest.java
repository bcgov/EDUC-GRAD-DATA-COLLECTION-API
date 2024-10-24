package ca.bc.gov.educ.graddatacollection.api.rules;


import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
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
    void testV117DemographicValidGradeRule() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent();
        demographicStudent.setGrade("22");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_GRADE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.GRADE_INVALID.getCode());

        var demographicStudent2 = createMockDemographicStudent();
        demographicStudent2.setGrade(null);
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_GRADE.getCode());
        assertThat(validationError3.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.GRADE_INVALID.getCode());
    }

    @Test
    void testV119DemographicValidGradeRule() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent();
        demographicStudent.setGradRequirementYear("1950");
        demographicStudent.setGrade("07");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_GRADE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.GRADE_AG_INVALID.getCode());
    }

    @Test
    void testV120DemographicValidGradeRule() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent();
        demographicStudent.setGradRequirementYear("SCCP");
        demographicStudent.setGrade("AD");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_GRADE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.GRADE_OG_INVALID.getCode());
    }

    @Test
    void testV124DemographicValidStatusRule() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent();
        demographicStudent.setStudentStatusCode("M");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_STATUS.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_MERGED.getCode());

        var demographicStudent2 = createMockDemographicStudent();
        demographicStudent.setStudentStatusCode(null);
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isZero();
    }
}
