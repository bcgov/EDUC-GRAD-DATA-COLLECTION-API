package ca.bc.gov.educ.graddatacollection.api.service;

import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ReportingPeriodEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ReportingPeriodRepository;
import ca.bc.gov.educ.graddatacollection.api.service.v1.ReportingSummaryService;
import ca.bc.gov.educ.graddatacollection.api.struct.external.gradschools.v1.GradSchool;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ReportingCycleSummary;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SchoolSubmissionCount;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportingSummaryServiceTest {

    @Mock
    private IncomingFilesetRepository incomingFilesetRepository;
    
    @Mock
    private ReportingPeriodRepository reportingPeriodRepository;
    
    @Mock
    private RestUtils restUtils;
    
    @InjectMocks
    private ReportingSummaryService reportingSummaryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetReportingSummary_Summer() {
        UUID reportingPeriodId = UUID.randomUUID();
        ReportingPeriodEntity entity = ReportingPeriodEntity.builder()
                .reportingPeriodID(reportingPeriodId)
                .summerStart(LocalDateTime.of(2025, 7, 1, 0, 0))
                .summerEnd(LocalDateTime.of(2025, 8, 31, 0, 0))
                .schYrStart(LocalDateTime.of(2025, 1, 1, 0, 0))
                .schYrEnd(LocalDateTime.of(2025, 6, 30, 0, 0))
                .build();
        when(reportingPeriodRepository.findById(reportingPeriodId)).thenReturn(Optional.of(entity));
        when(incomingFilesetRepository.findSchoolSubmissionsInSummerReportingPeriod(reportingPeriodId, entity.getSummerStart(), entity.getSummerEnd()))
                .thenReturn(Collections.emptyList());
        SchoolTombstone school = SchoolTombstone.builder().schoolId("SCHOOL1").schoolCategoryCode("PUBLIC").facilityTypeCode("STANDARD").openedDate("1964-09-01T00:00:00").build();

        var gradSchool = GradSchool.builder()
                .schoolID(UUID.randomUUID().toString())
                .canIssueTranscripts("Y")
                .canIssueCertificates("Y")
                .submissionModeCode("Append")
                .build();
        gradSchool.setSchoolID(school.getSchoolId());
        when(this.restUtils.getAllSchools()).thenReturn(List.of(school));
        when(this.restUtils.getGradSchoolBySchoolID(any())).thenReturn(Optional.of(gradSchool));

        ReportingCycleSummary result = reportingSummaryService.getReportingSummary(reportingPeriodId, "Summer");

        assertNotNull(result);
        assertFalse(result.getRows().isEmpty());
        assertTrue(result.getRows().stream().anyMatch(r -> r.getCategoryOrFacilityType().equals("Public") && r.getSchoolsExpected().equals("1")));
        verify(incomingFilesetRepository, times(1)).findSchoolSubmissionsInSummerReportingPeriod(reportingPeriodId, entity.getSummerStart(), entity.getSummerEnd());
    }

    @Test
    void testGetReportingSummary_Summer_WithClosedDateWithIn3Months() {
        UUID reportingPeriodId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime closedDate = now.minusMonths(2);
        ReportingPeriodEntity entity = ReportingPeriodEntity.builder()
                .reportingPeriodID(reportingPeriodId)
                .summerStart(now.minusMonths(1))
                .summerEnd(now.plusMonths(1))
                .schYrStart(now.minusMonths(6))
                .schYrEnd(now.minusMonths(2))
                .periodStart(now.minusMonths(6))
                .periodEnd(now.plusMonths(1))
                .build();
        when(reportingPeriodRepository.findById(reportingPeriodId)).thenReturn(Optional.of(entity));
        when(incomingFilesetRepository.findSchoolSubmissionsInSummerReportingPeriod(reportingPeriodId, entity.getSummerStart(), entity.getSummerEnd()))
                .thenReturn(Collections.emptyList());
        SchoolTombstone school = SchoolTombstone.builder()
                .schoolId("SCHOOL1")
                .schoolCategoryCode("PUBLIC")
                .facilityTypeCode("STANDARD")
                .openedDate("1964-09-01T00:00:00")
                .closedDate(closedDate.toString())
                .build();

        var gradSchool = GradSchool.builder()
                .schoolID(UUID.randomUUID().toString())
                .canIssueTranscripts("Y")
                .canIssueCertificates("Y")
                .submissionModeCode("Append")
                .build();
        gradSchool.setSchoolID(school.getSchoolId());
        when(this.restUtils.getAllSchools()).thenReturn(List.of(school));
        when(this.restUtils.getGradSchoolBySchoolID(any())).thenReturn(Optional.of(gradSchool));

        ReportingCycleSummary result = reportingSummaryService.getReportingSummary(reportingPeriodId, "Summer");

        assertNotNull(result);
        assertFalse(result.getRows().isEmpty());
        assertTrue(result.getRows().stream().anyMatch(r -> r.getCategoryOrFacilityType().equals("Public") && r.getSchoolsExpected().equals("1")));
        verify(incomingFilesetRepository, times(1)).findSchoolSubmissionsInSummerReportingPeriod(reportingPeriodId, entity.getSummerStart(), entity.getSummerEnd());
    }

    @Test
    void testGetReportingSummary_Summer_WithClosedDatePast3Months() {
        UUID reportingPeriodId = UUID.randomUUID();
        ReportingPeriodEntity entity = ReportingPeriodEntity.builder()
                .reportingPeriodID(reportingPeriodId)
                .summerStart(LocalDateTime.of(2025, 7, 1, 0, 0))
                .summerEnd(LocalDateTime.of(2025, 8, 31, 0, 0))
                .schYrStart(LocalDateTime.of(2025, 1, 1, 0, 0))
                .schYrEnd(LocalDateTime.of(2025, 6, 30, 0, 0))
                .build();
        when(reportingPeriodRepository.findById(reportingPeriodId)).thenReturn(Optional.of(entity));
        when(incomingFilesetRepository.findSchoolSubmissionsInSummerReportingPeriod(reportingPeriodId, entity.getSummerStart(), entity.getSummerEnd()))
                .thenReturn(Collections.emptyList());
        SchoolTombstone school = SchoolTombstone.builder()
                .schoolId("SCHOOL1")
                .schoolCategoryCode("PUBLIC")
                .facilityTypeCode("STANDARD")
                .openedDate("1964-09-01T00:00:00")
                .closedDate("2025-01-01T00:00:00")
                .build();

        var gradSchool = GradSchool.builder()
                .schoolID(UUID.randomUUID().toString())
                .canIssueTranscripts("Y")
                .canIssueCertificates("Y")
                .submissionModeCode("Append")
                .build();
        gradSchool.setSchoolID(school.getSchoolId());
        when(this.restUtils.getAllSchools()).thenReturn(List.of(school));
        when(this.restUtils.getGradSchoolBySchoolID(any())).thenReturn(Optional.of(gradSchool));


        ReportingCycleSummary result = reportingSummaryService.getReportingSummary(reportingPeriodId, "Summer");

        assertNotNull(result);
        assertFalse(result.getRows().isEmpty());
        assertTrue(result.getRows().stream().anyMatch(r -> r.getCategoryOrFacilityType().equals("Public") && r.getSchoolsExpected().equals("0")));
        verify(incomingFilesetRepository, times(1)).findSchoolSubmissionsInSummerReportingPeriod(reportingPeriodId, entity.getSummerStart(), entity.getSummerEnd());
    }

    @Test
    void testGetReportingSummary_SchoolYear() {
        UUID reportingPeriodId = UUID.randomUUID();
        ReportingPeriodEntity entity = ReportingPeriodEntity.builder()
                .reportingPeriodID(reportingPeriodId)
                .schYrStart(LocalDateTime.of(2025, 1, 1, 0, 0))
                .schYrEnd(LocalDateTime.of(2025, 6, 30, 0, 0))
                .summerStart(LocalDateTime.of(2025, 7, 1, 0, 0))
                .summerEnd(LocalDateTime.of(2025, 8, 31, 0, 0))
                .build();
        when(reportingPeriodRepository.findById(reportingPeriodId)).thenReturn(Optional.of(entity));
        when(incomingFilesetRepository.findSchoolSubmissionsInSchoolReportingPeriod(reportingPeriodId, entity.getSchYrStart(), entity.getSchYrEnd()))
                .thenReturn(Collections.emptyList());
        when(incomingFilesetRepository.findSchoolSubmissionsInLast30Days(reportingPeriodId, entity.getSchYrStart()))
                .thenReturn(Collections.emptyList());

        var gradSchool = GradSchool.builder()
                .schoolID(UUID.randomUUID().toString())
                .canIssueTranscripts("Y")
                .canIssueCertificates("Y")
                .submissionModeCode("Append")
                .build();
        when(this.restUtils.getAllSchools()).thenReturn(Collections.emptyList());
        when(this.restUtils.getGradSchoolBySchoolID(any())).thenReturn(Optional.of(gradSchool));

        ReportingCycleSummary result = reportingSummaryService.getReportingSummary(reportingPeriodId, "Other");
        assertNotNull(result);
        assertFalse(result.getRows().isEmpty());
        verify(incomingFilesetRepository, times(1)).findSchoolSubmissionsInSchoolReportingPeriod(reportingPeriodId, entity.getSchYrStart(), entity.getSchYrEnd());
        verify(incomingFilesetRepository, times(1)).findSchoolSubmissionsInLast30Days(reportingPeriodId, entity.getSchYrStart());
    }

    @Test
    void testGetSchoolSubmissionCounts_Summer() {
        UUID reportingPeriodId = UUID.randomUUID();
        ReportingPeriodEntity entity = ReportingPeriodEntity.builder()
                .reportingPeriodID(reportingPeriodId)
                .summerStart(LocalDateTime.of(2025, 7, 1, 0, 0))
                .summerEnd(LocalDateTime.of(2025, 8, 31, 0, 0))
                .schYrStart(LocalDateTime.of(2025, 1, 1, 0, 0))
                .schYrEnd(LocalDateTime.of(2025, 6, 30, 0, 0))
                .build();
        when(reportingPeriodRepository.findById(reportingPeriodId)).thenReturn(Optional.of(entity));
        
        SchoolSubmissionCount submission = new SchoolSubmissionCount() {
            @Override
            public String getSchoolID() { return "SCHOOL1"; }
            @Override
            public String getSubmissionCount() { return "1"; }
            @Override
            public LocalDateTime getLastSubmissionDate() { return null; }
        };
        
        when(incomingFilesetRepository.findSchoolSubmissionsInSummerReportingPeriod(reportingPeriodId, entity.getSummerStart(), entity.getSummerEnd()))
                .thenReturn(List.of(submission));
        SchoolTombstone school = SchoolTombstone.builder().schoolId("SCHOOL1").schoolCategoryCode("PUBLIC").facilityTypeCode("STANDARD").openedDate("1964-09-01T00:00:00").build();
        var gradSchool = GradSchool.builder()
                .schoolID(UUID.randomUUID().toString())
                .canIssueTranscripts("Y")
                .canIssueCertificates("Y")
                .submissionModeCode("Append")
                .build();
        gradSchool.setSchoolID(school.getSchoolId());
        when(this.restUtils.getAllSchools()).thenReturn(List.of(school));
        when(this.restUtils.getGradSchoolBySchoolID(any())).thenReturn(Optional.of(gradSchool));

        List<SchoolSubmissionCount> result = reportingSummaryService.getSchoolSubmissionCounts(reportingPeriodId, null, true);
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(incomingFilesetRepository, times(1))
                .findSchoolSubmissionsInSummerReportingPeriod(reportingPeriodId, entity.getSummerStart(), entity.getSummerEnd());
    }

    @Test
    void testGetSchoolSubmissionCounts_FilterByCategory() {
        UUID reportingPeriodId = UUID.randomUUID();
        ReportingPeriodEntity entity = ReportingPeriodEntity.builder()
                .reportingPeriodID(reportingPeriodId)
                .schYrStart(LocalDateTime.of(2025, 1, 1, 0, 0))
                .schYrEnd(LocalDateTime.of(2025, 6, 30, 0, 0))
                .summerStart(LocalDateTime.of(2025, 7, 1, 0, 0))
                .summerEnd(LocalDateTime.of(2025, 8, 31, 0, 0))
                .build();
        when(reportingPeriodRepository.findById(reportingPeriodId)).thenReturn(Optional.of(entity));
        
        SchoolSubmissionCount submission1 = new SchoolSubmissionCount() {
            @Override
            public String getSchoolID() { return "SCHOOL1"; }
            @Override
            public String getSubmissionCount() { return "2"; }
            @Override
            public LocalDateTime getLastSubmissionDate() { return null; }
        };
        SchoolSubmissionCount submission2 = new SchoolSubmissionCount() {
            @Override
            public String getSchoolID() { return "SCHOOL2"; }
            @Override
            public String getSubmissionCount() { return "3"; }
            @Override
            public LocalDateTime getLastSubmissionDate() { return null; }
        };
        
        when(incomingFilesetRepository.findSchoolSubmissionsInSchoolReportingPeriod(reportingPeriodId, entity.getSchYrStart(), entity.getSchYrEnd()))
                .thenReturn(List.of(submission1, submission2));
        SchoolTombstone school1 = SchoolTombstone.builder().schoolId("SCHOOL1").schoolCategoryCode("PUBLIC").facilityTypeCode("STANDARD").openedDate("1964-09-01T00:00:00").build();
        SchoolTombstone school2 = SchoolTombstone.builder().schoolId("SCHOOL2").schoolCategoryCode("INDEPEND").facilityTypeCode("STANDARD").openedDate("1964-09-01T00:00:00").build();
        var gradSchool = GradSchool.builder()
                .schoolID(UUID.randomUUID().toString())
                .canIssueTranscripts("Y")
                .canIssueCertificates("Y")
                .submissionModeCode("Append")
                .build();
        gradSchool.setSchoolID(school1.getSchoolId());
        when(this.restUtils.getAllSchools()).thenReturn(List.of(school1, school2));
        when(this.restUtils.getGradSchoolBySchoolID(any())).thenReturn(Optional.of(gradSchool));

        List<SchoolSubmissionCount> result = reportingSummaryService.getSchoolSubmissionCounts(reportingPeriodId, "PUBLIC", false);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SCHOOL1", result.getFirst().getSchoolID());

        List<SchoolSubmissionCount> result2 = reportingSummaryService.getSchoolSubmissionCounts(reportingPeriodId, "INDEPEND", false);
        assertNotNull(result2);
        assertEquals(1, result2.size());
        assertEquals("SCHOOL2", result2.getFirst().getSchoolID());

        List<SchoolSubmissionCount> resultAll = reportingSummaryService.getSchoolSubmissionCounts(reportingPeriodId, "", false);
        assertEquals(2, resultAll.size());
        verify(incomingFilesetRepository, times(3))
                .findSchoolSubmissionsInSchoolReportingPeriod(reportingPeriodId, entity.getSchYrStart(), entity.getSchYrEnd());
    }
    
    @Test
    void testGetReportingSummary_ThrowsEntityNotFoundException_WhenReportingPeriodNotFound() {
        UUID reportingPeriodId = UUID.randomUUID();
        when(reportingPeriodRepository.findById(reportingPeriodId)).thenReturn(Optional.empty());
        
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> reportingSummaryService.getReportingSummary(reportingPeriodId, "Summer"));
        assertNotNull(exception.getMessage());
    }
}

