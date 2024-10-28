package ca.bc.gov.educ.graddatacollection.api.rules.demographic;

import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

import static ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode.*;

public enum DemographicStudentValidationIssueTypeCode {

  STUDENT_CITIZENSHIP_CODE_INVALID("STUDENTCITIZENSHIPCODEINVALID", "Invalid citizenship code - must be C, O or blank.", ERROR),
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
