package ca.bc.gov.educ.graddatacollection.api.rules.assessment.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.AssessmentRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.AssessmentStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V03 | ERROR     | The Assessment Code provided is not valid for the Assessment Session  |    V02  |
 *                      specified.
 */
@Component
@Slf4j
@Order(30)
public class CourseCodeRule implements AssessmentValidationBaseRule {

    private final AssessmentRulesService assessmentRulesService;

    public CourseCodeRule(AssessmentRulesService assessmentRulesService) {
        this.assessmentRulesService = assessmentRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<AssessmentStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V03: for assessment {} and assessmentStudentID :: {}", studentRuleData.getAssessmentStudentEntity().getAssessmentID() ,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        var shouldExecute = isValidationDependencyResolved("V03", validationErrorsMap);

        log.debug("In shouldExecute of V03: Condition returned - {} for assessmentStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        return  shouldExecute;
    }

    @Override
    public List<AssessmentStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getAssessmentStudentEntity();
        log.debug("In executeValidation of V03 for assessmentStudentID :: {}", student.getAssessmentStudentID());
        final List<AssessmentStudentValidationIssue> errors = new ArrayList<>();

        log.debug("V03: Assessment Student is :: {}", student);
        log.debug("V03: Assessment Student is valid for session :: {}", assessmentRulesService.courseIsValidForOpenSession(student.getCourseYear(), student.getCourseMonth(), student.getCourseCode()));

        String logTemplate = "V03: Error: {} for assessmentStudentID :: {}";

        if (!assessmentRulesService.sessionYearIsValid(student.getCourseYear())) {
            log.debug(logTemplate, AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_YEAR.getMessage(), student.getAssessmentStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_YEAR, AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_YEAR, AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_YEAR.getMessage()));
        }else if (!assessmentRulesService.sessionMonthIsValid(student.getCourseMonth())) {
            log.debug(logTemplate, AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getMessage(), student.getAssessmentStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_MONTH, AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH, AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getMessage()));
        }else if (!assessmentRulesService.sessionIsValidAndOpen(student.getCourseYear(), student.getCourseMonth())) {
            String errorMessage = AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_DATE_NOT_UNAPPROVED_SESSION.getMessage().formatted(StringEscapeUtils.escapeHtml4(student.getCourseYear()), StringEscapeUtils.escapeHtml4(student.getCourseMonth()));
            log.debug(logTemplate, errorMessage, student.getAssessmentStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_YEAR, AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_DATE_NOT_UNAPPROVED_SESSION, errorMessage));
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_MONTH, AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_DATE_NOT_UNAPPROVED_SESSION, errorMessage));
        }else if (!assessmentRulesService.courseIsValidForOpenSession(student.getCourseYear(), student.getCourseMonth(), student.getCourseCode())) {
            String errorMessage = AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getMessage().formatted(StringEscapeUtils.escapeHtml4(student.getCourseCode()), StringEscapeUtils.escapeHtml4(student.getCourseYear()), StringEscapeUtils.escapeHtml4(student.getCourseMonth()));
            log.debug(logTemplate, errorMessage, student.getAssessmentStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_CODE, AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID, errorMessage));
        }

        return errors;
    }

}
