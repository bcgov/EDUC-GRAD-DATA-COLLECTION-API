package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseRulesService;
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
 *  | C18  | ERROR    | The number of credits reported for the course is not an allowable     | C03, C16     |
 *  |      |          | credit value in the Course Registry. This course cannot be updated.   |              |
 */
@Component
@Slf4j
@Order(180)
public class ValidNumberOfCreditsRule implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;
    private static final List<String> letterGradeWithAllowableCredit = List.of("F", "W");
    private static final List<String> allowableCredit = List.of("0", "1", "2", "3", "4");

    public ValidNumberOfCreditsRule(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }
    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C18: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C18", validationErrorsMap);

        log.debug("In shouldExecute of C18: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of C18 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        var coursesRecord = courseRulesService.getCoregCoursesRecord(studentRuleData, student.getCourseCode(), student.getCourseLevel());
        boolean hasError = false;
        if (coursesRecord != null) {
            if(StringUtils.isNotBlank(student.getNumberOfCredits())) {
                var creds = StringUtils.stripStart(student.getNumberOfCredits(),"0");
                boolean zeroCredWithAllowableLetterGrade = StringUtils.isNumeric(creds) && Integer.parseInt(creds) == 0
                        && letterGradeWithAllowableCredit.stream().noneMatch(s -> s.equalsIgnoreCase(student.getFinalLetterGrade()));
                boolean locallyDevelopedCourse = coursesRecord.getCourseCategory() != null && coursesRecord.getCourseCategory().getType().equalsIgnoreCase("LD") && allowableCredit.stream().noneMatch(s -> s.equalsIgnoreCase(creds));
                if(zeroCredWithAllowableLetterGrade || locallyDevelopedCourse || coursesRecord.getCourseAllowableCredit().stream().noneMatch(cac -> cac.getCreditValue().equalsIgnoreCase(creds))) {
                    hasError = true;
                }
            } else if(letterGradeWithAllowableCredit.stream().noneMatch(s -> s.equalsIgnoreCase(student.getFinalLetterGrade())) || !coursesRecord.getCourseCategory().getType().equalsIgnoreCase("LD")) {
                hasError = true;
            }
        } else {
            hasError = true;
        }

        if(hasError) {
            log.debug("C18: Error: {} for courseStudentID :: {}", CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID.getMessage(), student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.NUMBER_OF_CREDITS, CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID, CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID.getMessage()));
        }
        return errors;
    }
}
