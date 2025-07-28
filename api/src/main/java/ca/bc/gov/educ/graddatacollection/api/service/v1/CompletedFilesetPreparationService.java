package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.DemographicStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.IncomingFilesetMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudent;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFilesetSagaData;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompletedFilesetPreparationService {
    
    private final IncomingFilesetRepository incomingFilesetRepository;

    @Transactional(readOnly = true)
    public List<IncomingFilesetSagaData> prepareFilesetData(List<UUID> filesetIds) {
        return filesetIds.stream()
                .map(id -> incomingFilesetRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException(IncomingFilesetEntity.class, "incomingFilesetID", id.toString())))
                .map(el -> {
                    val incomingFilesetSagaDataRecord = new IncomingFilesetSagaData();
                    incomingFilesetSagaDataRecord.setIncomingFileset(IncomingFilesetMapper.mapper.toStructure(el));
                    if(!el.getDemographicStudentEntities().isEmpty()) {
                        val student = el.getDemographicStudentEntities().stream()
                                .findFirst()
                                .orElseThrow(() -> new EntityNotFoundException(DemographicStudent.class));
                        incomingFilesetSagaDataRecord.setDemographicStudent(DemographicStudentMapper.mapper.toDemographicStudent(student));
                    }
                    return incomingFilesetSagaDataRecord;
                })
                .toList();
    }
}
