package ca.bc.gov.educ.graddatacollection.api.mappers.v1;

import ca.bc.gov.educ.graddatacollection.api.mappers.UUIDMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentValidationIssueEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class})
@SuppressWarnings("squid:S1214")
public interface DemographicStudentValidationIssueMapper {

  DemographicStudentValidationIssueMapper mapper = Mappers.getMapper(DemographicStudentValidationIssueMapper.class);

  DemographicStudentValidationIssueEntity toModel(DemographicStudentValidationIssue structure);
  @Mapping(target = "demographicStudentID", source = "entity.demographicStudent.demographicStudentID")
  DemographicStudentValidationIssue toStructure(DemographicStudentValidationIssueEntity entity);

}
