package ca.bc.gov.educ.graddatacollection.api.mappers.v1;

import ca.bc.gov.educ.graddatacollection.api.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.graddatacollection.api.mappers.UUIDMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFileset;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
public interface IncomingFilesetMapper {
    IncomingFilesetMapper mapper = Mappers.getMapper(IncomingFilesetMapper.class);

    IncomingFileset toStructure(final IncomingFilesetEntity incomingFilesetEntity);

}
