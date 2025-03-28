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
 *  | C06 | ERROR    | Course session cannot be a duplicate course/level/session within the  |   C03  |
 *  |      |          | same .CRS file                                                        |              |
 */
@Component
@Slf4j
@Order(60)
public class DuplicateCourseSessionRule implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public DuplicateCourseSessionRule(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C06: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C06", validationErrorsMap);

        log.debug("In shouldExecute of C06: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of C06 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        if (courseRulesService.checkIfStudentHasDuplicateInFileset(student.getPen(), student.getCourseCode(), student.getCourseMonth(), student.getCourseYear(), student.getCourseLevel())) {
            log.debug("C06: Error: Duplicate course and session date reported for the same student. These courses will not be updated. for courseStudentID :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.PEN, CourseStudentValidationIssueTypeCode.COURSE_SESSION_DUPLICATE, CourseStudentValidationIssueTypeCode.COURSE_SESSION_DUPLICATE.getMessage()));
        }

        return errors;
    }

}
