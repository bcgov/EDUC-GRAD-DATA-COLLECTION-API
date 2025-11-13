package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.EquivalencyChallengeCode;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | C09  | ERROR    | The submitted value <CRS VALUE> is not an allowable value, per the    | C03          |
 *  |      |          | current GRAD file specification. This course cannot be updated.       |              |
 */
@Component
@Slf4j
@Order(90)
public class InvalidCourseTypeRule implements CourseValidationBaseRule {

    private final RestUtils restUtils;

    public InvalidCourseTypeRule(RestUtils restUtils) {
        this.restUtils = restUtils;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C09: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C09", validationErrorsMap)
                && StringUtils.isNotBlank(studentRuleData.getCourseStudentEntity().getCourseStatus())
                && !studentRuleData.getCourseStudentEntity().getCourseStatus().equalsIgnoreCase("W");

        log.debug("In shouldExecute of C09: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of C09 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        List<EquivalencyChallengeCode> equivalencyChallengeCodesList = restUtils.getEquivalencyChallengeCodeList();

        if (StringUtils.isNotBlank(student.getCourseType()) &&
                equivalencyChallengeCodesList.stream().noneMatch(equivalencyChallengeCode -> student.getCourseType().equalsIgnoreCase(equivalencyChallengeCode.getEquivalentOrChallengeCode()))) {
            String errorMessage = CourseStudentValidationIssueTypeCode.EQUIVALENCY_CHALLENGE_CODE_INVALID.getMessage().formatted(StringEscapeUtils.escapeHtml4(student.getCourseType()));
            log.debug("C09: Error: {} for courseStudentID :: {}", errorMessage, student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_TYPE, CourseStudentValidationIssueTypeCode.EQUIVALENCY_CHALLENGE_CODE_INVALID, errorMessage));
        }
        return errors;
    }
}
