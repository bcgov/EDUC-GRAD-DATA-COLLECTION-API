package ca.bc.gov.educ.graddatacollection.api.rules.demographic;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

import static ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode.*;

public enum DemographicValidationRulesDependencyMatrix {
    ENTRY1("V01", new String[]{TXID_INVALID.getCode()}),
    ENTRY17("V17", new String[]{GRADE_INVALID.getCode()}),
    ENTRY18("V18", new String[]{GRADE_NOT_IN_GRAD.getCode()}),
    // TODO must v19 and v20 rely on v30
    ENTRY19("V19", new String[]{GRADE_INVALID.getCode()}),
    ENTRY20("V20", new String[]{GRADE_INVALID.getCode()}),
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
