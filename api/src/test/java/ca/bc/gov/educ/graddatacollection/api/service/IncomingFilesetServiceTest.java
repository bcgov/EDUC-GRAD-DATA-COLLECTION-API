package ca.bc.gov.educ.graddatacollection.api.service;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ReportingPeriodRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.service.v1.IncomingFilesetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IncomingFilesetServiceTest extends BaseGradDataCollectionAPITest {
    @MockBean
    protected RestUtils restUtils;
    @MockBean
    protected MessagePublisher messagePublisher;
    @Autowired
    ApplicationProperties applicationProperties;
    @Autowired
    IncomingFilesetService incomingFilesetService;
    @Autowired
    IncomingFilesetRepository incomingFilesetRepository;
    @Autowired
    ReportingPeriodRepository reportingPeriodRepository;

    @BeforeEach
    public void setUp() {
        this.incomingFilesetRepository.deleteAll();
        this.reportingPeriodRepository.deleteAll();
    }

    @Test
    void testStaleIncomingFilesetsArePurged() {
        var mockFileset = this.setupMockIncomingFileset(false, LocalDateTime.now().minusHours(applicationProperties.getIncomingFilesetStaleInHours() + 1));
        assertThat(incomingFilesetRepository.findById(mockFileset.getIncomingFilesetID())).isNotEmpty();
        incomingFilesetService.purgeStaleFinalIncomingFilesetRecords();
        assertThat(incomingFilesetRepository.findById(mockFileset.getIncomingFilesetID())).isEmpty();
    }

    @Test
    void testFreshIncomingFilesetsAreNotPurged() {
        var mockFileset = this.setupMockIncomingFileset(false, LocalDateTime.now().minusHours(applicationProperties.getIncomingFilesetStaleInHours() - 1));
        assertThat(incomingFilesetRepository.findById(mockFileset.getIncomingFilesetID())).isNotEmpty();
        incomingFilesetService.purgeStaleFinalIncomingFilesetRecords();
        assertThat(incomingFilesetRepository.findById(mockFileset.getIncomingFilesetID())).isNotEmpty();
    }

    @Test
    void testStaleAndLoadedIncomingFilesetsAreNotPurged() {
        var mockFileset = this.setupMockIncomingFileset(true, LocalDateTime.now().minusHours(applicationProperties.getIncomingFilesetStaleInHours() + 1));
        assertThat(incomingFilesetRepository.findById(mockFileset.getIncomingFilesetID())).isNotEmpty();
        incomingFilesetService.purgeStaleFinalIncomingFilesetRecords();
        assertThat(incomingFilesetRepository.findById(mockFileset.getIncomingFilesetID())).isNotEmpty();
    }

    @Test
    void testFreshAndLoadedIncomingFilesetsAreNotPurged() {
        var mockFileset = this.setupMockIncomingFileset(true, LocalDateTime.now().minusHours(applicationProperties.getIncomingFilesetStaleInHours() - 1));
        assertThat(incomingFilesetRepository.findById(mockFileset.getIncomingFilesetID())).isNotEmpty();
        incomingFilesetService.purgeStaleFinalIncomingFilesetRecords();
        assertThat(incomingFilesetRepository.findById(mockFileset.getIncomingFilesetID())).isNotEmpty();
    }

    @Test
    void testPrepareAndSendCompletedFilesetsForFurtherProcessing() {
        var school = this.createMockSchoolTombstone();
        when(this.restUtils.getSchoolBySchoolID(school.getSchoolId())).thenReturn(Optional.of(school));

        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = this.createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        mockFileset.setSchoolID(UUID.fromString(school.getSchoolId()));

        incomingFilesetRepository.save(mockFileset);

        mockFileset.getDemographicStudentEntities().add(createMockDemographicStudent(mockFileset));
        mockFileset.getAssessmentStudentEntities().add(createMockAssessmentStudentFromFileset(mockFileset));
        mockFileset.getCourseStudentEntities().add(createMockCourseStudent(mockFileset));
        mockFileset.getErrorFilesetStudentEntities().add(createMockErrorFilesetStudentEntity(mockFileset));
        
        incomingFilesetRepository.save(mockFileset);

        incomingFilesetService.prepareAndSendCompletedFilesetsForFurtherProcessing(List.of(mockFileset.getIncomingFilesetID()));

        verify(messagePublisher, times(1)).dispatchMessage(any(String.class), any(byte[].class));
    }

    private IncomingFilesetEntity setupMockIncomingFileset(boolean allFilesUploaded, LocalDateTime timestamp) {
        var school = this.createMockSchoolTombstone();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(school.getSchoolId())).thenReturn(Optional.of(school));

        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = allFilesUploaded ? this.createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod) : this.createMockIncomingFilesetEntityWithDEMFile(UUID.fromString(school.getSchoolId()), reportingPeriod);
        mockFileset.setSchoolID(UUID.fromString(school.getSchoolId()));
        mockFileset.setCreateDate(timestamp);
        mockFileset.setUpdateDate(timestamp);

        return incomingFilesetRepository.save(mockFileset);
    }
}
