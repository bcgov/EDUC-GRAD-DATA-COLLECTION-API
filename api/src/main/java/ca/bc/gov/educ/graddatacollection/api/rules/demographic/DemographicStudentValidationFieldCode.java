package ca.bc.gov.educ.graddatacollection.api.rules.demographic;

import lombok.Getter;

public enum DemographicStudentValidationFieldCode {
  STUDENT_PEN("STUDENT_PEN"),
  STUDENT_ADDRESS("STUDENT_ADDRESS"),
  STUDENT_CITIZENSHIP_CODE("STUDENT_CITIZENSHIP_CODE"),
  SCCP_COMPLETION_DATE("SCCP_COMPLETION_DATE"),
  ;

  @Getter
  private final String code;

  DemographicStudentValidationFieldCode(String code) {
    this.code = code;
  }
}
