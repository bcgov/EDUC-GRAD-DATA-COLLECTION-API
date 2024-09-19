package ca.bc.gov.educ.graddatacollection.api.constants.v1;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

import static ca.bc.gov.educ.graddatacollection.api.constants.StudentValidationIssueTypeCode.INVALID_GRADE_CODE;

@Getter
public enum ValidationRulesDependencyMatrix {
    ENTRY1("V1", new String[]{INVALID_GRADE_CODE.getCode()}),
    ;

    private final String ruleID;
    private final String[] baseRuleErrorCode;
    ValidationRulesDependencyMatrix(String ruleID, String[] baseRuleErrorCode) {
        this.ruleID = ruleID;
        this.baseRuleErrorCode = baseRuleErrorCode;
    }

    public static Optional<ValidationRulesDependencyMatrix> findByValue(String ruleID) {
        return Arrays.stream(values()).filter(code -> code.ruleID.equalsIgnoreCase(ruleID)).findFirst();
    }
}
