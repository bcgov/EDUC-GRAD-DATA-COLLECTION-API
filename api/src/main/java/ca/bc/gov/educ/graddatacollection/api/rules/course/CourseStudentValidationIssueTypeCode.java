package ca.bc.gov.educ.graddatacollection.api.rules.course;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum CourseStudentValidationIssueTypeCode {

  DEM_DATA_MISSING("DEM_DATA_MISSING", "This student is missing demographic data based on Student PEN, Surname and Local ID.");

  private static final Map<String, CourseStudentValidationIssueTypeCode> CODE_MAP = new HashMap<>();

  static {
    for (CourseStudentValidationIssueTypeCode type : values()) {
      CODE_MAP.put(type.getCode(), type);
    }
  }

  @Getter
  private final String code;

  @Getter
  private final String message;

  CourseStudentValidationIssueTypeCode(String code, String message) {
    this.code = code;
    this.message = message;
  }
  public static CourseStudentValidationIssueTypeCode findByValue(String value) {
    return CODE_MAP.get(value);
  }
}
