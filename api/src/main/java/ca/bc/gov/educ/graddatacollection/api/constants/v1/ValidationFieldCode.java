package ca.bc.gov.educ.graddatacollection.api.constants.v1;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum ValidationFieldCode {
  PEN("PEN", "PEN"),
  COURSE_LEVEL("COURSE_LEVEL", "Course Level"),
  COURSE_CODE("COURSE_CODE", "Course Code"),
  COURSE_TYPE("COURSE_TYPE", "Course Type"),
  INTERIM_SCHOOL_PERCENT("INTERIM_SCHOOL_PERCENT", "Interim School Percentage"),
  INTERIM_LETTER_GRADE("INTERIM_LETTER_GRADE", "Interim Letter Grade"),
  FINAL_SCHOOL_PERCENT("FINAL_SCHOOL_PERCENT", "Final School Percent"),
  FINAL_LETTER_GRADE("FINAL_LETTER_GRADE", "Final Letter Grade"),
  PROVINCIAL_SPECIAL_CASE("PROVINCIAL_SPECIAL_CASE", "Provincial Special Case"),
  COURSE_STATUS("COURSE_STATUS", "Course Status"),
  NUM_CREDITS("NUM_CREDITS", "Number Credits"),
  CRSE_TYPE("CRSE_TYPE", "Course Type"),
  TO_WRITE_FLAG("TO_WRITE_FLAG", "To Write Flag"),
  EXAM_SCHOOL("EXAM_SCHOOL", "School of Exam"),
  COURSE_SESSION("COURSE_SESSION", "Course Session"),
  COURSE_MONTH("COURSE_MONTH", "Course Month"),
  COURSE_YEAR("COURSE_YEAR", "Course Year"),
  INTERIM_LETTER_GRADE_PERCENTAGE("INTERIM_LETTER_GRADE_PERCENTAGE", "Interim Letter Grade Percentage"),
  FINAL_LETTER_GRADE_PERCENTAGE("FINAL_LETTER_GRADE_PERCENTAGE", "Final Letter Grade Percentage"),
  EQUIVALENCY_CHALLENGE("EQUIVALENCY_CHALLENGE", "Equivalency Challenge"),
  GRADUATION_REQUIREMENT("GRADUATION_REQUIREMENT", "Graduation Requirement"),
  LOCAL_ID("LOCAL_ID", "Local ID"),
  IS_ELECTRONIC_EXAM("IS_ELECTRONIC_EXAM", "Electronic Exam"),
  LAST_NAME("LAST_NAME", "Last Name"),
  INTERIM_PERCENT("INTERIM_PERCENT", "Interim Percent"),
  INTERIM_PERCENTAGE("INTERIM_PERCENTAGE", "Interim Percentage"),
  INTERIM_GRADE("INTERIM_GRADE", "Interim Grade"),
  FINAL_PERCENTAGE("FINAL_PERCENTAGE", "Final Percentage"),
  FINAL_GRADE("FINAL_GRADE", "Final Grade"),
  NUMBER_OF_CREDITS("NUMBER_OF_CREDITS", "Number of Credits"),
  RELATED_COURSE("RELATED_COURSE", "Related Course"),
  RELATED_LEVEL("RELATED_LEVEL", "Related Level"),
  COURSE_DESCRIPTION("COURSE_DESCRIPTION", "Course Description"),
  COURSE_GRADUATION_REQUIREMENT("COURSE_GRADUATION_REQUIREMENT", "Course Graduation Requirement"),
  MIDDLE_NAME("MIDDLE_NAME", "Middle name"),
  FIRST_NAME("FIRST_NAME", "First Name"),
  ADDRESS1("ADDRESS1", "Address 1"),
  ADDRESS2("ADDRESS2", "Address 2"),
  CITY("CITY", "City"),
  PROVINCIAL_CODE("PROVINCIAL_CODE", "Provincial Code"),
  COUNTRY_CODE("COUNTRY_CODE", "Country Code"),
  POSTAL_CODE("POSTAL_CODE", "Postal Code"),
  BIRTHDATE("BIRTHDATE", "Birthdate"),
  GENDER("GENDER", "Gender"),
  CITIZENSHIP("CITIZENSHIP", "Citizenship"),
  GRADE("GRADE", "Grade"),
  PROGRAM_CODE_1("PROGRAM_CODE_1", "ProgramCode1"),
  PROGRAM_CODE_2("PROGRAM_CODE_2", "ProgramCode2"),
  PROGRAM_CODE_3("PROGRAM_CODE_3", "ProgramCode3"),
  PROGRAM_CODE_4("PROGRAM_CODE_4", "ProgramCode4"),
  PROGRAM_CODE_5("PROGRAM_CODE_5", "ProgramCode5"),
  PROGRAM_CADRE_FLAG("PROGRAM_CADRE_FLAG", "Program Cadre Flag"),
  GRAD_REQUIREMENT_YEAR("GRAD_REQUIREMENT_YEAR", "Grad Requirement Year"),
  SCHOOL_CERTIFICATE_COMPLETION_DATE("SCHOOL_CERTIFICATE_COMPLETION_DATE", "School Certificate Completion Date"),
  STUDENT_STATUS("STUDENT_STATUS", "Student Status")

  ;
  private final String code;
  private final String description;

  ValidationFieldCode(String code, String  description) {
    this.code = code;
    this.description = description;
  }

  public static Optional<ValidationFieldCode> findByCode(String fieldCode) {
    return Arrays.stream(values()).filter(field -> field.code.equalsIgnoreCase(fieldCode)).findFirst();
  }
}
