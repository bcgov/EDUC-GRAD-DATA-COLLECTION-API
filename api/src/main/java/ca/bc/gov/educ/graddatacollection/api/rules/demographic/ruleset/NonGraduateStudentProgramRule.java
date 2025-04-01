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
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V18  | WARN     | Warn if the program is closed and the student has not yet graduated   |  V03, V05    |
 *  |      |          | If the students' program completion date is null and the program has  |              |
 *                      expired then Warning.                                                 |              |
 *                      If the students' program completion date is not null - no warning     |              |
 */

@Component
@Slf4j
@Order(180)
public class NonGraduateStudentProgramRule implements DemographicValidationBaseRule {

    private final RestUtils restUtils;
    private final DemographicRulesService demographicRulesService;

    public NonGraduateStudentProgramRule(RestUtils restUtils, DemographicRulesService demographicRulesService) {
        this.restUtils = restUtils;
        this.demographicRulesService = demographicRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentProgram-V18: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = isValidationDependencyResolved("V18", validationErrorsMap);

        log.debug("In shouldExecute of StudentProgram-V18: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentProgram-V18 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        var gradStudent = demographicRulesService.getGradStudentRecord(studentRuleData, student.getPen());
        List<GraduationProgramCode> graduationProgramCodes = restUtils.getGraduationProgramCodeList(true);
        String studentProgram = student.getGradRequirementYear();

        if (gradStudent != null && StringUtils.isNotBlank(studentProgram)) {
            String completionDateStr = gradStudent.getProgramCompletionDate();

            if (StringUtils.isBlank(completionDateStr)) {
                boolean foundOpenProgram = graduationProgramCodes.stream().anyMatch(code -> {
                    String gradCode = code.getProgramCode();
                    String baseGradCode = gradCode.contains("-") ? gradCode.split("-")[0] : gradCode;
                    return baseGradCode.equalsIgnoreCase(studentProgram);
                });

                if (!foundOpenProgram) {
                    log.debug("StudentProgram-V18: Warning: Reported graduation program is closed. Students will not be able to graduate on this program. demographicStudentID :: {}", student.getDemographicStudentID());
                    String message = "The "+ StringEscapeUtils.escapeHtml4(studentProgram)+" graduation program is closed. The student cannot graduate on this program.";
                    errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, ValidationFieldCode.GRAD_REQUIREMENT_YEAR, DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_PROGRAM_CLOSED, message));
                }
            } else {
                log.debug("StudentProgram-V18: Program completion date provided for grad student with PEN {}. Skipping V123.", student.getPen());
            }
        }
        return errors;
    }
}
