package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DemographicStudentService {

    private final IncomingFilesetRepository incomingFilesetRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public IncomingFilesetEntity reconcileStudentsAndSaveSdcSchoolCollection(IncomingFilesetEntity currentFileset, List<DemographicStudentEntity> finalStudents) {
        currentFileset.getDemographicStudentEntities().clear();
        currentFileset.getDemographicStudentEntities().addAll(finalStudents);

        log.debug("About to save school file data for fileset: {}", currentFileset.getIncomingFilesetID());
        return this.incomingFilesetRepository.save(currentFileset);
    }
}
