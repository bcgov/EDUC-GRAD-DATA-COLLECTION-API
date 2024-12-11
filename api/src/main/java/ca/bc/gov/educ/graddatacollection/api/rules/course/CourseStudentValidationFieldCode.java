package ca.bc.gov.educ.graddatacollection.api.rules.course;

import lombok.Getter;

public enum CourseStudentValidationFieldCode {
  PEN("PEN"),
  COURSE_STATUS("COURSE_STATUS"),
  COURSE_SESSION("COURSE_SESSION"),
  INTERIM_PCT("INTERIM_PCT"),
  FINAL_PCT("FINAL_PCT"),
  ;

  @Getter
  private final String code;

  CourseStudentValidationFieldCode(String code) {
    this.code = code;
  }
}
