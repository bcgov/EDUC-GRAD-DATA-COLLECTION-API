package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.DemographicRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                         | Dependent On |
 *  |------|----------|----------------------------------------------|--------------|
 *  | D07  | ERROR    | Must be a valid grade               	     | -            |
 *
 */

@Component
@Slf4j
@Order(70)
public class InvalidGradeRule implements DemographicValidationBaseRule {

    private final RestUtils restUtils;
    private final DemographicRulesService demographicRulesService;

    public InvalidGradeRule(RestUtils restUtils, DemographicRulesService demographicRulesService) {
        this.restUtils = restUtils;
        this.demographicRulesService = demographicRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentGrade-D07: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = true;

        log.debug("In shouldExecute of StudentGrade-D07: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentGrade-D07 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        var activeGradGrades = restUtils.getGradGradeList(true);
        boolean isSummer = demographicRulesService.isSummerCollection(student.getIncomingFileset());

        if ((!isSummer && StringUtils.isBlank(student.getGrade())) || (StringUtils.isNotBlank(student.getGrade()) && activeGradGrades.stream().noneMatch(grade -> grade.getStudentGradeCode().equalsIgnoreCase(StringUtils.leftPad(student.getGrade(),2,"0"))))) {
            String errorMessage = DemographicStudentValidationIssueTypeCode.GRADE_INVALID.getMessage().formatted(StringEscapeUtils.escapeHtml4(student.getGrade()));
            log.debug("StudentGrade-D07: {} for demographicStudentID :: {}", errorMessage, student.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.GRADE, DemographicStudentValidationIssueTypeCode.GRADE_INVALID, errorMessage));
        }
        return errors;
    }
}
