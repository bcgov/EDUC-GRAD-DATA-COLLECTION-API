package ca.bc.gov.educ.graddatacollection.api.batch.service;

import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileError;
import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.batch.processor.BaseExcelProcessor;
import ca.bc.gov.educ.graddatacollection.api.batch.validation.GradFileValidator;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradCollectionStatus;
import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.service.v1.ReportingPeriodService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SummerStudentDataResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service("csv")
@Slf4j
public class GradCsvFileService extends BaseExcelProcessor {

    protected GradCsvFileService(ApplicationProperties applicationProperties, GradFileValidator gradFileValidator, ReportingPeriodService reportingPeriodService) {
        super(applicationProperties, gradFileValidator, reportingPeriodService);
    }

    @Override
    public SummerStudentDataResponse extractData(String guid, byte[] fileContents, String schoolID, String districtID) throws FileUnProcessableException {
        try (final var reader = new InputStreamReader(new ByteArrayInputStream(fileContents), StandardCharsets.UTF_8);
             final CSVParser csvParser = CSVFormat.DEFAULT.parse(reader);
             final XSSFWorkbook workbook = new XSSFWorkbook()) {

            final var sheet = workbook.createSheet();
            int rowIdx = 0;

            for (final CSVRecord csvRecord : csvParser) {
                final var row = sheet.createRow(rowIdx++);
                for (int col = 0; col < csvRecord.size(); col++) {
                    String value = csvRecord.get(col);
                    row.createCell(col).setCellValue(value != null ? value : "");
                }
            }

            if (sheet.getPhysicalNumberOfRows() == 0) {
                throw new FileUnProcessableException(FileError.EMPTY_EXCEL_NOT_ALLOWED, guid, GradCollectionStatus.LOAD_FAIL);
            }

            return this.processSheet(sheet, schoolID, districtID, guid);
        } catch (final IOException e) {
            log.error("Exception while processing CSV file", e);
            throw new GradDataCollectionAPIRuntimeException(e.getMessage());
        }
    }
}

