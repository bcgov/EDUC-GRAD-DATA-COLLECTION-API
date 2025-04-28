package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.SCCPEffectiveDate;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | D08 | ERROR    | Must be a valid date                                           	      | -            |
 *
 */
@Component
@Slf4j
@Order(80)
public class InvalidSCCPCompletionDateRule implements DemographicValidationBaseRule {

    private static final DateTimeFormatter YYYYMMDD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final LocalDate SCCP_EFFECTIVE_DATE = LocalDate.parse(SCCPEffectiveDate.SCCP_EFFECTIVE_DATE.getDate(), YYYYMMDD_FORMATTER);

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of SCCPCompletionDate-D08: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = true;

        log.debug("In shouldExecute of SCCPCompletionDate-D08: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of SCCPCompletionDate-D08 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        String sccpCompletionDate = student.getSchoolCertificateCompletionDate();

        if (!StringUtils.isBlank(sccpCompletionDate)) {
            String escapedSccpCompletionDate = StringEscapeUtils.escapeHtml4(student.getSchoolCertificateCompletionDate());
            try {
                LocalDate parsedSccpCompletionDate = LocalDate.parse(sccpCompletionDate, YYYYMMDD_FORMATTER);
                if (parsedSccpCompletionDate.isBefore(SCCP_EFFECTIVE_DATE)) {
                    String tooEarlyErrorMessage = DemographicStudentValidationIssueTypeCode.SCCP_DATE_TOO_EARLY.getMessage().formatted(escapedSccpCompletionDate);
                    logDebugStatement(tooEarlyErrorMessage, student.getDemographicStudentID());
                    errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.SCHOOL_CERTIFICATE_COMPLETION_DATE, DemographicStudentValidationIssueTypeCode.SCCP_DATE_TOO_EARLY, tooEarlyErrorMessage));
                }
            } catch (DateTimeParseException ex) {
                String invalidSccpCompletionDateErrorMessage = DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getMessage().formatted(escapedSccpCompletionDate);
                logDebugStatement(invalidSccpCompletionDateErrorMessage, student.getDemographicStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.SCHOOL_CERTIFICATE_COMPLETION_DATE, DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE, invalidSccpCompletionDateErrorMessage));
            }
        }

        return errors;
    }

    private void logDebugStatement(String errorMessage, java.util.UUID demographicStudentID) {
        log.debug("SCCPCompletionDate-D08: {} for demographicStudentID :: {}", errorMessage, demographicStudentID);
    }

}
