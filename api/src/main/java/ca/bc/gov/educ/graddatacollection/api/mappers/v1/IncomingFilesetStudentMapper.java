package ca.bc.gov.educ.graddatacollection.api.mappers.v1;

import ca.bc.gov.educ.graddatacollection.api.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.graddatacollection.api.mappers.UUIDMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.*;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.AssessmentStudent;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudent;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudent;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFilesetStudent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(uses = {
        UUIDMapper.class,
        LocalDateTimeMapper.class,
        DemographicStudentMapper.class,
        CourseStudentMapper.class,
        AssessmentStudentMapper.class
})
public interface IncomingFilesetStudentMapper {
    IncomingFilesetStudentMapper mapper = Mappers.getMapper(IncomingFilesetStudentMapper.class);

    @Mapping(target = "incomingFilesetID", source = "incomingFilesetID")
    @Mapping(target = "pen", source = "pen")
    @Mapping(target = "demographicStudent", source = "demographicStudentEntity", qualifiedByName = "toDemographicStudent")
    @Mapping(target = "courseStudents", source = "courseStudentEntities", qualifiedByName = "toCourseStudentListFromList")
    @Mapping(target = "assessmentStudents", source = "assessmentStudentEntities", qualifiedByName = "toAssessmentStudentListFromList")
    IncomingFilesetStudent toStructure(String pen,
                                       java.util.UUID incomingFilesetID,
                                       FinalDemographicStudentEntity demographicStudentEntity,
                                       List<FinalCourseStudentEntity> courseStudentEntities,
                                       List<FinalAssessmentStudentEntity> assessmentStudentEntities);

    @Named("toDemographicStudent")
    default DemographicStudent toDemographicStudent(FinalDemographicStudentEntity entity) {
        if (entity == null) {
            return null;
        }
        return DemographicStudentMapper.mapper.toDemographicStudent(entity);
    }

    @Named("toCourseStudentListFromList")
    default List<CourseStudent> toCourseStudentListFromList(List<FinalCourseStudentEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(CourseStudentMapper.mapper::toCourseStudent)
                .collect(Collectors.toList());
    }

    @Named("toAssessmentStudentListFromList")
    default List<AssessmentStudent> toAssessmentStudentListFromList(List<FinalAssessmentStudentEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(AssessmentStudentMapper.mapper::toAssessmentStudent)
                .collect(Collectors.toList());
    }
}
