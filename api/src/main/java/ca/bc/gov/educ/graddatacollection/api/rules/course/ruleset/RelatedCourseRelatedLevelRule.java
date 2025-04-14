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

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | C20 | ERROR    | If the course is an independent directed studies, there should be     |   C03, C16   |
 *  |      |          | a related course code and related course level.
 */
@Component
@Slf4j
@Order(200)
public class RelatedCourseRelatedLevelRule implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public RelatedCourseRelatedLevelRule(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }
    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C20: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C20", validationErrorsMap);

        log.debug("In shouldExecute of C20: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var courseStudent = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of C20 for courseStudentID :: {}", courseStudent.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        var coursesRecord = courseRulesService.getCoregCoursesRecord(studentRuleData, courseStudent.getCourseCode(), courseStudent.getCourseLevel());

        if (coursesRecord != null && "Independent Directed Studies".equalsIgnoreCase(coursesRecord.getProgramGuideTitle())) {
            log.debug("C20: Error: Related course code and level missing for Independent Directed Studies course. This course will not be updated. for courseStudentID :: {}", courseStudent.getCourseStudentID());
            if (courseStudent.getRelatedCourse() == null) {
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.RELATED_COURSE, CourseStudentValidationIssueTypeCode.RELATED_COURSE_RELATED_LEVEL_MISSING_FOR_INDY, CourseStudentValidationIssueTypeCode.RELATED_COURSE_RELATED_LEVEL_MISSING_FOR_INDY.getMessage()));
            }
            if (courseStudent.getRelatedLevel() == null) {
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.RELATED_LEVEL, CourseStudentValidationIssueTypeCode.RELATED_COURSE_RELATED_LEVEL_MISSING_FOR_INDY, CourseStudentValidationIssueTypeCode.RELATED_COURSE_RELATED_LEVEL_MISSING_FOR_INDY.getMessage()));
            }
        }

        return errors;
    }
}
