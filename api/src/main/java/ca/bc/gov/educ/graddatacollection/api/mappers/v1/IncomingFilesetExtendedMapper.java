package ca.bc.gov.educ.graddatacollection.api.mappers.v1;

import ca.bc.gov.educ.graddatacollection.api.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.graddatacollection.api.mappers.UUIDMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.AssessmentStudent;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudent;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudent;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFilesetExtended;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(uses = {
        UUIDMapper.class,
        LocalDateTimeMapper.class,
        DemographicStudentMapper.class,
        CourseStudentMapper.class,
        AssessmentStudentMapper.class
})
public interface IncomingFilesetExtendedMapper {
    IncomingFilesetExtendedMapper mapper = Mappers.getMapper(IncomingFilesetExtendedMapper.class);

    @Mapping(target = "demographicStudents", source = "demographicStudentEntities", qualifiedByName = "toDemographicStudentList")
    @Mapping(target = "courseStudents", source = "courseStudentEntities", qualifiedByName = "toCourseStudentList")
    @Mapping(target = "assessmentStudents", source = "assessmentStudentEntities", qualifiedByName = "toAssessmentStudentList")
    IncomingFilesetExtended toStructure(IncomingFilesetEntity incomingFilesetEntity);

    @Named("toDemographicStudentList")
    default List<DemographicStudent> toDemographicStudentList(Set<DemographicStudentEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(DemographicStudentMapper.mapper::toDemographicStudent)
                .collect(Collectors.toList());
    }

    @Named("toCourseStudentList")
    default List<CourseStudent> toCourseStudentList(Set<CourseStudentEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(CourseStudentMapper.mapper::toCourseStudent)
                .collect(Collectors.toList());
    }

    @Named("toAssessmentStudentList")
    default List<AssessmentStudent> toAssessmentStudentList(Set<AssessmentStudentEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(AssessmentStudentMapper.mapper::toAssessmentStudent)
                .collect(Collectors.toList());
    }

    default IncomingFilesetExtended toStructureWithPen(IncomingFilesetEntity incomingFilesetEntity, String pen) {
        IncomingFilesetExtended extended = toStructure(incomingFilesetEntity);
        if (extended.getDemographicStudents() != null) {
            extended.setDemographicStudents(
                    extended.getDemographicStudents().stream()
                            .filter(ds -> pen.equals(ds.getPen()))
                            .collect(Collectors.toList())
            );
        }
        if (extended.getCourseStudents() != null) {
            extended.setCourseStudents(
                    extended.getCourseStudents().stream()
                            .filter(cs -> pen.equals(cs.getPen()))
                            .collect(Collectors.toList())
            );
        }
        if (extended.getAssessmentStudents() != null) {
            extended.setAssessmentStudents(
                    extended.getAssessmentStudents().stream()
                            .filter(as -> pen.equals(as.getPen()))
                            .collect(Collectors.toList())
            );
        }
        return extended;
    }
}
