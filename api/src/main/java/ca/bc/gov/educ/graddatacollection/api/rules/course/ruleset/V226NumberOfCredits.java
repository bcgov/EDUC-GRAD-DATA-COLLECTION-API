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
 *  | V226 | ERROR    | The number of credits must be equal to at least one of the Course     |   V202       |
 *  |      |          | Allowable Credits that were available for the course session.         |              |
 */
@Component
@Slf4j
@Order(260)
public class V226NumberOfCredits implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public V226NumberOfCredits(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }
    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V226: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V226", validationErrorsMap);

        log.debug("In shouldExecute of V226: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of V226 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        String paddedCourseCode = String.format("%-5s", student.getCourseCode());
        var coursesRecord = courseRulesService.getCoregCoursesRecord(studentRuleData, paddedCourseCode + student.getCourseLevel());

        if (coursesRecord != null) {
            if (coursesRecord.getCourseAllowableCredit().stream().noneMatch(cac -> cac.getCreditValue().equalsIgnoreCase(student.getNumberOfCredits()))) {
                log.debug("V226: Error: The number of credits reported for the course is not an allowable credit value in the Course Registry. This course will not be updated. for courseStudentID :: {}", student.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.NUMBER_OF_CREDITS, CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID, CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID.getMessage()));
            }
        } else {
            log.debug("V226: Error: No Coreg course record match. This course will not be updated. for courseStudentID :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.NUMBER_OF_CREDITS, CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID, CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID.getMessage()));
        }

        return errors;
    }
}
