package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.LetterGrade;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | C37  | ERROR    | Final letter grade must match final pct unless course session is	  |C03,C32,C30,C31  |
 *  |      |          | prior to 199409.
 *  Updated rule:
 *  1. If a final letter grade has been submitted and the final letter grade does not have associated percent range (i.e. low and high percent are 0s) in the letter grade table, an final percent should not be submitted.
 *  2. If a final letter grade has been submitted and the final letter grade has an associated, non-zero, percent range in the letter grade table, an final percent must be submitted.
 *  3. If a final letter grade has been submitted and the final letter grade has an associated, non-zero, percent range in the letter grade table, the submitted final percent must fall within the percent range associated with the submitted final letter grade.
 */
@Component
@Slf4j
@Order(370)
public class FinalPercentageGradeMismatch implements CourseValidationBaseRule {

    private final RestUtils restUtils;

    public FinalPercentageGradeMismatch(RestUtils restUtils) {
        this.restUtils = restUtils;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C37: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C37", validationErrorsMap);

        log.debug("In shouldExecute of C37: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of C37 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        if (StringUtils.isNotBlank(student.getCourseYear()) && StringUtils.isNotBlank(student.getCourseMonth())) {
            try {
                YearMonth courseSession = YearMonth.of(Integer.parseInt(student.getCourseYear()), Integer.parseInt(student.getCourseMonth()));
                YearMonth cutoffDate = YearMonth.of(1994, 9);

                if (courseSession.isAfter(cutoffDate)) {
                    LocalDate sessionStartDate = LocalDate.of(Integer.parseInt(student.getCourseYear()), Integer.parseInt(student.getCourseMonth()), 1);
                    List<LetterGrade> letterGradeList = restUtils.getLetterGradeList(sessionStartDate.atStartOfDay());

                    String finalLetterGrade = student.getFinalLetterGrade();
                    String finalPercentStr = student.getFinalPercentage();
                    boolean hasFinalLetterGrade = StringUtils.isNotBlank(finalLetterGrade);
                    boolean hasFinalPercent = StringUtils.isNotBlank(finalPercentStr);

                    if (hasFinalLetterGrade) {
                        Optional<LetterGrade> optionalStudentLetterGrade = letterGradeList.stream()
                                .filter(letterGrade -> letterGrade.getGrade().equalsIgnoreCase(finalLetterGrade))
                                .findFirst();

                        if (optionalStudentLetterGrade.isPresent()) {
                            LetterGrade studentLetterGrade = optionalStudentLetterGrade.get();
                            int percentLow = studentLetterGrade.getPercentRangeLow();
                            int percentHigh = studentLetterGrade.getPercentRangeHigh();
                            boolean hasPercentRange = percentLow != 0 || percentHigh != 0;

                            // 1. If no percent range, final percent should NOT be submitted
                            if (!hasPercentRange && hasFinalPercent) {
                                errors.add(createValidationIssue(
                                        StudentValidationIssueSeverityCode.ERROR,
                                        ValidationFieldCode.FINAL_PERCENTAGE,
                                        CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_PERCENT_SHOULD_NOT_BE_PROVIDED,
                                        CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_PERCENT_SHOULD_NOT_BE_PROVIDED.getMessage()
                                ));
                            }

                            // 2. If percent range exists, final percent MUST be submitted
                            if (hasPercentRange && !hasFinalPercent) {
                                errors.add(createValidationIssue(
                                        StudentValidationIssueSeverityCode.ERROR,
                                        ValidationFieldCode.FINAL_PERCENTAGE,
                                        CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_PERCENT_REQUIRED,
                                        CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_PERCENT_REQUIRED.getMessage()
                                ));
                            }

                            // 3. If percent range exists and final percent is submitted, it must be within range
                            if (hasPercentRange && hasFinalPercent) {
                                try {
                                    int finalPercentage = Integer.parseInt(finalPercentStr);
                                    if (finalPercentage < percentLow || finalPercentage > percentHigh) {
                                        errors.add(createValidationIssue(
                                                StudentValidationIssueSeverityCode.ERROR,
                                                ValidationFieldCode.FINAL_PERCENTAGE,
                                                CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_PERCENT_OUT_OF_RANGE,
                                                CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_PERCENT_OUT_OF_RANGE.getMessage()
                                        ));
                                    }
                                } catch (NumberFormatException e) {
                                    errors.add(createValidationIssue(
                                            StudentValidationIssueSeverityCode.ERROR,
                                            ValidationFieldCode.FINAL_PERCENTAGE,
                                            CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_PERCENT_OUT_OF_RANGE,
                                            CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_PERCENT_OUT_OF_RANGE.getMessage()
                                    ));
                                }
                            }
                        }
                    }
                }

            } catch (NumberFormatException | DateTimeException e) {
                log.debug("C37: Skipping validation due to invalid course year or month for courseStudentID :: {}", student.getCourseStudentID());
            }
        }
        return errors;
    }
}
