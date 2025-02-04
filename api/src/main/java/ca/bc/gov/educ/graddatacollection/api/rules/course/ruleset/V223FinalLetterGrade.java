package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V223 | ERROR    | Course Code of GT/GTF can only have a letter grade of RM              | V202, V219   |
 *
 */
@Component
@Slf4j
@Order(230)
public class V223FinalLetterGrade implements CourseValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V223: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V223", validationErrorsMap);

        log.debug("In shouldExecute of V223: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of V223 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        List<String> acceptableCourses = List.of("GT", "GTF");

        if (acceptableCourses.stream().anyMatch(course -> StringUtils.equalsIgnoreCase(course, student.getCourseCode())) && !StringUtils.equalsIgnoreCase(student.getFinalGrade(), "RM")) {
            log.debug("V223: Error: Invalid letter grade reported for course code GT or GTF.  Use RM (Requirement Met). This course will not be updated for courseStudentID :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, CourseStudentValidationFieldCode.FINAL_LETTER_GRADE, CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_NOT_RM, CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_NOT_RM.getMessage()));
        }
        return errors;
    }
}
