package ca.bc.gov.educ.graddatacollection.api.rules.demographic;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

import static ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode.*;

public enum DemographicValidationRulesDependencyMatrix {
    ENTRY2("D10", new String[]{STUDENT_PEN_NOT_FOUND.getCode()}),
    ENTRY3("D11", new String[]{STUDENT_PEN_NOT_FOUND.getCode()}),
    ENTRY4("D12", new String[]{STUDENT_PEN_NOT_FOUND.getCode()}),
    ENTRY5("D13", new String[]{STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode()}),
    ENTRY6("D14", new String[]{STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode()}),
    ENTRY7("D15", new String[]{GRADE_INVALID.getCode()}),
    ENTRY8("D16", new String[]{STUDENT_PEN_NOT_FOUND.getCode(), STUDENT_BIRTHDATE_INVALID.getCode()}),
    ENTRY10("D18", new String[]{STUDENT_PEN_NOT_FOUND.getCode(), STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode()}),
    ENTRY11("D19", new String[]{STUDENT_PEN_NOT_FOUND.getCode(), STUDENT_STATUS_INVALID.getCode()}),
    ENTRY12("D20", new String[]{STUDENT_PEN_NOT_FOUND.getCode(), STUDENT_STATUS_INVALID.getCode()}),
    ENTRY13("D21", new String[]{STUDENT_PEN_NOT_FOUND.getCode(), STUDENT_STATUS_INVALID.getCode()}),
    ENTRY15("D23", new String[]{STUDENT_BIRTHDATE_INVALID.getCode(), STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode()}),
    ENTRY16("D24", new String[]{STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode(), GRADE_INVALID.getCode()}),
    ENTRY17("D25", new String[]{STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode(), SCCP_INVALID_DATE.getCode(), SCCP_DATE_TOO_EARLY.getCode()}),
    ENTRY18("D26", new String[]{GRADE_INVALID.getCode(), SCCP_INVALID_STUDENT_PROGRAM.getCode()}),
    ENTRY19("D27", new String[]{STUDENT_PEN_NOT_FOUND.getCode(), STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode(), SCCP_INVALID_DATE.getCode(), SCCP_INVALID_STUDENT_PROGRAM.getCode()}),
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
