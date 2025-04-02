package ca.bc.gov.educ.graddatacollection.api.service;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ErrorFilesetStudentEntity;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        errorFilesetStudentRepository.deleteAll();
        incomingFilesetRepository.deleteAll();
        reportingPeriodRepository.deleteAll();
    }

    @Test
    void testFlagErrorOnStudents_NewStudentError_NoExceptions() {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        mockFileset.setSchoolID(UUID.fromString(school.getSchoolId()));
        var fileset = incomingFilesetRepository.save(mockFileset);
        LocalDateTime current = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        try {
            errorFilesetStudentService.flagErrorOnStudent(fileset.getIncomingFilesetID(), "123456789", false, null, null, null, null, "ABC", current, "ABC", current);
            Optional<ErrorFilesetStudentEntity> saved =  errorFilesetStudentRepository.findByIncomingFileset_IncomingFilesetIDAndPen(fileset.getIncomingFilesetID(), "123456789");
            assertThat(saved).isPresent();
        } catch(Exception e) {
            Assertions.fail("Should not have thrown any exception");
        }
    }

    @Test
    void testFlagErrorOnStudents_NewStudentError_WithDemographics_NoExceptions() {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        mockFileset.setSchoolID(UUID.fromString(school.getSchoolId()));
        var fileset = incomingFilesetRepository.save(mockFileset);
        LocalDateTime current = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        try {
            errorFilesetStudentService.flagErrorOnStudent(fileset.getIncomingFilesetID(), "123456789", true, "Test", "McTest", "1234", "19000101", "ABC", current, "ABC", current);
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
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        mockFileset.setSchoolID(UUID.fromString(school.getSchoolId()));
        var fileset = incomingFilesetRepository.save(mockFileset);
        LocalDateTime current = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        try {
            errorFilesetStudentService.flagErrorOnStudent(fileset.getIncomingFilesetID(), "123456789", false, "Test", "McTest", "1234", "19000101", "ABC", current, "ABC", current);
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
        var school = this.createMockSchool();
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
            errorFilesetStudentService.flagErrorOnStudent(fileset.getIncomingFilesetID(), "123456789", false, null, null, null, null, "ABC", current, "ABC", current);
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
    void testFlagErrorOnStudents_ExistingStudentError_isDemLoadedFalse_IsNotUpdated() {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        mockFileset.setSchoolID(UUID.fromString(school.getSchoolId()));
        var fileset = incomingFilesetRepository.save(mockFileset);

        LocalDateTime current = LocalDateTime.now().plusMinutes(1).truncatedTo(ChronoUnit.SECONDS);

        var errorStudent = createMockErrorFilesetStudentEntity(fileset);
        errorStudent.setPen("123456789");
        errorStudent.setCreateDate(current.minusMinutes(1));
        errorStudent.setUpdateDate(current.minusMinutes(1));
        errorFilesetStudentRepository.save(errorStudent);

        try {
            errorFilesetStudentService.flagErrorOnStudent(fileset.getIncomingFilesetID(), "123456789", false, "Test", "McTest", "1234", "19000101", "ABC", current, "ABC", current);
            Optional<ErrorFilesetStudentEntity> saved =  errorFilesetStudentRepository.findByIncomingFileset_IncomingFilesetIDAndPen(fileset.getIncomingFilesetID(), "123456789");
            assertThat(saved).isPresent();
            ErrorFilesetStudentEntity savedErrorFilesetStudentEntity = saved.get();
            assertThat(savedErrorFilesetStudentEntity.getFirstName()).isNotEqualTo("Test");
            assertThat(savedErrorFilesetStudentEntity.getFirstName()).isEqualTo(errorStudent.getFirstName());
            assertThat(savedErrorFilesetStudentEntity.getLastName()).isNotEqualTo("McTest");
            assertThat(savedErrorFilesetStudentEntity.getLastName()).isEqualTo(errorStudent.getLastName());
            assertThat(savedErrorFilesetStudentEntity.getLocalID()).isNotEqualTo("1234");
            assertThat(savedErrorFilesetStudentEntity.getLocalID()).isEqualTo(errorStudent.getLocalID());
            assertThat(savedErrorFilesetStudentEntity.getBirthdate()).isNotEqualTo("19000101");
            assertThat(savedErrorFilesetStudentEntity.getBirthdate()).isEqualTo(errorStudent.getBirthdate());
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
        var school = this.createMockSchool();
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
            errorFilesetStudentService.flagErrorOnStudent(fileset.getIncomingFilesetID(), "123456789", true, "Test", "McTest", "1234", "19000101", "ABC", current, "ABC", current);
            Optional<ErrorFilesetStudentEntity> saved =  errorFilesetStudentRepository.findByIncomingFileset_IncomingFilesetIDAndPen(fileset.getIncomingFilesetID(), "123456789");
            assertThat(saved).isPresent();
            ErrorFilesetStudentEntity savedErrorFilesetStudentEntity = saved.get();
            assertThat(savedErrorFilesetStudentEntity.getFirstName()).isEqualTo("Test");
            assertThat(savedErrorFilesetStudentEntity.getLastName()).isEqualTo("McTest");
            assertThat(savedErrorFilesetStudentEntity.getLocalID()).isEqualTo("1234");
            assertThat(savedErrorFilesetStudentEntity.getBirthdate()).isEqualTo("19000101");
            assertThat(savedErrorFilesetStudentEntity.getCreateUser()).isEqualTo(errorStudent.getCreateUser());
            assertThat(savedErrorFilesetStudentEntity.getCreateDate().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(errorStudent.getCreateDate().truncatedTo(ChronoUnit.SECONDS));
            assertThat(savedErrorFilesetStudentEntity.getUpdateUser()).isEqualTo("ABC");
            assertThat(savedErrorFilesetStudentEntity.getUpdateDate().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(current);
        } catch(Exception e) {
            Assertions.fail("Should not have thrown any exception");
        }
    }

}
