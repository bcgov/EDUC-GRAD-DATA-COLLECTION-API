package ca.bc.gov.educ.graddatacollection.api.rules.demographic;

import lombok.Getter;

public enum DemographicStudentValidationFieldCode {
  STUDENT_PEN("STUDENT_PEN"),
  STUDENT_CITIZENSHIP_CODE("STUDENT_CITIZENSHIP_CODE"),
  STUDENT_GRADE("STUDENT_GRADE"),
  STUDENT_STATUS("STUDENT_STATUS"),
  SCCP_COMPLETION_DATE("SCCP_COMPLETION_DATE"),
  STUDENT_BIRTHDATE("STUDENT_BIRTHDATE"),
  ;

  @Getter
  private final String code;

  DemographicStudentValidationFieldCode(String code) {
    this.code = code;
  }
}
