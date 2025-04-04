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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ReportingPeriodValidatorTest {

    private ReportingPeriodRepository reportingPeriodRepository;
    private ReportingPeriodValidator validator;

    @BeforeEach
    void setUp() {
        reportingPeriodRepository = mock(ReportingPeriodRepository.class);
        validator = new ReportingPeriodValidator(reportingPeriodRepository);
    }

    @Test
    void testValidatePayload_ValidReportingPeriodID_NoErrors() {
        UUID id = UUID.randomUUID();
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .build();

        when(reportingPeriodRepository.findById(id))
                .thenReturn(Optional.of(ReportingPeriodEntity.builder().reportingPeriodID(id).build()));

        List<FieldError> errors = validator.validatePayload(period);
        assertEquals(0, errors.size());
    }

    @Test
    void testValidatePayload_InvalidReportingPeriodID_ReturnsError() {
        UUID id = UUID.randomUUID();
        ReportingPeriod period = ReportingPeriod.builder()
                .reportingPeriodID(id.toString())
                .build();

        when(reportingPeriodRepository.findById(id)).thenReturn(Optional.empty());

        List<FieldError> errors = validator.validatePayload(period);
        assertEquals(1, errors.size());
        assertEquals("reportingPeriodId", errors.get(0).getField());
        assertEquals(id.toString(), errors.get(0).getRejectedValue());
    }
}
