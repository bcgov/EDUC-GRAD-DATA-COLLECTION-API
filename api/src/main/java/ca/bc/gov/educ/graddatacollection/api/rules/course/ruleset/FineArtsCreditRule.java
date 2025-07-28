package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradRequirementYearCodes;
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
import java.util.Arrays;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | C33  | ERROR    | For the 1996 graduation program, check number of credits for Fine     |C03, C18      |
 *  |      |          | Arts/Applied Skills.
 *  |      |          | If B - credits for course must be 4-credits
 */
@Component
@Slf4j
@Order(330)
public class FineArtsCreditRule implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;
    private static final String [] BOARD_AUTHORITY_OR_LOCALLY_DEVELOPED = new String[]{"BA", "LD"};

    public FineArtsCreditRule(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C33: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C33", validationErrorsMap);

        log.debug("In shouldExecute of C33: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var courseStudent = studentRuleData.getCourseStudentEntity();
        var demStudent = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of C33 for courseStudentID :: {}", courseStudent.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();
        var coursesRecord = courseRulesService.getCoregCoursesRecord(studentRuleData, courseStudent.getCourseCode(), courseStudent.getCourseLevel());

        if (demStudent != null &&
                GradRequirementYearCodes.YEAR_1996.getCode().equalsIgnoreCase(demStudent.getGradRequirementYear()) &&
                Arrays.stream(BOARD_AUTHORITY_OR_LOCALLY_DEVELOPED).anyMatch(boardAuthorityOrLocallyDeveloped -> boardAuthorityOrLocallyDeveloped.equalsIgnoreCase(coursesRecord.getCourseCategory().getCode())) &&
                "B".equalsIgnoreCase(courseStudent.getCourseGraduationRequirement()) &&
                !"4".equalsIgnoreCase(courseStudent.getNumberOfCredits())) {
            log.debug("C33: Error: {} for courseStudentID :: {}", CourseStudentValidationIssueTypeCode.GRADUATION_REQUIREMENT_NUMBER_CREDITS_INVALID.getMessage(), courseStudent.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_GRADUATION_REQUIREMENT, CourseStudentValidationIssueTypeCode.GRADUATION_REQUIREMENT_NUMBER_CREDITS_INVALID, CourseStudentValidationIssueTypeCode.GRADUATION_REQUIREMENT_NUMBER_CREDITS_INVALID.getMessage()));
        }
        return errors;
    }
}
