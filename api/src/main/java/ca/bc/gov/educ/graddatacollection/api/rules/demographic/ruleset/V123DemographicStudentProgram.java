package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.DemographicRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GraduationProgramCode;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V123 | WARN     | Warn if the program is closed and the student has not yet graduated   |  V121        |
 *  |      |          | If the students' program completion date is null and the program has  |              |
 *                      expired then Warning.                                                 |              |
 *                      If the students' program completion date is not null - no warning     |              |
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
        List<GraduationProgramCode> graduationProgramCodes = restUtils.getGraduationProgramCodes();
        String studentProgram = student.getGradRequirementYear();

        if (gradStudent != null && StringUtils.isNotEmpty(studentProgram)) {
            String completionDateStr = gradStudent.getProgramCompletionDate();

            if (StringUtils.isEmpty(completionDateStr)) {
                Date now = new Date();
                boolean programClosed = graduationProgramCodes.stream().anyMatch(code -> {
                    String gradCode = code.getProgramCode();
                    String baseGradCode = gradCode.contains("-") ? gradCode.split("-")[0] : gradCode;
                    if (code.getExpiryDate() != null && baseGradCode.equalsIgnoreCase(studentProgram)) {
                        return code.getExpiryDate().before(now);
                    }
                    return false;
                });

                if (programClosed) {
                    log.debug("StudentProgram-V123: Warning: Reported graduation program is closed. Students will not be able to graduate on this program. demographicStudentID :: {}", student.getDemographicStudentID());
                    errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, ValidationFieldCode.GRAD_REQUIREMENT_YEAR, DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_PROGRAM_CLOSED, DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_PROGRAM_CLOSED.getMessage()));
                }
            } else {
                log.debug("StudentProgram-V123: Program completion date provided for grad student with PEN {}. Skipping V123.", student.getPen());
            }
        }
        return errors;
    }
}
