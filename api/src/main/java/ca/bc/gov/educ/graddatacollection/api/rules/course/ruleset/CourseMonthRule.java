package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | C07  | ERROR    | The submitted value <CRS VALUE> is not an allowable value, per the    | C03          |
 *  |      |          | current GRAD file specification. This course cannot be updated.       |              |
 */
@Component
@Slf4j
@Order(70)
public class CourseMonthRule implements CourseValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C07: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C07", validationErrorsMap);

        log.debug("In shouldExecute of C07: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of C07 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();
        String errorMessage = CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getMessage().formatted(StringEscapeUtils.escapeHtml4(student.getCourseMonth()));

        if (StringUtils.isNotBlank(student.getCourseMonth())) {
            try {
                int monthValue = Integer.parseInt(student.getCourseMonth());

                if (monthValue > 12 || monthValue < 1) {
                    logDebugStatement(errorMessage, student.getCourseStudentID());
                    errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_MONTH, CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID, errorMessage));
                }
            } catch (NumberFormatException e) {
                logDebugStatement(errorMessage, student.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_MONTH, CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID, errorMessage));
            }
        } else {
            logDebugStatement(errorMessage, student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_MONTH, CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID, errorMessage));
        }
        return errors;
    }

    private void logDebugStatement(String errorMessage, java.util.UUID courseStudentID) {
        log.debug("C07: Error: {} for courseStudentID :: {}", errorMessage, courseStudentID);
    }

}
