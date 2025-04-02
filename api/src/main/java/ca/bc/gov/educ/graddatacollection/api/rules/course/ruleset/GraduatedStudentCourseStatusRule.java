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
 *  | ID   | Severity | Rule                                                                  | Dependent On  |
 *  |------|----------|-----------------------------------------------------------------------|---------------|
 *  | C12 | ERROR    |  If course status = "W" and student has graduated and the course has  | C03, C04 |
 *                       been used for graduation
 *
 */
@Component
@Slf4j
@Order(120)
public class GraduatedStudentCourseStatusRule implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public GraduatedStudentCourseStatusRule(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C12: for course {} and courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID() ,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C12", validationErrorsMap);

        log.debug("In shouldExecute of C12: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of C12 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        var studentCourseRecord = courseRulesService.getStudentCourseRecord(studentRuleData, student.getPen());
        var gradStudent = courseRulesService.getGradStudentRecord(studentRuleData, student.getPen());

        if ("W".equalsIgnoreCase(student.getCourseStatus())
                && gradStudent != null
                && gradStudent.getGraduated().equalsIgnoreCase("true")
                && studentCourseRecord != null
                && studentCourseRecord.stream().anyMatch(record ->
                    record.getCourseCode().equalsIgnoreCase(student.getCourseCode())
                        && StringUtils.isNotBlank(record.getGradReqMet())
        )) {
            log.debug("C12: Error: A student course has been submitted as \"W\" (withdrawal) but has already been used to meet a graduation requirement. This course cannot be deleted. for course student id :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_STATUS, CourseStudentValidationIssueTypeCode.COURSE_USED_FOR_GRADUATION, CourseStudentValidationIssueTypeCode.COURSE_USED_FOR_GRADUATION.getMessage()));
        }
        return errors;
    }

}
