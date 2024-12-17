package ca.bc.gov.educ.graddatacollection.api.service;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.service.v1.IncomingFilesetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

class IncomingFilesetServiceTest extends BaseGradDataCollectionAPITest {
    @MockBean
    protected RestUtils restUtils;
    @Autowired
    ApplicationProperties applicationProperties;
    @Autowired
    IncomingFilesetService incomingFilesetService;
    @Autowired
    IncomingFilesetRepository incomingFilesetRepository;

    @BeforeEach
    public void setUp() {
        this.incomingFilesetRepository.deleteAll();
    }

    @Test
    void testStaleIncomingFilesetsArePurged() {
        var mockFileset = this.setupMockIncomingFileset(false, LocalDateTime.now().minusHours(applicationProperties.getIncomingFilesetStaleInHours() + 1));
        assertThat(incomingFilesetRepository.findById(mockFileset.getIncomingFilesetID())).isNotEmpty();
        incomingFilesetService.purgeStaleIncomingFilesetRecords();
        assertThat(incomingFilesetRepository.findById(mockFileset.getIncomingFilesetID())).isEmpty();
    }

    @Test
    void testFreshIncomingFilesetsAreNotPurged() {
        var mockFileset = this.setupMockIncomingFileset(false, LocalDateTime.now().minusHours(applicationProperties.getIncomingFilesetStaleInHours() - 1));
        assertThat(incomingFilesetRepository.findById(mockFileset.getIncomingFilesetID())).isNotEmpty();
        incomingFilesetService.purgeStaleIncomingFilesetRecords();
        assertThat(incomingFilesetRepository.findById(mockFileset.getIncomingFilesetID())).isNotEmpty();
    }

    @Test
    void testStaleAndLoadedIncomingFilesetsAreNotPurged() {
        var mockFileset = this.setupMockIncomingFileset(true, LocalDateTime.now().minusHours(applicationProperties.getIncomingFilesetStaleInHours() + 1));
        assertThat(incomingFilesetRepository.findById(mockFileset.getIncomingFilesetID())).isNotEmpty();
        incomingFilesetService.purgeStaleIncomingFilesetRecords();
        assertThat(incomingFilesetRepository.findById(mockFileset.getIncomingFilesetID())).isNotEmpty();
    }

    @Test
    void testFreshAndLoadedIncomingFilesetsAreNotPurged() {
        var mockFileset = this.setupMockIncomingFileset(true, LocalDateTime.now().minusHours(applicationProperties.getIncomingFilesetStaleInHours() - 1));
        assertThat(incomingFilesetRepository.findById(mockFileset.getIncomingFilesetID())).isNotEmpty();
        incomingFilesetService.purgeStaleIncomingFilesetRecords();
        assertThat(incomingFilesetRepository.findById(mockFileset.getIncomingFilesetID())).isNotEmpty();
    }

    private IncomingFilesetEntity setupMockIncomingFileset(boolean allFilesUploaded, LocalDateTime timestamp) {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(school.getSchoolId())).thenReturn(Optional.of(school));

        var mockFileset = allFilesUploaded ? this.createMockIncomingFilesetEntityWithAllFilesLoaded() : this.createMockIncomingFilesetEntityWithDEMFile(UUID.fromString(school.getSchoolId()));
        mockFileset.setSchoolID(UUID.fromString(school.getSchoolId()));
        mockFileset.setCreateDate(timestamp);
        mockFileset.setUpdateDate(timestamp);

        return incomingFilesetRepository.save(mockFileset);
    }
}
