package ca.bc.gov.educ.graddatacollection.api.rules.demographic;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

import static ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode.*;

public enum DemographicValidationRulesDependencyMatrix {
    ENTRY1("V09", new String[]{STUDENT_PEN_NOT_FOUND.getCode()}),
    ENTRY2("V10", new String[]{STUDENT_PEN_NOT_FOUND.getCode()}),
    ENTRY3("V11", new String[]{STUDENT_PEN_NOT_FOUND.getCode()}),
    ENTRY4("V12", new String[]{STUDENT_PEN_NOT_FOUND.getCode()}),
    ENTRY5("V13", new String[]{STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode()}),
    ENTRY6("V14", new String[]{STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode()}),
    ENTRY7("V15", new String[]{GRADE_INVALID.getCode()}),
    ENTRY8("V16", new String[]{STUDENT_PEN_NOT_FOUND.getCode(), STUDENT_BIRTHDATE_INVALID.getCode()}),
    ENTRY9("V17", new String[]{STUDENT_PEN_NOT_FOUND.getCode(), STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode()}),
    ENTRY10("V18", new String[]{STUDENT_PEN_NOT_FOUND.getCode(), STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode()}),
    ENTRY11("V19", new String[]{STUDENT_PEN_NOT_FOUND.getCode(), STUDENT_STATUS_INVALID.getCode()}),
    ENTRY12("V20", new String[]{STUDENT_PEN_NOT_FOUND.getCode(), STUDENT_STATUS_INVALID.getCode()}),
    ENTRY13("V21", new String[]{STUDENT_PEN_NOT_FOUND.getCode(), STUDENT_STATUS_INVALID.getCode()}),
    ENTRY14("V22", new String[]{STUDENT_PEN_NOT_FOUND.getCode(), STUDENT_STATUS_INVALID.getCode()}),
    ENTRY15("V23", new String[]{STUDENT_BIRTHDATE_INVALID.getCode(), STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode()}),
    ENTRY16("V24", new String[]{STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode(), GRADE_INVALID.getCode()}),
    ENTRY17("V25", new String[]{STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode(), SCCP_INVALID_DATE.getCode()}),
    ENTRY18("V26", new String[]{GRADE_INVALID.getCode(), SCCP_INVALID_STUDENT_PROGRAM.getCode()}),

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
