package ca.bc.gov.educ.graddatacollection.api.constants.v1;

import lombok.Getter;

/**
 * The enum for school category codes
 */
@Getter
public enum CourseStatusCodes {
    ACTIVE("A"),
    WITHDRAWN("W");

    private final String code;
    CourseStatusCodes(String code) { this.code = code; }
}
