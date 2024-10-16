package ca.bc.gov.educ.graddatacollection.api.rules.course;

import lombok.Getter;

public enum CourseStudentValidationFieldCode {
  TX_ID("TX_ID");

  @Getter
  private final String code;

  CourseStudentValidationFieldCode(String code) {
    this.code = code;
  }
}
