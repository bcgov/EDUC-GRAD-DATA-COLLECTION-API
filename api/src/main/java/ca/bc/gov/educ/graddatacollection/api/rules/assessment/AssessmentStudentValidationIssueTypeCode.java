package ca.bc.gov.educ.graddatacollection.api.rules.assessment;

import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

import static ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode.ERROR;

public enum AssessmentStudentValidationIssueTypeCode {

  TXID_INVALID("TXIDINVALID", "TX_ID must be one of 'E06' or 'D06'", ERROR);

  private static final Map<String, AssessmentStudentValidationIssueTypeCode> CODE_MAP = new HashMap<>();

  static {
    for (AssessmentStudentValidationIssueTypeCode type : values()) {
      CODE_MAP.put(type.getCode(), type);
    }
  }

  @Getter
  private final String code;

  @Getter
  private final String message;

  @Getter
  private final StudentValidationIssueSeverityCode severityCode;

  AssessmentStudentValidationIssueTypeCode(String code, String message, StudentValidationIssueSeverityCode severityCode) {
    this.code = code;
    this.message = message;
    this.severityCode = severityCode;
  }
  public static AssessmentStudentValidationIssueTypeCode findByValue(String value) {
    return CODE_MAP.get(value);
  }
}
