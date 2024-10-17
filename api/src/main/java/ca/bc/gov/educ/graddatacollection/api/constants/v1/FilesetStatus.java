package ca.bc.gov.educ.graddatacollection.api.constants.v1;

import lombok.Getter;


public enum FilesetStatus {
    DELETED("DELETED"),
    COMPLETED("COMPLETED"),
    LOADED("LOADED"),
    NOT_LOADED("NOTLOADED")
    ;

    @Getter
    private final String code;

    /**
     * Instantiates a new Pen request batch status codes.
     *
     * @param code the code
     */
    FilesetStatus(final String code) {
        this.code = code;
    }
}
