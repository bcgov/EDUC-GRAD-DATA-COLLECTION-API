package ca.bc.gov.educ.graddatacollection.api.rules.course;

import lombok.Getter;

public enum CourseStudentValidationFieldCode {
  PEN("PEN"),
  COURSE_STATUS("COURSE_STATUS"),
  COURSE_SESSION("COURSE_SESSION"),
  INTERIM_PCT("INTERIM_PCT"),
  INTERIM_LETTER_GRADE("INTERIM_LETTER_GRADE"),
  INTERIM_LETTER_GRADE_PERCENTAGE("INTERIM_LETTER_GRADE_PERCENTAGE"),
  FINAL_PCT("FINAL_PCT"),
  FINAL_LETTER_GRADE("FINAL_LETTER_GRADE"),
  FINAL_LETTER_GRADE_PERCENTAGE("FINAL_LETTER_GRADE_PERCENTAGE"),
  ;

  @Getter
  private final String code;

  CourseStudentValidationFieldCode(String code) {
    this.code = code;
  }
}
