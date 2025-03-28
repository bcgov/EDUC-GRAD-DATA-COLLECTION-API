package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.LetterGrade;
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
 *  | C23 | ERROR    | Must be a valid letter grade for the course session provided	      | C03, C16|
 *
 */
@Component
@Slf4j
@Order(230)
public class InvalidInterimGradeRule implements CourseValidationBaseRule {

    private final RestUtils restUtils;
    private final CourseRulesService courseRulesService;

    public InvalidInterimGradeRule(RestUtils restUtils, CourseRulesService courseRulesService) {
        this.restUtils = restUtils;
        this.courseRulesService = courseRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C23: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C23", validationErrorsMap);

        log.debug("In shouldExecute of C23: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of C23 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        List<LetterGrade> letterGradeList = restUtils.getLetterGradeList(true);

        if (letterGradeList.stream().noneMatch(letterGrade -> courseRulesService.letterGradeMatch(letterGrade, student.getInterimGrade()))) {
            log.debug("C23: Error: Invalid letter grade. This course will not be updated for courseStudentID :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.INTERIM_GRADE, CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_INVALID, CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_INVALID.getMessage()));
        }
        return errors;
    }
}
