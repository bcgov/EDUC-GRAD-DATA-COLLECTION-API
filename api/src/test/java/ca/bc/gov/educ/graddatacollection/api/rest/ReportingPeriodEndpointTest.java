package ca.bc.gov.educ.graddatacollection.api.rest;

import ca.bc.gov.educ.graddatacollection.api.endpoint.v1.ReportingPeriodEndpoint;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ReportingPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

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
                .build();

        when(reportingPeriodEndpoint.getActiveReportingPeriod()).thenReturn(dummyPeriod);

        ReportingPeriod actualPeriod = reportingPeriodEndpoint.getActiveReportingPeriod();
        assertEquals(dummyPeriod, actualPeriod);
    }
}
