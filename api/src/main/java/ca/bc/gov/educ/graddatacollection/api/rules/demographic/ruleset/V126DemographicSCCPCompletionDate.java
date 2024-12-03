package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.SCCPEffectiveDate;
import ca.bc.gov.educ.graddatacollection.api.helpers.DateValidator;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V126 | ERROR    | Must be a valid date                                           	      | -            |
 *
 */
@Component
@Slf4j
@Order(2600)
public class V126DemographicSCCPCompletionDate implements DemographicValidationBaseRule {

    private static final DateTimeFormatter YYYYMMDD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final LocalDate SCCP_EFFECTIVE_DATE = LocalDate.parse(SCCPEffectiveDate.SCCP_EFFECTIVE_DATE.getDate(), YYYYMMDD_FORMATTER);

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of SCCPCompletionDate-V126: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = true;

        log.debug("In shouldExecute of SCCPCompletionDate-V126: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of SCCPCompletionDate-V126 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        String sccpCompletionDate = student.getSchoolCertificateCompletionDate();

        if (!StringUtils.isEmpty(sccpCompletionDate) &&
                (!DateValidator.isValidYYYYMMDD(sccpCompletionDate) ||
                LocalDate.parse(sccpCompletionDate, YYYYMMDD_FORMATTER).isBefore(SCCP_EFFECTIVE_DATE))) {
                log.debug("SCCPCompletionDate-V126: Invalid SCCP completion date. Must be blank or YYYYMMDD. The student's DEM file will not be processed. for demographicStudentID :: {}", student.getDemographicStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, DemographicStudentValidationFieldCode.SCCP_COMPLETION_DATE, DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE));
            }

        return errors;
    }

}
