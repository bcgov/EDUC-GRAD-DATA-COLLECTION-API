package ca.bc.gov.educ.graddatacollection.api.constants.v1;

import lombok.Getter;


public enum FilesetStatus {
    DEM_LOADED("DEMLOADED"),
    XAM_LOADED("XAMLOADED"),
    CRS_LOADED("CRSLOADED"),
    DELETED("DELETED"),
    COMPLETED("COMPLETED"),
    LOADED("LOADED")
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
