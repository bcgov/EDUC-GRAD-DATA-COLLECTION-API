package ca.bc.gov.educ.graddatacollection.api.mappers.v1;

import ca.bc.gov.educ.graddatacollection.api.mappers.UUIDMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentLightEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = UUIDMapper.class)
public interface CourseStudentMapper {

    CourseStudentMapper mapper = Mappers.getMapper(CourseStudentMapper.class);

    @Mapping(target = "incomingFilesetID", source = "incomingFileset.incomingFilesetID")
    CourseStudent toCourseStudent(CourseStudentEntity courseStudentEntity);

    @Mapping(target = "incomingFilesetID", source = "incomingFileset.incomingFilesetID")
    CourseStudent toCourseStudent(CourseStudentLightEntity courseStudentEntity);
}
