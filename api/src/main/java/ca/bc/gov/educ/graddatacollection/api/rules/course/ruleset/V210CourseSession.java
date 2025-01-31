package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationFieldCode;
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
import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V210 | WARN     | Course session date plus day of 01 should not be before the course    |   V202, V209 |
 *  |      |          | start date                                                            |              |
 */
@Component
@Slf4j
@Order(100)
public class V210CourseSession implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public V210CourseSession(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }
    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V210: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V210", validationErrorsMap);

        log.debug("In shouldExecute of V210: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of V210 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        String paddedCourseCode = String.format("%-5s", student.getCourseCode());
        var coursesRecord = courseRulesService.getCoregCoursesRecord(studentRuleData, paddedCourseCode + student.getCourseLevel());

        // todo remove is not blank and regex on get course year if we add a rule to format check course year + double check v209 dependency
        if (coursesRecord != null && StringUtils.isNotBlank(student.getCourseYear()) && student.getCourseYear().matches("\\d{4}")) {
            LocalDate courseSessionDate = LocalDate.parse(student.getCourseYear() + "-" + student.getCourseMonth() + "-01");
            String dateOnlyStr = coursesRecord.getStartDate().split(" ")[0];
            LocalDate courseStartDate = LocalDate.parse(dateOnlyStr);

            if (courseSessionDate.isBefore(courseStartDate)) {
                log.debug("V210: Warning: The school is reporting a student enrolled in a course at time when the course was not open (i.e., course session date is before the course open date). for courseStudentID :: {}", student.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, CourseStudentValidationFieldCode.COURSE_SESSION, CourseStudentValidationIssueTypeCode.COURSE_SESSION_START_DATE_INVALID, CourseStudentValidationIssueTypeCode.COURSE_SESSION_START_DATE_INVALID.getMessage()));
            }
        }

        return errors;
    }

}
