package ca.bc.gov.educ.graddatacollection.api.rules.demographic;

import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

import static ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode.*;

public enum DemographicStudentValidationIssueTypeCode {
  STUDENT_PEN_BLANK("STUDENTPENBLANK", "PEN is blank. Correct PEN in system or through PEN Web.", ERROR),
  STUDENT_ADDRESS_BLANK("STUDENTADDRESSBLANK", "Missing student address.", WARNING),
  STUDENT_CITY_BLANK("STUDENTCITYBLANK", "Missing student city.", WARNING),
  STUDENT_POSTAL_CODE_INVALID("STUDENTPOSTALCODEINVALID", "Invalid postal code.", WARNING),
  STUDENT_PROVINCE_CODE_INVALID("STUDENTPROVINCECODEINVALID", "Invalid Province code.", WARNING),
  STUDENT_COUNTRY_CODE_INVALID("STUDENTCOUNTRYCODEINVALID", "Invalid Country code.", WARNING),
  STUDENT_CITIZENSHIP_CODE_INVALID("STUDENTCITIZENSHIPCODEINVALID", "Invalid citizenship code - must be C, O or blank.", ERROR),
  GRADE_INVALID("GRADEINVALID", "Invalid grade.", ERROR),
  GRADE_NOT_IN_GRAD("GRADENOTINGRAD", "Is this the students' true grade?", WARNING),
  GRADE_AG_INVALID("GRADEAGINVALID", "Student reported on the Adult Graduation program (1950) must be grade AD or AN.", WARNING),
  GRADE_OG_INVALID("GRADEOGINVALID", "Student grade should not be AD or AN for the reported graduation program.", WARNING),
  STUDENT_STATUS_MERGED("STUDENTSTATUSMERGED", "Student PEN has been merged with a pre-existing PEN.", ERROR),
  STUDENT_STATUS_INVALID("STUDENTSTATUSINVALID", "Invalid student status - must be A, D, or T.", ERROR),
  SCCP_INVALID_DATE("SCCPINVALIDDATE", "Invalid SCCP completion date (YYYYMMDD).", ERROR),
  SCCP_INVALID_STUDENT_PROGRAM("SCCPINVALIDSTUDENTPROGRAM", "Student must be on the SCCP program. SCCP Completion date not updated.", WARNING),
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

  @Getter
  private final StudentValidationIssueSeverityCode severityCode;

  DemographicStudentValidationIssueTypeCode(String code, String message, StudentValidationIssueSeverityCode severityCode) {
    this.code = code;
    this.message = message;
    this.severityCode = severityCode;
  }
  public static DemographicStudentValidationIssueTypeCode findByValue(String value) {
    return CODE_MAP.get(value);
  }
}
