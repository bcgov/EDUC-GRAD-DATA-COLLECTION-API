package ca.bc.gov.educ.graddatacollection.api.rules.demographic;

import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

import static ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode.*;

public enum DemographicStudentValidationIssueTypeCode {
  STUDENT_PEN_BLANK("STUDENTPENBLANK", "PEN is blank. Correct PEN in system or through PEN Web."),
  STUDENT_ADDRESS_BLANK("STUDENTADDRESSBLANK", "Missing student address."),
  STUDENT_CITY_BLANK("STUDENTCITYBLANK", "Missing student city."),
  STUDENT_POSTAL_CODE_INVALID("STUDENTPOSTALCODEINVALID", "Invalid postal code."),
  STUDENT_PROVINCE_CODE_INVALID("STUDENTPROVINCECODEINVALID", "Invalid Province code."),
  STUDENT_COUNTRY_CODE_INVALID("STUDENTCOUNTRYCODEINVALID", "Invalid Country code."),
  STUDENT_CITIZENSHIP_CODE_INVALID("STUDENTCITIZENSHIPCODEINVALID", "Invalid citizenship code - must be C, O or blank."),
  GRADE_INVALID("GRADEINVALID", "Invalid grade."),
  GRADE_NOT_IN_GRAD("GRADENOTINGRAD", "Is this the students' true grade?"),
  GRADE_AG_INVALID("GRADEAGINVALID", "Student reported on the Adult Graduation program (1950) must be grade AD or AN."),
  GRADE_OG_INVALID("GRADEOGINVALID", "Student grade should not be AD or AN for the reported graduation program."),
  STUDENT_STATUS_MERGED("STUDENTSTATUSMERGED", "Student PEN has been merged with a pre-existing PEN."),
  STUDENT_STATUS_INVALID("STUDENTSTATUSINVALID", "Invalid student status - must be A, D, or T."),
  SCCP_INVALID_DATE("SCCPINVALIDDATE", "Invalid SCCP completion date (YYYYMMDD)."),
  SCCP_INVALID_STUDENT_PROGRAM("SCCPINVALIDSTUDENTPROGRAM", "Student must be on the SCCP program. SCCP Completion date not updated."),
  STUDENT_BIRTHDATE_INVALID("STUDENTBIRTHDATEINVALID", "Student date of birth is not valid."),
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
