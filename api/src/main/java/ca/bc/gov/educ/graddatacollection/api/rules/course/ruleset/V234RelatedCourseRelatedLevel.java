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
 *  | V234 | ERROR    | Related Course Code and Related Course Level must be a valid          |   V202       |
 *  |      |          | course/level                                                          |   V233       |
 *  |      |          |                                                                       |   V206       |
 */
@Component
@Slf4j
@Order(340)
public class V234RelatedCourseRelatedLevel implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public V234RelatedCourseRelatedLevel(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }
    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V234: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V234", validationErrorsMap);

        log.debug("In shouldExecute of V234: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var courseStudent = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of V234 for courseStudentID :: {}", courseStudent.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        String paddedCourseCode = String.format("%-5s", courseStudent.getRelatedCourse());
        var coursesRecord = courseRulesService.getCoregCoursesRecord(studentRuleData, paddedCourseCode + courseStudent.getRelatedLevel());

        if (coursesRecord == null) {
            log.debug("V234: Error: Invalid related course code used for the Independent Directed Studies course. Please check the Course Registry. This course will not be updated. for courseStudentID :: {}", courseStudent.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.RELATED_COURSE, CourseStudentValidationIssueTypeCode.RELATED_COURSE_RELATED_LEVEL_INVALID, CourseStudentValidationIssueTypeCode.RELATED_COURSE_RELATED_LEVEL_INVALID.getMessage()));
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.RELATED_LEVEL, CourseStudentValidationIssueTypeCode.RELATED_COURSE_RELATED_LEVEL_INVALID, CourseStudentValidationIssueTypeCode.RELATED_COURSE_RELATED_LEVEL_INVALID.getMessage()));
        }

        return errors;
    }
}
