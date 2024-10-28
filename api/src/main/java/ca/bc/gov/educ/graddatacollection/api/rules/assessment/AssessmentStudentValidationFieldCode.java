package ca.bc.gov.educ.graddatacollection.api.rules.assessment;

import lombok.Getter;

public enum AssessmentStudentValidationFieldCode {
  PEN("PEN"),
  COURSE_LEVEL("COURSE_LEVEL");

  @Getter
  private final String code;

  AssessmentStudentValidationFieldCode(String code) {
    this.code = code;
  }
}
