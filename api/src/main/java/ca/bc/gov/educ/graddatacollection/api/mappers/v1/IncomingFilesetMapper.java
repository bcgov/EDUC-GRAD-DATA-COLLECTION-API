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

    // Main entity mapping - ignore all child collections, we'll handle them manually
    @Mapping(target = "demographicStudentEntities", ignore = true)
    @Mapping(target = "courseStudentEntities", ignore = true)
    @Mapping(target = "assessmentStudentEntities", ignore = true)
    @Mapping(target = "errorFilesetStudentEntities", ignore = true)
    FinalIncomingFilesetEntity stagedToEntity(IncomingFilesetEntity staged);

    // Individual child entity mappers - ignore parent reference and validation issues (we'll copy those manually)
    @Mapping(target = "incomingFileset", ignore = true)
    @Mapping(target = "demographicStudentValidationIssueEntities", ignore = true)
    FinalDemographicStudentEntity stagedDemographicToEntity(DemographicStudentEntity staged);

    @Mapping(target = "incomingFileset", ignore = true)
    @Mapping(target = "courseStudentValidationIssueEntities", ignore = true)
    FinalCourseStudentEntity stagedCourseToEntity(CourseStudentEntity staged);

    @Mapping(target = "incomingFileset", ignore = true)
    @Mapping(target = "assessmentStudentValidationIssueEntities", ignore = true)
    FinalAssessmentStudentEntity stagedAssessmentToEntity(AssessmentStudentEntity staged);

    @Mapping(target = "incomingFileset", ignore = true)
    @Mapping(target = "demographicStudentEntities", ignore = true)
    @Mapping(target = "courseStudentEntities", ignore = true)
    @Mapping(target = "assessmentStudentEntities", ignore = true)
    FinalErrorFilesetStudentEntity stagedErrorToEntity(ErrorFilesetStudentEntity staged);

    // Validation issue mappers
    @Mapping(target = "demographicStudent", ignore = true)
    FinalDemographicStudentValidationIssueEntity stagedDemographicValidationIssueToEntity(DemographicStudentValidationIssueEntity staged);

    @Mapping(target = "courseStudent", ignore = true)
    FinalCourseStudentValidationIssueEntity stagedCourseValidationIssueToEntity(CourseStudentValidationIssueEntity staged);

    @Mapping(target = "assessmentStudent", ignore = true)
    FinalAssessmentStudentValidationIssueEntity stagedAssessmentValidationIssueToEntity(AssessmentStudentValidationIssueEntity staged);
}
