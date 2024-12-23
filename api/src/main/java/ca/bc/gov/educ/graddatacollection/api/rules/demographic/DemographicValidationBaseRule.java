package ca.bc.gov.educ.graddatacollection.api.rules.demographic;

import ca.bc.gov.educ.graddatacollection.api.rules.Rule;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface DemographicValidationBaseRule extends Rule<StudentRuleData, DemographicStudentValidationIssue> {
  default DemographicStudentValidationIssue createValidationIssue(StudentValidationIssueSeverityCode severityCode, DemographicStudentValidationFieldCode fieldCode, DemographicStudentValidationIssueTypeCode typeCode){
    DemographicStudentValidationIssue sdcSchoolCollectionStudentValidationIssue = new DemographicStudentValidationIssue();
    sdcSchoolCollectionStudentValidationIssue.setValidationIssueSeverityCode(severityCode.toString());
    sdcSchoolCollectionStudentValidationIssue.setValidationIssueCode(typeCode.getCode());
    sdcSchoolCollectionStudentValidationIssue.setValidationIssueFieldCode(fieldCode.getCode());
    return sdcSchoolCollectionStudentValidationIssue;
  }

  default boolean isValidationDependencyResolved(String fieldName, List<DemographicStudentValidationIssue> validationErrorsMap) {
    Optional<DemographicValidationRulesDependencyMatrix> errorCodesToCheck = DemographicValidationRulesDependencyMatrix.findByValue(fieldName);
    if(errorCodesToCheck.isPresent()) {
      String[] errorCodes = errorCodesToCheck.get().getBaseRuleErrorCode();
      return validationErrorsMap.stream().noneMatch(val -> Arrays.stream(errorCodes).anyMatch(val.getValidationIssueCode()::contentEquals));
    }
    return false;
  }
}
