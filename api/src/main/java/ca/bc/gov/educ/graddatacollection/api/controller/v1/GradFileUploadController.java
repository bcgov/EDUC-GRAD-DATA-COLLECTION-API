package ca.bc.gov.educ.graddatacollection.api.controller.v1;

import ca.bc.gov.educ.graddatacollection.api.batch.processor.GradBatchFileProcessor;
import ca.bc.gov.educ.graddatacollection.api.batch.processor.GradExcelFileProcessor;
import ca.bc.gov.educ.graddatacollection.api.endpoint.v1.GradFileUploadEndpoint;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.IncomingFilesetMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFileset;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SummerStudentData;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SummerStudentDataResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class GradFileUploadController implements GradFileUploadEndpoint {
    private final GradBatchFileProcessor gradBatchFileProcessor;
    private final GradExcelFileProcessor gradExcelFileProcessor;
    public static final String LOAD_MSG = "Running file load for file:";
    public static final String DATA_SAVED_MSG = "File data committed for file: ";

    public GradFileUploadController(GradBatchFileProcessor gradBatchFileProcessor, GradExcelFileProcessor gradExcelFileProcessor) {
        this.gradBatchFileProcessor = gradBatchFileProcessor;
        this.gradExcelFileProcessor = gradExcelFileProcessor;
    }

    @Override
    public ResponseEntity<IncomingFileset> processSchoolBatchFile(GradFileUpload fileUpload, String schoolID) {
        log.info(LOAD_MSG + fileUpload.getFileName());
        IncomingFilesetEntity incomingFilesetEntity = gradBatchFileProcessor.processSchoolBatchFile(fileUpload, schoolID);
        log.info(DATA_SAVED_MSG + fileUpload.getFileName());
        return ResponseEntity.ok(IncomingFilesetMapper.mapper.toStructure(incomingFilesetEntity));
    }

    @Override
    public ResponseEntity<IncomingFileset> processDistrictBatchFile(GradFileUpload fileUpload, String districtID) {
        log.info(LOAD_MSG + fileUpload.getFileName());
        IncomingFilesetEntity incomingFilesetEntity = gradBatchFileProcessor.processDistrictBatchFile(fileUpload, districtID);
        log.info(DATA_SAVED_MSG + fileUpload.getFileName());
        return ResponseEntity.ok(IncomingFilesetMapper.mapper.toStructure(incomingFilesetEntity));
    }

    @Override
    public ResponseEntity<SummerStudentDataResponse> processSchoolExcelFile(GradFileUpload fileUpload, String schoolID) {
        log.info(LOAD_MSG + fileUpload.getFileName());
        SummerStudentDataResponse summerStudents =  gradExcelFileProcessor.processSchoolExcelFile(fileUpload, schoolID);
        return ResponseEntity.ok(summerStudents);
    }
}
