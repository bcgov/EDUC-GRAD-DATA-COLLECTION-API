package ca.bc.gov.educ.graddatacollection.api.mappers.v1;


import ca.bc.gov.educ.graddatacollection.api.mappers.UUIDMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudent;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = UUIDMapper.class)
@DecoratedWith(DemographicStudentDecorator.class)
public interface DemographicStudentMapper {

  DemographicStudentMapper mapper = Mappers.getMapper(DemographicStudentMapper.class);

  @Mapping(target = "incomingFilesetID", source = "incomingFileset.incomingFilesetID")
  DemographicStudent toDemographicStudent(DemographicStudentEntity demographicStudentEntity);

  DemographicStudent toDemographicStudentWithValidationIssues(DemographicStudentEntity demographicStudentEntity);

}
