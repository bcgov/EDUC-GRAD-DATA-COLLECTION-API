package ca.bc.gov.educ.graddatacollection.api.constants.v1;

import lombok.Getter;


public enum SchoolStudentStatus {
    LOADED("LOADED"),
    ERROR("ERROR"),
    WARNING("WARNING"),
    VERIFIED("VERIFIED"),
    UPDATE_CRS("UPDATE_CRS")
    ;

    @Getter
    private final String code;

    /**
     * Instantiates a new Pen request batch status codes.
     *
     * @param code the code
     */
    SchoolStudentStatus(final String code) {
        this.code = code;
    }
}
