package ca.bc.gov.educ.graddatacollection.api.constants.v1;

import lombok.Getter;


@Getter
public enum CustomSearchType {
    DEMERROR("DEM-ERROR"),
    CRSERROR("CRS-ERROR"),
    XAMERROR("XAM-ERROR"),
    ERROR("ERROR"),
    WARNING("WARNING")
    ;


    private final String code;
    CustomSearchType(String code) {
        this.code = code;
    }
}
