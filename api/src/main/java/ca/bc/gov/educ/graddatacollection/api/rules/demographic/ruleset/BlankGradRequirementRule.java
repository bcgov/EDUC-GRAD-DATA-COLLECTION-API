package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradRequirementYearCodes;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                   | Dependent On |
 *  |------|----------|------------------------------------------------------------------------|--------------|
 *  | D12  | WARNING  | 1. Warning if a null Grad Req Year is reported                         |    D03       |
 *  |      |          | 2. Warning if all the following are true:                              |              |
 *  |      |          |    - Grad Req Year is not null                                         |              |
 *  |      |          |    - The student has graduated                                         |              |
 *  |      |          |    - The student's program in GRAD is not SCCP                         |              |
 *  |      |          |    - The submitted grad program is different from the program in GRAD  |              |
 */
@Component
@Slf4j
@Order(120)
public class BlankGradRequirementRule implements DemographicValidationBaseRule {

    private final RestUtils restUtils;
    private final DemographicRulesService demographicRulesService;

    public BlankGradRequirementRule(RestUtils restUtils, DemographicRulesService demographicRulesService) {
        this.restUtils = restUtils;
        this.demographicRulesService = demographicRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentProgram-D12: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute =  isValidationDependencyResolved("D12", validationErrorsMap);

        log.debug("In shouldExecute of StudentProgram-D12: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentProgram-12 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        List<GraduationProgramCode> graduationProgramCodes = restUtils.getGraduationProgramCodeList(true);

        var gradRecord = demographicRulesService.getGradStudentRecord(studentRuleData, student.getPen());

        var gradProgramValue = gradRecord != null ? getGradProgramValue(gradRecord.getProgram()) : null;
        boolean gradRequirementYearIsBlank = StringUtils.isBlank(student.getGradRequirementYear());
        boolean isGraduated = gradRecord != null && StringUtils.isNotBlank(gradRecord.getGraduated()) && gradRecord.getGraduated().equalsIgnoreCase("true");
        boolean isSCCPProgram = gradRecord != null && StringUtils.isNotBlank(gradProgramValue) && gradProgramValue.equalsIgnoreCase(GradRequirementYearCodes.SCCP.getCode());
        boolean isGradProgramChanged = gradRecord != null && StringUtils.isNotBlank(student.getGradRequirementYear()) && !student.getGradRequirementYear().equalsIgnoreCase(gradProgramValue);

        if(gradRequirementYearIsBlank) {
            String gradProgramForErrorMessage = (gradRecord != null && gradProgramValue != null) ? gradProgramValue : graduationProgramCodes.stream()
                    .filter(g -> g.getProgramCode() != null)
                    .filter(g -> !g.getProgramCode().equals(GradRequirementYearCodes.YEAR_1950.getCode()))
                    .filter(g -> !g.getProgramCode().equals(GradRequirementYearCodes.SCCP.getCode()))
                    .filter(g -> g.getProgramCode().endsWith("-EN"))
                    .filter(g -> {
                        if (g.getEffectiveDate() == null) return false;
                        String effectiveDate = g.getEffectiveDate().substring(0, 10);
                        return !LocalDate.parse(effectiveDate).isAfter(LocalDate.now());
                    })
                    .filter(g -> {
                        if (g.getExpiryDate() == null) return true;
                        String expiryDate = g.getExpiryDate().substring(0, 10);
                        return LocalDate.parse(expiryDate).isAfter(LocalDate.now());
                    })
                    .sorted((a, b) -> {
                        String dateA = a.getEffectiveDate().substring(0, 10);
                        String dateB = b.getEffectiveDate().substring(0, 10);
                        return dateB.compareTo(dateA);
                    })
                    .map(GraduationProgramCode::getProgramCode)
                    .findFirst()
                    .orElse("");
            String gradProgramErrorMessage = DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL.getMessage().formatted(gradProgramForErrorMessage);
            log.debug("StudentProgram-D12: {} for demographicStudentID :: {}", gradProgramErrorMessage, student.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, ValidationFieldCode.GRAD_REQUIREMENT_YEAR, DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL, gradProgramErrorMessage));

        } else if (isGraduated && !isSCCPProgram && isGradProgramChanged) {
            String gradProgramErrorMessage = DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL_GRAD.getMessage().formatted(gradRecord.getProgram());
            log.debug("StudentProgram-D12: {} for demographicStudentID :: {}", DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL_GRAD.getMessage(), student.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, ValidationFieldCode.GRAD_REQUIREMENT_YEAR, DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL_GRAD, gradProgramErrorMessage));

        }
        return errors;
    }
    
    private String getGradProgramValue(String gradProgram) {
        if(!StringUtils.isBlank(gradProgram) && gradProgram.length() > 4) {
            return gradProgram.substring(0, 4);
        }
        return gradProgram;
    }

}
