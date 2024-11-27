package ca.bc.gov.educ.graddatacollection.api.controller.v1;

import ca.bc.gov.educ.graddatacollection.api.batch.processor.GradBatchFileProcessor;
import ca.bc.gov.educ.graddatacollection.api.endpoint.v1.GradFileUploadEndpoint;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.IncomingFilesetMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFileset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class GradFileUploadController implements GradFileUploadEndpoint {
    private final GradBatchFileProcessor gradFileService;

    public GradFileUploadController(GradBatchFileProcessor gradFileService) {
        this.gradFileService = gradFileService;
    }

    @Override
    public ResponseEntity<IncomingFileset> processSdcBatchFile(GradFileUpload fileUpload, String schoolID, String correlationID) {
        log.info("Running file load for file: " + fileUpload.getFileName());
        IncomingFilesetEntity incomingFilesetEntity = gradFileService.processBatchFile(fileUpload, schoolID);
        log.info("File data committed for file: " + fileUpload.getFileName());
        return ResponseEntity.ok(IncomingFilesetMapper.mapper.toStructure(incomingFilesetEntity));
    }
}
