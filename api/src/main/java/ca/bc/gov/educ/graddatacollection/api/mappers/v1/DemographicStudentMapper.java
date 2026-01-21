package ca.bc.gov.educ.graddatacollection.api.mappers.v1;


import ca.bc.gov.educ.graddatacollection.api.mappers.UUIDMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentLightEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.FinalDemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = UUIDMapper.class)
public interface DemographicStudentMapper {

  DemographicStudentMapper mapper = Mappers.getMapper(DemographicStudentMapper.class);

  @Mapping(target = "incomingFilesetID", source = "incomingFileset.incomingFilesetID")
  DemographicStudent toDemographicStudent(DemographicStudentEntity demographicStudentEntity);

  @Mapping(target = "incomingFilesetID", source = "incomingFileset.incomingFilesetID")
  DemographicStudent toDemographicStudent(FinalDemographicStudentEntity demographicStudentEntity);

  DemographicStudent toDemographicStudent(DemographicStudentLightEntity demographicStudentEntity);

}
