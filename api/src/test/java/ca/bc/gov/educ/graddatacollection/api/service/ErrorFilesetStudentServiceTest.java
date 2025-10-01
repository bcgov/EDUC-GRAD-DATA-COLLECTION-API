package ca.bc.gov.educ.graddatacollection.api.service;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ErrorFilesetStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ErrorFilesetStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ReportingPeriodRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.service.v1.ErrorFilesetStudentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ErrorFilesetStudentServiceTest extends BaseGradDataCollectionAPITest {
    @MockBean
    protected RestUtils restUtils;
    @Autowired
    IncomingFilesetRepository incomingFilesetRepository;
    @Autowired
    ErrorFilesetStudentRepository errorFilesetStudentRepository;
    @Autowired
    ErrorFilesetStudentService errorFilesetStudentService;
    @Autowired
    ReportingPeriodRepository reportingPeriodRepository;
    @Autowired
    DemographicStudentRepository demographicStudentRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        demographicStudentRepository.deleteAll();
        errorFilesetStudentRepository.deleteAll();
        incomingFilesetRepository.deleteAll();
        reportingPeriodRepository.deleteAll();
    }

    @Test
    void testFlagErrorOnStudents_NewStudentError_NoExceptions() {
        var school = this.createMockSchoolTombstone();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        mockFileset.setSchoolID(UUID.fromString(school.getSchoolId()));
        var fileset = incomingFilesetRepository.save(mockFileset);
        LocalDateTime current = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        try {
            errorFilesetStudentService.flagErrorOnStudent(fileset.getIncomingFilesetID(), "123456789", null, "ABC", current, "ABC", current);
            Optional<ErrorFilesetStudentEntity> saved =  errorFilesetStudentRepository.findByIncomingFileset_IncomingFilesetIDAndPen(fileset.getIncomingFilesetID(), "123456789");
            assertThat(saved).isPresent();
        } catch(Exception e) {
            Assertions.fail("Should not have thrown any exception");
        }
    }

    @Test
    void testFlagErrorOnStudents_NewStudentError_WithDemographics_NoExceptions() {
        var school = this.createMockSchoolTombstone();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        mockFileset.setSchoolID(UUID.fromString(school.getSchoolId()));
        var fileset = incomingFilesetRepository.save(mockFileset);
        LocalDateTime current = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        try {
            var demRecord = createMockDemographicStudent(fileset);
            demRecord.setFirstName("Test");
            demRecord.setLastName("McTest");
            demRecord.setLocalID("1234");
            demRecord.setBirthdate("19000101");
            errorFilesetStudentService.flagErrorOnStudent(fileset.getIncomingFilesetID(), "123456789", demRecord, "ABC", current, "ABC", current);
            Optional<ErrorFilesetStudentEntity> saved =  errorFilesetStudentRepository.findByIncomingFileset_IncomingFilesetIDAndPen(fileset.getIncomingFilesetID(), "123456789");
            assertThat(saved).isPresent();
            ErrorFilesetStudentEntity savedErrorFilesetStudentEntity = saved.get();
            assertThat(savedErrorFilesetStudentEntity.getFirstName()).isEqualTo("Test");
            assertThat(savedErrorFilesetStudentEntity.getLastName()).isEqualTo("McTest");
            assertThat(savedErrorFilesetStudentEntity.getLocalID()).isEqualTo("1234");
            assertThat(savedErrorFilesetStudentEntity.getBirthdate()).isEqualTo("19000101");
            assertThat(savedErrorFilesetStudentEntity.getCreateUser()).isEqualTo("ABC");
            assertThat(savedErrorFilesetStudentEntity.getCreateDate().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(current);
            assertThat(savedErrorFilesetStudentEntity.getUpdateUser()).isEqualTo("ABC");
            assertThat(savedErrorFilesetStudentEntity.getUpdateDate().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(current);
        } catch(Exception e) {
            Assertions.fail("Should not have thrown any exception");
        }
    }

    @Test
    void testFlagErrorOnStudents_NewStudentError_isDemLoadedFalse_DemographicsNotSaved() {
        var school = this.createMockSchoolTombstone();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        mockFileset.setSchoolID(UUID.fromString(school.getSchoolId()));
        var fileset = incomingFilesetRepository.save(mockFileset);
        LocalDateTime current = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        try {
            errorFilesetStudentService.flagErrorOnStudent(fileset.getIncomingFilesetID(), "123456789", null, "ABC", current, "ABC", current);
            Optional<ErrorFilesetStudentEntity> saved =  errorFilesetStudentRepository.findByIncomingFileset_IncomingFilesetIDAndPen(fileset.getIncomingFilesetID(), "123456789");
            assertThat(saved).isPresent();
            ErrorFilesetStudentEntity savedErrorFilesetStudentEntity = saved.get();
            assertThat(savedErrorFilesetStudentEntity.getFirstName()).isNull();
            assertThat(savedErrorFilesetStudentEntity.getLastName()).isNull();
            assertThat(savedErrorFilesetStudentEntity.getLocalID()).isNull();
            assertThat(savedErrorFilesetStudentEntity.getBirthdate()).isNull();
            assertThat(savedErrorFilesetStudentEntity.getCreateUser()).isEqualTo("ABC");
            assertThat(savedErrorFilesetStudentEntity.getCreateDate().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(current);
            assertThat(savedErrorFilesetStudentEntity.getUpdateUser()).isEqualTo("ABC");
            assertThat(savedErrorFilesetStudentEntity.getUpdateDate().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(current);
        } catch(Exception e) {
            Assertions.fail("Should not have thrown any exception");
        }
    }

    @Test
    void testFlagErrorOnStudents_ExistingStudentError_WithoutDemographics_IsNotUpdated() {
        var school = this.createMockSchoolTombstone();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        mockFileset.setSchoolID(UUID.fromString(school.getSchoolId()));
        var fileset = incomingFilesetRepository.save(mockFileset);

        LocalDateTime current = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        var errorStudent = createMockErrorFilesetStudentEntity(fileset);
        errorStudent.setPen("123456789");
        errorStudent.setCreateDate(current.minusMinutes(1));
        errorStudent.setUpdateDate(current.minusMinutes(1));
        errorFilesetStudentRepository.save(errorStudent);

        try {
            errorFilesetStudentService.flagErrorOnStudent(fileset.getIncomingFilesetID(), "123456789", null, "ABC", current, "ABC", current);
            Optional<ErrorFilesetStudentEntity> saved =  errorFilesetStudentRepository.findByIncomingFileset_IncomingFilesetIDAndPen(fileset.getIncomingFilesetID(), "123456789");
            assertThat(saved).isPresent();
            ErrorFilesetStudentEntity savedErrorFilesetStudentEntity = saved.get();
            assertThat(savedErrorFilesetStudentEntity.getCreateUser()).isNotEqualTo("ABC");
            assertThat(savedErrorFilesetStudentEntity.getCreateUser()).isEqualTo(errorStudent.getCreateUser());
            assertThat(savedErrorFilesetStudentEntity.getCreateDate().truncatedTo(ChronoUnit.SECONDS)).isNotEqualTo(current);
            assertThat(savedErrorFilesetStudentEntity.getCreateDate().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(errorStudent.getCreateDate().truncatedTo(ChronoUnit.SECONDS));
            assertThat(savedErrorFilesetStudentEntity.getUpdateUser()).isNotEqualTo("ABC");
            assertThat(savedErrorFilesetStudentEntity.getUpdateUser()).isEqualTo(errorStudent.getUpdateUser());
            assertThat(savedErrorFilesetStudentEntity.getUpdateDate().truncatedTo(ChronoUnit.SECONDS)).isNotEqualTo(current);
            assertThat(savedErrorFilesetStudentEntity.getUpdateDate().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(errorStudent.getUpdateDate().truncatedTo(ChronoUnit.SECONDS));
        } catch(Exception e) {
            Assertions.fail("Should not have thrown any exception");
        }
    }

    @Test
    void testFlagErrorOnStudents_ExistingStudentError_WithDemographics_NoExceptions() {
        var school = this.createMockSchoolTombstone();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        mockFileset.setSchoolID(UUID.fromString(school.getSchoolId()));
        var fileset = incomingFilesetRepository.save(mockFileset);

        LocalDateTime current = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        var errorStudent = createMockErrorFilesetStudentEntity(fileset);
        errorStudent.setPen("123456789");
        errorStudent.setCreateDate(current.minusMinutes(1));
        errorStudent.setUpdateDate(current.minusMinutes(1));
        errorFilesetStudentRepository.save(errorStudent);

        try {
            var demRecord = createMockDemographicStudent(fileset);
            demRecord.setFirstName("Test");
            demRecord.setLastName("McTest");
            demRecord.setLocalID("1234");
            demRecord.setBirthdate("19000101");
            errorFilesetStudentService.flagErrorOnStudent(fileset.getIncomingFilesetID(), "123456789", demRecord,  "ABC", current, "ABC", current);
            Optional<ErrorFilesetStudentEntity> saved =  errorFilesetStudentRepository.findByIncomingFileset_IncomingFilesetIDAndPen(fileset.getIncomingFilesetID(), "123456789");
            assertThat(saved).isPresent();
            ErrorFilesetStudentEntity savedErrorFilesetStudentEntity = saved.get();
            assertThat(savedErrorFilesetStudentEntity.getFirstName()).isEqualTo("Jane");
            assertThat(savedErrorFilesetStudentEntity.getLastName()).isEqualTo("Smith");
            assertThat(savedErrorFilesetStudentEntity.getLocalID()).isEqualTo("123456789");
            assertThat(savedErrorFilesetStudentEntity.getBirthdate()).isEqualTo("19000101");
            assertThat(savedErrorFilesetStudentEntity.getCreateUser()).isEqualTo(errorStudent.getCreateUser());
            assertThat(savedErrorFilesetStudentEntity.getCreateDate().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(errorStudent.getCreateDate().truncatedTo(ChronoUnit.SECONDS));
            assertThat(savedErrorFilesetStudentEntity.getUpdateUser()).isEqualTo(ApplicationProperties.GRAD_DATA_COLLECTION_API);
        } catch(Exception e) {
            Assertions.fail("Should not have thrown any exception");
        }
    }
}
