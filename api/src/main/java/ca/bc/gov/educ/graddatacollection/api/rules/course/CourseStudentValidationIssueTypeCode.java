package ca.bc.gov.educ.graddatacollection.api.rules.course;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum CourseStudentValidationIssueTypeCode {

  DEM_DATA_MISSING("DEM_DATA_MISSING", "This student is missing demographic data based on Student PEN, Surname and Local ID."),
  DEM_ISSUE("DEM_ISSUE", "Student CRS record will not be processed due to an issue with the student's demographics."),
  COURSE_STATUS_INVALID("COURSE_STATUS_INVALID", "Course status must be A=active or W=withdraw."),
  COURSE_SESSION_INVALID("COURSE_SESSION_INVALID", "Course session is too far into the future (next year reporting cycle) or too far in the past. This course will not be updated."),
  COURSE_MONTH_INVALID("COURSE_MONTH_INVALID", "Course month must be between 01 and 12 (January to December). This course will not be updated."),
  INTERIM_PCT_INVALID("INTERIM_PCT_INVALID", "Interim percent range must be 0 to 100. This course will not be updated."),
  INTERIM_LETTER_GRADE_INVALID("INTERIM_LETTER_GRADE_INVALID", "Invalid letter grade. This course will not be updated."),
  INTERIM_LETTER_GRADE_PERCENTAGE_MISMATCH("INTERIM_LETTER_GRADE_PERCENTAGE_MISMATCH", "The interim percent does not fall within the required range for the reported letter grade. This course will not be updated."),
  FINAL_PCT_INVALID("FINAL_PCT_INVALID", "Final percent range must be 0 to 100. This course will not be updated."),
  FINAL_PCT_NOT_BLANK("FINAL_PCT_NOT_BLANK", "For course session dates prior to 199409 the final percent must be blank. This course will not be updated."),
  FINAL_LETTER_GRADE_INVALID("FINAL_LETTER_GRADE_INVALID", "Invalid Letter Grade"),
  FINAL_LETTER_GRADE_PERCENTAGE_MISMATCH("FINAL_LETTER_GRADE_PERCENTAGE_MISMATCH", "The final percent does not fall within the required range for the reported letter grade. This course will not be updated."),
  FINAL_LETTER_GRADE_RM("FINAL_LETTER_GRADE_RM", "RM can only be used for course codes GT or GTF. This course will not be updated."),
  FINAL_LETTER_GRADE_NOT_RM("FINAL_LETTER_GRADE_NOT_RM", "Invalid letter grade reported for course code GT or GTF. Use RM (Requirement Met). This course will not be updated."),
  FINAL_LETTER_GRADE_OR_PERCENT_NOT_BLANK("FINAL_LETTER_GRADE_OR_PERCENT_NOT_BLANK", "Final mark submitted but course session date is in the future. Change the course session date or remove the final mark. This course will not be updated."),
  FINAL_LETTER_GRADE_OR_PERCENT_BLANK("FINAL_LETTER_GRADE_OR_PERCENT_BLANK", "Course session has passed with no final mark. Report final mark or change the course session date. This course will not be updated."),
  FINAL_LETTER_GRADE_IE("FINAL_LETTER_GRADE_IE", "Course session date is more than 12 months old. Report final mark other than IE or update course session date if the course is still in progress."),
  EQUIVALENCY_CHALLENGE_CODE_INVALID("EQUIVALENCY_CHALLENGE_CODE_INVALID", "Invalid entry, the reported value will be ignored. Report E or C or leave blank. This course will not be updated."),
  GRADUATION_REQUIREMENT_INVALID("GRADUATION_REQUIREMENT_INVALID", "Invalid entry. Values not applicable for students on the 1986 program. This course will not be updated."),
  GRADUATION_REQUIREMENT_NUMBER_CREDITS_INVALID("GRADUATION_REQUIREMENT_NUMBER_CREDITS_INVALID", "Invalid entry. Number of credits must be 4 where B reported for a Board Authority Authorized or Locally Developed course for a student on the 1996 program. This course will not be updated.")
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
