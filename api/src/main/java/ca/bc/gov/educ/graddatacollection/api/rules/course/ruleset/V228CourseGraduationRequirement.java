package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradRequirementYearCodes;
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

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V228 | ERROR    | Must be blank for 1986 graduation program	                          |              |
 *
 */
@Component
@Slf4j
@Order(280)
public class V228CourseGraduationRequirement implements CourseValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V228: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = true;

        log.debug("In shouldExecute of V228: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var courseStudent = studentRuleData.getCourseStudentEntity();
        var demStudent = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of V228 for courseStudentID :: {}", courseStudent.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        if (demStudent.getGradRequirementYear().equalsIgnoreCase(GradRequirementYearCodes.YEAR_1986.getCode()) && StringUtils.isNotBlank(courseStudent.getCourseGraduationRequirement())) {
            log.debug("V228: Error: Invalid entry. Values not applicable for students on the 1986 program. This course will not be updated. for courseStudentID :: {}", courseStudent.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, CourseStudentValidationFieldCode.GRADUATION_REQUIREMENT, CourseStudentValidationIssueTypeCode.GRADUATION_REQUIREMENT_INVALID));
        }
        return errors;
    }
}
