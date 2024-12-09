package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V214 | ERROR    | Interim percent range must be 0 to 100. This course will not be       | -            |
 *                      updated.
 */
@Component
@Slf4j
@Order(140)
public class V214InterimPercent implements CourseValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V214: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = true;

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

        if (!student.getInterimPercentage().isBlank()) {
            try {
                double interimPercentage = Double.parseDouble(student.getInterimPercentage());

                if (interimPercentage < 0 || interimPercentage > 100) {
                    log.debug("V214: Error: Interim percent range must be 0 to 100. This course will not be updated for courseStudentID :: {}", student.getCourseStudentID());
                    errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, CourseStudentValidationFieldCode.INTERIM_PCT, CourseStudentValidationIssueTypeCode.INTERIM_PCT_INVALID));
                }
            } catch (NumberFormatException e) {
                log.debug("V214: Error: Interim percent range must be 0 to 100. This course will not be updated for courseStudentID :: {}", student.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, CourseStudentValidationFieldCode.INTERIM_PCT, CourseStudentValidationIssueTypeCode.INTERIM_PCT_INVALID));
            }

        }
        return errors;
    }

}
