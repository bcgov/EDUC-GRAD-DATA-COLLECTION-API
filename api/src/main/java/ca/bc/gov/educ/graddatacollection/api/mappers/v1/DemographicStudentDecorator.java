package ca.bc.gov.educ.graddatacollection.api.mappers.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudent;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;

@Slf4j
public abstract class DemographicStudentDecorator implements DemographicStudentMapper {

  private final DemographicStudentMapper delegate;

  protected DemographicStudentDecorator(DemographicStudentMapper delegate) {
    this.delegate = delegate;
  }

  @Override
  public DemographicStudent toDemographicStudentWithValidationIssues(DemographicStudentEntity demographicStudentEntity) {
    final DemographicStudent demographicStudent = this.delegate.toDemographicStudent(demographicStudentEntity);
    DemographicStudentValidationIssueMapper studentValidationIssueMapper = DemographicStudentValidationIssueMapper.mapper;
    demographicStudent.setDemographicStudentValidationIssue(new ArrayList<>());
    demographicStudentEntity.getDemographicStudentValidationIssueEntities().stream().forEach(issue -> demographicStudent.getDemographicStudentValidationIssue().add(studentValidationIssueMapper.toStructure(issue)));
    return demographicStudent;
  }

}
