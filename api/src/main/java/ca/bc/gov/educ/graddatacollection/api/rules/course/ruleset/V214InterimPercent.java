package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V214 | ERROR    | Interim percent cannot be negative or greater than 100                | V202 V212    |
 *
 */
@Component
@Slf4j
@Order(140)
public class V214InterimPercent implements CourseValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V214: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V214", validationErrorsMap);

        log.debug("In shouldExecute of V214: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of V214 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        if (StringUtils.isNotBlank(student.getInterimPercentage())) {
            try {
                double interimPercentage = Double.parseDouble(student.getInterimPercentage());

                if (interimPercentage < 0 || interimPercentage > 100) {
                    log.debug("V214: Error: Interim percent range must be 0 to 100. This course will not be updated for courseStudentID :: {}", student.getCourseStudentID());
                    errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.INTERIM_PERCENT, CourseStudentValidationIssueTypeCode.INTERIM_PCT_INVALID, CourseStudentValidationIssueTypeCode.INTERIM_PCT_INVALID.getMessage()));
                }
            } catch (NumberFormatException e) {
                log.debug("V214: Error: Interim percent range must be 0 to 100. This course will not be updated for courseStudentID :: {}", student.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.INTERIM_PERCENT, CourseStudentValidationIssueTypeCode.INTERIM_PCT_INVALID, CourseStudentValidationIssueTypeCode.INTERIM_PCT_INVALID.getMessage()));
            }

        }
        return errors;
    }

}
