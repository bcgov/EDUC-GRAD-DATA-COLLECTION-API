package ca.bc.gov.educ.graddatacollection.api.rules.assessment;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum AssessmentStudentValidationIssueTypeCode {

  DEM_DATA_MISSING("DEM_DATA_MISSING", "The student's registration data cannot be processed because they are either missing from the DEM file or their Surname or Local ID in the XAM file does not match the DEM file."),
  COURSE_LEVEL_NOT_BLANK("COURSE_LEVEL_NOT_BLANK", "Course level must be blank. This registration cannot be updated."),
  COURSE_SESSION_INVALID("COURSE_SESSION_INVALID", "%s is not offered in %s%s. Check that a valid assessment code and session date, in the current school year, were reported. This registration cannot be updated."),
  COURSE_SESSION_INVALID_MONTH("COURSE_SESSION_INVALID_MONTH", "Assessments are not offered in the reported session month. Must be Nov, Jan, Apr, or Jun. The registration cannot be updated."),
  COURSE_SESSION_DUP("COURSE_SESSION_DUP", "The student has already received a Proficiency Score or Special Case for this assessment session. The registration cannot be updated."),
  COURSE_SESSION_EXCEED("COURSE_SESSION_EXCEED", "The student has reached the maximum number of writes for %s. The registration will not be updated."),
  INTERIM_SCHOOL_PERCENTAGE_NOT_BLANK("INTERIM_SCHOOL_PERCENTAGE_NOT_BLANK", "Interim school percentage must be blank. This registration cannot be updated."),
  INTERIM_LETTER_GRADE_NOT_BLANK("INTERIM_LETTER_GRADE_NOT_BLANK", "Interim letter grade must be blank. This registration cannot be updated."),
  FINAL_SCHOOL_PERCENTAGE_NOT_BLANK("FINAL_SCHOOL_PERCENTAGE_NOT_BLANK", "Final school percent must be blank. This registration cannot be updated."),
  FINAL_PERCENTAGE_NOT_BLANK("FINAL_PERCENTAGE_NOT_BLANK", "Final percentage result cannot be submitted by the school and must be blank. This registration cannot be updated."),
  FINAL_LETTER_GRADE_NOT_BLANK("FINAL_LETTER_GRADE_NOT_BLANK", "Final letter grade must be blank. This registration cannot be updated."),
  PROVINCIAL_SPECIAL_CASE_NOT_BLANK("PROVINCIAL_SPECIAL_CASE_NOT_BLANK", "Submitted special case %s was not awarded by the ministry. This registration cannot be updated."),
  COURSE_STATUS_INVALID("COURSE_STATUS_INVALID", "The submitted registration status %s is not a valid status. This registration cannot be updated."),
  NUMBER_OF_CREDITS_NOT_BLANK("NUMBER_OF_CREDITS_NOT_BLANK", "Number of credits must be blank. This registration cannot be updated."),
  COURSE_TYPE_NOT_BLANK("COURSE_TYPE_NOT_BLANK", "Course type must be blank. This registration cannot be updated."),
  TO_WRITE_FLAG_NOT_BLANK("TO_WRITE_FLAG_NOT_BLANK", "To write flag must be blank. This registration cannot be updated."),
  EXAM_SCHOOL_INVALID("EXAM_SCHOOL_INVALID", "Invalid assessment centre provided. This registration cannot be updated."),
  DUPLICATE_XAM_RECORD("DUPLICATE_XAM_RECORD", "Duplicate registration and session date reported for the same student in the XAM file. These registrations cannot be updated."),
  NUMERACY_DUPLICATE("NUMERACY_DUPLICATE", "Student has already been registered for a numeracy assessment for this session: %s. This registration cannot be updated."),
  COURSE_CODE_CSF("COURSE_CODE_CSF", "Student is enrolled in a Programme Francophone school and the registration is for a French Immersion assessment. This registration cannot be updated."),
  COURSE_CODE_NON_CSF("COURSE_CODE_NON_CSF", "LTP12 is a Programme Francophone assessment. Please use LTF12 if student is in French Immersion program."),
  COURSE_CODE_ATTEMPTS("COURSE_CODE_ATTEMPTS", "The student has reached the maximum number of attempts. The registration cannot be updated. For questions, please contact the Ministry at assessments@gov.bc.ca."),
  COURSE_STATUS_W_INVALID("COURSE_STATUS_W_INVALID", "The registration does not exist in the student record so it cannot be withdrawn."),
  COURSE_ALREADY_WRITTEN("COURSE_ALREADY_WRITTEN", "Assessment has been written by the student, withdrawal is not allowed."),
  DEM_ISSUE("DEM_ISSUE", "An error in the XAM file for this student is preventing the processing of their course data.");

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
