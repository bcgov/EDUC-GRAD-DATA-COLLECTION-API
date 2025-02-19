package ca.bc.gov.educ.graddatacollection.api.batch.mappers;

import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentAssessmentDetails;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentCourseDetails;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentDemogDetails;
import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
@DecoratedWith(BatchFileDecorator.class)
public interface BatchFileMapper {
    BatchFileMapper mapper = Mappers.getMapper(BatchFileMapper.class);

    String GRAD_DATA_COLLECTION_API = "GRAD_DATA_COLLECTION_API";

    @Mapping(target = "incomingFilesetID", ignore = true)
    @Mapping(target = "demFileUploadDate", expression = "java(java.time.LocalDateTime.now() )")
    @Mapping(target = "updateUser", source = "upload.updateUser")
    @Mapping(target = "updateDate", expression = "java(java.time.LocalDateTime.now() )")
    @Mapping(target = "createUser", constant = GRAD_DATA_COLLECTION_API)
    @Mapping(target = "createDate",expression = "java(java.time.LocalDateTime.now() )")
    IncomingFilesetEntity toIncomingDEMBatchEntity(final GradFileUpload upload, final String schoolID);

    @Mapping(target = "studentStatusCode", ignore = true)
    @Mapping(target = "demographicStudentID", ignore = true)
    @Mapping(target = "updateUser", constant = GRAD_DATA_COLLECTION_API)
    @Mapping(target = "updateDate", expression = "java(java.time.LocalDateTime.now() )")
    @Mapping(target = "createUser", constant = GRAD_DATA_COLLECTION_API)
    @Mapping(target = "createDate",expression = "java(java.time.LocalDateTime.now() )")
    DemographicStudentEntity toDEMStudentEntity(GradStudentDemogDetails studentDetails, IncomingFilesetEntity incomingFilesetEntity);

    @Mapping(target = "incomingFilesetID", ignore = true)
    @Mapping(target = "crsFileUploadDate", expression = "java(java.time.LocalDateTime.now() )")
    @Mapping(target = "updateUser", source = "upload.updateUser")
    @Mapping(target = "updateDate", expression = "java(java.time.LocalDateTime.now() )")
    @Mapping(target = "createUser", constant = GRAD_DATA_COLLECTION_API)
    @Mapping(target = "createDate",expression = "java(java.time.LocalDateTime.now() )")
    IncomingFilesetEntity toIncomingCRSBatchEntity(final GradFileUpload upload, final String schoolID);

    @Mapping(target = "studentStatusCode", ignore = true)
    @Mapping(target = "courseStudentID", ignore = true)
    @Mapping(target = "updateUser", constant = GRAD_DATA_COLLECTION_API)
    @Mapping(target = "updateDate", expression = "java(java.time.LocalDateTime.now() )")
    @Mapping(target = "createUser", constant = GRAD_DATA_COLLECTION_API)
    @Mapping(target = "createDate",expression = "java(java.time.LocalDateTime.now() )")
    CourseStudentEntity toCRSStudentEntity(GradStudentCourseDetails courseDetails, IncomingFilesetEntity incomingFilesetEntity);

    @Mapping(target = "incomingFilesetID", ignore = true)
    @Mapping(target = "xamFileUploadDate", expression = "java(java.time.LocalDateTime.now() )")
    @Mapping(target = "updateUser", source = "upload.updateUser")
    @Mapping(target = "updateDate", expression = "java(java.time.LocalDateTime.now() )")
    @Mapping(target = "createUser", constant = GRAD_DATA_COLLECTION_API)
    @Mapping(target = "createDate",expression = "java(java.time.LocalDateTime.now() )")
    IncomingFilesetEntity toIncomingXAMBatchEntity(final GradFileUpload upload, final String schoolID);

    @Mapping(target = "studentStatusCode", ignore = true)
    @Mapping(target = "assessmentStudentID", ignore = true)
    @Mapping(target = "updateUser", constant = GRAD_DATA_COLLECTION_API)
    @Mapping(target = "updateDate", expression = "java(java.time.LocalDateTime.now() )")
    @Mapping(target = "createUser", constant = GRAD_DATA_COLLECTION_API)
    @Mapping(target = "createDate",expression = "java(java.time.LocalDateTime.now() )")
    AssessmentStudentEntity toXAMStudentEntity(GradStudentAssessmentDetails assessmentDetails, IncomingFilesetEntity incomingFilesetEntity);
}
