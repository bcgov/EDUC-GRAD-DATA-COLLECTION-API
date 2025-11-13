package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseRulesService;
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
 *  | C23  | ERROR    | The submitted interim letter grade <INTERIM LETTER GRADE> is not a    | C03, C160,   |
 *  |      |          | valid letter grade. This course cannot be updated.                    | C07, C08     |
 */
@Component
@Slf4j
@Order(230)
public class InvalidInterimGradeRule implements CourseValidationBaseRule {

    private final RestUtils restUtils;

    public InvalidInterimGradeRule(RestUtils restUtils, CourseRulesService courseRulesService) {
        this.restUtils = restUtils;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C23: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C23", validationErrorsMap)
                && StringUtils.isNotBlank(studentRuleData.getCourseStudentEntity().getCourseStatus())
                && !studentRuleData.getCourseStudentEntity().getCourseStatus().equalsIgnoreCase("W");

        log.debug("In shouldExecute of C23: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of C23 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        LocalDate sessionStartDate = LocalDate.of(Integer.parseInt(student.getCourseYear()), Integer.parseInt(student.getCourseMonth()), 1);
        List<LetterGrade> letterGradeList = restUtils.getLetterGradeList(sessionStartDate.atStartOfDay());

        if (StringUtils.isNotBlank(student.getInterimLetterGrade()) && letterGradeList.stream().noneMatch(letterGrade -> letterGrade.getGrade().equals(student.getInterimLetterGrade()))) {
            String errorMessage = CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_INVALID.getMessage().formatted(StringEscapeUtils.escapeHtml4(student.getInterimLetterGrade()));
            log.debug("C23: Error: {} for courseStudentID :: {}", errorMessage, student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.INTERIM_GRADE, CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_INVALID, errorMessage));
        }
        return errors;
    }
}
