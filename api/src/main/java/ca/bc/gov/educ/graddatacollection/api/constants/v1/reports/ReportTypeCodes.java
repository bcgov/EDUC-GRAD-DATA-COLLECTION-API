package ca.bc.gov.educ.graddatacollection.api.constants.v1.reports;

import lombok.Getter;

@Getter
public enum ReportTypeCodes {
    STUDENT_ERROR_REPORT("student-error-report");

    private final String code;
    ReportTypeCodes(String code) { this.code = code; }

    }
