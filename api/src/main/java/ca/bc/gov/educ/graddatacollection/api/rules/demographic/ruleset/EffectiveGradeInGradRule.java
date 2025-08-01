package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicValidationBaseRule;
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
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | D15 | WARN     | Must be a valid grade that is currently effective in GRAD             | D07         |
 *
 */

@Component
@Slf4j
@Order(150)
public class EffectiveGradeInGradRule implements DemographicValidationBaseRule {

    private final RestUtils restUtils;

    public EffectiveGradeInGradRule(RestUtils restUtils) {
        this.restUtils = restUtils;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentGrade-D15: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = isValidationDependencyResolved("D15", validationErrorsMap);

        log.debug("In shouldExecute of StudentGrade-D15: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentGrade-D15 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        if(StringUtils.isNotBlank(student.getGrade())) {
            var activeGradGrades = restUtils.getGradGradeList(true);
            var incomingGrade = StringUtils.isNumeric(student.getGrade())  && student.getGrade().length() == 1
                    ? "0" + student.getGrade()
                    : student.getGrade();
            var matchedGradGrade = activeGradGrades.stream().filter(grade -> grade.getStudentGradeCode().equalsIgnoreCase(incomingGrade)).findFirst();

            if (matchedGradGrade.isPresent() && matchedGradGrade.get().getExpected().equalsIgnoreCase("N")) {
                String errorMessage = DemographicStudentValidationIssueTypeCode.GRADE_NOT_EXPECTED.getMessage().formatted(StringEscapeUtils.escapeHtml4(student.getGrade()));
                log.debug("StudentGrade-D15: {} for demographicStudentID :: {}", errorMessage, student.getDemographicStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, ValidationFieldCode.GRADE, DemographicStudentValidationIssueTypeCode.GRADE_NOT_EXPECTED, errorMessage));
            }
        }
        return errors;
    }
}
