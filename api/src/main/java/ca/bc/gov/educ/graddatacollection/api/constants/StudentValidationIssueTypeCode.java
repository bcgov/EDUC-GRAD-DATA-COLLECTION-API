package ca.bc.gov.educ.graddatacollection.api.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

import static ca.bc.gov.educ.graddatacollection.api.constants.StudentValidationIssueSeverityCode.*;

/**
 * The enum Pen request batch student validation issue type code.
 */
@Getter
public enum StudentValidationIssueTypeCode {

    INVALID_GRADE_CODE("INVALIDGRADECODE", "Invalid grade code.", ERROR);

    private static final Map<String, StudentValidationIssueTypeCode> CODE_MAP = new HashMap<>();

    static {
        for (StudentValidationIssueTypeCode type : values()) {
            CODE_MAP.put(type.getCode(), type);
        }
    }

    /**
     * The Code.
     */
    private final String code;

    /**
     * Validation message
     */
    private final String message;

    private final StudentValidationIssueSeverityCode severityCode;

    /**
     * Instantiates a new Pen request batch student validation issue type code.
     *
     * @param code the code
     */
    StudentValidationIssueTypeCode(String code, String message, StudentValidationIssueSeverityCode severityCode) {
        this.code = code;
        this.message = message;
        this.severityCode = severityCode;
    }
    public static StudentValidationIssueTypeCode findByValue(String value) {
        return CODE_MAP.get(value);
    }
}
