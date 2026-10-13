package ca.bc.gov.educ.graddatacollection.api.service;

import ca.bc.gov.educ.graddatacollection.api.repository.v1.FinalIncomingFilesetPurgeRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ReportingPeriodRepository;
import ca.bc.gov.educ.graddatacollection.api.service.v1.ReportingPeriodService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

class ReportingPeriodRetentionTest {
    @ParameterizedTest
    @CsvSource({
            "2026-09-30, 2021-10-01T00:00:00",
            "2026-10-01, 2022-10-01T00:00:00",
            "2026-10-13, 2022-10-01T00:00:00",
            "2027-01-01, 2022-10-01T00:00:00"
    })
    void retainsFiveReportingYearsAcrossRollover(String date, String expectedCutoff) {
        // get the date
        LocalDate today = LocalDate.parse(date);
        LocalDateTime cutoff = LocalDateTime.parse(expectedCutoff);
        var repository = mock(FinalIncomingFilesetPurgeRepository.class);
        var service = new ReportingPeriodService(mock(ReportingPeriodRepository.class), repository);
        try (MockedStatic<LocalDate> dates = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
            dates.when(LocalDate::now).thenReturn(today);
            service.purgeExpiredFinalFilesets();
        }
        verify(repository).deleteWithReportingPeriodStartBefore(cutoff);
    }
}
