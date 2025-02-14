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
 *  | V236 | ERROR    | This field can only be accepted if the students' course is a          |   V202       |
 *  |      |          | "generic" course.                                                     |              |
 *  |      |          |                                                                       |              |
 */
@Component
@Slf4j
@Order(360)
public class V236CourseDescription implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public V236CourseDescription(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }
    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V236: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V236", validationErrorsMap);

        log.debug("In shouldExecute of V236: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var courseStudent = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of V236 for courseStudentID :: {}", courseStudent.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        if (courseStudent.getCourseDescription() != null) {

            String paddedCourseCode = String.format("%-5s", courseStudent.getCourseCode());
            var coursesRecord = courseRulesService.getCoregCoursesRecord(studentRuleData, paddedCourseCode + courseStudent.getCourseLevel());

            if (!"G".equalsIgnoreCase(coursesRecord.getGenericCourseType())) {
                log.debug("V236: Error: The ministry course title must be used for this course. Please check the Course Registry: descriptive titles only allowed if Generic Course Type = G. This course will not be updated. for courseStudentID :: {}", courseStudent.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_DESCRIPTION, CourseStudentValidationIssueTypeCode.COURSE_DESCRIPTION_INVALID, CourseStudentValidationIssueTypeCode.COURSE_DESCRIPTION_INVALID.getMessage()));
            }
        }

        return errors;
    }
}
