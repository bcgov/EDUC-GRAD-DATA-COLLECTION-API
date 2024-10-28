package ca.bc.gov.educ.graddatacollection.api.rules.demographic;

import lombok.Getter;

public enum DemographicStudentValidationFieldCode {
  TX_ID("TX_ID"),
  STUDENT_GRADE("STUDENT_GRADE"),
  STUDENT_STATUS("STUDENT_STATUS"),
  SCCP_COMPLETION_DATE("SCCP_COMPLETION_DATE"),
  ;

  @Getter
  private final String code;

  DemographicStudentValidationFieldCode(String code) {
    this.code = code;
  }
}
