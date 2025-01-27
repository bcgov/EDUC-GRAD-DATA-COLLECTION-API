package ca.bc.gov.educ.graddatacollection.api.rules.course;

import ca.bc.gov.educ.graddatacollection.api.rules.Rule;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface CourseValidationBaseRule extends Rule<StudentRuleData, CourseStudentValidationIssue> {
  default CourseStudentValidationIssue createValidationIssue(StudentValidationIssueSeverityCode severityCode, CourseStudentValidationFieldCode fieldCode, CourseStudentValidationIssueTypeCode typeCode, String description){
    CourseStudentValidationIssue sdcSchoolCollectionStudentValidationIssue = new CourseStudentValidationIssue();
    sdcSchoolCollectionStudentValidationIssue.setValidationIssueSeverityCode(severityCode.toString());
    sdcSchoolCollectionStudentValidationIssue.setValidationIssueCode(typeCode.getCode());
    sdcSchoolCollectionStudentValidationIssue.setValidationIssueFieldCode(fieldCode.getCode());
    sdcSchoolCollectionStudentValidationIssue.setValidationIssueDescription(description);
    return sdcSchoolCollectionStudentValidationIssue;
  }

  default boolean isValidationDependencyResolved(String fieldName, List<CourseStudentValidationIssue> validationErrorsMap) {
    Optional<CourseValidationRulesDependencyMatrix> errorCodesToCheck = CourseValidationRulesDependencyMatrix.findByValue(fieldName);
    if(errorCodesToCheck.isPresent()) {
      String[] errorCodes = errorCodesToCheck.get().getBaseRuleErrorCode();
      return validationErrorsMap.stream().noneMatch(val -> Arrays.stream(errorCodes).anyMatch(val.getValidationIssueCode()::contentEquals));
    }
    return false;
  }
}
