package ca.bc.gov.educ.graddatacollection.api.service;

import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ReportingPeriodEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ReportingPeriodRepository;
import ca.bc.gov.educ.graddatacollection.api.service.v1.ReportingPeriodService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportingPeriodServiceTest {

    @Mock
    private ReportingPeriodRepository reportingPeriodRepository;

    @InjectMocks
    private ReportingPeriodService reportingPeriodService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetActiveReportingPeriod_ReturnsEntity() {
        ReportingPeriodEntity testEntity = ReportingPeriodEntity.builder()
                .reportingPeriodID(UUID.randomUUID())
                .schYrStart(LocalDateTime.of(2025, 1, 1, 0, 0))
                .schYrEnd(LocalDateTime.of(2025, 6, 30, 0, 0))
                .summerStart(LocalDateTime.of(2025, 7, 1, 0, 0))
                .summerEnd(LocalDateTime.of(2025, 8, 31, 0, 0))
                .createUser("testUser")
                .createDate(LocalDateTime.now())
                .updateUser("testUser")
                .updateDate(LocalDateTime.now())
                .build();

        when(reportingPeriodRepository.findActiveReportingPeriod()).thenReturn(Optional.of(testEntity));

        ReportingPeriodEntity result = reportingPeriodService.getActiveReportingPeriod();

        assertEquals(testEntity, result);
        verify(reportingPeriodRepository, times(1)).findActiveReportingPeriod();
    }

    @Test
    void testGetActiveReportingPeriod_ThrowsEntityNotFoundException_WhenEmpty() {
        when(reportingPeriodRepository.findActiveReportingPeriod()).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> reportingPeriodService.getActiveReportingPeriod());

        assertNotNull(exception.getMessage());
        verify(reportingPeriodRepository, times(1)).findActiveReportingPeriod();
    }
}
