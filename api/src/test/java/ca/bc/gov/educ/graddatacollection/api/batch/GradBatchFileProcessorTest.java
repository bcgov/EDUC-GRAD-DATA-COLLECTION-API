package ca.bc.gov.educ.graddatacollection.api.batch;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.batch.processor.GradBatchFileProcessor;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.AssessmentStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.CourseStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileInputStream;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class GradBatchFileProcessorTest extends BaseGradDataCollectionAPITest {

    @Autowired
    DemographicStudentRepository demographicStudentRepository;
    @Autowired
    IncomingFilesetRepository incomingFilesetRepository;
    @Autowired
    CourseStudentRepository courseStudentRepository;
    @Autowired
    GradBatchFileProcessor gradBatchFileProcessor;
    @Autowired
    AssessmentStudentRepository assessmentStudentRepository;
    @Autowired
    RestUtils restUtils;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.demographicStudentRepository.deleteAll();
        this.incomingFilesetRepository.deleteAll();
        this.courseStudentRepository.deleteAll();
        this.assessmentStudentRepository.deleteAll();
    }

    @Test
    void testProcessDEMFile_givenIncomingFilesetRecordExists_ShouldUpdateCRSRecord() throws Exception {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var mockFileset = createMockIncomingFilesetEntityWithCRSFile(UUID.fromString(school.getSchoolId()));
        incomingFilesetRepository.save(mockFileset);

        final FileInputStream fis = new FileInputStream("src/test/resources/student-dem-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload demFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-dem-file.dem")
                .fileType("dem")
                .build();

        gradBatchFileProcessor.processBatchFile(demFile, school.getSchoolId());
        final var result =  incomingFilesetRepository.findAll();
        assertThat(result).hasSize(1);

        final var entity = result.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getDemFileName()).isEqualTo("student-dem-file.dem");
        assertThat(entity.getCrsFileName()).isEqualTo("Test.crs");
        assertThat(entity.getDemFileStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getCrsFileStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getXamFileStatusCode()).isEqualTo("NOTLOADED");

        final var uploadedDEMStudents = demographicStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedDEMStudents).hasSize(5);
    }

    @Test
    void testProcessCRSFile_givenIncomingFilesetRecordExists_ShouldUpdateDEMRecord() throws Exception {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var mockFileset = createMockIncomingFilesetEntityWithDEMFile(UUID.fromString(school.getSchoolId()));
        incomingFilesetRepository.save(mockFileset);

        final FileInputStream fis = new FileInputStream("src/test/resources/student-crs-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload crsFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-crs-file.crs")
                .fileType("crs")
                .build();

        gradBatchFileProcessor.processBatchFile(crsFile, school.getSchoolId());
        final var result =  incomingFilesetRepository.findAll();
        assertThat(result).hasSize(1);

        final var entity = result.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getDemFileName()).isEqualTo("Test.dem");
        assertThat(entity.getCrsFileName()).isEqualTo("student-crs-file.crs");
        assertThat(entity.getDemFileStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getCrsFileStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getXamFileStatusCode()).isEqualTo("NOTLOADED");

        final var uploadedDEMStudents = courseStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedDEMStudents).hasSize(93);
    }

    @Test
    void testProcessXAMFile_givenIncomingFilesetRecordExists_ShouldUpdateDEMRecord() throws Exception {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));

        var mockFileset = createMockIncomingFilesetEntityWithDEMFile(UUID.fromString(school.getSchoolId()));
        incomingFilesetRepository.save(mockFileset);

        final FileInputStream fis = new FileInputStream("src/test/resources/student-xam-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload xamFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-xam-file.xam")
                .fileType("xam")
                .build();

        gradBatchFileProcessor.processBatchFile(xamFile, school.getSchoolId());
        final var result =  incomingFilesetRepository.findAll();
        assertThat(result).hasSize(1);

        final var entity = result.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getDemFileName()).isEqualTo("Test.dem");
        assertThat(entity.getXamFileName()).isEqualTo("student-xam-file.xam");
        assertThat(entity.getDemFileStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getCrsFileStatusCode()).isEqualTo("NOTLOADED");
        assertThat(entity.getXamFileStatusCode()).isEqualTo("LOADED");

        final var uploadedDEMStudents = assessmentStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedDEMStudents).hasSize(206);
    }
}
