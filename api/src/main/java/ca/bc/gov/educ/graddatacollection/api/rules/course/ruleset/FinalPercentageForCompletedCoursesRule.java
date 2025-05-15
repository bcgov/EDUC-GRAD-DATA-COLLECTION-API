package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
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
 *  | C25  | ERROR    | Final pct or Final Letter Grade should be included for completed      | C03, C16, C31|
 *  |      |          | courses
 *  |      |          | Future = Course Session < today's date
 */
@Component
@Slf4j
@Order(250)
public class FinalPercentageForCompletedCoursesRule implements CourseValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C25: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C25", validationErrorsMap);

        log.debug("In shouldExecute of C25: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of C25 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        if (StringUtils.isNotBlank(student.getCourseYear()) && StringUtils.isNotBlank(student.getCourseMonth())) {
            try {
                YearMonth courseSession = YearMonth.of(Integer.parseInt(student.getCourseYear()), Integer.parseInt(student.getCourseMonth()));
                YearMonth currentDate = YearMonth.now();

                // Only one of final percent or final letter grade is required
                boolean hasFinalLetterGrade = StringUtils.isNotBlank(student.getFinalLetterGrade());
                boolean hasFinalPercent = StringUtils.isNotBlank(student.getFinalPercentage());

                if (courseSession.isBefore(currentDate) && !hasFinalLetterGrade && !hasFinalPercent) {
                    log.debug("C25: Error: {} for courseStudentID :: {}", CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_OR_PERCENT_BLANK.getMessage(), student.getCourseStudentID());
                    errors.add(createValidationIssue(
                        StudentValidationIssueSeverityCode.ERROR,
                        ValidationFieldCode.FINAL_PERCENTAGE,
                        CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_OR_PERCENT_BLANK,
                        CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_OR_PERCENT_BLANK.getMessage()
                    ));
                }
            } catch (NumberFormatException | DateTimeException e) {
                log.debug("C25: Skipping validation due to invalid course year or month for courseStudentID :: {}", student.getCourseStudentID());
            }
        }
        return errors;
    }
}
