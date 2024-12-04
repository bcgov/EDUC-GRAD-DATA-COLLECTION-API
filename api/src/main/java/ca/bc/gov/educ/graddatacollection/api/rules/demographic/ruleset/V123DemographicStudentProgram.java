package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.DemographicRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.ProgramRequirementCode;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V123 | WARN     | Warn if the program is closed and the student has not yet graduated   |  V121        |
 *  |      |          |                                                                       |              |
 */

@Component
@Slf4j
@Order(2300)
public class V123DemographicStudentProgram implements DemographicValidationBaseRule {

    private final RestUtils restUtils;
    private final DemographicRulesService demographicRulesService;

    public V123DemographicStudentProgram(RestUtils restUtils, DemographicRulesService demographicRulesService) {
        this.restUtils = restUtils;
        this.demographicRulesService = demographicRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentProgram-V123: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = isValidationDependencyResolved("V123", validationErrorsMap);

        log.debug("In shouldExecute of StudentProgram-V123: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentProgram-V123 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        var gradStudent = demographicRulesService.getGradStudentRecord(studentRuleData, student.getPen());

        List<ProgramRequirementCode> programRequirementCodes = restUtils.getProgramRequirementCodes();

        if (gradStudent != null &&
            programRequirementCodes.stream().anyMatch(code -> code.getProReqCode().equalsIgnoreCase(gradStudent.getProgram()) &&
            code.getRequirementTypeCode().getExpiryDate().before(Date.valueOf(gradStudent.getProgramCompletionDate())))) {
            log.debug("StudentProgram-V123: Warning: Reported graduation program is closed. Students will not be able to graduate on this program. demographicStudentID :: {}", student.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, DemographicStudentValidationFieldCode.STUDENT_PROGRAM_CODE, DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_PROGRAM_CLOSED));
        }

        return errors;
    }
}
