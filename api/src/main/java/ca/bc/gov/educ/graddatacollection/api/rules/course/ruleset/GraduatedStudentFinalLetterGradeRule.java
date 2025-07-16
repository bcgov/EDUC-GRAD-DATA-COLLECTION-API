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
 *  | C35 | ERROR    | 	Error if Final LG = "W" and student has graduated and the course  | C03, C32   |
 *                          has been used for graduation.
 *
 */
@Component
@Slf4j
@Order(350)
public class GraduatedStudentFinalLetterGradeRule implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public GraduatedStudentFinalLetterGradeRule(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C35: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C35", validationErrorsMap);

        log.debug("In shouldExecute of C35: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var courseStudentEntity = studentRuleData.getCourseStudentEntity();
        var student = courseRulesService.getStudentApiStudent(studentRuleData, courseStudentEntity.getPen());
        log.debug("In executeValidation of C35 for courseStudentID :: {}", courseStudentEntity.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        var studentCourseRecord = courseRulesService.getStudentCourseRecord(studentRuleData, student.getStudentID());
        var gradStudent = courseRulesService.getGradStudentRecord(studentRuleData, courseStudentEntity.getPen());
        var externalID = courseRulesService.formatExternalID(courseStudentEntity.getCourseCode(), courseStudentEntity.getCourseLevel());

        if (studentCourseRecord.stream().anyMatch(record ->
                (record.getGradCourseCode38().getExternalCode().equalsIgnoreCase(externalID) || record.getGradCourseCode39().getExternalCode().equalsIgnoreCase(externalID))
                    && gradStudent != null
                    && gradStudent.getGraduated().equalsIgnoreCase("true")
                    && record.getCourseSession().equalsIgnoreCase(courseStudentEntity.getCourseYear() + "/" + courseStudentEntity.getCourseMonth())
                    && StringUtils.isNotBlank(courseStudentEntity.getFinalLetterGrade()) && courseStudentEntity.getFinalLetterGrade().equalsIgnoreCase("W"))) {
            log.debug("C35: Error: A student course has been submitted as W (withdrawal) but has already been used to meet a graduation requirement. This course cannot be deleted. :: {}", courseStudentEntity.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.FINAL_LETTER_GRADE, CourseStudentValidationIssueTypeCode.FINAL_LETTER_USED_FOR_GRADUATION, CourseStudentValidationIssueTypeCode.FINAL_LETTER_USED_FOR_GRADUATION.getMessage()));
        }
        return errors;
    }
}
