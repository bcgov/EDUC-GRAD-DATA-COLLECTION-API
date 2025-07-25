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
 *  | C36 | ERROR    | 	Error if Final Letter Grad = "W" and course code and session date | C03, C32   |
 *                          does not exist in GRAD for the student.
 *
 */
@Component
@Slf4j
@Order(360)
public class FinalLetterSessionRule implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public FinalLetterSessionRule(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C36: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C36", validationErrorsMap);

        log.debug("In shouldExecute of C36: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var courseStudentEntity = studentRuleData.getCourseStudentEntity();
        var student = courseRulesService.getStudentApiStudent(studentRuleData, courseStudentEntity.getPen());

        log.debug("In executeValidation of C36 for courseStudentID :: {}", courseStudentEntity.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        var studentCourseRecord = courseRulesService.getStudentCourseRecord(studentRuleData, student.getStudentID());
        var externalID = courseRulesService.formatExternalID(courseStudentEntity.getCourseCode(), courseStudentEntity.getCourseLevel());

        if (studentCourseRecord.stream().noneMatch(record ->
                (record.getGradCourseCode38().getExternalCode().equalsIgnoreCase(externalID) || record.getGradCourseCode39().getExternalCode().equalsIgnoreCase(externalID))
                    && record.getCourseSession().equalsIgnoreCase(courseStudentEntity.getCourseYear() + "/" + courseStudentEntity.getCourseMonth()))
                    && StringUtils.isNotBlank(courseStudentEntity.getFinalLetterGrade()) && courseStudentEntity.getFinalLetterGrade().equalsIgnoreCase("W")) {
            log.debug("C36: Error: Final Letter Grad = W and course code and session date does not exist in GRAD for the student. :: {}", courseStudentEntity.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.FINAL_LETTER_GRADE, CourseStudentValidationIssueTypeCode.FINAL_LETTER_WRONG_SESSION, CourseStudentValidationIssueTypeCode.FINAL_LETTER_WRONG_SESSION.getMessage()));
        }
        return errors;
    }
}
