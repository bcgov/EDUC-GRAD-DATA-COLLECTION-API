package ca.bc.gov.educ.graddatacollection.api.mappers.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ErrorFilesetValidationIssueType;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ErrorFilesetStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorFilesetStudent;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorFilesetStudentValidationIssue;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public abstract class ErrorFilesetStudentDecorator implements ErrorFilesetStudentMapper {

  private final ErrorFilesetStudentMapper delegate;

  protected ErrorFilesetStudentDecorator(ErrorFilesetStudentMapper delegate) {
    this.delegate = delegate;
  }

  @Override
  public ErrorFilesetStudent toStructure(ErrorFilesetStudentEntity errorFilesetStudentEntity) {
    final ErrorFilesetStudent filesetStudent = this.delegate.toStructure(errorFilesetStudentEntity);
    filesetStudent.setErrorFilesetStudentValidationIssues(new ArrayList<>());

    errorFilesetStudentEntity.getDemographicStudentEntities().stream().forEach(demographicStudent ->
            demographicStudent.getDemographicStudentValidationIssueEntities().forEach(demographicIssueEntity ->
                    filesetStudent.getErrorFilesetStudentValidationIssues().add(
                            getValidationIssue(ErrorFilesetValidationIssueType.DEMOGRAPHICS,
                                    demographicIssueEntity.getValidationIssueCode(),
                                    demographicIssueEntity.getValidationIssueFieldCode(),
                                    demographicIssueEntity.getValidationIssueSeverityCode()))));

    errorFilesetStudentEntity.getCourseStudentEntities().stream().forEach(courseStudent ->
            courseStudent.getCourseStudentValidationIssueEntities().forEach(courseIssueEntity ->
                    filesetStudent.getErrorFilesetStudentValidationIssues().add(
                            getValidationIssue(ErrorFilesetValidationIssueType.COURSE,
                                    courseIssueEntity.getValidationIssueCode(),
                                    courseIssueEntity.getValidationIssueFieldCode(),
                                    courseIssueEntity.getValidationIssueSeverityCode()))));

    errorFilesetStudentEntity.getAssessmentStudentEntities().stream().forEach(assessmentStudent ->
            assessmentStudent.getAssessmentStudentValidationIssueEntities().forEach(assessmentIssueEntity ->
                    filesetStudent.getErrorFilesetStudentValidationIssues().add(
                            getValidationIssue(ErrorFilesetValidationIssueType.ASSESSMENT,
                                    assessmentIssueEntity.getValidationIssueCode(),
                                    assessmentIssueEntity.getValidationIssueFieldCode(),
                                    assessmentIssueEntity.getValidationIssueSeverityCode()))));

    return filesetStudent;
  }

  private ErrorFilesetStudentValidationIssue getValidationIssue(ErrorFilesetValidationIssueType errorFilesetValidationIssueType, String validationIssueCode, String validationIssueFieldCode, String validationIssueSeverityCode){
    ErrorFilesetStudentValidationIssue issue = new ErrorFilesetStudentValidationIssue();
    issue.setErrorFilesetValidationIssueTypeCode(errorFilesetValidationIssueType.getCode());
    issue.setValidationIssueCode(validationIssueCode);
    issue.setValidationIssueFieldCode(validationIssueFieldCode);
    issue.setValidationIssueSeverityCode(validationIssueSeverityCode);
    return issue;
  }
}
