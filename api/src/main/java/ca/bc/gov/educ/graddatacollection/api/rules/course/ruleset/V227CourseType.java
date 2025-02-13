package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.EquivalencyChallengeCode;
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
 *  | V227 | ERROR    | Must be a valid code	                                    	      |  V202        |
 *
 */
@Component
@Slf4j
@Order(270)
public class V227CourseType implements CourseValidationBaseRule {

    private final RestUtils restUtils;

    public V227CourseType(RestUtils restUtils) {
        this.restUtils = restUtils;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V227: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V227", validationErrorsMap);

        log.debug("In shouldExecute of V227: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of V227 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        List<EquivalencyChallengeCode> equivalencyChallengeCodesList = restUtils.getEquivalencyChallengeCodes();

        if (StringUtils.isNotBlank(student.getCourseType()) &&
                equivalencyChallengeCodesList.stream().noneMatch(equivalencyChallengeCode -> student.getCourseType().equalsIgnoreCase(equivalencyChallengeCode.getEquivalentOrChallengeCode()))) {
            log.debug("V227: Error: Invalid entry, the reported value will be ignored. Report E or C or leave blank. This course will not be updated. for courseStudentID :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_TYPE, CourseStudentValidationIssueTypeCode.EQUIVALENCY_CHALLENGE_CODE_INVALID, CourseStudentValidationIssueTypeCode.EQUIVALENCY_CHALLENGE_CODE_INVALID.getMessage()));
        }
        return errors;
    }
}
