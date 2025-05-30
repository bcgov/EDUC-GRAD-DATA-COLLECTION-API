package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.FineArtsAppliedSkillsCourseGradReqt;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | C27  | ERROR    | Can only enter Fine Arts/Applied Skills if the course is Board        | C03, C16, C10|
 *  |      |          | Authority Authorized  for the 2004/2018/2023 graduation program
 */
@Component
@Slf4j
@Order(270)
public class GraduationProgramCourseRule implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public GraduationProgramCourseRule(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }
    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C27: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C27", validationErrorsMap);

        log.debug("In shouldExecute of C27: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var courseStudent = studentRuleData.getCourseStudentEntity();
        var demStudent = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of C27 for courseStudentID :: {}", courseStudent.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        var coursesRecord = courseRulesService.getCoregCoursesRecord(studentRuleData, courseStudent.getCourseCode(), courseStudent.getCourseLevel());

        String errorMessage = CourseStudentValidationIssueTypeCode.GRAD_REQT_FINE_ARTS_APPLIED_SKILLS_2004_2018_2023_GRAD_PROG_INVALID.getMessage().formatted(StringEscapeUtils.escapeHtml4(courseStudent.getCourseGraduationRequirement()));

        if (coursesRecord != null && demStudent != null) {
            if ((StringUtils.isNotBlank(courseStudent.getCourseGraduationRequirement()) && FineArtsAppliedSkillsCourseGradReqt.getCodes().contains(courseStudent.getCourseGraduationRequirement())) &&
                ("2004".equalsIgnoreCase(demStudent.getGradRequirementYear()) || "2018".equalsIgnoreCase(demStudent.getGradRequirementYear()) || "2023".equalsIgnoreCase(demStudent.getGradRequirementYear()))
                && coursesRecord.getCourseCategory() != null
                && !("CC".equalsIgnoreCase(coursesRecord.getCourseCategory().getType()) && "BA".equalsIgnoreCase(coursesRecord.getCourseCategory().getCode()))
            ) {
                log.debug("C27: Error: {} for courseStudentID :: {}", errorMessage, courseStudent.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_GRADUATION_REQUIREMENT, CourseStudentValidationIssueTypeCode.GRAD_REQT_FINE_ARTS_APPLIED_SKILLS_2004_2018_2023_GRAD_PROG_INVALID, errorMessage));
            }
        } else {
            log.debug("C27: Error: No Coreg course record match. This course will not be updated. for courseStudentID :: {}", courseStudent.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_GRADUATION_REQUIREMENT, CourseStudentValidationIssueTypeCode.GRAD_REQT_FINE_ARTS_APPLIED_SKILLS_2004_2018_2023_GRAD_PROG_INVALID, errorMessage));
        }

        return errors;
    }
}
