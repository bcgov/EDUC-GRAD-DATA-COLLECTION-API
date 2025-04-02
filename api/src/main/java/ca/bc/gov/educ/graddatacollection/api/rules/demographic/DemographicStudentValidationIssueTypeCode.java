package ca.bc.gov.educ.graddatacollection.api.rules.demographic;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum DemographicStudentValidationIssueTypeCode {
  STUDENT_LOCAL_ID_MISMATCH("STUDENTLOCALIDMISMATCH", "The submitted STUDENT LOCAL ID does not match the ministry database. If the submitted STUDENT LOCAL ID is correct, submit PEN update request through Secure Messaging Inbox in EDX."),
  STUDENT_PEN_NOT_FOUND("STUDENTPENNOTFOUND", "Student PEN is not valid so the student record cannot be updated. Use the correct PEN."),
  STUDENT_PEN_MISMATCH("STUDENTPENMISMATCH", "Student in DEM file but not in CRS file (i.e., course data has not been submitted for this student)."),
  STUDENT_SURNAME_MISMATCH("STUDENTSURNAMEMISMATCH", "The submitted SURNAME does not match the ministry database. If the submitted SURNAME is correct, submit PEN update request through the Secure Messaging Inbox in the Education Data Exchange (EDX) https://educationdataexchange.gov.bc.ca/login"),
  STUDENT_MIDDLE_MISMATCH("STUDENTMIDDLEMISMATCH", "The submitted MIDDLE NAME does not match the ministry database. If the submitted MIDDLE NAME is correct, submit PEN update request the Secure Messaging Inbox in the Education Data Exchange (EDX) https://educationdataexchange.gov.bc.ca/login"),
  STUDENT_GIVEN_MISMATCH("STUDENTGIVENMISMATCH", "The submitted FIRST NAME does not match the ministry database. If the submitted FIRST NAME is correct, submit PEN update request through the Secure Messaging Inbox in the Education Data Exchange (EDX) https://educationdataexchange.gov.bc.ca/login"),
  STUDENT_BIRTHDATE_MISMATCH("STUDENTBIRTHDATEMISMATCH", "The submitted BIRTHDATE does not match the ministry database. If the submitted BIRTHDATE is correct, submit PEN update request through the Secure Messaging Inbox in the Education Data Exchange (EDX) https://educationdataexchange.gov.bc.ca/login"),
  STUDENT_ADDRESS_BLANK("STUDENTADDRESSBLANK", "Missing student address."),
  STUDENT_CITY_BLANK("STUDENTCITYBLANK", "Missing student city."),
  STUDENT_POSTAL_CODE_INVALID("STUDENTPOSTALCODEINVALID", "Invalid postal code."),
  STUDENT_PROVINCE_CODE_INVALID("STUDENTPROVINCECODEINVALID", "Invalid Province code."),
  STUDENT_COUNTRY_CODE_INVALID("STUDENTCOUNTRYCODEINVALID", "Invalid Country code."),
  STUDENT_CITIZENSHIP_CODE_INVALID("STUDENTCITIZENSHIPCODEINVALID", "Invalid citizenship code - must be C, O or blank."),
  GRADE_INVALID("GRADEINVALID", "Invalid student grade level."),
  GRADE_NOT_EXPECTED("GRADENOTEXPECTED", "An unexpected grade level has been submitted for the student."),
  GRADE_AG_INVALID("GRADEAGINVALID", "Grade and program mismatch. Student cannot be reported on the Adult Graduation program (1950) unless grade level is AD or AN."),
  GRADE_OG_INVALID("GRADEOGINVALID", "Grade and program mismatch. Student cannot be reported in grade level AD or AN unless they are on the Adult Graduation program (1950)."),
  STUDENT_STATUS_MERGED("STUDENTSTATUSMERGED", "Student PEN has been merged with a pre-existing PEN. Ensure the valid, pre-existing PEN appears in system data file extracts."),
  STUDENT_STATUS_INVALID("STUDENTSTATUSINVALID", "Invalid student status - must be A, D, or T."),
  STUDENT_STATUS_PEN_MISMATCH("STUDENTSTATUSPENMISMATCH", "The submitted STUDENT STATUS does not match the ministry database. If the submitted STUDENT STATUS is correct, submit PEN update request through the Secure Messaging Inbox in the Education Data Exchange (EDX) https://educationdataexchange.gov.bc.ca/login."),
  STUDENT_STATUS_SCHOOL_OF_RECORD_MISMATCH("STUDENTSTATUSSCHOOLOFRECORDMISMATCH", "Student cannot be reported as T = terminated because the reporting school does not match the student's current school in the GRAD system."),
  STUDENT_STATUS_INCORRECT_NEW_STUDENT("STUDENTSTATUSINCORRECTNEWSTUDENT", "Student is being submitted with status of %s"),
  SCCP_INVALID_DATE("SCCPINVALIDDATE", "Invalid SCCP completion date. Must be blank or YYYYMMDD."),
  SCCP_INVALID_STUDENT_PROGRAM("SCCPINVALIDSTUDENTPROGRAM", "Student is not on the SCCP program. The submitted completion date will not be updated."),
  STUDENT_BIRTHDATE_INVALID("STUDENTBIRTHDATEINVALID", "The student date of birth is not a valid date. The format must be YYYYMMDD"),
  STUDENT_BIRTHDATE_ADULT("STUDENTBIRTHDATEADULT", "Student cannot start the Adult Graduation Program before they are 18 years old."),
  STUDENT_PROGRAM_CODE_INVALID("STUDENTPROGRAMCODEINVALID", "Invalid program code."),
  STUDENT_PROGRAM_SCHOOL_CATEGORY_CODE_INVALID("STUDENTPROGRAMSCHOOLCATEGORYCODEINVALID", "1950 and SCCP are not valid program codes for offshore schools."),
  STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID("STUDENTPROGRAMGRADREQUIREMENTYEARINVALID", "Invalid graduation program code."),
  STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL("STUDENTPROGRAMGRADREQUIREMENTYEARNULL", "Student must be reported on a graduation program. No information in student's DEM file will be processed."),
  STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_PROGRAM_CLOSED("STUDENTPROGRAMGRADREQUIREMENTYEARPROGRAMCLOSED", "Reported graduation program is closed. Students will not be able to graduate on this program."),
  STUDENT_PROGRAM_ALREADY_GRADUATED("STUDENTPROGRAMALREADYGRADUATED", "The student graduated on GRAD_REQT_YEAR so the program code cannot be changed. The student's DEM file will not be processed.")
  ;

  private static final Map<String, DemographicStudentValidationIssueTypeCode> CODE_MAP = new HashMap<>();

  static {
    for (DemographicStudentValidationIssueTypeCode type : values()) {
      CODE_MAP.put(type.getCode(), type);
    }
  }

  @Getter
  private final String code;

  @Getter
  private final String message;

  DemographicStudentValidationIssueTypeCode(String code, String message) {
    this.code = code;
    this.message = message;
  }
  public static DemographicStudentValidationIssueTypeCode findByValue(String value) {
    return CODE_MAP.get(value);
  }
}
