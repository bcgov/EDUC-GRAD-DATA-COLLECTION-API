package ca.bc.gov.educ.graddatacollection.api.rules.assessment;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum AssessmentStudentValidationIssueTypeCode {

  DEM_DATA_MISSING("DEM_DATA_MISSING", "This student appears in the XAM file but is missing from the DEM file."),
  COURSE_LEVEL_NOT_BLANK("COURSE_LEVEL_NOT_BLANK", "Course level value is ignored and must be blank."),
  COURSE_SESSION_INVALID("COURSE_SESSION_INVALID", "The session code is not a valid ministry assessment session code. The student registration will not be updated."),
  COURSE_SESSION_INVALID_MONTH("COURSE_SESSION_INVALID_MONTH", "The session date is not a valid ministry assessment session. Must be November, January, April or June. The student registration will not be updated."),
  COURSE_SESSION_DUP("COURSE_SESSION_DUP", "The student has already received a Proficiency Score or Special Case for this assessment session."),
  COURSE_SESSION_EXCEED("COURSE_SESSION_EXCEED", "The student has reached the maximum number of writes for %s. The registration will not be updated."),
  INTERIM_SCHOOL_PERCENTAGE_NOT_BLANK("INTERIM_SCHOOL_PERCENTAGE_NOT_BLANK", "Interim school percentage value is ignored and must be blank."),
  INTERIM_LETTER_GRADE_NOT_BLANK("INTERIM_LETTER_GRADE_NOT_BLANK", "Interim letter grade value is ignored and must be blank."),
  FINAL_SCHOOL_PERCENTAGE_NOT_BLANK("FINAL_SCHOOL_PERCENTAGE_NOT_BLANK", "Final school percentage value is ignored and must be blank."),
  FINAL_PERCENTAGE_NOT_BLANK("FINAL_PERCENTAGE_NOT_BLANK", "Final percentage result cannot be submitted by the school."),
  FINAL_LETTER_GRADE_NOT_BLANK("FINAL_LETTER_GRADE_NOT_BLANK", "Final letter grade value is ignored and must be blank."),
  PROVINCIAL_SPECIAL_CASE_NOT_BLANK("PROVINCIAL_SPECIAL_CASE_NOT_BLANK", "Provincial special case cannot be submitted by the school."),
  COURSE_STATUS_INVALID("COURSE_STATUS_INVALID", "Assessment registration must be A=active or W=withdraw."),
  NUMBER_OF_CREDITS_NOT_BLANK("NUMBER_OF_CREDITS_NOT_BLANK", "Number of credits value is ignored and must be blank."),
  COURSE_TYPE_NOT_BLANK("COURSE_TYPE_NOT_BLANK", "Course Type value is ignored and must be blank."),
  TO_WRITE_FLAG_NOT_BLANK("TO_WRITE_FLAG_NOT_BLANK", "To write flag value is ignored and must be blank."),
  EXAM_SCHOOL_INVALID("EXAM_SCHOOL_INVALID", "Invalid assessment center provided."),
  DUPLICATE_XAM_RECORD("DUPLICATE_XAM_RECORD", "There are more than one CODE/SESSION DATE registrations in the file for the same student."),
  COURSE_CODE_CSF("COURSE_CODE_CSF", "Student is enrolled in a Programme Francophone school and the registration is for a French Immersion assessment. This registration will not be updated."),
  COURSE_CODE_NON_CSF("COURSE_CODE_NON_CSF", "LTP12 is a Programme Francophone assessment. Please use LTF12 if student is in French Immersion program."),
  COURSE_CODE_ATTEMPTS("COURSE_CODE_ATTEMPTS", "The student has reached the maximum number of writes for <Assmt CODE>. The registration will not be updated."),
  COURSE_ALREADY_WRITTEN("COURSE_ALREADY_WRITTEN", "Assessment has been written by the student, withdrawal is not allowed."),
  DEM_ISSUE("DEM_ISSUE", "Student XAM record will not be processed due to an issue with the student's demographics.");

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
