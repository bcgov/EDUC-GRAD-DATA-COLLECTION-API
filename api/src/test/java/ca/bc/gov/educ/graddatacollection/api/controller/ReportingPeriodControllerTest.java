package ca.bc.gov.educ.graddatacollection.api.controller;

import ca.bc.gov.educ.graddatacollection.api.controller.v1.ReportingPeriodController;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ReportingPeriodEntity;
import ca.bc.gov.educ.graddatacollection.api.service.v1.ReportingPeriodService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.ReportingSummaryService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ReportingPeriod;
import ca.bc.gov.educ.graddatacollection.api.validator.ReportingPeriodValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ReportingPeriodControllerTest {

    private ReportingPeriodService reportingPeriodService;
    private ReportingSummaryService reportingSummaryService;
    private ReportingPeriodValidator reportingPeriodValidator;
    private ReportingPeriodController controller;

    @BeforeEach
    void setup() {
        reportingPeriodService = mock(ReportingPeriodService.class);
        reportingPeriodValidator = mock(ReportingPeriodValidator.class);
        reportingSummaryService = mock(ReportingSummaryService.class);
        controller = new ReportingPeriodController(reportingPeriodService, reportingSummaryService, reportingPeriodValidator);
    }

    @Test
    void testGetActiveReportingPeriod_ReturnsMappedObject() {
        UUID id = UUID.randomUUID();
        ReportingPeriodEntity entity = ReportingPeriodEntity.builder()
                .reportingPeriodID(id)
                .schYrStart(LocalDateTime.of(2024, 10, 7, 0, 0))
                .schYrEnd(LocalDateTime.of(2025, 7, 18, 0, 0))
                .summerStart(LocalDateTime.of(2025, 8, 18, 0, 0))
                .summerEnd(LocalDateTime.of(2025, 8, 18, 0, 0))
                .build();

        when(reportingPeriodService.getActiveReportingPeriod()).thenReturn(entity);

        ReportingPeriod result = controller.getActiveReportingPeriod();

        assertEquals("2024-10-07T00:00:00", result.getSchYrStart());
        assertEquals("2025-07-18T00:00:00", result.getSchYrEnd());
        assertEquals("2025-08-18T00:00:00", result.getSummerStart());
        assertEquals("2025-08-18T00:00:00", result.getSummerEnd());
    }

    @Test
    void testUpdateReportingPeriod_DelegatesToServiceAndReturnsMappedResult() {
        UUID id = UUID.randomUUID();

        ReportingPeriod input = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("2024-10-07T00:00:00")
                .schYrEnd("2025-07-18T00:00:00")
                .summerStart("2025-08-18T00:00:00")
                .summerEnd("2025-08-18T00:00:00")
                .updateUser("tester")
                .build();

        ReportingPeriodEntity saved = ReportingPeriodEntity.builder()
                .reportingPeriodID(id)
                .schYrStart(LocalDateTime.of(2024, 10, 7, 0, 0))
                .schYrEnd(LocalDateTime.of(2025, 7, 18, 0, 0))
                .summerStart(LocalDateTime.of(2025, 8, 18, 0, 0))
                .summerEnd(LocalDateTime.of(2025, 8, 18, 0, 0))
                .build();

        when(reportingPeriodService.updateReportingPeriod(any())).thenReturn(saved);

        ReportingPeriod result = controller.updateReportingPeriod(input);

        assertEquals("2024-10-07T00:00:00", result.getSchYrStart());
        assertEquals("2025-07-18T00:00:00", result.getSchYrEnd());
        assertEquals("2025-08-18T00:00:00", result.getSummerStart());
        assertEquals("2025-08-18T00:00:00", result.getSummerEnd());
        verify(reportingPeriodValidator).validatePayload(input);
    }
}
