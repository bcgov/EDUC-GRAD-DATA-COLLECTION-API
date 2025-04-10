package ca.bc.gov.educ.graddatacollection.api.batch.service;

import ca.bc.gov.educ.graddatacollection.api.batch.constants.ExcelFileType;
import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.batch.processor.BaseExcelProcessor;
import ca.bc.gov.educ.graddatacollection.api.batch.validation.GradFileValidator;
import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SummerStudentDataResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service("xls")
@Slf4j
public class GradExcelFileService extends BaseExcelProcessor {

    protected GradExcelFileService(ApplicationProperties applicationProperties, GradFileValidator gradFileValidator) {
        super(applicationProperties, gradFileValidator);
    }

    @Override
    public SummerStudentDataResponse extractData(String guid, byte[] fileContents, String schoolID, String districtID) throws FileUnProcessableException {
        try {
            final File outputFile = this.getFile(fileContents, ExcelFileType.XLS.getCode());
            try (final POIFSFileSystem fs = new POIFSFileSystem(outputFile)) {
                try (final HSSFWorkbook wb = new HSSFWorkbook(fs.getRoot(), true)) {
                    return this.processSheet(wb.getSheetAt(0), schoolID, districtID, guid);
                }
            }
        } catch (final IOException e) {
            log.error("exception", e);
            throw new GradDataCollectionAPIRuntimeException(e.getMessage());
        }
    }
}
