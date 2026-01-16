package ca.bc.gov.educ.graddatacollection.api.mappers.v1;


import ca.bc.gov.educ.graddatacollection.api.mappers.UUIDMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentLightEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.FinalAssessmentStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.AssessmentStudent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = UUIDMapper.class)
public interface AssessmentStudentMapper {

  AssessmentStudentMapper mapper = Mappers.getMapper(AssessmentStudentMapper.class);

  @Mapping(target = "incomingFilesetID", source = "incomingFileset.incomingFilesetID")
  AssessmentStudent toAssessmentStudent(AssessmentStudentEntity assessmentStudentEntity);

  @Mapping(target = "incomingFilesetID", source = "incomingFileset.incomingFilesetID")
  AssessmentStudent toAssessmentStudent(FinalAssessmentStudentEntity assessmentStudentEntity);

  @Mapping(target = "incomingFilesetID", source = "incomingFileset.incomingFilesetID")
  AssessmentStudent toAssessmentStudent(AssessmentStudentLightEntity assessmentStudentEntity);

}
