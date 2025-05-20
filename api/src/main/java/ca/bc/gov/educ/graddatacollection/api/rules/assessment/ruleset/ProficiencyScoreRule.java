package ca.bc.gov.educ.graddatacollection.api.rules.assessment.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.AssessmentRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1.AssessmentStudentDetailResponse;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.AssessmentStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *  | ID          | Severity | Rule                                                                                              | Dependent On |
 *  |-------------|----------|---------------------------------------------------------------------------------------------------|--------------|
 *  | V19         | ERROR    | The student has already received a Proficiency Score or Special Case for this assessment session. | V03, V18     |
 */
@Component
@Slf4j
@Order(190)
public class ProficiencyScoreRule implements AssessmentValidationBaseRule {

    private final AssessmentRulesService assessmentRulesService;

    public ProficiencyScoreRule(AssessmentRulesService assessmentRulesService) {
        this.assessmentRulesService = assessmentRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<AssessmentStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V19: for assessment {} and assessmentStudentID :: {}", studentRuleData.getAssessmentStudentEntity().getAssessmentID() ,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        var shouldExecute = isValidationDependencyResolved("V19", validationErrorsMap);

        log.debug("In shouldExecute of V19: Condition returned - {} for assessmentStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        return  shouldExecute;
    }

    @Override
    public List<AssessmentStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getAssessmentStudentEntity();
        log.debug("In executeValidation of V19 for assessmentStudentID :: {}", student.getAssessmentStudentID());
        final List<AssessmentStudentValidationIssue> errors = new ArrayList<>();

        if (!studentRuleData.getAssessmentStudentEntity().getCourseStatus().equalsIgnoreCase("W")) {
            return errors;
        }

        var studentApiStudent = assessmentRulesService.getStudentApiStudent(studentRuleData, student.getPen());
        var assessmentID = assessmentRulesService.getAssessmentID(student.getCourseYear(), student.getCourseMonth(), student.getCourseCode());
        log.info("V19: Found assesssment ID is :: {} for assessmentStudentID :: {}", assessmentID, student.getAssessmentStudentID());

        AssessmentStudentDetailResponse studAssessmentDetail = studentRuleData.getAssessmentStudentDetail();

        if (studentApiStudent != null && studAssessmentDetail == null) {
            studAssessmentDetail = assessmentRulesService.getAssessmentStudentDetail(UUID.fromString(studentApiStudent.getStudentID()), UUID.fromString(assessmentID));
            studentRuleData.setAssessmentStudentDetail(studAssessmentDetail);
        }

        if (studAssessmentDetail == null || (studAssessmentDetail.isHasPriorRegistration() && studAssessmentDetail.isAlreadyWrittenAssessment())) {
            log.debug("V19: Error: {} for assessmentStudentID :: {}", AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_DUP.getMessage(), student.getAssessmentStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_CODE, AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_DUP, AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_DUP.getMessage()));
        }

        return errors;
    }

}
