package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class IncomingFilesetService {

    private final IncomingFilesetRepository incomingFilesetRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public IncomingFilesetEntity saveIncomingFilesetRecord(IncomingFilesetEntity currentFileset) {
        log.debug("About to save school file data for fileset: {}", currentFileset.getIncomingFilesetID());
        return this.incomingFilesetRepository.save(currentFileset);
    }
}
