package ca.bc.gov.educ.graddatacollection.api.constants.v1;

import lombok.Getter;


public enum ErrorFilesetValidationIssueType {
    DEMOGRAPHICS("DEMOGRAPHICS"),
    ASSESSMENT("ASSESSMENT"),
    COURSE("COURSE");

    @Getter
    private final String code;

    ErrorFilesetValidationIssueType(final String code) {
        this.code = code;
    }
}
