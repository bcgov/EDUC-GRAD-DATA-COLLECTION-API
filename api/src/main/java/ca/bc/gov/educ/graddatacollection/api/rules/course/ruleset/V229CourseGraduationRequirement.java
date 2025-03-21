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
 *  | V229 | ERROR    | Can only enter Fine Arts/Applied Skills if the course is Board        |   V202       |
 *  |      |          | Authority Authorized or Locally Developed for the 1996 graduation     |   V231       |
 *  |      |          | program                                                               |   V206   |
 */
@Component
@Slf4j
@Order(290)
public class V229CourseGraduationRequirement implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public V229CourseGraduationRequirement(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }
    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V229: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V229", validationErrorsMap);

        log.debug("In shouldExecute of V229: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var courseStudent = studentRuleData.getCourseStudentEntity();
        var demStudent = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of V229 for courseStudentID :: {}", courseStudent.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        String paddedCourseCode = String.format("%-5s", courseStudent.getCourseCode());
        var coursesRecord = courseRulesService.getCoregCoursesRecord(studentRuleData, paddedCourseCode + courseStudent.getCourseLevel());

        if (coursesRecord != null && demStudent != null) {
            if ("1996".equalsIgnoreCase(demStudent.getGradRequirementYear())
                && !("CC".equalsIgnoreCase(coursesRecord.getCourseCategory().getType())
                    && ("BA".equalsIgnoreCase(coursesRecord.getCourseCategory().getCode()) || "LD".equalsIgnoreCase(coursesRecord.getCourseCategory().getCode())))) {
                log.debug("V229: Error: Invalid entry. Values only applicable for Board Authority Authorized or Locally Developed courses for students on the 1996 program. This course will not be updated. for courseStudentID :: {}", courseStudent.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_GRADUATION_REQUIREMENT, CourseStudentValidationIssueTypeCode.GRAD_REQT_FINE_ARTS_APPLIED_SKILLS_1996_GRAD_PROG_INVALID, CourseStudentValidationIssueTypeCode.GRAD_REQT_FINE_ARTS_APPLIED_SKILLS_1996_GRAD_PROG_INVALID.getMessage()));
            }
        } else {
            log.debug("V229: Error: No Coreg course record match. This course will not be updated. for courseStudentID :: {}", courseStudent.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_GRADUATION_REQUIREMENT, CourseStudentValidationIssueTypeCode.GRAD_REQT_FINE_ARTS_APPLIED_SKILLS_1996_GRAD_PROG_INVALID, CourseStudentValidationIssueTypeCode.GRAD_REQT_FINE_ARTS_APPLIED_SKILLS_1996_GRAD_PROG_INVALID.getMessage()));
        }

        return errors;
    }
}
