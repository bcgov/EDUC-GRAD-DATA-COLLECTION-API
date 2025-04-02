package ca.bc.gov.educ.graddatacollection.api.batch;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.batch.processor.GradBatchFileProcessor;
import ca.bc.gov.educ.graddatacollection.api.batch.validation.GradFileValidator;
import ca.bc.gov.educ.graddatacollection.api.exception.InvalidPayloadException;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.*;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    ReportingPeriodRepository reportingPeriodRepository;
    @Autowired
    GradBatchFileProcessor gradBatchFileProcessor;
    @Autowired
    GradFileValidator gradFileValidator;
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
        this.reportingPeriodRepository.deleteAll();
    }

    @Test
    void testProcessDEMFile_givenIncomingFilesetRecordExists_ShouldUpdateCRSRecord() throws Exception {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithCRSFile(UUID.fromString(school.getSchoolId()), reportingPeriod);
        incomingFilesetRepository.save(mockFileset);

        final FileInputStream fis = new FileInputStream("src/test/resources/student-dem-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload demFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-dem-file.dem")
                .fileType("dem")
                .build();

        gradBatchFileProcessor.processSchoolBatchFile(demFile, school.getSchoolId());
        final var result =  incomingFilesetRepository.findAll();
        assertThat(result).hasSize(1);

        final var entity = result.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getDemFileName()).isEqualTo("student-dem-file.dem");
        assertThat(entity.getCrsFileName()).isEqualTo("Test.crs");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");

        final var uploadedDEMStudents = demographicStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedDEMStudents).hasSize(5);
    }

    @Test
    void testProcessDEMFile_givenMissingPEN_ShouldThrowError() throws Exception {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithCRSFile(UUID.fromString(school.getSchoolId()), reportingPeriod);
        incomingFilesetRepository.save(mockFileset);

        final FileInputStream fis = new FileInputStream("src/test/resources/student-dem-file-missing-pen.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload demFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-dem-file.dem")
                .fileType("dem")
                .build();

        assertThrows(InvalidPayloadException.class, () ->gradBatchFileProcessor.processSchoolBatchFile(demFile, school.getSchoolId()));
    }

    @Test
    void testProcessDEMFile_givenDupePEN_ShouldThrowError() throws Exception {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithCRSFile(UUID.fromString(school.getSchoolId()), reportingPeriod);
        incomingFilesetRepository.save(mockFileset);

        final FileInputStream fis = new FileInputStream("src/test/resources/student-dem-file-dupe-pen.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload demFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-dem-file.dem")
                .fileType("dem")
                .build();

        assertThrows(InvalidPayloadException.class, () ->gradBatchFileProcessor.processSchoolBatchFile(demFile, school.getSchoolId()));
    }

    @Test
    void testProcessCRSFile_givenIncomingFilesetRecordExists_ShouldUpdateDEMRecord() throws Exception {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));

        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithDEMFile(UUID.fromString(school.getSchoolId()), reportingPeriod);
        incomingFilesetRepository.save(mockFileset);

        final FileInputStream fis = new FileInputStream("src/test/resources/student-crs-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload crsFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-crs-file.crs")
                .fileType("crs")
                .build();

        gradBatchFileProcessor.processSchoolBatchFile(crsFile, school.getSchoolId());
        final var result =  incomingFilesetRepository.findAll();
        assertThat(result).hasSize(1);

        final var entity = result.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getDemFileName()).isEqualTo("Test.dem");
        assertThat(entity.getCrsFileName()).isEqualTo("student-crs-file.crs");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getXamFileName()).isNull();

        final var uploadedDEMStudents = courseStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedDEMStudents).hasSize(93);
    }

    @Test
    void testProcessXAMFile_givenIncomingFilesetRecordExists_ShouldUpdateDEMRecord() throws Exception {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));

        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithDEMFile(UUID.fromString(school.getSchoolId()), reportingPeriod);
        incomingFilesetRepository.save(mockFileset);

        final FileInputStream fis = new FileInputStream("src/test/resources/student-xam-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload xamFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-xam-file.xam")
                .fileType("xam")
                .build();

        gradBatchFileProcessor.processSchoolBatchFile(xamFile, school.getSchoolId());
        final var result =  incomingFilesetRepository.findAll();
        assertThat(result).hasSize(1);

        final var entity = result.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getDemFileName()).isEqualTo("Test.dem");
        assertThat(entity.getXamFileName()).isEqualTo("student-xam-file.xam");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");

        final var uploadedDEMStudents = assessmentStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedDEMStudents).hasSize(206);
    }

    @Test
    void testProcessCRSFile_givenFileWithNoCurrentSession_shouldThrowError() throws Exception {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithDEMFile(UUID.fromString(school.getSchoolId()), reportingPeriod);
        incomingFilesetRepository.save(mockFileset);

        final FileInputStream fis = new FileInputStream("src/test/resources/crs-file-with-no-current-session.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload crsFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("crs-file-with-no-current-session.crs")
                .fileType("crs")
                .build();

        var id = school.getSchoolId();
        assertThrows(InvalidPayloadException.class, () ->gradBatchFileProcessor.processSchoolBatchFile(crsFile, id));
    }

    @Test
    void testProcessCRSFile_givenFileWithSessionInTheFuture_shouldSaveFileToDB() throws Exception {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithDEMFile(UUID.fromString(school.getSchoolId()), reportingPeriod);
        incomingFilesetRepository.save(mockFileset);

        final FileInputStream fis = new FileInputStream("src/test/resources/crs-file-with-future-session.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload crsFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("crs-file-with-future-session.crs")
                .fileType("crs")
                .build();

        gradBatchFileProcessor.processSchoolBatchFile(crsFile, school.getSchoolId());

        final var result =  incomingFilesetRepository.findAll();
        assertThat(result).hasSize(1);

        final var entity = result.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getDemFileName()).isEqualTo("Test.dem");
        assertThat(entity.getCrsFileName()).isEqualTo("crs-file-with-future-session.crs");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getXamFileName()).isNull();

        final var uploadedDEMStudents = courseStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedDEMStudents).hasSize(3);
    }

    @Test
    void testProcessDistrictBatchFile_givenSchoolOutsideDistrict_ShouldThrowException() throws Exception {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        var districtID = UUID.randomUUID();
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        when(this.restUtils.getSchoolByMincode(anyString())).thenReturn(Optional.of(school));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithCRSFile(UUID.fromString(school.getSchoolId()), reportingPeriod);
        incomingFilesetRepository.save(mockFileset);

        final FileInputStream fis = new FileInputStream("src/test/resources/student-dem-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload demFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-dem-file.dem")
                .fileType("dem")
                .build();

        var id = districtID.toString();
        assertThrows(InvalidPayloadException.class, () ->gradBatchFileProcessor.processDistrictBatchFile(demFile, id));
    }

    @Test
    void testProcessDistrictBatchFile_givenSchoolTranscriptInEligible_ShouldThrowException() throws Exception {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        school.setCanIssueTranscripts(false);
        var districtID = UUID.randomUUID();
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        when(this.restUtils.getSchoolByMincode(anyString())).thenReturn(Optional.of(school));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithCRSFile(UUID.fromString(school.getSchoolId()), reportingPeriod);
        incomingFilesetRepository.save(mockFileset);

        final FileInputStream fis = new FileInputStream("src/test/resources/student-dem-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload demFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-dem-file.dem")
                .fileType("dem")
                .build();

        var id = districtID.toString();
        assertThrows(InvalidPayloadException.class, () ->gradBatchFileProcessor.processDistrictBatchFile(demFile, id));
    }

    @Test
    void testProcessDistrictBatchFile_givenFileLoadInProgress_ShouldThrowException() throws Exception {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        var districtID = UUID.randomUUID();
        school.setDistrictId(String.valueOf(districtID));
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        when(this.restUtils.getSchoolByMincode(anyString())).thenReturn(Optional.of(school));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        mockFileset.setSchoolID(UUID.fromString(school.getSchoolId()));
        var savedFileSet = incomingFilesetRepository.save(mockFileset);

        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setIncomingFileset(savedFileSet);
        demStudent.setStudentStatusCode("LOADED");
        demographicStudentRepository.save(demStudent);
        var assessmentStudentEntity = createMockAssessmentStudent();
        assessmentStudentEntity.setIncomingFileset(savedFileSet);
        assessmentStudentEntity.setStudentStatusCode("LOADED");
        assessmentStudentRepository.save(assessmentStudentEntity);
        var courseStudentEntity = createMockCourseStudent(savedFileSet);
        courseStudentEntity.setStudentStatusCode("LOADED");
        courseStudentRepository.save(courseStudentEntity);

        final FileInputStream fis = new FileInputStream("src/test/resources/student-dem-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload demFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-dem-file.dem")
                .fileType("dem")
                .build();

        var id = districtID.toString();
        assertThrows(InvalidPayloadException.class, () -> gradBatchFileProcessor.processDistrictBatchFile(demFile, id));
    }

    @Test
    void testProcessDistrictBatchFile_givenEmptyCRSFileIsCreatedBySchool_ShouldUpdateTheSameRecord() throws Exception {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        var districtID = UUID.randomUUID();
        school.setDistrictId(String.valueOf(districtID));
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        when(this.restUtils.getSchoolByMincode(anyString())).thenReturn(Optional.of(school));
        var reportingPeriodEntity = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithDEMFile(UUID.fromString(school.getSchoolId()), reportingPeriodEntity);
        incomingFilesetRepository.save(mockFileset);

        final FileInputStream fis = new FileInputStream("src/test/resources/empty-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload crsFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("school-empty-crs-file.crs")
                .fileType("crs")
                .build();

        gradBatchFileProcessor.processSchoolBatchFile(crsFile, school.getSchoolId());
        final var schoolResult =  incomingFilesetRepository.findAll();
        assertThat(schoolResult).hasSize(1);

        final var entity = schoolResult.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getDemFileName()).isEqualTo("Test.dem");
        assertThat(entity.getCrsFileName()).isEqualTo("school-empty-crs-file.crs");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getXamFileName()).isNull();

        final FileInputStream fis2 = new FileInputStream("src/test/resources/empty-file.txt");
        final String fileContents2 = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis2));
        GradFileUpload crsFile2 = GradFileUpload.builder()
                .fileContents(fileContents2)
                .createUser("ABC")
                .fileName("district-empty-crs-file.crs")
                .fileType("crs")
                .build();

        gradBatchFileProcessor.processDistrictBatchFile(crsFile2, districtID.toString());
        final var districtResult =  incomingFilesetRepository.findAll();
        assertThat(districtResult).hasSize(1);

        final var entity2 = districtResult.get(0);
        assertThat(entity2.getIncomingFilesetID()).isNotNull();
        assertThat(entity2.getDemFileName()).isEqualTo("Test.dem");
        assertThat(entity2.getCrsFileName()).isEqualTo("district-empty-crs-file.crs");
        assertThat(entity2.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity2.getXamFileName()).isNull();
    }

    @Test
    void testValidateSchoolIsTranscriptEligibleAndOpen_AllConditionsMet() {
        String guid = UUID.randomUUID().toString();
        SchoolTombstone school = createMockSchool();
        school.setCanIssueTranscripts(true);
        school.setOpenedDate(LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        school.setClosedDate(null);

        assertDoesNotThrow(() -> gradFileValidator.validateSchoolIsTranscriptEligibleAndOpen(guid, school, school.getSchoolId()));
    }

    @Test
    void testValidateSchoolIsTranscriptEligibleAndOpen_TranscriptIneligible() {
        String guid = UUID.randomUUID().toString();
        SchoolTombstone school = createMockSchool();
        school.setCanIssueTranscripts(false);
        school.setOpenedDate(LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        assertThrows(FileUnProcessableException.class,
                () -> gradFileValidator.validateSchoolIsTranscriptEligibleAndOpen(guid, school, school.getSchoolId()));
    }

    @Test
    void testValidateSchoolIsTranscriptEligibleAndOpen_SchoolOpeningInFuture() {
        String guid = UUID.randomUUID().toString();
        SchoolTombstone school = createMockSchool();
        school.setCanIssueTranscripts(true);
        school.setOpenedDate(LocalDateTime.now().plusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        assertThrows(FileUnProcessableException.class,
                () -> gradFileValidator.validateSchoolIsTranscriptEligibleAndOpen(guid, school, school.getSchoolId()));
    }

    @Test
    void testValidateSchoolIsTranscriptEligibleAndOpen_SchoolOpenedToday() {
        String guid = UUID.randomUUID().toString();
        SchoolTombstone school = createMockSchool();
        LocalDateTime today = LocalDateTime.now();
        school.setCanIssueTranscripts(true);
        school.setOpenedDate(today.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        assertDoesNotThrow(() -> gradFileValidator.validateSchoolIsTranscriptEligibleAndOpen(guid, school, school.getSchoolId()));
    }

    @Test
    void testValidateSchoolIsTranscriptEligibleAndOpen_SchoolClosedInFuture() {
        String guid = UUID.randomUUID().toString();
        SchoolTombstone school = createMockSchool();
        school.setCanIssueTranscripts(true);
        school.setOpenedDate(LocalDateTime.now().minusMonths(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        school.setClosedDate(LocalDateTime.now().plusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        assertDoesNotThrow(() -> gradFileValidator.validateSchoolIsTranscriptEligibleAndOpen(guid, school, school.getSchoolId()));
    }

    @Test
    void testValidateSchoolIsTranscriptEligibleAndOpen_SchoolClosedInPast() {
        String guid = UUID.randomUUID().toString();
        SchoolTombstone school = createMockSchool();
        school.setCanIssueTranscripts(true);
        school.setOpenedDate(LocalDateTime.now().minusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        school.setClosedDate(LocalDateTime.now().minusMonths(4).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        assertThrows(FileUnProcessableException.class,
                () -> gradFileValidator.validateSchoolIsTranscriptEligibleAndOpen(guid, school, school.getSchoolId()));
    }

    @Test
    void testValidateSchoolIsTranscriptEligibleAndOpen_SchoolClosedToday() {
        String guid = UUID.randomUUID().toString();
        SchoolTombstone school = createMockSchool();
        LocalDateTime today = LocalDateTime.now();
        school.setCanIssueTranscripts(true);
        school.setOpenedDate(today.minusMonths(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        school.setClosedDate(today.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        assertDoesNotThrow(() -> gradFileValidator.validateSchoolIsTranscriptEligibleAndOpen(guid, school, school.getSchoolId()));
    }

    @Test
    void testValidateSchoolIsTranscriptEligibleAndOpen_InvalidOpenDateFormat() {
        String guid = UUID.randomUUID().toString();
        SchoolTombstone school = createMockSchool();
        school.setCanIssueTranscripts(true);
        school.setOpenedDate("Invalid Date");

        assertThrows(FileUnProcessableException.class,
                () -> gradFileValidator.validateSchoolIsTranscriptEligibleAndOpen(guid, school, school.getSchoolId()));
    }

    @Test
    void testValidateSchoolIsTranscriptEligibleAndOpen_InvalidCloseDateFormat() {
        String guid = UUID.randomUUID().toString();
        SchoolTombstone school = createMockSchool();
        school.setCanIssueTranscripts(true);
        school.setOpenedDate(LocalDateTime.now().minusMonths(3).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        school.setClosedDate("Invalid Date");

        assertThrows(FileUnProcessableException.class,
                () -> gradFileValidator.validateSchoolIsTranscriptEligibleAndOpen(guid, school, school.getSchoolId()));
    }

    @Test
    void testCheckForMincodeMismatch_MatchingMincodes_NoExceptionThrown() throws FileUnProcessableException {
        String guid = UUID.randomUUID().toString();
        String fileMincode = "12345678";
        String studentMincode = "12345678";
        String schoolID = UUID.randomUUID().toString();

        gradFileValidator.checkForMincodeMismatch(guid, fileMincode, studentMincode, schoolID, null);
    }

    @Test
    void testCheckForMincodeMismatch_MismatchedMincodes_SchoolIDProvided_ThrowsException() {
        String guid = UUID.randomUUID().toString();
        String fileMincode = "12345678";
        String studentMincode = "87654321";
        String schoolID = UUID.randomUUID().toString();

        assertThrows(FileUnProcessableException.class,
                () -> gradFileValidator.checkForMincodeMismatch(guid, fileMincode, studentMincode, schoolID, null));
    }

    @Test
    void testCheckForMincodeMismatch_MismatchedMincodes_DistrictIDProvided_ThrowsException() {
        String guid = UUID.randomUUID().toString();
        String fileMincode = "12345678";
        String studentMincode = "87654321";
        String districtID = UUID.randomUUID().toString();

        assertThrows(FileUnProcessableException.class,
                () -> gradFileValidator.checkForMincodeMismatch(guid, fileMincode, studentMincode, null, districtID));
    }

    @Test
    void testCheckForMincodeMismatch_NullMincodes_NoExceptionThrown() throws FileUnProcessableException {
        String guid = UUID.randomUUID().toString();
        String schoolID = UUID.randomUUID().toString();

        gradFileValidator.checkForMincodeMismatch(guid, null, null, schoolID, null);
    }

}
