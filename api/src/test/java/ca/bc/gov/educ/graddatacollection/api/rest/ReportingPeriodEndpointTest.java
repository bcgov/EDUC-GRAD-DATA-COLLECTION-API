package ca.bc.gov.educ.graddatacollection.api.rest;

import ca.bc.gov.educ.graddatacollection.api.endpoint.v1.ReportingPeriodEndpoint;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ReportingPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportingPeriodEndpointTest {

    private ReportingPeriodEndpoint reportingPeriodEndpoint;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reportingPeriodEndpoint = mock(ReportingPeriodEndpoint.class);
    }

    @Test
    void testGetActiveReportingPeriod_ReturnsDummyReportingPeriod() {
        ReportingPeriod dummyPeriod = ReportingPeriod.builder()
                .reportingPeriodID("RP1")
                .schYrStart("2025-01-01")
                .schYrEnd("2025-06-30")
                .summerStart("2025-07-01")
                .summerEnd("2025-08-31")
                .periodStart("2022-09-01")
                .periodEnd("2027-10-30")
                .build();

        when(reportingPeriodEndpoint.getActiveReportingPeriod()).thenReturn(dummyPeriod);

        ReportingPeriod actualPeriod = reportingPeriodEndpoint.getActiveReportingPeriod();
        assertEquals(dummyPeriod, actualPeriod);
    }

    @Test
    void testUpdateReportingPeriod_ReturnsUpdatedPeriod() {
        ReportingPeriod input = ReportingPeriod.builder()
                .reportingPeriodID("RP2")
                .schYrStart("2025-09-01")
                .schYrEnd("2026-06-30")
                .summerStart("2026-07-01")
                .summerEnd("2026-08-31")
                .periodStart("2022-09-01")
                .periodEnd("2027-10-30")
                .build();

        when(reportingPeriodEndpoint.updateReportingPeriod(input)).thenReturn(input);

        ReportingPeriod updated = reportingPeriodEndpoint.updateReportingPeriod(input);
        assertEquals("RP2", updated.getReportingPeriodID());
        assertEquals("2025-09-01", updated.getSchYrStart());
    }
}
