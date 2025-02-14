package ca.bc.gov.educ.graddatacollection.api.rules.course;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum CourseStudentValidationIssueTypeCode {

  DEM_DATA_MISSING("DEM_DATA_MISSING", "This student appears in the CRS file but is missing from the DEM file. No course records for this student will be updated."),
  DEM_ISSUE("DEM_ISSUE", "Student CRS record will not be processed due to an issue with the student's demographics."),
  COURSE_STATUS_INVALID("COURSE_STATUS_INVALID", "Course status must be A=active or W=withdraw."),
  COURSE_CODE_COREG_TRAX_INVALID("COURSE_CODE_COREG_TRAX_INVALID", "The submitted course code does not exist in the ministry course registry. This course cannot be updated."),
  COURSE_CODE_COREG_MYEDBC_INVALID("COURSE_CODE_COREG_MYEDBC_INVALID", "The submitted course code is a local course code, not a ministry code. This course cannot be updated."),
  COURSE_SESSION_DUPLICATE("COURSE_SESSION_DUPLICATE", "Duplicate course and session date reported for the same student. These courses will not be updated."),
  COURSE_SESSION_INVALID("COURSE_SESSION_INVALID", "Course session is too far into the future (next year reporting cycle) or too far in the past. This course will not be updated."),
  COURSE_MONTH_INVALID("COURSE_MONTH_INVALID", "Course month must be between 01 and 12 (January to December). This course will not be updated."),
  COURSE_YEAR_INVALID("COURSE_YEAR_INVALID", "Course year must be four digits. This course will not be updated."),
  COURSE_SESSION_START_DATE_INVALID("COURSE_SESSION_START_DATE_INVALID", "The school is reporting a student enrolled in a course at time when the course was not open (i.e., course session date is before the course open date)."),
  COURSE_SESSION_COMPLETION_END_DATE_INVALID("COURSE_SESSION_COMPLETION_END_DATE_INVALID", "The school is reporting student activity in a course at a time when the course was not open (i.e., course session date is after the completion end date). This course will not be updated."),
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
  NUMBER_OF_CREDITS_INVALID("NUMBER_OF_CREDITS_INVALID", "The number of credits reported for the course is not an allowable credit value in the Course Registry. This course will not be updated."),
  EQUIVALENCY_CHALLENGE_CODE_INVALID("EQUIVALENCY_CHALLENGE_CODE_INVALID", "Invalid entry, the reported value will be ignored. Report E or C or leave blank. This course will not be updated."),
  GRADUATION_REQUIREMENT_INVALID("GRADUATION_REQUIREMENT_INVALID", "Invalid entry. Values not applicable for students on the 1986 program. This course will not be updated."),
  GRAD_REQT_FINE_ARTS_APPLIED_SKILLS_1996_GRAD_PROG_INVALID("GRAD_REQT_FINE_ARTS_APPLIED_SKILLS_1996_GRAD_PROG_INVALID", "Invalid entry. Values only applicable for Board Authority Authorized or Locally Developed courses for students on the 1996 program. This course will not be updated."),
  GRAD_REQT_FINE_ARTS_APPLIED_SKILLS_2004_2018_2023_GRAD_PROG_INVALID("GRAD_REQT_FINE_ARTS_APPLIED_SKILLS_2004_2018_2023_GRAD_PROG_INVALID", ""),
  INVALID_FINE_ARTS_APPLIED_SKILLS_CODE("INVALID_FINE_ARTS_APPLIED_SKILLS_CODE", "Invalid Fine Arts/Applied Skills code.  Must be one of A, F or B."),
  GRADUATION_REQUIREMENT_NUMBER_CREDITS_INVALID("GRADUATION_REQUIREMENT_NUMBER_CREDITS_INVALID", "Invalid entry. Number of credits must be 4 where B reported for a Board Authority Authorized or Locally Developed course for a student on the 1996 program. This course will not be updated."),
  COURSE_NOT_INDEPENDENT_DIRECTED_STUDIES("COURSE_NOT_INDEPENDENT_DIRECTED_STUDIES", "Invalid entry. A related course code and level can only be applied to an Independent Directed Studies course. This course will not be updated."),
  RELATED_COURSE_RELATED_LEVEL_INVALID("RELATED_COURSE_RELATED_LEVEL_INVALID", "Invalid related course code used for the Independent Directed Studies course. Please check the Course Registry. This course will not be updated."),
  RELATED_COURSE_RELATED_LEVEL_MISSING_FOR_INDY("RELATED_COURSE_RELATED_LEVEL_MISSING_FOR_INDY", "Related course code and level missing for Independent Directed Studies course. This course will not be updated.")
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
