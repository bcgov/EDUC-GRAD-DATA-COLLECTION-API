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
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | C29 | ERROR    | Interim percent must match interim letter grade if interim letter 	  |C03, C22, C23 |
 *  |      |          | grade provided
 */
@Component
@Slf4j
@Order(290)
public class InterimPercentageGradeMismatchRule implements CourseValidationBaseRule {

    private final RestUtils restUtils;

    public InterimPercentageGradeMismatchRule(RestUtils restUtils) {
        this.restUtils = restUtils;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C29: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C29", validationErrorsMap);

        log.debug("In shouldExecute of C29: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of C29 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        if (StringUtils.isBlank(student.getInterimPercentage())) {
            log.debug("C29: Interim percentage is missing while an interim letter grade is provided for courseStudentID :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.INTERIM_PERCENTAGE, CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_PERCENTAGE_MISMATCH, CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_PERCENTAGE_MISMATCH.getMessage()));
            return errors;
        }

        List<LetterGrade> letterGradeList = restUtils.getLetterGradeList(true);

        int interimPercentage = Integer.parseInt(student.getInterimPercentage());
        Optional<LetterGrade> optionalStudentLetterGrade = letterGradeList.stream().filter(letterGrade -> letterGrade.getGrade().equalsIgnoreCase(student.getInterimGrade())).findFirst();

        if (optionalStudentLetterGrade.isEmpty() ||
                interimPercentage < optionalStudentLetterGrade.get().getPercentRangeLow() ||
                interimPercentage > optionalStudentLetterGrade.get().getPercentRangeHigh()) {
            log.debug("C29: Error: The interim percent does not fall within the required range for the reported letter grade. This course will not be updated for courseStudentID :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.INTERIM_PERCENTAGE, CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_PERCENTAGE_MISMATCH, CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_PERCENTAGE_MISMATCH.getMessage()));
        }
        return errors;
    }
}
