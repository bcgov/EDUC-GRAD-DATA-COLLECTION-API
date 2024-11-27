package ca.bc.gov.educ.graddatacollection.api.rules.demographic;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

import static ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode.*;

public enum DemographicValidationRulesDependencyMatrix {
    ENTRY111("V111", new String[]{GRADE_INVALID.getCode()}),
    ENTRY112("V112", new String[]{GRADE_INVALID.getCode(), STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode()}),
    ENTRY113("V113", new String[]{GRADE_INVALID.getCode(), STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode()}),
    ENTRY118("V118", new String[]{STUDENT_STATUS_INVALID.getCode()}),
    ENTRY119("V119", new String[]{STUDENT_STATUS_INVALID.getCode()}),
    ENTRY120("V120", new String[]{STUDENT_STATUS_INVALID.getCode()}),
    ENTRY123("V123", new String[]{STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode()}),
    ENTRY127("V127", new String[]{SCCP_INVALID_DATE.getCode()}),
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
