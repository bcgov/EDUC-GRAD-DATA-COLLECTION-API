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
                .periodStart(LocalDateTime.of(2024, 9, 1, 0, 0))
                .periodEnd(LocalDateTime.of(2026, 9, 1, 0, 0))
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

    @Test
    void testGetReportingPeriod_ReturnsEntity() {
        UUID reportingPeriodID = UUID.randomUUID();
        ReportingPeriodEntity testEntity = ReportingPeriodEntity.builder()
                .reportingPeriodID(reportingPeriodID)
                .schYrStart(LocalDateTime.of(2025, 1, 1, 0, 0))
                .schYrEnd(LocalDateTime.of(2025, 6, 30, 0, 0))
                .summerStart(LocalDateTime.of(2025, 7, 1, 0, 0))
                .summerEnd(LocalDateTime.of(2025, 8, 31, 0, 0))
                .periodStart(LocalDateTime.of(2024, 9, 1, 0, 0))
                .periodEnd(LocalDateTime.of(2026, 9, 1, 0, 0))
                .createUser("testUser")
                .createDate(LocalDateTime.now())
                .updateUser("testUser")
                .updateDate(LocalDateTime.now())
                .build();

        when(reportingPeriodRepository.findById(any())).thenReturn(Optional.of(testEntity));

        ReportingPeriodEntity result = reportingPeriodService.getReportingPeriod(reportingPeriodID);

        assertEquals(testEntity, result);
        verify(reportingPeriodRepository, times(1)).findById(any());
    }

    @Test
    void testGetReportingPeriod_ThrowsEntityNotFoundException_WhenEmpty() {
        when(reportingPeriodRepository.findById(any())).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> reportingPeriodService.getReportingPeriod(UUID.randomUUID()));
        assertNotNull(exception.getMessage());
        verify(reportingPeriodRepository, times(1)).findById(any());
    }

    @Test
    void testUpdateReportingPeriod_Success() {
        UUID reportingPeriodId = UUID.randomUUID();

        ReportingPeriodEntity existingEntity = ReportingPeriodEntity.builder()
                .reportingPeriodID(reportingPeriodId)
                .createUser("originalUser")
                .createDate(LocalDateTime.of(2024, 1, 1, 0, 0))
                .build();

        ReportingPeriodEntity updatedEntity = ReportingPeriodEntity.builder()
                .reportingPeriodID(reportingPeriodId)
                .schYrStart(LocalDateTime.of(2025, 1, 1, 0, 0))
                .schYrEnd(LocalDateTime.of(2025, 6, 30, 0, 0))
                .summerStart(LocalDateTime.of(2025, 7, 1, 0, 0))
                .summerEnd(LocalDateTime.of(2025, 8, 31, 0, 0))
                .periodStart(LocalDateTime.of(2024, 9, 1, 0, 0))
                .periodEnd(LocalDateTime.of(2026, 9, 1, 0, 0))
                .updateUser("editorUser")
                .updateDate(LocalDateTime.now())
                .build();

        when(reportingPeriodRepository.findById(reportingPeriodId)).thenReturn(Optional.of(existingEntity));
        when(reportingPeriodRepository.save(any(ReportingPeriodEntity.class))).thenReturn(updatedEntity);

        ReportingPeriodEntity result = reportingPeriodService.updateReportingPeriod(updatedEntity);

        assertEquals(updatedEntity.getSchYrStart(), result.getSchYrStart());
        assertEquals(updatedEntity.getSchYrEnd(), result.getSchYrEnd());
        assertEquals(updatedEntity.getUpdateUser(), result.getUpdateUser());
        verify(reportingPeriodRepository).findById(reportingPeriodId);
        verify(reportingPeriodRepository).save(any(ReportingPeriodEntity.class));
    }

    @Test
    void testUpdateReportingPeriod_ThrowsEntityNotFoundException_WhenIdNotFound() {
        UUID reportingPeriodId = UUID.randomUUID();
        ReportingPeriodEntity updateAttempt = ReportingPeriodEntity.builder()
                .reportingPeriodID(reportingPeriodId)
                .build();

        when(reportingPeriodRepository.findById(reportingPeriodId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> reportingPeriodService.updateReportingPeriod(updateAttempt));
    }

    @Test
    void testGetPreviousReportingPeriod_ReturnsEntity() {
        ReportingPeriodEntity previous = ReportingPeriodEntity.builder()
                .reportingPeriodID(UUID.randomUUID())
                .schYrStart(LocalDateTime.of(2023, 10, 1, 0, 0))
                .schYrEnd(LocalDateTime.of(2024, 6, 30, 0, 0))
                .summerStart(LocalDateTime.of(2024, 7, 1, 0, 0))
                .summerEnd(LocalDateTime.of(2024, 8, 31, 0, 0))
                .periodStart(LocalDateTime.of(2022, 9, 1, 0, 0))
                .periodEnd(LocalDateTime.of(2026, 9, 1, 0, 0))
                .createUser("testUser")
                .createDate(LocalDateTime.now())
                .updateUser("testUser")
                .updateDate(LocalDateTime.now())
                .build();

        when(reportingPeriodRepository.findPreviousReportingPeriod()).thenReturn(Optional.of(previous));

        ReportingPeriodEntity result = reportingPeriodService.getPreviousReportingPeriod();

        assertEquals(previous, result);
        verify(reportingPeriodRepository, times(1)).findPreviousReportingPeriod();
    }

    @Test
    void testGetPreviousReportingPeriod_ThrowsEntityNotFoundException_WhenEmpty() {
        when(reportingPeriodRepository.findPreviousReportingPeriod()).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> reportingPeriodService.getPreviousReportingPeriod());

        assertNotNull(exception.getMessage());
        verify(reportingPeriodRepository, times(1)).findPreviousReportingPeriod();
    }
}
