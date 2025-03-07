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
 *  | V238 | ERROR    | 	Error if Final LG = "W" and student has graduated and the course  | V217         |
 *                          has been used for graduation.
 *
 */
@Component
@Slf4j
@Order(380)
public class V238FinalLetterGrade implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public V238FinalLetterGrade(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V238: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V238", validationErrorsMap);

        log.debug("In shouldExecute of V238: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of V238 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        var studentCourseRecord = courseRulesService.getStudentCourseRecord(studentRuleData, student.getPen());
        var gradStudent = courseRulesService.getGradStudentRecord(studentRuleData, student.getPen());

        if (studentCourseRecord.stream().anyMatch(record ->
                        record.getCourseCode().equalsIgnoreCase(student.getCourseCode())
                                && gradStudent != null
                                && gradStudent.getGraduated().equalsIgnoreCase("true")
                                && record.getCourseLevel().equalsIgnoreCase(student.getCourseLevel())
                                && record.getSessionDate().equalsIgnoreCase(student.getCourseYear() + "/" + student.getCourseMonth())
                                && StringUtils.isNotBlank(record.getGradReqMet())
                && StringUtils.isNotBlank(student.getFinalLetterGrade()) && student.getFinalLetterGrade().equalsIgnoreCase("W"))) {
            log.debug("V238: Error: A student course has been submitted as W (withdrawal) but has already been used to meet a graduation requirement. This course cannot be deleted. :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.FINAL_LETTER_GRADE, CourseStudentValidationIssueTypeCode.FINAL_LETTER_USED_FOR_GRADUATION, CourseStudentValidationIssueTypeCode.FINAL_LETTER_USED_FOR_GRADUATION.getMessage()));
        }
        return errors;
    }
}
