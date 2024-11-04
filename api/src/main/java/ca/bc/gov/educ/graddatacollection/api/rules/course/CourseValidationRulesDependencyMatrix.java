package ca.bc.gov.educ.graddatacollection.api.rules.course;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

public enum CourseValidationRulesDependencyMatrix {
    ENTRY1("V202", new String[]{CourseStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode()});

    @Getter
    private final String ruleID;
    @Getter
    private final String[] baseRuleErrorCode;
    CourseValidationRulesDependencyMatrix(String ruleID, String[] baseRuleErrorCode) {
        this.ruleID = ruleID;
        this.baseRuleErrorCode = baseRuleErrorCode;
    }

    public static Optional<CourseValidationRulesDependencyMatrix> findByValue(String ruleID) {
        return Arrays.stream(values()).filter(code -> code.ruleID.equalsIgnoreCase(ruleID)).findFirst();
    }
}
