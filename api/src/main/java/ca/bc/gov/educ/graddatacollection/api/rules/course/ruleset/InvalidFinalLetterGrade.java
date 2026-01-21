package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.LetterGrade;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | C32  | ERROR    | Must be a valid letter grade for the course session provided	      | C03, C07, C08, C16, C24 |
 */
@Component
@Slf4j
@Order(320)
public class InvalidFinalLetterGrade implements CourseValidationBaseRule {

    private final RestUtils restUtils;

    public InvalidFinalLetterGrade(RestUtils restUtils) {
        this.restUtils = restUtils;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C32: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C32", validationErrorsMap)
                && StringUtils.isNotBlank(studentRuleData.getCourseStudentEntity().getCourseStatus())
                && !studentRuleData.getCourseStudentEntity().getCourseStatus().equalsIgnoreCase("W");

        log.debug("In shouldExecute of C32: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of C32 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        LocalDate sessionStartDate = LocalDate.of(Integer.parseInt(student.getCourseYear()), Integer.parseInt(student.getCourseMonth()), 1);
        List<LetterGrade> letterGradeList = restUtils.getLetterGradeList(sessionStartDate.atStartOfDay());

        if (StringUtils.isNotBlank(student.getFinalLetterGrade()) && letterGradeList.stream().noneMatch(letterGrade -> letterGrade.getGrade().equals(student.getFinalLetterGrade()))) {
            String errorMessage = CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_INVALID.getMessage().formatted(StringEscapeUtils.escapeHtml4(student.getFinalLetterGrade()));
            log.debug("C32: Error: {} for courseStudentID :: {}", errorMessage, student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.FINAL_LETTER_GRADE, CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_INVALID, errorMessage));
        }
        return errors;
    }
}
