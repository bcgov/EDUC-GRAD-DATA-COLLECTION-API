package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradRequirementYearCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | C17  | ERROR    | The submitted value <CRS VALUE> is not an allowable value for         | C03, C10     |
 *  |      |          | students on the 1986 program. This course cannot be updated.          |              |
 */
@Component
@Slf4j
@Order(170)
public class ProgramGraduationRequirementRule implements CourseValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C17: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C17", validationErrorsMap);

        log.debug("In shouldExecute of C17: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var courseStudent = studentRuleData.getCourseStudentEntity();
        var demStudent = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of C17 for courseStudentID :: {}", courseStudent.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        if (demStudent != null && StringUtils.isNotBlank(demStudent.getGradRequirementYear())
                && demStudent.getGradRequirementYear().equalsIgnoreCase(GradRequirementYearCodes.YEAR_1986.getCode()) && StringUtils.isNotBlank(courseStudent.getCourseGraduationRequirement())) {
            String errorMessage = CourseStudentValidationIssueTypeCode.GRADUATION_REQUIREMENT_INVALID.getMessage().formatted(StringEscapeUtils.escapeHtml4(courseStudent.getCourseGraduationRequirement()));
            log.debug("C17: Error: {} for courseStudentID :: {}", errorMessage, courseStudent.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_GRADUATION_REQUIREMENT, CourseStudentValidationIssueTypeCode.GRADUATION_REQUIREMENT_INVALID, errorMessage));
        }
        return errors;
    }
}
