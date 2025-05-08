package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradRequirementYearCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.DemographicRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | D27 | ERROR     | If the student is reported on SCCP and with a SCCP Completion Date,   |  D03,D05,D08, D25 |
 *                      the submitted SCCP Completion Date must match the
 *                      GRAD PROGRAM_COMPLETION_DATE for the student unless either of the
 *                      following are true:
 *
 *                      the GRAD PROGRAM_COMPLETION_DATE is blank
 *
 *                      the GRAD SCHOOL_AT_GRAD is blank
 *
 */
@Component
@Slf4j
@Order(270)
public class SCCPCompletionDateAlreadyReportedRule implements DemographicValidationBaseRule {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final DemographicRulesService demographicRulesService;

    public SCCPCompletionDateAlreadyReportedRule(DemographicRulesService demographicRulesService) {
        this.demographicRulesService = demographicRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of SCCPCompletionDate-D27: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = isValidationDependencyResolved("D27", validationErrorsMap);

        log.debug("In shouldExecute of SCCPCompletionDate-D27: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of SCCPCompletionDate-D27 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        var gradStudent = demographicRulesService.getGradStudentRecord(studentRuleData, student.getPen());
        var hasCompletionDate = gradStudent != null && StringUtils.isNotBlank(gradStudent.getProgramCompletionDate());
        var hasSchoolAtGrad = gradStudent != null && StringUtils.isNotBlank(gradStudent.getSchoolAtGradId());
        var programCompletionDate = hasCompletionDate ? LocalDateTime.parse(gradStudent.getProgramCompletionDate(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).format(formatter) : null;

        if (hasCompletionDate && hasSchoolAtGrad && StringUtils.isNotBlank(student.getSchoolCertificateCompletionDate()) &&
                GradRequirementYearCodes.SCCP.getCode().equalsIgnoreCase(student.getGradRequirementYear()) && !student.getSchoolCertificateCompletionDate().equalsIgnoreCase(programCompletionDate)) {
            String invalidErrorMessage = DemographicStudentValidationIssueTypeCode.SCCP_INVALID_STUDENT_PROGRAM_ALREADY_REPORTED.getMessage().formatted(programCompletionDate);
            logDebugStatement(invalidErrorMessage, student.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.SCHOOL_CERTIFICATE_COMPLETION_DATE, DemographicStudentValidationIssueTypeCode.SCCP_INVALID_STUDENT_PROGRAM_ALREADY_REPORTED, invalidErrorMessage));
        }
        return errors;
    }

    private void logDebugStatement(String errorMessage, java.util.UUID demographicStudentID) {
        log.debug("SCCPCompletionDate-D27: {} for demographicStudentID :: {}", errorMessage, demographicStudentID);
    }
}
