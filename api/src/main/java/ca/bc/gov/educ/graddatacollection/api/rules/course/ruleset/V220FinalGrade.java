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
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V220 | ERROR    | Must be a valid letter grade for the course session provided	      | V217         |
 *
 */
@Component
@Slf4j
@Order(200)
public class V220FinalGrade implements CourseValidationBaseRule {

    private final RestUtils restUtils;
    private final CourseRulesService courseRulesService;

    public V220FinalGrade(RestUtils restUtils, CourseRulesService courseRulesService) {
        this.restUtils = restUtils;
        this.courseRulesService = courseRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V220: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V220", validationErrorsMap);

        log.debug("In shouldExecute of V220: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of V220 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        List<LetterGrade> letterGradeList = restUtils.getLetterGradeList();
        if (StringUtils.isNotBlank(student.getFinalGrade()) && letterGradeList.stream().noneMatch(letterGrade -> courseRulesService.letterGradeMatch(letterGrade, student.getFinalGrade()))) {
            log.debug("V220: Error: Invalid letter grade. This course will not be updated for courseStudentID :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.FINAL_GRADE, CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_INVALID, CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_INVALID.getMessage()));
        }
        return errors;
    }
}
