package ca.bc.gov.educ.graddatacollection.api.controller.v1;

import ca.bc.gov.educ.graddatacollection.api.batch.processor.GradBatchFileProcessor;
import ca.bc.gov.educ.graddatacollection.api.endpoint.v1.GradFileUploadEndpoint;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
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
    public ResponseEntity<String> processSdcBatchFile(GradFileUpload fileUpload, String schoolID, String correlationID) {
        //run some payload validation
        log.info("Running file load for file: " + fileUpload.getFileName());
        gradFileService.processBatchFile(fileUpload, schoolID);
        log.info("File data committed for file: " + fileUpload.getFileName());;
        return ResponseEntity.ok().build();
    }
}
