package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationFieldCode;
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
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V220 | ERROR    | Final letter grade must match final pct unless course session is	  | V217, V218   |
 *  |      |          | prior to 199409.                                                      | V219         |
 */
@Component
@Slf4j
@Order(200)
public class V220FinalLetterGradePercent implements CourseValidationBaseRule {

    private final RestUtils restUtils;

    public V220FinalLetterGradePercent(RestUtils restUtils) {
        this.restUtils = restUtils;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V220: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V220", validationErrorsMap);

        log.debug("In shouldExecute of V220: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of V220 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        if (StringUtils.isNotBlank(student.getCourseYear()) && StringUtils.isNotBlank(student.getCourseMonth())) {
            try {
                YearMonth courseSession = YearMonth.of(Integer.parseInt(student.getCourseYear()), Integer.parseInt(student.getCourseMonth()));
                YearMonth cutoffDate = YearMonth.of(1994, 9);

                if (courseSession.isAfter(cutoffDate)) {
                    List<LetterGrade> letterGradeList = restUtils.getLetterGrades();

                    int finalPercentage = Integer.parseInt(student.getFinalPercentage());
                    Optional<LetterGrade> optionalStudentLetterGrade = letterGradeList.stream().filter(letterGrade -> letterGrade.getGrade().equalsIgnoreCase(student.getFinalGrade())).findFirst();

                    if (optionalStudentLetterGrade.isEmpty() ||
                            finalPercentage < optionalStudentLetterGrade.get().getPercentRangeLow() ||
                            finalPercentage > optionalStudentLetterGrade.get().getPercentRangeHigh()) {
                        log.debug("V220: Error: The final percent does not fall within the required range for the reported letter grade. This course will not be updated for courseStudentID :: {}", student.getCourseStudentID());
                        errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, CourseStudentValidationFieldCode.FINAL_LETTER_GRADE_PERCENTAGE, CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_PERCENTAGE_MISMATCH));
                    }
                }

            } catch (NumberFormatException | DateTimeException e) {
                log.debug("V220: Skipping validation due to invalid course year or month for courseStudentID :: {}", student.getCourseStudentID());
            }
        }
        return errors;
    }
}
