package ca.bc.gov.educ.graddatacollection.api.constants;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * The enum Pen request batch student validation issue severity code.
 */
@Getter
public enum StudentValidationIssueSeverityCode {
    /**
     * Error student validation issue severity code.
     */
    ERROR("ERROR","Error");

    /**
     * The Code.
     */
    private final String label;
    private final String code;
    /**
     * Instantiates a new Pen request batch student validation field code.
     *
     * @param code the code
     */
    StudentValidationIssueSeverityCode(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static Optional<StudentValidationIssueSeverityCode> findByValue(String value) {
        return Arrays.stream(values()).filter(e -> Arrays.asList(e.code).contains(value)).findFirst();
    }
}
