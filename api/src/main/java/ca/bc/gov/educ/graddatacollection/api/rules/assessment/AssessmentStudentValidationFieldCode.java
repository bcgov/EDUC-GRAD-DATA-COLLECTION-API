package ca.bc.gov.educ.graddatacollection.api.rules.assessment;

import lombok.Getter;

public enum AssessmentStudentValidationFieldCode {
  TX_ID("TX_ID");

  @Getter
  private final String code;

  AssessmentStudentValidationFieldCode(String code) {
    this.code = code;
  }
}
