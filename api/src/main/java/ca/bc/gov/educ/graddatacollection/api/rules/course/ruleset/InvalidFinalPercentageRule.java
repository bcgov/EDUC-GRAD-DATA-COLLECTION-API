package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
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
 *  | C30 | ERROR    | Final percent cannot be negative or greater than 100                  | C03, C24|
 *
 */
@Component
@Slf4j
@Order(300)
public class InvalidFinalPercentageRule implements CourseValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C30: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C30", validationErrorsMap);

        log.debug("In shouldExecute of C30: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of C30 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        if (StringUtils.isNotBlank(student.getFinalPercentage())) {
            try {
                double finalePercentage = Double.parseDouble(student.getFinalPercentage());

                if (finalePercentage < 0 || finalePercentage > 100) {
                    log.debug("C30: Error: Final percent range must be 0 to 100. This course will not be updated for courseStudentID :: {}", student.getCourseStudentID());
                    errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.FINAL_PERCENTAGE, CourseStudentValidationIssueTypeCode.FINAL_PCT_INVALID, CourseStudentValidationIssueTypeCode.FINAL_PCT_INVALID.getMessage()));
                }
            } catch (NumberFormatException e) {
                log.debug("C30: Error: Final percent range must be 0 to 100. This course will not be updated for courseStudentID :: {}", student.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.FINAL_PERCENTAGE, CourseStudentValidationIssueTypeCode.FINAL_PCT_INVALID, CourseStudentValidationIssueTypeCode.FINAL_PCT_INVALID.getMessage()));
            }

        }
        return errors;
    }

}
