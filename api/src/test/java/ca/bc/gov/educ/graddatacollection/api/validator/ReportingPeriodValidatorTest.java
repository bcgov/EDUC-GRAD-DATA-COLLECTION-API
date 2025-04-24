package ca.bc.gov.educ.graddatacollection.api.validator;

import ca.bc.gov.educ.graddatacollection.api.model.v1.ReportingPeriodEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ReportingPeriodRepository;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ReportingPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.graddatacollection.api.validator.ReportingPeriodValidator.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ReportingPeriodValidatorTest {

    private ReportingPeriodRepository reportingPeriodRepository;
    private ReportingPeriodValidator validator;
    private UUID id;

    @BeforeEach
    void setUp() {
        reportingPeriodRepository = mock(ReportingPeriodRepository.class);
        validator = new ReportingPeriodValidator(reportingPeriodRepository);
        id = UUID.randomUUID();
        when(reportingPeriodRepository.findById(id))
                .thenReturn(Optional.of(ReportingPeriodEntity.builder().reportingPeriodID(id).build()));
    }

    @Test
    void testValidDates_NoErrors() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("2024-10-01T00:00:00")
                .schYrEnd("2025-06-30T00:00:00")
                .summerStart("2025-07-01T00:00:00")
                .summerEnd("2025-08-31T00:00:00")
                .periodStart("2020-07-30T00:00:00")
                .periodEnd("2026-08-31T00:00:00")
                .build();

        List<FieldError> errors = validator.validatePayload(period);
        assertEquals(0, errors.size());
    }

    @Test
    void testOverlappingSchoolAndSummerPeriods_ReturnsError() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("2024-10-01T00:00:00")
                .schYrEnd("2025-06-30T00:00:00")
                .summerStart("2025-06-01T00:00:00") // overlaps with school year
                .summerEnd("2025-08-31T00:00:00")
                .periodStart("2020-07-30T00:00:00")
                .periodEnd("2026-08-31T00:00:00")
                .build();

        List<FieldError> errors = validator.validatePayload(period);

        assertEquals(3, errors.stream()
                .filter(e -> e.getField().equals(SCHOOL_YEAR_START)
                        || e.getField().equals(SUMMER_START))
                .count());
    }

    @Test
    void testOutOfBoundsDates_ReturnsError() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("2023-09-30T00:00:00") // before Oct 1 of current cycle
                .schYrEnd("2025-10-01T00:00:00")   // after Sep 30 of current cycle
                .summerStart("2025-07-01T00:00:00")
                .summerEnd("2025-08-31T00:00:00")
                .periodStart("2020-07-30T00:00:00")
                .periodEnd("2026-08-31T00:00:00")
                .build();

        List<FieldError> errors = validator.validatePayload(period);

        // Only count the cycle-boundary errors for school year
        long cycleErrors = errors.stream()
                .filter(e -> e.getDefaultMessage().contains("within the reporting cycle"))
                .count();
        assertEquals(1, cycleErrors);
    }

    @Test
    void testStartDateAfterEndDate_ReturnsError() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("2025-06-30T00:00:00")
                .schYrEnd("2025-01-01T00:00:00")
                .summerStart("2025-08-01T00:00:00")
                .summerEnd("2025-07-01T00:00:00")
                .periodStart("2020-07-30T00:00:00")
                .periodEnd("2026-08-31T00:00:00")
                .build();

        List<FieldError> errors = validator.validatePayload(period);
        long logicalErrors = errors.stream()
                .filter(e -> e.getDefaultMessage().contains("must be before or equal to"))
                .count();
        assertEquals(2, logicalErrors);
    }

    @Test
    void testInvalidDateFormat_ReturnsError() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("not-a-date")
                .schYrEnd("2025-01-01T00:00:00")
                .summerStart("2025-08-01T00:00:00")
                .summerEnd("2025-09-01T00:00:00")
                .periodStart("2020-07-30T00:00:00")
                .periodEnd("2026-08-31T00:00:00")
                .build();

        List<FieldError> errors = validator.validatePayload(period);
        assertEquals(1, errors.size());
        assertEquals("date", errors.get(0).getField());
    }

    @Test
    void testInvalidReportingPeriodID_ReturnsError() {
        UUID badId = UUID.randomUUID();
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(badId.toString())
                .schYrStart("2024-10-01T00:00:00")
                .schYrEnd("2025-06-30T00:00:00")
                .summerStart("2025-07-01T00:00:00")
                .summerEnd("2025-08-31T00:00:00")
                .periodStart("2020-07-30T00:00:00")
                .periodEnd("2026-08-31T00:00:00")
                .build();

        when(reportingPeriodRepository.findById(badId)).thenReturn(Optional.empty());

        List<FieldError> errors = validator.validatePayload(period);
        assertEquals(1, errors.size());
        assertEquals("reportingPeriodId", errors.get(0).getField());
    }

    @Test
    void testSchoolYearStartAfterSummerStart_ReturnsError() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("2025-08-01T00:00:00") // after summerStart
                .schYrEnd("2025-08-31T00:00:00")
                .summerStart("2025-07-01T00:00:00")
                .summerEnd("2025-07-31T00:00:00")
                .periodStart("2020-07-30T00:00:00")
                .periodEnd("2026-08-31T00:00:00")
                .build();

        List<FieldError> errors = validator.validatePayload(period);

        long orderingErrors = errors.stream()
                .filter(e -> e.getDefaultMessage().contains("must start before Summer")
                        || e.getDefaultMessage().contains("must start after School Year ends"))
                .count();
        assertEquals(2, orderingErrors);
    }

    @Test
    void testBothPeriodsInFuture_ReturnsCycleErrors() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("2025-10-01T00:00:00") // after cycle end
                .schYrEnd("2026-06-30T00:00:00")
                .summerStart("2026-07-01T00:00:00")
                .summerEnd("2026-08-31T00:00:00")
                .periodStart("2020-07-30T00:00:00")
                .periodEnd("2026-08-31T00:00:00")
                .build();

        List<FieldError> errors = validator.validatePayload(period);

        // Expect one cycle-boundary error for each period
        long cycleFieldErrors = errors.stream()
                .filter(e -> e.getField().equals(SCHOOL_YEAR_START)
                        || e.getField().equals(SUMMER_START))
                .count();
        assertEquals(2, cycleFieldErrors);
    }

    @Test
    void testBothPeriodsInPast_NoActivePeriod_ReturnsError() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("2024-01-01T00:00:00")
                .schYrEnd("2024-03-30T00:00:00")
                .summerStart("2024-04-01T00:00:00")
                .summerEnd("2024-04-30T00:00:00")
                .periodStart("2020-07-30T00:00:00")
                .periodEnd("2026-08-31T00:00:00")
                .build();

        List<FieldError> errors = validator.validatePayload(period);

        assertEquals(1, errors.stream()
                .filter(e -> e.getField().equals(INVALID_PERIOD) && e.getDefaultMessage().contains("No active reporting period"))
                .count());
    }
}
