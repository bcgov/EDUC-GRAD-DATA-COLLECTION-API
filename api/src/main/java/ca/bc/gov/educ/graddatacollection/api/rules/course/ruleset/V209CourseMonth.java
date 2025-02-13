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
 *  | V209 | ERROR    | Course month must be between 01 and 12                                |   V202       |
 */
@Component
@Slf4j
@Order(90)
public class V209CourseMonth implements CourseValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V209: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V209", validationErrorsMap);

        log.debug("In shouldExecute of V209: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of V209 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        if (StringUtils.isNotBlank(student.getCourseMonth())) {
            try {
                int monthValue = Integer.parseInt(student.getCourseMonth());

                if (monthValue > 12 || monthValue < 1) {
                    log.debug("V209: Error: Course is not blank. Course month must be between 01 and 12 (January to December). This course will not be updated. for courseStudentID :: {}", student.getCourseStudentID());
                    errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_MONTH, CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID, CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getMessage()));
                }
            } catch (NumberFormatException e) {
                log.debug("V225: Skipping validation due to invalid course month for courseStudentID :: {}", student.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_MONTH, CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID, CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getMessage()));
            }
        } else {
            log.debug("V209: Error: Course is blank. Course month must be between 01 and 12 (January to December). This course will not be updated. for courseStudentID :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_MONTH, CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID, CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getMessage()));
        }
        return errors;
    }

}
