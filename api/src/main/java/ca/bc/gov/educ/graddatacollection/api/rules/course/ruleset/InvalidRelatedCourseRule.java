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

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | C19 | ERROR    | These fields can only be accepted if the students' course is an       |   C03, C16   |
 *  |      |          | Independent Directed Studies course
 */
@Component
@Slf4j
@Order(190)
public class InvalidRelatedCourseRule implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public InvalidRelatedCourseRule(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }
    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C19: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C19", validationErrorsMap);

        log.debug("In shouldExecute of C19: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var courseStudent = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of C19 for courseStudentID :: {}", courseStudent.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        var coursesRecord = courseRulesService.getCoregRelatedCoursesRecord(studentRuleData, courseStudent.getRelatedCourse(), courseStudent.getRelatedLevel());

        if (StringUtils.isNotBlank(courseStudent.getRelatedCourse()) && StringUtils.isNotBlank(courseStudent.getRelatedLevel())
                && coursesRecord != null && !"Independent Directed Studies".equalsIgnoreCase(coursesRecord.getProgramGuideTitle())) {
            log.debug("C19: Error: Invalid entry. A related course code can only be applied to an Independent Directed Studies course. This course will not be updated. for courseStudentID :: {}", courseStudent.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.RELATED_COURSE, CourseStudentValidationIssueTypeCode.COURSE_NOT_INDEPENDENT_DIRECTED_STUDIES, CourseStudentValidationIssueTypeCode.COURSE_NOT_INDEPENDENT_DIRECTED_STUDIES.getMessage()));
        }
        if (courseStudent.getRelatedLevel() != null && StringUtils.isNotBlank(courseStudent.getRelatedLevel())
                && coursesRecord != null &&!"Independent Directed Studies".equalsIgnoreCase(coursesRecord.getProgramGuideTitle())) {
            log.debug("C19: Error: Invalid entry. A related level can only be applied to an Independent Directed Studies course. This course will not be updated. for courseStudentID :: {}", courseStudent.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.RELATED_LEVEL, CourseStudentValidationIssueTypeCode.COURSE_NOT_INDEPENDENT_DIRECTED_STUDIES, CourseStudentValidationIssueTypeCode.COURSE_NOT_INDEPENDENT_DIRECTED_STUDIES.getMessage()));
        }

        return errors;
    }
}
