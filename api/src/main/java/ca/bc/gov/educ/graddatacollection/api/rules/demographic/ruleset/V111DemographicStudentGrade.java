package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V111 | WARN     | Must be a valid grade that is currently effective in GRAD             | V110         |
 *
 */

@Component
@Slf4j
@Order(1100)
public class V111DemographicStudentGrade implements DemographicValidationBaseRule {

    private final RestUtils restUtils;

    public V111DemographicStudentGrade(RestUtils restUtils) {
        this.restUtils = restUtils;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentGrade-V111: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = isValidationDependencyResolved("V111", validationErrorsMap);

        log.debug("In shouldExecute of StudentGrade-V111: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentGrade-V111 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        var activeGradGrades = restUtils.getGradGradeList(true);
        var matchedGradGrade = activeGradGrades.stream().filter(grade -> grade.getStudentGradeCode().equalsIgnoreCase(student.getGrade())).findFirst();

        if (matchedGradGrade.isPresent() && matchedGradGrade.get().getExpected().equalsIgnoreCase("N") ) {
            log.debug("StudentGrade-V111: Must be a valid grade that is currently effective in GRAD for demographicStudentID :: {}", student.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, ValidationFieldCode.GRADE, DemographicStudentValidationIssueTypeCode.GRADE_NOT_EXPECTED, DemographicStudentValidationIssueTypeCode.GRADE_NOT_EXPECTED.getMessage()));
        }
        return errors;
    }
}
