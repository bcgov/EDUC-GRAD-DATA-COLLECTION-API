package ca.bc.gov.educ.graddatacollection.api.batch.mapper;

import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentDemogDetails;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
@DecoratedWith(BatchFileDecorator.class)
public interface DemBatchFileMapper {
    DemBatchFileMapper mapper = Mappers.getMapper(DemBatchFileMapper.class);

    String GRAD_DATA_COLLECTION_API = "GRAD_DATA_COLLECTION_API";

    @Mapping(target = "demFileStatusCode", ignore = true)
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
}
