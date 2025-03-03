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
 *  | ID   | Severity | Rule                                                                  | Dependent On  |
 *  |------|----------|-----------------------------------------------------------------------|---------------|
 *  | V205 | ERROR    |  If course status = "W" and student has graduated and the course has  | 202, 203, 209 |
 *                       been used for graduation                                               237
 *
 */
@Component
@Slf4j
@Order(50)
public class V205CourseStatus implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public V205CourseStatus(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V205: for course {} and courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID() ,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V205", validationErrorsMap);

        log.debug("In shouldExecute of V205: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of V205 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        var studentCourseRecord = courseRulesService.getStudentCourseRecord(studentRuleData, student.getPen());
        var gradStudent = courseRulesService.getGradStudentRecord(studentRuleData, student.getPen());

        if ("W".equalsIgnoreCase(student.getCourseStatus())
                && gradStudent != null
                && gradStudent.getGraduated().equalsIgnoreCase("true")
                && studentCourseRecord != null
                && studentCourseRecord.stream().anyMatch(record ->
                    record.getCourseCode().equalsIgnoreCase(student.getCourseCode())
                    && record.getGradReqMet() != null
        )) {
            log.debug("V205: Error: A student course has been submitted as \"W\" (withdrawal) but has already been used to meet a graduation requirement. This course cannot be deleted. for course student id :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_STATUS, CourseStudentValidationIssueTypeCode.COURSE_USED_FOR_GRADUATION, CourseStudentValidationIssueTypeCode.COURSE_USED_FOR_GRADUATION.getMessage()));
        }
        return errors;
    }

}
