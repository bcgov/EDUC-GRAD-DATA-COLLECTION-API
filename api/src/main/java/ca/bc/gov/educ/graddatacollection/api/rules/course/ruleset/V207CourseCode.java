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
 *  | V207 | ERROR    | Schools cannot report Q code courses unless the q-code course already | V202, V206   |
 *                      exists for the student for the same Course Code/LeveL/Session in Student Course
 *
 */
@Component
@Slf4j
@Order(70)
public class V207CourseCode implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public V207CourseCode(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V207: for course {} and courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID() ,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V207", validationErrorsMap);

        log.debug("In shouldExecute of V207: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of V207 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        var studentCourseRecord = courseRulesService.getStudentCourseRecord(studentRuleData, student.getPen());

        if (student.getCourseCode() != null
                && "Q".equalsIgnoreCase(student.getCourseCode().substring(0,1))
                && studentCourseRecord != null
                && studentCourseRecord.stream().noneMatch(record ->
                record.getCourseCode().equalsIgnoreCase(student.getCourseCode())
                        && record.getCourseLevel().equalsIgnoreCase(student.getCourseLevel())
                        && record.getSessionDate().equalsIgnoreCase(student.getCourseYear() + "/" + student.getCourseMonth()) // yyyy/mm
        )) {
            log.debug("V207: Error: Invalid. New Q-code course submissions (not already in the student record) must be requested through a GRAD Change Form. for course student id :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_CODE, CourseStudentValidationIssueTypeCode.Q_CODE_INVALID, CourseStudentValidationIssueTypeCode.Q_CODE_INVALID.getMessage()));
        }
        return errors;
    }

}
