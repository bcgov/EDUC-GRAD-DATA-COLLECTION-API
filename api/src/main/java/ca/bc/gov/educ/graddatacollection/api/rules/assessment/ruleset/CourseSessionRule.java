package ca.bc.gov.educ.graddatacollection.api.rules.assessment.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.AssessmentRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.AssessmentStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
*  | ID   | Severity | Rule                                                                                                                        | Dependent On |
*  |------|----------|-----------------------------------------------------------------------------------------------------------------------------|--------------|
*  | V15  | ERROR    | Assessment session and code cannot be a duplicate assessment code and session within the .XAM file for the student.         | V03          |
*  |      |          | Numeracy assessments, NME10, NMF10, NME, and NMF, are all considered the same assessment code.                              |              |
*/
@Component
@Slf4j
@Order(150)
public class CourseSessionRule implements AssessmentValidationBaseRule {

    private final AssessmentRulesService assessmentRulesService;

    public CourseSessionRule(AssessmentRulesService assessmentRulesService) {
        this.assessmentRulesService = assessmentRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<AssessmentStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V15: for assessment {} and assessmentStudentID :: {}", studentRuleData.getAssessmentStudentEntity().getAssessmentID() ,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        var shouldExecute = isValidationDependencyResolved("V15", validationErrorsMap);

        log.debug("In shouldExecute of V15: Condition returned - {} for assessmentStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        return  shouldExecute;
    }

    @Override
    public List<AssessmentStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getAssessmentStudentEntity();
        log.debug("In executeValidation of V15 for assessmentStudentID :: {}", student.getAssessmentStudentID());
        final List<AssessmentStudentValidationIssue> errors = new ArrayList<>();

        if (assessmentRulesService.checkIfStudentHasDuplicatesInFilesetWithNumeracyCheck(student.getIncomingFileset().getIncomingFilesetID(), student.getPen(), student.getCourseCode(), student.getCourseMonth(), student.getCourseYear())){
            log.debug("V15: Error: {} for assessmentStudentID :: {}", AssessmentStudentValidationIssueTypeCode.DUPLICATE_XAM_RECORD.getMessage(), student.getAssessmentStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_SESSION, AssessmentStudentValidationIssueTypeCode.DUPLICATE_XAM_RECORD, AssessmentStudentValidationIssueTypeCode.DUPLICATE_XAM_RECORD.getMessage()));
        }
        return errors;
    }

}
