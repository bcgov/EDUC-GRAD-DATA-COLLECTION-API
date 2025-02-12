package ca.bc.gov.educ.graddatacollection.api.constants.v1;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


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
