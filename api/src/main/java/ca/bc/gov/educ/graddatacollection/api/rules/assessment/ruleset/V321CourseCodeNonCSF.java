package ca.bc.gov.educ.graddatacollection.api.rules.assessment.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolReportingRequirementCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.AssessmentStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V321 | ERROR    |  Student is in a Non-Francophone school and cannot register for       |--------------|
 *                       this assessment session for this student
 *
 */
@Component
@Slf4j
@Order(270)
public class V321CourseCodeNonCSF implements AssessmentValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<AssessmentStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V321: for assessment {} and assessmentStudentID :: {}", studentRuleData.getAssessmentStudentEntity().getAssessmentID() ,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        var shouldExecute = true;

        log.debug("In shouldExecute of V321: Condition returned - {} for assessmentStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        return  shouldExecute;
    }

    @Override
    public List<AssessmentStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getAssessmentStudentEntity();
        log.debug("In executeValidation of V321 for assessmentStudentID :: {}", student.getAssessmentStudentID());
        final List<AssessmentStudentValidationIssue> errors = new ArrayList<>();

        if (!studentRuleData.getSchool().getSchoolReportingRequirementCode().equalsIgnoreCase(SchoolReportingRequirementCodes.CSF.getCode()) && student.getCourseCode().equalsIgnoreCase("LTP12")){
            log.debug("V321: Student is in a Non-Francophone school and cannot register for this assessment session for this student :: {}", student.getAssessmentStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, ValidationFieldCode.COURSE_CODE, AssessmentStudentValidationIssueTypeCode.COURSE_CODE_NON_CSF, AssessmentStudentValidationIssueTypeCode.COURSE_CODE_NON_CSF.getMessage()));
        }
        return errors;
    }

}
