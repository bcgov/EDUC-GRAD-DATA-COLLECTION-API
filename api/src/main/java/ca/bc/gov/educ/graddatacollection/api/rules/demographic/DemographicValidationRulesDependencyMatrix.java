package ca.bc.gov.educ.graddatacollection.api.rules.demographic;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

import static ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode.TXID_INVALID;

public enum DemographicValidationRulesDependencyMatrix {
    ENTRY1("V01", new String[]{TXID_INVALID.getCode()});

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
