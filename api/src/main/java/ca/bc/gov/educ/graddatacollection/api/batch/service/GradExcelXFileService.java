package ca.bc.gov.educ.graddatacollection.api.batch.service;

import ca.bc.gov.educ.graddatacollection.api.batch.constants.ExcelFileType;
import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.batch.processor.BaseExcelProcessor;
import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SummerStudentDataResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service("xlsx")
@Slf4j
public class GradExcelXFileService extends BaseExcelProcessor {

    protected GradExcelXFileService(ApplicationProperties applicationProperties) {
        super(applicationProperties);
    }

    @Override
    public SummerStudentDataResponse extractData(String guid, byte[] fileContents, String schoolID, String districtID) throws FileUnProcessableException {
        try {
            final File outputFile = this.getFile(fileContents, ExcelFileType.XLSX.getCode());
            try (final OPCPackage pkg = OPCPackage.open(outputFile)) {
                try (final XSSFWorkbook wb = new XSSFWorkbook(pkg)) {
                    return this.processSheet(wb.getSheetAt(0), guid);
                }
            }
        } catch (final IOException | InvalidFormatException e) {
            log.error("exception", e);
            throw new GradDataCollectionAPIRuntimeException(e.getMessage());
        }
    }
}
