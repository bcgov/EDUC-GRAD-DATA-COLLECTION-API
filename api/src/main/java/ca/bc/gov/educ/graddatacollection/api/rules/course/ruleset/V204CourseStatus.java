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
 *  | V204 | ERROR    |  If course status = "W" course cannot be associated with a student    | 202, 203, 209 |
 *                       course exam if the record exists for the same course code/level/session     237
 *
 */
@Component
@Slf4j
@Order(40)
public class V204CourseStatus implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public V204CourseStatus(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V204: for course {} and courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID() ,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V204", validationErrorsMap);

        log.debug("In shouldExecute of V204: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of V204 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        var studentCourseRecord = courseRulesService.getStudentCourseRecord(studentRuleData, student.getPen());

        if ("W".equalsIgnoreCase(student.getCourseStatus()) && studentCourseRecord != null
            && studentCourseRecord.stream().anyMatch(record ->
                    record.getCourseCode().equalsIgnoreCase(student.getCourseCode())
                    && record.getCourseLevel().equalsIgnoreCase(student.getCourseLevel())
                    && record.getSessionDate().equalsIgnoreCase(student.getCourseYear() + "/" + student.getCourseMonth()) // yyyy/mm
                    )) {
            log.debug("V202: Error: A student course has been submitted as \"W\" (withdrawal) but has an associated exam record. This course cannot be deleted. for course student id :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_STATUS, CourseStudentValidationIssueTypeCode.COURSE_RECORD_EXISTS, CourseStudentValidationIssueTypeCode.COURSE_RECORD_EXISTS.getMessage()));
        }
        return errors;
    }

}
