package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseRulesService;
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
 *  | V230 | ERROR    | Can only enter Fine Arts/Applied Skills if the course is Board        |   V202       |
 *  |      |          | Authority Authorized  for the 2004/2018/2023 graduation program       |   V231       |
 *  |      |          |                                                                       |   V206    |
 */
@Component
@Slf4j
@Order(300)
public class V230CourseGraduationRequirement implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public V230CourseGraduationRequirement(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }
    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V230: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V230", validationErrorsMap);

        log.debug("In shouldExecute of V230: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var courseStudent = studentRuleData.getCourseStudentEntity();
        var demStudent = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of V230 for courseStudentID :: {}", courseStudent.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        String paddedCourseCode = String.format("%-5s", courseStudent.getCourseCode());
        var coursesRecord = courseRulesService.getCoregCoursesRecord(studentRuleData, paddedCourseCode + courseStudent.getCourseLevel());

        if (coursesRecord != null && demStudent != null) {
            if (
                ("2004".equalsIgnoreCase(demStudent.getGradRequirementYear()) || "2018".equalsIgnoreCase(demStudent.getGradRequirementYear()) || "2023".equalsIgnoreCase(demStudent.getGradRequirementYear()))
                && coursesRecord.getCourseCategory() != null
                && !("CC".equalsIgnoreCase(coursesRecord.getCourseCategory().getType()) && "BA".equalsIgnoreCase(coursesRecord.getCourseCategory().getCode()))
            ) {
                log.debug("V230: Error: Invalid entry. Values only applicable for Board Authority Authorized courses for students on 2004/2018/2023 programs. This course will not be updated. for courseStudentID :: {}", courseStudent.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_GRADUATION_REQUIREMENT, CourseStudentValidationIssueTypeCode.GRAD_REQT_FINE_ARTS_APPLIED_SKILLS_2004_2018_2023_GRAD_PROG_INVALID, CourseStudentValidationIssueTypeCode.GRAD_REQT_FINE_ARTS_APPLIED_SKILLS_2004_2018_2023_GRAD_PROG_INVALID.getMessage()));
            }
        } else {
            log.debug("V230: Error: No Coreg course record match. This course will not be updated. for courseStudentID :: {}", courseStudent.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_GRADUATION_REQUIREMENT, CourseStudentValidationIssueTypeCode.GRAD_REQT_FINE_ARTS_APPLIED_SKILLS_2004_2018_2023_GRAD_PROG_INVALID, CourseStudentValidationIssueTypeCode.GRAD_REQT_FINE_ARTS_APPLIED_SKILLS_2004_2018_2023_GRAD_PROG_INVALID.getMessage()));
        }

        return errors;
    }
}
