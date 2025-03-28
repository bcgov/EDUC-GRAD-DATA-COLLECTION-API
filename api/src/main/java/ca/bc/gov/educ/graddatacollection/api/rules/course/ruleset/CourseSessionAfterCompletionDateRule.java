package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | C14 | WARN     | Course session date plus day of 01 should not be after the course     | C03, C07, C08|
 *  |      |          | completion date
 */
@Component
@Slf4j
@Order(140)
public class CourseSessionAfterCompletionDateRule implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public CourseSessionAfterCompletionDateRule(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }
    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C14: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C14", validationErrorsMap);

        log.debug("In shouldExecute of C14: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of C14 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        String paddedCourseCode = String.format("%-5s", student.getCourseCode());
        var coursesRecord = courseRulesService.getCoregCoursesRecord(studentRuleData, paddedCourseCode + student.getCourseLevel());

        if (coursesRecord != null && StringUtils.isNotBlank(coursesRecord.getCompletionEndDate())) {
            LocalDate courseSessionDate = LocalDate.parse(student.getCourseYear() + "-" + student.getCourseMonth() + "-01");
            LocalDate courseCompletionEndDate = LocalDateTime.parse(coursesRecord.getCompletionEndDate()).toLocalDate();

            if (courseSessionDate.isAfter(courseCompletionEndDate)) {
                log.debug("C14: Warning: The school is reporting a student enrolled in a course at time when the course was not open (i.e., course session date is before the course open date). for courseStudentID :: {}", student.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_MONTH, CourseStudentValidationIssueTypeCode.COURSE_SESSION_COMPLETION_END_DATE_INVALID, CourseStudentValidationIssueTypeCode.COURSE_SESSION_COMPLETION_END_DATE_INVALID.getMessage()));
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_YEAR, CourseStudentValidationIssueTypeCode.COURSE_SESSION_COMPLETION_END_DATE_INVALID, CourseStudentValidationIssueTypeCode.COURSE_SESSION_COMPLETION_END_DATE_INVALID.getMessage()));
            }
        } else {
            log.debug("C14: Warning: No Coreg course record match or blank course record completion end date. for courseStudentID :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_MONTH, CourseStudentValidationIssueTypeCode.COURSE_SESSION_COMPLETION_END_DATE_INVALID, CourseStudentValidationIssueTypeCode.COURSE_SESSION_COMPLETION_END_DATE_INVALID.getMessage()));
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_YEAR, CourseStudentValidationIssueTypeCode.COURSE_SESSION_COMPLETION_END_DATE_INVALID, CourseStudentValidationIssueTypeCode.COURSE_SESSION_COMPLETION_END_DATE_INVALID.getMessage()));
        }

        return errors;
    }

}
