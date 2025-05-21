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
  STUDENT_CITIZENSHIP_CODE_INVALID("STUDENTCITIZENSHIPCODEINVALID", "The submitted value %s is not an allowable value, per the current GRAD file specification."),
  GRADE_INVALID("GRADEINVALID", "The submitted value %s is not an allowable value, per the current GRAD file specification."),
  GRADE_NOT_EXPECTED("GRADENOTEXPECTED", "The submitted value %s is not an expected grade level."),
  GRADE_AG_INVALID("GRADEAGINVALID", "Grade and program mismatch. Student cannot be reported on the Adult Graduation program (1950) unless grade level is AD or AN."),
  GRADE_OG_INVALID("GRADEOGINVALID", "Grade and program mismatch. Student cannot be reported in grade level AD or AN unless they are on the Adult Graduation program (1950)."),
  STUDENT_STATUS_MERGED("STUDENTSTATUSMERGED", "Student PEN has been merged with a pre-existing PEN. Ensure the valid, pre-existing PEN appears in system data file extracts."),
  STUDENT_STATUS_INVALID("STUDENTSTATUSINVALID", "The submitted value %s is not an allowable value, per the current GRAD file specification."),
  STUDENT_STATUS_PEN_MISMATCH("STUDENTSTATUSPENMISMATCH", "The submitted STUDENT STATUS does not match the ministry database. If the submitted STUDENT STATUS is correct, submit PEN update request through the Secure Messaging Inbox in the Education Data Exchange (EDX) https://educationdataexchange.gov.bc.ca/login."),
  STUDENT_STATUS_NOT_CURRENT_IN_GRAD("STUDENTSTATUSNOTCURRENTINGRAD", "This student cannot be withdrawn (submitted with a status of T) as the student is not a current student in the GRAD system."),
  STUDENT_STATUS_SCHOOL_OF_RECORD_MISMATCH("STUDENTSTATUSSCHOOLOFRECORDMISMATCH", "This student cannot be withdrawn (submitted with a status of T) as the reporting school does not match the student's current school in the GRAD system."),
  STUDENT_STATUS_INCORRECT_NEW_STUDENT("STUDENTSTATUSINCORRECTNEWSTUDENT", "This student cannot be withdrawn (submitted with a status of T) as the student does not yet exist in GRAD."),
  SCCP_INVALID_DATE("SCCPINVALIDDATE", "The submitted SCCP Completion Date %s is not a valid date. The format must be YYYYMMDD."),
  SCCP_DATE_TOO_EARLY("SCCPDATETOOEARLY", "The submitted SCCP Completion Date %s date cannot be before 1993/07/01."),
  SCCP_INVALID_STUDENT_PROGRAM("SCCPINVALIDSTUDENTPROGRAM", "Student is not on the SCCP program. The submitted completion date will not be updated."),
  SCCP_INVALID_STUDENT_PROGRAM_ALREADY_REPORTED("SCCPINVALIDALREADYREPORTED", "The student has already been reported as completing SCCP in %s. This date will not be updated. Please contact student.certification@gov.bc.ca for assistance."),
  STUDENT_BIRTHDATE_INVALID("STUDENTBIRTHDATEINVALID", "The submitted birthdate %s is not a valid date. The format must be YYYYMMDD."),
  STUDENT_BIRTHDATE_ADULT("STUDENTBIRTHDATEADULT", "Student cannot start the Adult Graduation Program before they are 18 years old."),
  STUDENT_PROGRAM_CODE_INVALID("STUDENTPROGRAMCODEINVALID", "The submitted value %s is not an allowable value, per the current GRAD file specification."),
  STUDENT_PROGRAM_SCHOOL_CATEGORY_CODE_INVALID("STUDENTPROGRAMSCHOOLCATEGORYCODEINVALID", "1950 and SCCP are not valid program codes for offshore schools."),
  STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID("STUDENTPROGRAMGRADREQUIREMENTYEARINVALID", "The submitted value %s is not an allowable value, per the current GRAD file specification."),
  STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL("STUDENTPROGRAMGRADREQUIREMENTYEARNULL", "Student has been submitted with a blank graduation program. Student must be reported on a graduation program."),
  STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_PROGRAM_CLOSED("STUDENTPROGRAMGRADREQUIREMENTYEARPROGRAMCLOSED", "Reported graduation program is closed. Students will not be able to graduate on this program."),
  STUDENT_PROGRAM_ALREADY_GRADUATED("STUDENTPROGRAMALREADYGRADUATED", "The student has a %s completion date so the program code cannot be changed or removed.")
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
