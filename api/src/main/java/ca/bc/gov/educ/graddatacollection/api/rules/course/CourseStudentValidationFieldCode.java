package ca.bc.gov.educ.graddatacollection.api.rules.course;

import lombok.Getter;

public enum CourseStudentValidationFieldCode {
  PEN("PEN"),
  COURSE_STATUS("COURSE_STATUS"),
  COURSE_SESSION("COURSE_SESSION"),
  COURSE_MONTH("COURSE_MONTH"),
  INTERIM_PCT("INTERIM_PCT"),
  INTERIM_LETTER_GRADE("INTERIM_LETTER_GRADE"),
  INTERIM_LETTER_GRADE_PERCENTAGE("INTERIM_LETTER_GRADE_PERCENTAGE"),
  FINAL_PCT("FINAL_PCT"),
  FINAL_LETTER_GRADE("FINAL_LETTER_GRADE"),
  FINAL_LETTER_GRADE_PERCENTAGE("FINAL_LETTER_GRADE_PERCENTAGE"),
  EQUIVALENCY_CHALLENGE("EQUIVALENCY_CHALLENGE"),
  GRADUATION_REQUIREMENT("GRADUATION_REQUIREMENT"),
  ;

  @Getter
  private final String code;

  CourseStudentValidationFieldCode(String code) {
    this.code = code;
  }
}
