package ca.bc.gov.educ.graddatacollection.api.rules.demographic;

import lombok.Getter;

public enum DemographicStudentValidationFieldCode {
  STUDENT_LOCAL_ID("STUDENT_LOCAL_ID"),
  STUDENT_PEN("STUDENT_PEN"),
  STUDENT_NAME("STUDENT_NAME"),
  STUDENT_ADDRESS("STUDENT_ADDRESS"),
  STUDENT_CITIZENSHIP_CODE("STUDENT_CITIZENSHIP_CODE"),
  STUDENT_GRADE("STUDENT_GRADE"),
  STUDENT_STATUS("STUDENT_STATUS"),
  SCCP_COMPLETION_DATE("SCCP_COMPLETION_DATE"),
  STUDENT_BIRTHDATE("STUDENT_BIRTHDATE"),
  STUDENT_PROGRAM_CODE("STUDENT_PROGRAM_CODE"),
  ;

  @Getter
  private final String code;

  DemographicStudentValidationFieldCode(String code) {
    this.code = code;
  }
}
