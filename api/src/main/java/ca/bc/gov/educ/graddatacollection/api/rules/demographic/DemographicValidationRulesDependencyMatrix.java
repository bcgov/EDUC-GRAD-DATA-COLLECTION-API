package ca.bc.gov.educ.graddatacollection.api.rules.demographic;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

import static ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode.*;

public enum DemographicValidationRulesDependencyMatrix {
    // TODO v12 and v13 must also rely on valid program v21
    ENTRY11("V11", new String[]{GRADE_INVALID.getCode()}),
    ENTRY12("V12", new String[]{GRADE_INVALID.getCode()}),
    ENTRY13("V13", new String[]{GRADE_INVALID.getCode()}),
    ENTRY19("V19", new String[]{STUDENT_STATUS_INVALID.getCode()}),
    ENTRY20("V20", new String[]{STUDENT_STATUS_INVALID.getCode()}),
    ENTRY27("V27", new String[]{SCCP_INVALID_DATE.getCode()}),
    ;

    @Getter
    private final String ruleID;
    @Getter
    private final String[] baseRuleErrorCode;
    DemographicValidationRulesDependencyMatrix(String ruleID, String[] baseRuleErrorCode) {
        this.ruleID = ruleID;
        this.baseRuleErrorCode = baseRuleErrorCode;
    }

    public static Optional<DemographicValidationRulesDependencyMatrix> findByValue(String ruleID) {
        return Arrays.stream(values()).filter(code -> code.ruleID.equalsIgnoreCase(ruleID)).findFirst();
    }
}
