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

import static ca.bc.gov.educ.graddatacollection.api.validator.ReportingPeriodValidator.INVALID_PERIOD;
import static ca.bc.gov.educ.graddatacollection.api.validator.ReportingPeriodValidator.REPORITNG_PERIOD_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
                .periodStart("2024-10-01T00:00:00")
                .periodEnd("2025-09-30T00:00:00")
                .build();

        List<FieldError> errors = validator.validatePayload(period);
        assertEquals(0, errors.size());
    }

    @Test
    void testReportingPeriodIdNotFound_ReturnsError() {
        UUID badId = UUID.randomUUID();
        when(reportingPeriodRepository.findById(badId)).thenReturn(Optional.empty());
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(badId.toString())
                .schYrStart("2024-10-01T00:00:00")
                .schYrEnd("2025-06-30T00:00:00")
                .summerStart("2025-07-01T00:00:00")
                .summerEnd("2025-08-31T00:00:00")
                .periodStart("2024-10-01T00:00:00")
                .periodEnd("2025-09-30T00:00:00")
                .build();
        List<FieldError> errors = validator.validatePayload(period);
        assertEquals(1, errors.size());
        assertEquals(REPORITNG_PERIOD_ID, errors.get(0).getField());
    }

    @Test
    void testInvalidReportingPeriodIdFormat_ReturnsError() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID("not-a-uuid")
                .schYrStart("2024-10-01T00:00:00")
                .schYrEnd("2025-06-30T00:00:00")
                .summerStart("2025-07-01T00:00:00")
                .summerEnd("2025-08-31T00:00:00")
                .periodStart("2024-10-01T00:00:00")
                .periodEnd("2025-09-30T00:00:00")
                .build();
        try {
            validator.validatePayload(period);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    void testInvalidDateFormat_ReturnsError() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("not-a-date")
                .schYrEnd("2025-06-30T00:00:00")
                .summerStart("2025-07-01T00:00:00")
                .summerEnd("2025-08-31T00:00:00")
                .periodStart("2024-10-01T00:00:00")
                .periodEnd("2025-09-30T00:00:00")
                .build();
        List<FieldError> errors = validator.validatePayload(period);
        assertEquals(1, errors.size());
        assertEquals("date", errors.get(0).getField());
    }

    @Test
    void testSchoolYearStartAfterEnd_ReturnsError() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("2025-07-01T00:00:00")
                .schYrEnd("2025-06-30T00:00:00")
                .summerStart("2025-07-02T00:00:00")
                .summerEnd("2025-08-31T00:00:00")
                .periodStart("2024-10-01T00:00:00")
                .periodEnd("2025-09-30T00:00:00")
                .build();
        List<FieldError> errors = validator.validatePayload(period);
        assertTrue(errors.stream().anyMatch(e -> e.getDefaultMessage().contains("School Year start date must be before or equal to end date.")));
    }

    @Test
    void testSummerStartAfterEnd_ReturnsError() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("2024-10-01T00:00:00")
                .schYrEnd("2025-06-30T00:00:00")
                .summerStart("2025-08-31T00:00:00")
                .summerEnd("2025-07-01T00:00:00")
                .periodStart("2024-10-01T00:00:00")
                .periodEnd("2025-09-30T00:00:00")
                .build();
        List<FieldError> errors = validator.validatePayload(period);
        assertTrue(errors.stream().anyMatch(e -> e.getDefaultMessage().contains("Summer start date must be before or equal to end date.")));
    }

    @Test
    void testSchoolYearOutsideReportingCycle_ReturnsError() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("2024-09-30T00:00:00")
                .schYrEnd("2025-10-01T00:00:00")
                .summerStart("2025-07-01T00:00:00")
                .summerEnd("2025-08-31T00:00:00")
                .periodStart("2024-10-01T00:00:00")
                .periodEnd("2025-09-30T00:00:00")
                .build();
        List<FieldError> errors = validator.validatePayload(period);
        assertTrue(errors.stream().anyMatch(e -> e.getDefaultMessage().contains("School Year must be within the reporting cycle")));
    }

    @Test
    void testSummerOutsideReportingCycle_ReturnsError() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("2024-10-01T00:00:00")
                .schYrEnd("2025-06-30T00:00:00")
                .summerStart("2025-09-29T00:00:00")
                .summerEnd("2025-10-01T00:00:00")
                .periodStart("2024-10-01T00:00:00")
                .periodEnd("2025-09-30T00:00:00")
                .build();
        List<FieldError> errors = validator.validatePayload(period);
        assertTrue(errors.stream().anyMatch(e -> e.getDefaultMessage().contains("Summer must be within the reporting cycle")));
    }

    @Test
    void testSchoolYearAndSummerOverlap_ReturnsError() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("2024-10-01T00:00:00")
                .schYrEnd("2025-07-15T00:00:00")
                .summerStart("2025-07-01T00:00:00")
                .summerEnd("2025-08-31T00:00:00")
                .periodStart("2024-10-01T00:00:00")
                .periodEnd("2025-09-30T00:00:00")
                .build();
        List<FieldError> errors = validator.validatePayload(period);
        assertTrue(errors.stream().anyMatch(e -> e.getField().equals(INVALID_PERIOD)));
    }

    @Test
    void testSchoolYearStartAfterSummerStart_ReturnsError() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("2025-08-01T00:00:00")
                .schYrEnd("2025-08-31T00:00:00")
                .summerStart("2025-07-01T00:00:00")
                .summerEnd("2025-07-31T00:00:00")
                .periodStart("2024-10-01T00:00:00")
                .periodEnd("2025-09-30T00:00:00")
                .build();
        List<FieldError> errors = validator.validatePayload(period);
        assertTrue(errors.stream().anyMatch(e -> e.getDefaultMessage().contains("School Year must start before Summer.")));
    }

    @Test
    void testSummerStartBeforeSchoolYearEnd_ReturnsError() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("2024-10-01T00:00:00")
                .schYrEnd("2025-07-15T00:00:00")
                .summerStart("2025-07-01T00:00:00")
                .summerEnd("2025-08-31T00:00:00")
                .periodStart("2024-10-01T00:00:00")
                .periodEnd("2025-09-30T00:00:00")
                .build();
        List<FieldError> errors = validator.validatePayload(period);
        assertTrue(errors.stream().anyMatch(e -> e.getDefaultMessage().contains("Summer must start after School Year ends.")));
    }

    @Test
    void testPeriodStartAfterPeriodEnd_ReturnsError() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("2024-10-01T00:00:00")
                .schYrEnd("2025-06-30T00:00:00")
                .summerStart("2025-07-01T00:00:00")
                .summerEnd("2025-08-31T00:00:00")
                .periodStart("2025-09-30T00:00:00")
                .periodEnd("2024-10-01T00:00:00")
                .build();
        List<FieldError> errors = validator.validatePayload(period);
        assertTrue(errors.stream().anyMatch(e -> e.getDefaultMessage().contains("School Year must be within the reporting cycle")));
        assertTrue(errors.stream().anyMatch(e -> e.getDefaultMessage().contains("Summer must be within the reporting cycle")));
    }

    @Test
    void testSchoolYearAndSummerExactlyTouching_NoOverlap_NoError() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("2024-10-01T00:00:00")
                .schYrEnd("2025-06-30T00:00:00")
                .summerStart("2025-06-30T00:00:00")
                .summerEnd("2025-08-31T00:00:00")
                .periodStart("2024-10-01T00:00:00")
                .periodEnd("2025-09-30T00:00:00")
                .build();
        List<FieldError> errors = validator.validatePayload(period);
        assertTrue(errors.stream().anyMatch(e -> e.getField().equals(INVALID_PERIOD)));
    }

    @Test
    void testSchoolYearAndSummerIdenticalDates_ReturnsOverlapError() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("2024-10-01T00:00:00")
                .schYrEnd("2025-08-31T00:00:00")
                .summerStart("2024-10-01T00:00:00")
                .summerEnd("2025-08-31T00:00:00")
                .periodStart("2024-10-01T00:00:00")
                .periodEnd("2025-09-30T00:00:00")
                .build();
        List<FieldError> errors = validator.validatePayload(period);
        assertTrue(errors.stream().anyMatch(e -> e.getField().equals(INVALID_PERIOD)));
    }

    @Test
    void testSummerStartEqualsSchoolYearEnd_ReturnsOverlapError() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("2024-10-01T00:00:00")
                .schYrEnd("2025-07-01T00:00:00")
                .summerStart("2025-07-01T00:00:00")
                .summerEnd("2025-08-31T00:00:00")
                .periodStart("2024-10-01T00:00:00")
                .periodEnd("2025-09-30T00:00:00")
                .build();
        List<FieldError> errors = validator.validatePayload(period);
        assertTrue(errors.stream().anyMatch(e -> e.getField().equals(INVALID_PERIOD)));
    }

    @Test
    void testSchoolYearAndSummerNonOverlapping_NoError() {
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .schYrStart("2024-10-01T00:00:00")
                .schYrEnd("2025-06-29T00:00:00")
                .summerStart("2025-06-30T00:00:00")
                .summerEnd("2025-08-31T00:00:00")
                .periodStart("2024-10-01T00:00:00")
                .periodEnd("2025-09-30T00:00:00")
                .build();
        List<FieldError> errors = validator.validatePayload(period);
        assertFalse(errors.stream().anyMatch(e -> e.getField().equals(INVALID_PERIOD)));
    }
}
