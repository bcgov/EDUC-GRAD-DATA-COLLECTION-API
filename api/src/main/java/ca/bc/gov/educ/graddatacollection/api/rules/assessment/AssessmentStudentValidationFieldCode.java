package ca.bc.gov.educ.graddatacollection.api.rules.assessment;

import lombok.Getter;

public enum AssessmentStudentValidationFieldCode {
  PEN("PEN"),
  COURSE_CODE("COURSE_CODE");

  @Getter
  private final String code;

  AssessmentStudentValidationFieldCode(String code) {
    this.code = code;
  }
}
