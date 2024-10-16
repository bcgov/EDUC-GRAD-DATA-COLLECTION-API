package ca.bc.gov.educ.graddatacollection.api.rules.assessment;

import ca.bc.gov.educ.graddatacollection.api.rules.Rule;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.AssessmentStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface AssessmentValidationBaseRule extends Rule<StudentRuleData, AssessmentStudentValidationIssue> {
  default AssessmentStudentValidationIssue createValidationIssue(StudentValidationIssueSeverityCode severityCode, AssessmentStudentValidationFieldCode fieldCode, AssessmentStudentValidationIssueTypeCode typeCode){
    AssessmentStudentValidationIssue sdcSchoolCollectionStudentValidationIssue = new AssessmentStudentValidationIssue();
    sdcSchoolCollectionStudentValidationIssue.setValidationIssueSeverityCode(severityCode.toString());
    sdcSchoolCollectionStudentValidationIssue.setValidationIssueCode(typeCode.getCode());
    sdcSchoolCollectionStudentValidationIssue.setValidationIssueFieldCode(fieldCode.getCode());
    return sdcSchoolCollectionStudentValidationIssue;
  }

  default boolean isValidationDependencyResolved(String fieldName, List<AssessmentStudentValidationIssue> validationErrorsMap) {
    Optional<AssessmentValidationRulesDependencyMatrix> errorCodesToCheck = AssessmentValidationRulesDependencyMatrix.findByValue(fieldName);
    if(errorCodesToCheck.isPresent()) {
      String[] errorCodes = errorCodesToCheck.get().getBaseRuleErrorCode();
      return validationErrorsMap.stream().noneMatch(val -> Arrays.stream(errorCodes).anyMatch(val.getValidationIssueCode()::contentEquals));
    }
    return false;
  }
}
