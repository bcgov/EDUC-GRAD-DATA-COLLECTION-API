package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.ProgramRequirementCode;
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
 *  | V121 | ERROR    | Must be a valid program                                               |              |
 *  |      |          | Students on Adult Graduation Program must be coded 1950.
 *                      Students on B.C. Graduation Program must be coded 2023 (or 2018,
 *                      2004, 1996, 1986 if already graduated on these earlier programs).
 *                      Students on SCCP must be coded SCCP.
 */

@Component
@Slf4j
@Order(2100)
public class V121DemographicStudentProgram implements DemographicValidationBaseRule {

    private final RestUtils restUtils;

    public V121DemographicStudentProgram(RestUtils restUtils) {
        this.restUtils = restUtils;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentProgram-V121: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = true;

        log.debug("In shouldExecute of StudentProgram-V121: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentProgram-V121 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        List<ProgramRequirementCode> programRequirementCodes = restUtils.getProgramRequirementCodes();

        // todo
        if (false) {
            log.debug("StudentProgram-V121: Invalid graduation program code. demographicStudentID :: {}", student.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, DemographicStudentValidationFieldCode.STUDENT_PROGRAM_CODE, DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID));
        }
        return errors;
    }
}
