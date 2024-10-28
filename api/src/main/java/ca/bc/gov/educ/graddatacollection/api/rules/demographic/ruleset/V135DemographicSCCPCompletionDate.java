package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradRequirementYearCodes;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationFieldCode;
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
 *  | V135 | WARN     | Student must be on the SCCP program	                           	      | V134         |
 *
 */
@Component
@Slf4j
@Order(3500)
public class V135DemographicSCCPCompletionDate implements DemographicValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of SCCPCompletionDate-V135: for demographicSCCPDate :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = isValidationDependencyResolved("V35", validationErrorsMap);

        log.debug("In shouldExecute of SCCPCompletionDate-V135: Condition returned - {} for demographicSCCPDate :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of SCCPCompletionDate-V135 for demographicSCCPDate :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        if (!GradRequirementYearCodes.SCCP.getCode().equals(student.getGradRequirementYear())) {
            log.debug("SCCPCompletionDate-V135: Student must be on the SCCP program. SCCP Completion date not updated. for demographicSCCPDate :: {}", student.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, DemographicStudentValidationFieldCode.SCCP_COMPLETION_DATE, DemographicStudentValidationIssueTypeCode.SCCP_INVALID_STUDENT_PROGRAM));
        }
        return errors;
    }

}
