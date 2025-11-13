package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | C40 | WARN     | If the Final Letter Grade is IE, the course cannot be past 12 months  |C03, C32 |
 *  |      |          | old.
 *  |      |          | Course Session > today's date plus 12 months.
 */
@Component
@Slf4j
@Order(400)
public class FinalLetterGradeCourseSessionRule implements CourseValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C40: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C40", validationErrorsMap)
                && StringUtils.isNotBlank(studentRuleData.getCourseStudentEntity().getCourseStatus())
                && !studentRuleData.getCourseStudentEntity().getCourseStatus().equalsIgnoreCase("W");

        log.debug("In shouldExecute of C40: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of C40 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        if (StringUtils.isNotBlank(student.getCourseYear()) && StringUtils.isNotBlank(student.getCourseMonth())) {
            try {
                YearMonth courseSession = YearMonth.of(Integer.parseInt(student.getCourseYear()), Integer.parseInt(student.getCourseMonth()));
                YearMonth currentDate = YearMonth.now();
                YearMonth cutoffDate = YearMonth.of(currentDate.getYear() - 1, currentDate.getMonth());

                if (StringUtils.equalsIgnoreCase("IE", student.getFinalLetterGrade()) && courseSession.isBefore(cutoffDate)) {
                    log.debug("C40:Warning: Course session date is more than 12 months old. Report final mark other than IE or update course session date if the course is still in progress. for courseStudentID :: {}", student.getCourseStudentID());
                    errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, ValidationFieldCode.FINAL_LETTER_GRADE, CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_IE, CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_IE.getMessage()));
                }
            } catch (NumberFormatException | DateTimeException e) {
                log.debug("C40: Skipping validation due to invalid course year or month for courseStudentID :: {}", student.getCourseStudentID());
            }
        }
        return errors;
    }
}
