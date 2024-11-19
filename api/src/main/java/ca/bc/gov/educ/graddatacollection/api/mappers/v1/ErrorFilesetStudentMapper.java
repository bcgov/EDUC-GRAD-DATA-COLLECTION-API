package ca.bc.gov.educ.graddatacollection.api.mappers.v1;

import ca.bc.gov.educ.graddatacollection.api.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.graddatacollection.api.mappers.UUIDMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ErrorFilesetStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorFilesetStudent;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@DecoratedWith(ErrorFilesetStudentDecorator.class)
public interface ErrorFilesetStudentMapper {
    ErrorFilesetStudentMapper mapper = Mappers.getMapper(ErrorFilesetStudentMapper.class);

    ErrorFilesetStudent toStructure(final ErrorFilesetStudentEntity errorFilesetStudentEntity);

}
