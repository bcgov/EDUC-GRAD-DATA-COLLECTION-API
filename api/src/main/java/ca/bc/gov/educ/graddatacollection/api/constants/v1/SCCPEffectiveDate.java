package ca.bc.gov.educ.graddatacollection.api.constants.v1;

import lombok.Getter;

@Getter
public enum SCCPEffectiveDate {
    SCCP_EFFECTIVE_DATE("19930701");

    private final String date;

    SCCPEffectiveDate(String date) {
        this.date = date;
    }
}