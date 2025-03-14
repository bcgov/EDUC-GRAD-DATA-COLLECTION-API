package ca.bc.gov.educ.graddatacollection.api.constants.v1.reports;

import lombok.Getter;

@Getter
public enum ErrorReportHeaders {

    PEN("PEN"),
    LOCAL_ID("Local ID"),
    LAST_NAME("Last Name"),
    FIRST_NAME("First Name"),
    DATE_OF_BIRTH("Date of Birth"),
    FILE_TYPE("File Type"),
    SEVERITY_CODE("Severity Code"),
    ERROR_CONTEXT("Error Context"),
    FIELD("Field"),
    DESCRIPTION("Description");

    private final String code;
    ErrorReportHeaders(String code) { this.code = code; }
}
