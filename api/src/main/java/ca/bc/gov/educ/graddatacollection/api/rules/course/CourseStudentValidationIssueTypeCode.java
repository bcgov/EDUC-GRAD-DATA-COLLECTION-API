package ca.bc.gov.educ.graddatacollection.api.rules.course;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum CourseStudentValidationIssueTypeCode {

  DEM_DATA_MISSING("DEM_DATA_MISSING", "This student is missing demographic data based on Student PEN, Surname and Local ID."),
  DEM_ISSUE("DEM_ISSUE", "Student CRS record will not be processed due to an issue with the student's demographics."),
  COURSE_STATUS_INVALID("COURSE_STATUS_INVALID", "Course status must be A=active or W=withdraw."),
  INTERIM_PCT_INVALID("INTERIM_PCT_INVALID", "Interim percent range must be 0 to 100. This course will not be updated.")
  ;

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
