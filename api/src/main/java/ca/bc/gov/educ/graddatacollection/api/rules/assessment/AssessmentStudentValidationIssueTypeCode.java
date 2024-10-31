package ca.bc.gov.educ.graddatacollection.api.rules.assessment;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum AssessmentStudentValidationIssueTypeCode {

  TXID_INVALID("TXID_INVALID", "TX_ID must be one of 'E06' or 'D06'."),
  DEM_DATA_MISSING("DEM_DATA_MISSING", "This student is missing demographic data based on Student PEN, Surname, Mincode and Local ID."),
  COURSE_LEVEL_NOT_BLANK("COURSE_LEVEL_NOT_BLANK", "Course level value is ignored and must be blank."),
  COURSE_CODE_INVALID("COURSE_CODE_INVALID", "The Assessment Code provided is not valid for the Assessment Session specified.");

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

  AssessmentStudentValidationIssueTypeCode(String code, String message) {
    this.code = code;
    this.message = message;
  }
  public static AssessmentStudentValidationIssueTypeCode findByValue(String value) {
    return CODE_MAP.get(value);
  }
}
