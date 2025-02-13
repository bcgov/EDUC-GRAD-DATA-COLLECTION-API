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
 *  | V211 | WARN     | Course session date plus day of 01 should not be after the course     |   V202, V209 |
 *  |      |          | completion date                                                       |   V237       |
 */
@Component
@Slf4j
@Order(110)
public class V211CourseSession implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public V211CourseSession(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }
    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V211: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V211", validationErrorsMap);

        log.debug("In shouldExecute of V211: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of V211 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        String paddedCourseCode = String.format("%-5s", student.getCourseCode());
        var coursesRecord = courseRulesService.getCoregCoursesRecord(studentRuleData, paddedCourseCode + student.getCourseLevel());

        if (coursesRecord != null && StringUtils.isNotBlank(coursesRecord.getCompletionEndDate())) {
            LocalDate courseSessionDate = LocalDate.parse(student.getCourseYear() + "-" + student.getCourseMonth() + "-01");
            LocalDate courseCompletionEndDate = LocalDateTime.parse(coursesRecord.getCompletionEndDate()).toLocalDate();

            if (courseSessionDate.isAfter(courseCompletionEndDate)) {
                log.debug("V211: Warning: The school is reporting a student enrolled in a course at time when the course was not open (i.e., course session date is before the course open date). for courseStudentID :: {}", student.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, ValidationFieldCode.COURSE_SESSION, CourseStudentValidationIssueTypeCode.COURSE_SESSION_COMPLETION_END_DATE_INVALID, CourseStudentValidationIssueTypeCode.COURSE_SESSION_COMPLETION_END_DATE_INVALID.getMessage()));
            }
        } else {
            log.debug("V211: Warning: No Coreg course record match or blank course record completion end date. for courseStudentID :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, ValidationFieldCode.COURSE_SESSION, CourseStudentValidationIssueTypeCode.COURSE_SESSION_COMPLETION_END_DATE_INVALID, CourseStudentValidationIssueTypeCode.COURSE_SESSION_COMPLETION_END_DATE_INVALID.getMessage()));
        }

        return errors;
    }

}
