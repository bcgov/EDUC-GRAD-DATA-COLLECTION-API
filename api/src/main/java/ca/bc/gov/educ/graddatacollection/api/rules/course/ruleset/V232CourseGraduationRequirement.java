package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradRequirementYearCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
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
 *  | V232 | ERROR    | For the 1996 graduation program, check number of credits for Fine     |    V202      |
 *  |      |          | Arts/Applied Skills.                                                  |    V231      |
 *  |      |          | If B - credits for course must be 4-credits	                          |              |
 *
 */
@Component
@Slf4j
@Order(320)
public class V232CourseGraduationRequirement implements CourseValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V232: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V232", validationErrorsMap);

        log.debug("In shouldExecute of V232: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var courseStudent = studentRuleData.getCourseStudentEntity();
        var demStudent = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of V232 for courseStudentID :: {}", courseStudent.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        if (demStudent != null &&
                GradRequirementYearCodes.YEAR_1996.getCode().equalsIgnoreCase(demStudent.getGradRequirementYear()) &&
                "B".equalsIgnoreCase(courseStudent.getCourseGraduationRequirement()) &&
                !"4".equalsIgnoreCase(courseStudent.getNumberOfCredits())) {
            log.debug("V232: Error: Invalid entry. Number of credits must be 4 where B reported for a Board Authority Authorized or Locally Developed course for a student on the 1996 program. This course will not be updated. for courseStudentID :: {}", courseStudent.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_GRADUATION_REQUIREMENT, CourseStudentValidationIssueTypeCode.GRADUATION_REQUIREMENT_NUMBER_CREDITS_INVALID, CourseStudentValidationIssueTypeCode.GRADUATION_REQUIREMENT_NUMBER_CREDITS_INVALID.getMessage()));
        }
        return errors;
    }
}
