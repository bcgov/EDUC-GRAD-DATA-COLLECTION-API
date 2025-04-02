package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | C13 | WARN     | Course session date plus day of 01 should not be before the course    |C03, C07, C08 |
 *  |      |          | start date
 */
@Component
@Slf4j
@Order(130)
public class CourseSessionBeforeCourseStartRule implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public CourseSessionBeforeCourseStartRule(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }
    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C13: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C13", validationErrorsMap);

        log.debug("In shouldExecute of C13: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of C13 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        String paddedCourseCode = String.format("%-5s", student.getCourseCode());
        var coursesRecord = courseRulesService.getCoregCoursesRecord(studentRuleData, paddedCourseCode + student.getCourseLevel());

        if (coursesRecord != null) {
            LocalDate courseSessionDate = LocalDate.parse(student.getCourseYear() + "-" + student.getCourseMonth() + "-01");
            LocalDate courseStartDate = LocalDateTime.parse(coursesRecord.getStartDate()).toLocalDate();

            if (courseSessionDate.isBefore(courseStartDate)) {
                log.debug("C13: Warning: The school is reporting a student enrolled in a course at time when the course was not open (i.e., course session date is before the course open date). for courseStudentID :: {}", student.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, ValidationFieldCode.COURSE_MONTH, CourseStudentValidationIssueTypeCode.COURSE_SESSION_START_DATE_INVALID, CourseStudentValidationIssueTypeCode.COURSE_SESSION_START_DATE_INVALID.getMessage()));
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, ValidationFieldCode.COURSE_YEAR, CourseStudentValidationIssueTypeCode.COURSE_SESSION_START_DATE_INVALID, CourseStudentValidationIssueTypeCode.COURSE_SESSION_START_DATE_INVALID.getMessage()));
            }
        } else {
            log.debug("C13: Warning: No Coreg course record match. for courseStudentID :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, ValidationFieldCode.COURSE_MONTH, CourseStudentValidationIssueTypeCode.COURSE_SESSION_START_DATE_INVALID, CourseStudentValidationIssueTypeCode.COURSE_SESSION_START_DATE_INVALID.getMessage()));
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, ValidationFieldCode.COURSE_YEAR, CourseStudentValidationIssueTypeCode.COURSE_SESSION_START_DATE_INVALID, CourseStudentValidationIssueTypeCode.COURSE_SESSION_START_DATE_INVALID.getMessage()));
        }

        return errors;
    }

}
