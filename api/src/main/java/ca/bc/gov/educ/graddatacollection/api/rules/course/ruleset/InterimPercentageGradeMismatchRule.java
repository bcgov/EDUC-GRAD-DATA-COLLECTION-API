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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | C29  | ERROR    | Interim percent must match interim letter grade if interim letter 	  |C03, C07, C08 |
 *  |      |          | grade provided                                                        |C16, C22, C23 |
 *  updated rule:
 *  1. If interim letter grade has been submitted and the interim letter grade does not have associated percent range
 *  (i.e. low and high percent are 0s) in the letter grade table, an interim percent should not be submitted.
 *  2. If interim letter grade has been submitted and the interim letter grade has an associated, non-zero,
 *  percent range in the letter grade table, an interim percent must be submitted.
 *  3. If interim letter grade has been submitted and the interim letter grade has an associated, non-zero, percent range
 *  in the letter grade table, the submitted interim percent must fall within the percent range associated with the submitted interim letter grade.
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

        var shouldExecute = isValidationDependencyResolved("C29", validationErrorsMap)
                && StringUtils.isNotBlank(studentRuleData.getCourseStudentEntity().getCourseStatus())
                && !studentRuleData.getCourseStudentEntity().getCourseStatus().equalsIgnoreCase("W");

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

        LocalDate sessionStartDate = LocalDate.of(Integer.parseInt(student.getCourseYear()), Integer.parseInt(student.getCourseMonth()), 1);
        List<LetterGrade> letterGradeList = restUtils.getLetterGradeList(sessionStartDate.atStartOfDay());

        String interimLetterGrade = student.getInterimLetterGrade();
        String interimPercentStr = student.getInterimPercentage();
        Optional<Integer> parsedInterimPercentage = parseInterimPercentage(interimPercentStr);
        boolean hasInterimLetterGrade = StringUtils.isNotBlank(interimLetterGrade);
        boolean hasInterimPercentValue = StringUtils.isNotBlank(interimPercentStr);

        if (hasInterimLetterGrade) {
            Optional<LetterGrade> optionalStudentLetterGrade = letterGradeList.stream()
                .filter(letterGrade -> letterGrade.getGrade().equalsIgnoreCase(interimLetterGrade))
                .findFirst();

            if (optionalStudentLetterGrade.isPresent()) {
                LetterGrade studentLetterGrade = optionalStudentLetterGrade.get();
                Integer percentLow = studentLetterGrade.getPercentRangeLow();
                Integer percentHigh = studentLetterGrade.getPercentRangeHigh();
                boolean hasPercentRange = percentLow != null && percentHigh != null;

                boolean interimPercentageIsZero = parsedInterimPercentage.map(integer -> integer.equals(0)).orElse(false);
                // 1. If no percent range, interim percent should NOT be submitted
                if (!hasPercentRange && hasInterimPercentValue && !interimPercentageIsZero) {
                    errors.add(createValidationIssue(
                            StudentValidationIssueSeverityCode.ERROR,
                            ValidationFieldCode.INTERIM_PERCENTAGE,
                            CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_PERCENT_SHOULD_NOT_BE_PROVIDED,
                            CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_PERCENT_SHOULD_NOT_BE_PROVIDED.getMessage()
                    ));
                }

                // 2. If percent range exists, interim percent MUST be submitted
                if (hasPercentRange && !hasInterimPercentValue) {
                    errors.add(createValidationIssue(
                            StudentValidationIssueSeverityCode.ERROR,
                            ValidationFieldCode.INTERIM_PERCENTAGE,
                            CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_PERCENT_REQUIRED,
                            CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_PERCENT_REQUIRED.getMessage()
                    ));
                }

                // 3. If percent range exists and interim percent is submitted, it must be within range
                if (hasPercentRange && hasInterimPercentValue) {
                    boolean interimPercentageBelowMinimum = parsedInterimPercentage.map(integer -> integer < percentLow).orElse(false);
                    boolean interimPercentageBelowMaximum = parsedInterimPercentage.map(integer -> integer > percentHigh).orElse(false);
                    if (parsedInterimPercentage.isEmpty() || interimPercentageBelowMinimum || interimPercentageBelowMaximum) {
                        errors.add(createValidationIssue(
                                StudentValidationIssueSeverityCode.ERROR,
                                ValidationFieldCode.INTERIM_PERCENTAGE,
                                CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_PERCENT_OUT_OF_RANGE,
                                CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_PERCENT_OUT_OF_RANGE.getMessage()
                        ));
                    }
                }
            }
        }

        return errors;
    }

    private Optional<Integer> parseInterimPercentage(String interimPercentage) {
        try {
            return Optional.of(Integer.parseInt(interimPercentage));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
