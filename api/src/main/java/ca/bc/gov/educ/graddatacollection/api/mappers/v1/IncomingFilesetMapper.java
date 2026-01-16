package ca.bc.gov.educ.graddatacollection.api.mappers.v1;

import ca.bc.gov.educ.graddatacollection.api.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.graddatacollection.api.mappers.UUIDMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.*;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFileset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
public interface IncomingFilesetMapper {
    IncomingFilesetMapper mapper = Mappers.getMapper(IncomingFilesetMapper.class);

    IncomingFileset toStructure(final IncomingFilesetEntity incomingFilesetEntity);

    IncomingFileset toStructure(final FinalIncomingFilesetEntity incomingFilesetEntity);

    // Main entity mapping - recursively maps children
    FinalIncomingFilesetEntity stagedToEntity(IncomingFilesetEntity staged);

    // Child mappings - ignore the parent reference to prevent stack overflow
    @Mapping(target = "incomingFileset", ignore = true)
    FinalDemographicStudentEntity stagedDemographicToEntity(DemographicStudentEntity staged);

    @Mapping(target = "incomingFileset", ignore = true)
    FinalCourseStudentEntity stagedCourseToEntity(CourseStudentEntity staged);

    @Mapping(target = "incomingFileset", ignore = true)
    FinalAssessmentStudentEntity stagedAssessmentToEntity(AssessmentStudentEntity staged);

    @Mapping(target = "incomingFileset", ignore = true)
    FinalErrorFilesetStudentEntity stagedErrorToEntity(ErrorFilesetStudentEntity staged);
}
