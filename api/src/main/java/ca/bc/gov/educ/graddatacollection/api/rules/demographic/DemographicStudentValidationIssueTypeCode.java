package ca.bc.gov.educ.graddatacollection.api.rules.demographic;

import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

import static ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode.ERROR;
import static ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode.INFO_WARNING;

public enum DemographicStudentValidationIssueTypeCode {

  TXID_INVALID("TXIDINVALID", "TX_ID must be one of 'D02' or 'E02'", ERROR),
  GRADE_INVALID("GRADEINVALID", "Invalid grade.", ERROR),
  GRADE_NOT_IN_GRAD("GRADENOTINGRAD", "Is this the students' true grade?", INFO_WARNING),
  GRADE_AG_INVALID("GRADEAGINVALID", "Student reported on the Adult Graduation program (1950) must be grade AD or AN.", INFO_WARNING),
  GRADE_OG_INVALID("GRADEOGINVALID", "Student grade should not be AD or AN for the reported graduation program.", INFO_WARNING),
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
