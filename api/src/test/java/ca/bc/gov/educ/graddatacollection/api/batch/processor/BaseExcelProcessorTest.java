package ca.bc.gov.educ.graddatacollection.api.batch.processor;

import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.batch.validation.GradFileValidator;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ReportingPeriodEntity;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.service.v1.ReportingPeriodService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SummerStudentDataResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseExcelProcessorTest {

    private static final String[] HEADERS = {
            "School Code", "PEN", "Legal Surname", "Legal Given Name",
            "Legal Middle Name", "Birthdate", "Ministry Course Code",
            "Ministry Course Level", "Session Date", "Final Percent",
            "Final Letter Grade", "Credits"
    };

    @Mock private ApplicationProperties applicationProperties;
    @Mock private GradFileValidator gradFileValidator;
    @Mock private ReportingPeriodService reportingPeriodService;

    private TestableExcelProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TestableExcelProcessor(applicationProperties, gradFileValidator, reportingPeriodService);
        when(reportingPeriodService.getActiveReportingPeriod()).thenReturn(buildReportingPeriod());
    }

    @Test
    void processSheet_withNoBom_shouldParseHeadersSuccessfully() throws Exception {
        try (Workbook wb = buildWorkbook("School Code")) {
            SummerStudentDataResponse response = processor.callProcessSheet(wb.getSheetAt(0), UUID.randomUUID().toString(), null, UUID.randomUUID().toString());
            assertThat(response.getHeaders()).hasSize(HEADERS.length);
        }
    }

    @Test
    void processSheet_withBomPrefixOnFirstHeader_shouldParseHeadersSuccessfully() throws Exception {
        try (Workbook wb = buildWorkbook("\uFEFFSchool Code")) {
            SummerStudentDataResponse response = processor.callProcessSheet(wb.getSheetAt(0), UUID.randomUUID().toString(), null, UUID.randomUUID().toString());
            assertThat(response.getHeaders()).hasSize(HEADERS.length);
        }
    }

    private Workbook buildWorkbook(String firstHeaderValue) {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Data");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue(firstHeaderValue);
        for (int i = 1; i < HEADERS.length; i++) {
            header.createCell(i).setCellValue(HEADERS[i]);
        }
        return wb;
    }

    private ReportingPeriodEntity buildReportingPeriod() {
        LocalDateTime now = LocalDateTime.now();
        return ReportingPeriodEntity.builder()
                .reportingPeriodID(UUID.randomUUID())
                .periodStart(now.minusYears(2))
                .periodEnd(now.plusYears(2))
                .schYrStart(now.minusYears(1))
                .schYrEnd(now)
                .summerStart(now)
                .summerEnd(now.plusMonths(3))
                .createUser("TEST")
                .createDate(now.minusDays(1))
                .updateUser("TEST")
                .updateDate(now.minusDays(1))
                .build();
    }

    static class TestableExcelProcessor extends BaseExcelProcessor {

        TestableExcelProcessor(ApplicationProperties props, GradFileValidator validator, ReportingPeriodService service) {
            super(props, validator, service);
        }

        SummerStudentDataResponse callProcessSheet(Sheet sheet, String schoolID, String districtID, String guid)
                throws FileUnProcessableException {
            return processSheet(sheet, schoolID, districtID, guid);
        }

        @Override
        public SummerStudentDataResponse extractData(String guid, byte[] fileContents, String schoolID, String districtID)
                throws FileUnProcessableException {
            throw new UnsupportedOperationException("not needed for these tests");
        }
    }
}
