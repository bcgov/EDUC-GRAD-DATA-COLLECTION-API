package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
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

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V219 | ERROR    | If course session is prior to 199409 no Final percent should be       |   V217       |
 *                      entered for these courses.  This field should be blank.
 *
 */
@Component
@Slf4j
@Order(190)
public class V219FinalPercent implements CourseValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V219: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V219", validationErrorsMap);

        log.debug("In shouldExecute of V219: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of V219 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        if (StringUtils.isNotBlank(student.getCourseYear()) && StringUtils.isNotBlank(student.getCourseMonth())) {
            try {
                YearMonth courseSession = YearMonth.of(Integer.parseInt(student.getCourseYear()), Integer.parseInt(student.getCourseMonth()));

                YearMonth cutoffDate = YearMonth.of(1994, 9);

                if (courseSession.isBefore(cutoffDate) && !student.getFinalPercentage().isBlank()) {
                    log.debug("V219: Error: For course session dates prior to 199409 the final percent must be blank. This course will not be updated. for courseStudentID :: {}", student.getCourseStudentID());
                    errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, CourseStudentValidationFieldCode.FINAL_PCT, CourseStudentValidationIssueTypeCode.FINAL_PCT_NOT_BLANK, CourseStudentValidationIssueTypeCode.FINAL_PCT_NOT_BLANK.getMessage()));
                }
            } catch (NumberFormatException | DateTimeException e) {
                log.debug("V219: Skipping validation due to invalid course year or month for courseStudentID :: {}", student.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, CourseStudentValidationFieldCode.FINAL_PCT, CourseStudentValidationIssueTypeCode.FINAL_PCT_NOT_BLANK, CourseStudentValidationIssueTypeCode.FINAL_PCT_NOT_BLANK.getMessage()));
            }
        }
        return errors;
    }

}
