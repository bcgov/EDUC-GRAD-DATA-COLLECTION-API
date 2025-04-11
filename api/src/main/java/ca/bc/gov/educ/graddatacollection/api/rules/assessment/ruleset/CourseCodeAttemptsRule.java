package ca.bc.gov.educ.graddatacollection.api.rules.assessment.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.AssessmentRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.AssessmentStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V20 | ERROR    |  Error if the course status is not "W" and the student already        |V03, V18|
 *                       has three write attempts for the submitted assessment (course code)
 *
 */
@Component
@Slf4j
@Order(200)
public class CourseCodeAttemptsRule implements AssessmentValidationBaseRule {

    private final AssessmentRulesService assessmentRulesService;

    public CourseCodeAttemptsRule(AssessmentRulesService assessmentRulesService) {
        this.assessmentRulesService = assessmentRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<AssessmentStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V20: for assessment {} and assessmentStudentID :: {}", studentRuleData.getAssessmentStudentEntity().getAssessmentID() ,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        var shouldExecute = isValidationDependencyResolved("V20", validationErrorsMap);

        log.debug("In shouldExecute of V20: Condition returned - {} for assessmentStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        return  shouldExecute;
    }

    @Override
    public List<AssessmentStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getAssessmentStudentEntity();
        log.debug("In executeValidation of V20 for assessmentStudentID :: {}", student.getAssessmentStudentID());
        final List<AssessmentStudentValidationIssue> errors = new ArrayList<>();

        int numberOfAttempts = 0;
        if(studentRuleData.getAssessmentStudentDetail() == null){
            if(studentRuleData.getStudentApiStudent() == null) {
                var studentApiStudent = assessmentRulesService.getStudentApiStudent(studentRuleData, student.getPen());
                studentRuleData.setStudentApiStudent(studentApiStudent);
            }
            var studAssessmentDetail = assessmentRulesService.getAssessmentStudentDetail(UUID.fromString(studentRuleData.getStudentApiStudent().getStudentID()), student.getAssessmentID());
            studentRuleData.setAssessmentStudentDetail(studAssessmentDetail);
        }

        if(studentRuleData.getAssessmentStudentDetail() != null && StringUtils.isNotBlank(studentRuleData.getAssessmentStudentDetail().getNumberOfAttempts())){
            numberOfAttempts = Integer.parseInt(studentRuleData.getAssessmentStudentDetail().getNumberOfAttempts());
        }

        if (numberOfAttempts >= 3 && !studentRuleData.getAssessmentStudentEntity().getCourseStatus().equalsIgnoreCase("W")){
            log.debug("V20: Error if the course status is not W and the student already has three write attempts for the submitted assessment (course code). :: {}", student.getAssessmentStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_CODE, AssessmentStudentValidationIssueTypeCode.COURSE_CODE_ATTEMPTS,
                    "The student has reached the maximum number of writes for " + studentRuleData.getAssessmentStudentEntity().getCourseCode() + ". The registration will not be updated."));
        }
        return errors;
    }

}
