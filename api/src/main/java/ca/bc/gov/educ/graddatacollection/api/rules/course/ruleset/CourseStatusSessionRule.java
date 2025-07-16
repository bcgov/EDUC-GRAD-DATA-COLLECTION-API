package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | C34 | ERROR    | 	Course Status = "W" and the course code and session date does not |C03, C04, C16|
 *                          exist in GRAD for the student.
 *
 */
@Component
@Slf4j
@Order(340)
public class CourseStatusSessionRule implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public CourseStatusSessionRule(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C34: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C34", validationErrorsMap);

        log.debug("In shouldExecute of C34: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var courseStudentEntity = studentRuleData.getCourseStudentEntity();
        var student = courseRulesService.getStudentApiStudent(studentRuleData, courseStudentEntity.getPen());
        log.debug("In executeValidation of C34 for courseStudentID :: {}", courseStudentEntity.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        var studentCourseRecord = courseRulesService.getStudentCourseRecord(studentRuleData, student.getStudentID());

        if (studentCourseRecord.stream().noneMatch(record ->
                        record.getGradCourseCode().getExternalCode().equalsIgnoreCase(courseRulesService.formatExternalID(courseStudentEntity.getCourseCode(), courseStudentEntity.getCourseLevel()))
                                && record.getCourseSession().equalsIgnoreCase(courseStudentEntity.getCourseYear() + "/" + courseStudentEntity.getCourseMonth()))
                && StringUtils.isNotBlank(courseStudentEntity.getCourseStatus()) && courseStudentEntity.getCourseStatus().equalsIgnoreCase("W")) {
            log.debug("C34: Error: Course Status = W and the course code and session date does not exist in GRAD for the student. :: {}", courseStudentEntity.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_STATUS, CourseStudentValidationIssueTypeCode.COURSE_WRONG_SESSION, CourseStudentValidationIssueTypeCode.COURSE_WRONG_SESSION.getMessage()));
        }
        return errors;
    }
}
