package ca.bc.gov.educ.graddatacollection.api.rules.demographic;

import lombok.Getter;

public enum DemographicStudentValidationFieldCode {
  TX_ID("TX_ID");

  @Getter
  private final String code;

  DemographicStudentValidationFieldCode(String code) {
    this.code = code;
  }
}
