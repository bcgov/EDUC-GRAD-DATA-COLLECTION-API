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
 *  | V224 | ERROR    | Final pct or Final Letter Grade should be included for completed      | V202, V218,  |
 *  |      |          | courses                                                               | V220         |
 *  |      |          | Future = Course Session < today's date                                |              |
 */
@Component
@Slf4j
@Order(240)
public class V224FinalPercentage implements CourseValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V224: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V224", validationErrorsMap);

        log.debug("In shouldExecute of V224: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of V224 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        if (StringUtils.isNotBlank(student.getCourseYear()) && StringUtils.isNotBlank(student.getCourseMonth())) {
            try {
                YearMonth courseSession = YearMonth.of(Integer.parseInt(student.getCourseYear()), Integer.parseInt(student.getCourseMonth()));
                YearMonth currentDate = YearMonth.now();
                // refer to v218 - final percentage before 199409 must be blank
                YearMonth cutoffDate = YearMonth.of(1994, 9);

                if (courseSession.isBefore(currentDate) && (StringUtils.isBlank(student.getFinalGrade()) || (courseSession.isAfter(cutoffDate) && StringUtils.isBlank(student.getFinalPercentage())))) {
                    log.debug("V224: Error: Course session has passed with no final mark. Report final mark or change the course session date. This course will not be updated / added. for courseStudentID :: {}", student.getCourseStudentID());
                    errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.FINAL_PERCENTAGE, CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_OR_PERCENT_BLANK, CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_OR_PERCENT_BLANK.getMessage()));
                }
            } catch (NumberFormatException | DateTimeException e) {
                log.debug("V224: Skipping validation due to invalid course year or month for courseStudentID :: {}", student.getCourseStudentID());
            }
        }
        return errors;
    }
}
