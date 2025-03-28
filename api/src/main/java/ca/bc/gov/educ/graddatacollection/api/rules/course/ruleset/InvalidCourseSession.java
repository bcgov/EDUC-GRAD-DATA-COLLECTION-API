package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | C16 | ERROR    | Course session must be no greater than next school year or no less    |C03, C07, C08 |
 *                      than 198401
 */
@Component
@Slf4j
@Order(160)
public class InvalidCourseSession implements CourseValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C16: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C16", validationErrorsMap);

        log.debug("In shouldExecute of C16: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of C16 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        try {
            YearMonth courseSession = YearMonth.of(Integer.parseInt(student.getCourseYear()), Integer.parseInt(student.getCourseMonth()));
            YearMonth earliestValidDate = YearMonth.of(1984, 1);
            LocalDate today = LocalDate.now();
            YearMonth currentSchoolYearStart = YearMonth.of(today.getMonthValue() >= 10 ? today.getYear() : today.getYear() - 1, 10);
            YearMonth nextSchoolYearEnd = YearMonth.of(currentSchoolYearStart.getYear() + 1, 9);

            if (courseSession.isBefore(earliestValidDate) || courseSession.isAfter(nextSchoolYearEnd)) {
                log.debug("C16: Error: Course session is too far into the future (next year reporting cycle) or too far in the past. This course will not be updated. for courseStudentID :: {}", student.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_MONTH, CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID, CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getMessage()));
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_YEAR, CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID, CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getMessage()));
            }
        } catch (NumberFormatException | DateTimeException e) {
            log.debug("C16: Skipping validation due to invalid course year or month for courseStudentID :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_MONTH, CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID, CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getMessage()));
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_YEAR, CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID, CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getMessage()));
        }

        return errors;
    }

}
