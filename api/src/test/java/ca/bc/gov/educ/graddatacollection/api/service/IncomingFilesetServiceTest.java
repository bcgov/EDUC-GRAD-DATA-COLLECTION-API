package ca.bc.gov.educ.graddatacollection.api.service;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.StudentStatusCodes;
import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

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

    @Test
    void testGetErrorFilesetStudentBySchoolIdWithFilesetId_Success() {
        String pen = "123456789";
        UUID schoolId = UUID.randomUUID();
        IncomingFilesetEntity entity = new IncomingFilesetEntity();
        entity.setIncomingFilesetID(UUID.randomUUID());
        entity.setSchoolID(schoolId);
        entity.setFilesetStatusCode(FilesetStatus.COMPLETED.getCode());
        IncomingFilesetEntity saved = incomingFilesetRepository.save(entity);

        IncomingFilesetEntity result = incomingFilesetService.getErrorFilesetStudent(pen, saved.getIncomingFilesetID(), schoolId, null);
        assertThat(result).isEqualTo(saved);
    }

    @Test
    void testGetErrorFilesetStudentBySchoolIdWithFilesetId_NotFound() {
        String pen = "123456789";
        UUID schoolId = UUID.randomUUID();
        UUID filesetId = UUID.randomUUID();

        assertThatThrownBy(() -> incomingFilesetService.getErrorFilesetStudent(pen, filesetId, schoolId, null))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void testGetErrorFilesetStudentByDistrictIdWithFilesetId_Success() {
        String pen = "123456789";
        UUID districtId = UUID.randomUUID();
        IncomingFilesetEntity entity = new IncomingFilesetEntity();
        entity.setIncomingFilesetID(UUID.randomUUID());
        entity.setDistrictID(districtId);
        entity.setFilesetStatusCode(FilesetStatus.COMPLETED.getCode());
        IncomingFilesetEntity saved = incomingFilesetRepository.save(entity);

        IncomingFilesetEntity result = incomingFilesetService.getErrorFilesetStudent(pen, saved.getIncomingFilesetID(), null, districtId);
        assertThat(result).isEqualTo(saved);
    }

    @Test
    void testGetErrorFilesetStudentByDistrictIdWithFilesetId_NotFound() {
        String pen = "123456789";
        UUID districtId = UUID.randomUUID();
        UUID filesetId = UUID.randomUUID();

        assertThatThrownBy(() -> incomingFilesetService.getErrorFilesetStudent(pen, filesetId, null, districtId)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void testGetErrorFilesetStudentBySchoolIdWithoutFilesetId_Success() {
        String pen = "123456789";
        UUID schoolId = UUID.randomUUID();
        IncomingFilesetEntity entity = new IncomingFilesetEntity();
        entity.setIncomingFilesetID(UUID.randomUUID());
        entity.setSchoolID(schoolId);
        entity.setFilesetStatusCode(FilesetStatus.COMPLETED.getCode());
        entity.setUpdateUser("TEST");
        entity.setCreateUser("TEST");

        var demStudent = new DemographicStudentEntity();
        demStudent.setPen(pen);
        demStudent.setIncomingFileset(entity);
        demStudent.setStudentStatusCode(StudentStatusCodes.A.getCode());
        demStudent.setCreateUser("TEST");
        demStudent.setUpdateUser("TEST");
        entity.getDemographicStudentEntities().add(demStudent);
        var courseStudent = new CourseStudentEntity();
        courseStudent.setPen(pen);
        courseStudent.setIncomingFileset(entity);
        courseStudent.setStudentStatusCode(StudentStatusCodes.A.getCode());
        courseStudent.setCreateUser("TEST");
        courseStudent.setUpdateUser("TEST");
        entity.getCourseStudentEntities().add(courseStudent);
        var assessmentStudent = new AssessmentStudentEntity();
        assessmentStudent.setPen(pen);
        assessmentStudent.setIncomingFileset(entity);
        assessmentStudent.setStudentStatusCode(StudentStatusCodes.A.getCode());
        assessmentStudent.setCreateUser("TEST");
        assessmentStudent.setUpdateUser("TEST");
        entity.getAssessmentStudentEntities().add(assessmentStudent);
        IncomingFilesetEntity saved = incomingFilesetRepository.save(entity);

        IncomingFilesetEntity result = incomingFilesetService.getErrorFilesetStudent(pen, null, schoolId, null);
        assertThat(result).isEqualTo(saved);
    }

    @Test
    void testGetErrorFilesetStudentByDistrictIdWithoutFilesetId_NotFound() {
        String pen = "123456789";
        UUID districtId = UUID.randomUUID();

        assertThatThrownBy(() -> incomingFilesetService.getErrorFilesetStudent(pen, null, null, districtId)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void testGetErrorFilesetStudentWithNoSchoolOrDistrictProvided() {
        String pen = "123456789";

        assertThatThrownBy(() -> incomingFilesetService.getErrorFilesetStudent(pen, null, null, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testGetErrorFilesetStudentByDistrictIdWithoutFilesetId_Success() {
        String pen = "123456789";
        UUID districtId = UUID.randomUUID();
        IncomingFilesetEntity entity = new IncomingFilesetEntity();
        entity.setIncomingFilesetID(UUID.randomUUID());
        entity.setDistrictID(districtId);
        entity.setFilesetStatusCode(FilesetStatus.COMPLETED.getCode());
        entity.setCreateUser("TEST");
        entity.setUpdateUser("TEST");

        DemographicStudentEntity demStudent = new DemographicStudentEntity();
        demStudent.setPen(pen);
        demStudent.setIncomingFileset(entity);
        demStudent.setStudentStatusCode(StudentStatusCodes.A.getCode());
        demStudent.setCreateUser("TEST");
        demStudent.setUpdateUser("TEST");
        entity.getDemographicStudentEntities().add(demStudent);

        CourseStudentEntity courseStudent = new CourseStudentEntity();
        courseStudent.setPen(pen);
        courseStudent.setIncomingFileset(entity);
        courseStudent.setStudentStatusCode(StudentStatusCodes.A.getCode());
        courseStudent.setCreateUser("TEST");
        courseStudent.setUpdateUser("TEST");
        entity.getCourseStudentEntities().add(courseStudent);

        AssessmentStudentEntity assessmentStudent = new AssessmentStudentEntity();
        assessmentStudent.setPen(pen);
        assessmentStudent.setIncomingFileset(entity);
        assessmentStudent.setStudentStatusCode(StudentStatusCodes.A.getCode());
        assessmentStudent.setCreateUser("TEST");
        assessmentStudent.setUpdateUser("TEST");
        entity.getAssessmentStudentEntities().add(assessmentStudent);

        IncomingFilesetEntity saved = incomingFilesetRepository.save(entity);

        IncomingFilesetEntity result = incomingFilesetService.getErrorFilesetStudent(pen, null, null, districtId);
        assertThat(result).isEqualTo(saved);
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
