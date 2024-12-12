package ca.bc.gov.educ.graddatacollection.api.rules.course;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum CourseStudentValidationIssueTypeCode {

  DEM_DATA_MISSING("DEM_DATA_MISSING", "This student is missing demographic data based on Student PEN, Surname and Local ID."),
  DEM_ISSUE("DEM_ISSUE", "Student CRS record will not be processed due to an issue with the student's demographics."),
  COURSE_STATUS_INVALID("COURSE_STATUS_INVALID", "Course status must be A=active or W=withdraw."),
  COURSE_SESSION_INVALID("COURSE_SESSION_INVALID", "Course session is too far into the future (next year reporting cycle) or too far in the past. This course will not be updated."),
  INTERIM_PCT_INVALID("INTERIM_PCT_INVALID", "Interim percent range must be 0 to 100. This course will not be updated."),
  INTERIM_LETTER_GRADE_INVALID("INTERIM_LETTER_GRADE_INVALID", "Invalid letter grade. This course will not be updated."),
  INTERIM_LETTER_GRADE_PERCENTAGE_MISMATCH("INTERIM_LETTER_GRADE_PERCENTAGE_MISMATCH", "The interim percent does not fall within the required range for the reported letter grade. This course will not be updated."),
  FINAL_PCT_INVALID("FINAL_PCT_INVALID", "Final percent range must be 0 to 100. This course will not be updated."),
  FINAL_PCT_NOT_BLANK("FINAL_PCT_NOT_BLANK", "For course session dates prior to 199409 the final percent must be blank. This course will not be updated."),
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
