package ca.bc.gov.educ.graddatacollection.api.mappers.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.CustomSearchType;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ErrorFilesetValidationIssueType;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ErrorFilesetStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorFilesetStudent;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorFilesetStudentValidationIssue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class ErrorFilesetStudentDecorator implements ErrorFilesetStudentMapper {

  private final ErrorFilesetStudentMapper delegate;
  private static final String MISSING_COURSE_CODE = "missing course code";
  private static final String MISSING_COURSE_YEAR = "missing course year";
  private static final String MISSING_COURSE_MONTH = "missing course month";

  protected ErrorFilesetStudentDecorator(ErrorFilesetStudentMapper delegate) {
    this.delegate = delegate;
  }

  @Override
  public ErrorFilesetStudent toStructure(ErrorFilesetStudentEntity errorFilesetStudentEntity) {
    final ErrorFilesetStudent filesetStudent = this.delegate.toStructure(errorFilesetStudentEntity);
    filesetStudent.setErrorFilesetStudentValidationIssues(new ArrayList<>());

    setDemIssueType(errorFilesetStudentEntity, filesetStudent);
    setCourseIssueType(errorFilesetStudentEntity, filesetStudent);
    setXamIssueType(errorFilesetStudentEntity, filesetStudent);
    return filesetStudent;
  }

  @Override
  public ErrorFilesetStudent toStructureWithFilter(ErrorFilesetStudentEntity errorFilesetStudentEntity, List<String> validationFilter) {
    final ErrorFilesetStudent filesetStudent = this.delegate.toStructure(errorFilesetStudentEntity);
    filesetStudent.setErrorFilesetStudentValidationIssues(new ArrayList<>());
    boolean hasIssueTypeFilter = validationFilter.stream().anyMatch(type -> type.equalsIgnoreCase("DEM-ERROR") || type.equalsIgnoreCase("CRS-ERROR") || type.equalsIgnoreCase("XAM-ERROR"));
    boolean hasFieldCodeFilter = validationFilter.stream().anyMatch(type -> ValidationFieldCode.findByCode(type).isPresent());
    Optional<String> fieldCodeValueOpt = validationFilter.stream().filter(type -> ValidationFieldCode.findByCode(type).isPresent()).findFirst();

    if(hasIssueTypeFilter) {
      if(validationFilter.contains(CustomSearchType.DEMERROR.getCode())) {
        setDemIssueType(errorFilesetStudentEntity, filesetStudent);
      }
      if(validationFilter.contains(CustomSearchType.CRSERROR.getCode())) {
        setCourseIssueType(errorFilesetStudentEntity, filesetStudent);
      }
      if(validationFilter.contains(CustomSearchType.XAMERROR.getCode())) {
        setXamIssueType(errorFilesetStudentEntity, filesetStudent);
      }
    } else {
      setDemIssueType(errorFilesetStudentEntity, filesetStudent);
      setCourseIssueType(errorFilesetStudentEntity, filesetStudent);
      setXamIssueType(errorFilesetStudentEntity, filesetStudent);
    }

    if(validationFilter.contains(CustomSearchType.ERROR.getCode())) {
      var errors =  filesetStudent.getErrorFilesetStudentValidationIssues().stream().filter(stu -> stu.getValidationIssueSeverityCode().equalsIgnoreCase("ERROR")).toList();
      filesetStudent.setErrorFilesetStudentValidationIssues(errors);
    }

    if(validationFilter.contains(CustomSearchType.WARNING.getCode())) {
      var warnings = filesetStudent.getErrorFilesetStudentValidationIssues().stream().filter(stu -> stu.getValidationIssueSeverityCode().equalsIgnoreCase("WARNING")).toList();
      filesetStudent.setErrorFilesetStudentValidationIssues(warnings);
    }

    if(hasFieldCodeFilter && fieldCodeValueOpt.isPresent()) {
      var fieldCodeValue = fieldCodeValueOpt.get();
      var fieldCode = filesetStudent.getErrorFilesetStudentValidationIssues().stream().filter(stu -> stu.getValidationIssueFieldCode().equalsIgnoreCase(fieldCodeValue)).toList();
      filesetStudent.setErrorFilesetStudentValidationIssues(fieldCode);
    }

    return filesetStudent;
  }

  private ErrorFilesetStudentValidationIssue getValidationIssue(ErrorFilesetValidationIssueType errorFilesetValidationIssueType, String validationIssueDescription, String validationIssueCode, String validationIssueFieldCode, String validationIssueSeverityCode, String errorContext){
    ErrorFilesetStudentValidationIssue issue = new ErrorFilesetStudentValidationIssue();
    issue.setErrorFilesetValidationIssueTypeCode(errorFilesetValidationIssueType.getCode());
    issue.setValidationIssueDescription(validationIssueDescription);
    issue.setValidationIssueCode(validationIssueCode);
    issue.setValidationIssueFieldCode(validationIssueFieldCode);
    issue.setValidationIssueSeverityCode(validationIssueSeverityCode);
    issue.setErrorContext(errorContext);
    return issue;
  }

  private void setDemIssueType(ErrorFilesetStudentEntity errorFilesetStudentEntity, ErrorFilesetStudent filesetStudent) {
      errorFilesetStudentEntity.getDemographicStudentEntities().forEach(demographicStudent ->
              demographicStudent.getDemographicStudentValidationIssueEntities().forEach(demographicIssueEntity ->
                      filesetStudent.getErrorFilesetStudentValidationIssues().add(
                              getValidationIssue(ErrorFilesetValidationIssueType.DEMOGRAPHICS,
                                      demographicIssueEntity.getValidationIssueDescription(),
                                      demographicIssueEntity.getValidationIssueCode(),
                                      demographicIssueEntity.getValidationIssueFieldCode(),
                                      demographicIssueEntity.getValidationIssueSeverityCode(),
                                      null))));
  }

  private void setCourseIssueType(ErrorFilesetStudentEntity errorFilesetStudentEntity, ErrorFilesetStudent filesetStudent) {
    errorFilesetStudentEntity.getCourseStudentEntities().forEach(courseStudent ->
      courseStudent.getCourseStudentValidationIssueEntities().stream().forEach(courseIssueEntity ->
              filesetStudent.getErrorFilesetStudentValidationIssues().add(
                      getValidationIssue(ErrorFilesetValidationIssueType.COURSE,
                              courseIssueEntity.getValidationIssueDescription(),
                              courseIssueEntity.getValidationIssueCode(),
                              courseIssueEntity.getValidationIssueFieldCode(),
                              courseIssueEntity.getValidationIssueSeverityCode(),
                              setCourseErrorContext(courseStudent)))));

  }

  private void setXamIssueType(ErrorFilesetStudentEntity errorFilesetStudentEntity, ErrorFilesetStudent filesetStudent) {
      errorFilesetStudentEntity.getAssessmentStudentEntities().forEach(assessmentStudent ->
                assessmentStudent.getAssessmentStudentValidationIssueEntities().stream().forEach(assessmentIssueEntity ->
                        filesetStudent.getErrorFilesetStudentValidationIssues().add(
                                getValidationIssue(ErrorFilesetValidationIssueType.ASSESSMENT,
                                        assessmentIssueEntity.getValidationIssueDescription(),
                                        assessmentIssueEntity.getValidationIssueCode(),
                                        assessmentIssueEntity.getValidationIssueFieldCode(),
                                        assessmentIssueEntity.getValidationIssueSeverityCode(),
                                        setAssessmentErrorContext(assessmentStudent)))));

  }

  private String setCourseErrorContext(CourseStudentEntity courseStudent) {
    if(StringUtils.isEmpty(courseStudent.getCourseCode())) {
      return MISSING_COURSE_CODE;
    } else if(StringUtils.isEmpty(courseStudent.getCourseYear())) {
      return MISSING_COURSE_YEAR;
    } else if(StringUtils.isEmpty(courseStudent.getCourseMonth())) {
      return MISSING_COURSE_MONTH;
    } else if(StringUtils.isEmpty(courseStudent.getCourseLevel())) {
      return courseStudent.getCourseCode() + " - " + courseStudent.getCourseYear() + "/" + courseStudent.getCourseMonth();
    } else {
      return courseStudent.getCourseCode() + courseStudent.getCourseLevel() + " - " + courseStudent.getCourseYear() + "/" + courseStudent.getCourseMonth();
    }
  }

  private String setAssessmentErrorContext(AssessmentStudentEntity assessmentStudent) {
    if(StringUtils.isEmpty(assessmentStudent.getCourseCode())) {
      return MISSING_COURSE_CODE;
    } else if(StringUtils.isEmpty(assessmentStudent.getCourseYear())) {
      return MISSING_COURSE_YEAR;
    } else if(StringUtils.isEmpty(assessmentStudent.getCourseMonth())) {
      return MISSING_COURSE_MONTH;
    } else {
      return assessmentStudent.getCourseCode() + " - " + assessmentStudent.getCourseYear() + "/" + assessmentStudent.getCourseMonth();
    }
  }

}
