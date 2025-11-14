package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CoregCoursesRecord;
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
    private static final List<String> allowedCreditForLDCourses = List.of("0", "1", "2", "3", "4","00", "01", "02", "03", "04");
    private static final String NO_OF_COURSES_LOG = "C18: Error: {} for courseStudentID :: {}";

    public ValidNumberOfCreditsRule(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }
    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C18: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C18", validationErrorsMap)
                && StringUtils.isNotBlank(studentRuleData.getCourseStudentEntity().getCourseStatus())
                && !studentRuleData.getCourseStudentEntity().getCourseStatus().equalsIgnoreCase("W");

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

        
        //Error if the number of credits is not equal to any of the Course Allowable Credits available for the course session, unless either of the following are true:
        
        //The Final Letter Grade is “F” or “W” and the number of credits is blank or 0,
        //The Course Type in CoReg is “Locally Developed” and the number of credits is blank, 0, 1, 2, 3, or 4.
        var coursesRecord = courseRulesService.getCoregCoursesRecord(studentRuleData, student.getCourseCode(), student.getCourseLevel());
        boolean courseTypeIsLD = coursesRecord.getCourseCategory() != null && coursesRecord.getCourseCategory().getCode().equalsIgnoreCase("LD");

        if(courseTypeIsLD) {
            if(StringUtils.isNotBlank(student.getNumberOfCredits())
                    && allowedCreditForLDCourses.stream().noneMatch(s -> s.equalsIgnoreCase(student.getNumberOfCredits()))) {
                log.debug(NO_OF_COURSES_LOG, CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID.getMessage(), student.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.NUMBER_OF_CREDITS, CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID, CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID.getMessage()));
            }
        } else {
            checkErrorForNonLDCourses(errors, student, coursesRecord);
        }
        return errors;
    }

    private void checkErrorForNonLDCourses(List<CourseStudentValidationIssue> errors, CourseStudentEntity student, CoregCoursesRecord coursesRecord) {
        if(StringUtils.isBlank(student.getNumberOfCredits()) || (StringUtils.isNumeric(student.getNumberOfCredits()) && Integer.parseInt(student.getNumberOfCredits()) == 0)) {
            boolean zeroCredWithAllowableLetterGrade = StringUtils.isNotBlank(student.getFinalLetterGrade()) && letterGradeWithAllowableCredit.stream().noneMatch(s -> s.equalsIgnoreCase(student.getFinalLetterGrade()));
            if(zeroCredWithAllowableLetterGrade) {
                log.debug(NO_OF_COURSES_LOG, CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID.getMessage(), student.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.NUMBER_OF_CREDITS, CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID, CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID.getMessage()));
            }
        } else {
            var creds = getNumberOfCredits(student.getNumberOfCredits());
            if(coursesRecord.getCourseAllowableCredit().stream().noneMatch(cac -> cac.getCreditValue().equalsIgnoreCase(creds))) {
                log.debug(NO_OF_COURSES_LOG, CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID.getMessage(), student.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.NUMBER_OF_CREDITS, CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID, CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID.getMessage()));
            }
        }
    }
    
    private String getNumberOfCredits(String numberOfCredits) {
        if (StringUtils.isNotBlank(numberOfCredits) && StringUtils.isNumeric(numberOfCredits) && Integer.parseInt(numberOfCredits) == 0) {
            return "0";
        }
        
        return StringUtils.stripStart(numberOfCredits, "0");
    }
}
