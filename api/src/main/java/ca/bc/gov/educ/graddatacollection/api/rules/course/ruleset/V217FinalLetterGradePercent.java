package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V217 | ERROR    | Final pct or Final Letter Grade should not be included for future     | V202, V212   |
 *  |      |          | courses                                                               |              |
 *  |      |          | Future = Course Session > today's date                                |              |
 */
@Component
@Slf4j
@Order(170)
public class V217FinalLetterGradePercent implements CourseValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V217: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V217", validationErrorsMap);

        log.debug("In shouldExecute of V217: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of V217 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        if (StringUtils.isNotBlank(student.getCourseYear()) && StringUtils.isNotBlank(student.getCourseMonth())) {
            try {
                YearMonth courseSession = YearMonth.of(Integer.parseInt(student.getCourseYear()), Integer.parseInt(student.getCourseMonth()));
                YearMonth currentDate = YearMonth.now();

                if (courseSession.isAfter(currentDate) && (StringUtils.isNotBlank(student.getFinalGrade()) || StringUtils.isNotBlank(student.getFinalPercentage()))) {
                    log.debug("V217: Error: Final mark submitted but course session date is in the future. Change the course session date or remove the final mark. This course will not be updated. for courseStudentID :: {}", student.getCourseStudentID());
                    errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, CourseStudentValidationFieldCode.FINAL_LETTER_GRADE_PERCENTAGE, CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_OR_PERCENT_NOT_BLANK, CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_OR_PERCENT_NOT_BLANK.getMessage()));
                }
            } catch (NumberFormatException | DateTimeException e) {
                log.debug("V217: Skipping validation due to invalid course year or month for courseStudentID :: {}", student.getCourseStudentID());
            }
        }
        return errors;
    }
}
