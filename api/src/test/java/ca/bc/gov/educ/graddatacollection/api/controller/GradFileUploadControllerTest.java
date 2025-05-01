package ca.bc.gov.educ.graddatacollection.api.controller;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.*;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SummerFileUpload;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SummerStudentData;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import junitparams.Parameters;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.graddatacollection.api.constants.v1.URL.BASE_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GradFileUploadControllerTest extends BaseGradDataCollectionAPITest {

    @Autowired
    RestUtils restUtils;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    DemographicStudentRepository demographicStudentRepository;
    @Autowired
    IncomingFilesetRepository incomingFilesetRepository;
    @Autowired
    CourseStudentRepository courseStudentRepository;
    @Autowired
    AssessmentStudentRepository assessmentStudentRepository;
    @Autowired
    ReportingPeriodRepository reportingPeriodRepository;

    protected static final ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.demographicStudentRepository.deleteAll();
        this.incomingFilesetRepository.deleteAll();
        this.courseStudentRepository.deleteAll();
        this.assessmentStudentRepository.deleteAll();
        reportingPeriodRepository.deleteAll();
    }

    @Test
    void testProcessGradFile_givenVerFileAndFiletypeVER_ShouldReturnBadRequest() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/student-dem-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload verFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-dem-file.ver")
                .fileType("ver")
                .build();

        this.mockMvc.perform(post( BASE_URL + "/" + UUID.randomUUID() + "/file")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(verFile))
                .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.subErrors[0].message").value("File type not allowed."));
    }

    @Test
    void testProcessGradFile_givenFiletypeDEM_WithIncorrectRecordLength_ShouldReturnBadRequest() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/student-dem-file-incorrect-length.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload verFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-dem-file-incorrect-length.dem")
                .fileType("dem")
                .build();

        this.mockMvc.perform(post( BASE_URL + "/" + UUID.randomUUID() + "/file")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(verFile))
                .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.subErrors[0].message").value("Line 1 has too many characters."));
    }

    @Test
    void testProcessGradFile_givenFiletypeXAM_WithIncorrectRecordLength_ShouldReturnBadRequest() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/student-xam-file-incorrect-length.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload verFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-xam-file-incorrect-length.xam")
                .fileType("xam")
                .build();

        this.mockMvc.perform(post( BASE_URL + "/" + UUID.randomUUID() + "/file")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(verFile))
                .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.subErrors[0].message").value("Line 1 has too many characters."));
    }

    @Test
    void testProcessGradFile_givenFiletypeCRS_WithIncorrectRecordLength_ShouldReturnBadRequest() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/student-crs-file-incorrect-length.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload verFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-crs-file-incorrect-length.crs")
                .fileType("crs")
                .build();

        this.mockMvc.perform(post( BASE_URL + "/" + UUID.randomUUID() + "/file")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(verFile))
                .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.subErrors[0].message").value("Line 1 is missing characters."));
    }

    @Test
    void testProcessGradFile_givenFiletypeXAM_ShouldReturnOk() throws Exception {
        reportingPeriodRepository.save(createMockReportingPeriodEntity());
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/student-xam-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload verFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-xam-file.xam")
                .fileType("xam")
                .build();

        this.mockMvc.perform(post( BASE_URL + "/" + schoolTombstone.getSchoolId() + "/file")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(verFile))
                .contentType(APPLICATION_JSON)).andExpect(status().isOk());

        final var result =  incomingFilesetRepository.findAll();
        assertThat(result).hasSize(1);
        final var entity = result.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getXamFileName()).isEqualTo("student-xam-file.xam");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getDemFileName()).isNull();

        final var uploadedCRSStudents = assessmentStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedCRSStudents).hasSize(206);
    }

    @Test
    void testProcessGradFile_givenFiletypeCRS_ShouldReturnOk() throws Exception {
        reportingPeriodRepository.save(createMockReportingPeriodEntity());
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final var fis = this.getClass().getResourceAsStream("/student-crs-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload verFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-crs-file.crs")
                .fileType("crs")
                .build();

        this.mockMvc.perform(post( BASE_URL + "/" + schoolTombstone.getSchoolId() + "/file")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(verFile))
                .contentType(APPLICATION_JSON)).andExpect(status().isOk());

        final var result =  incomingFilesetRepository.findAll();
        assertThat(result).hasSize(1);
        final var entity = result.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getCrsFileName()).isEqualTo("student-crs-file.crs");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getDemFileName()).isNull();
        assertThat(entity.getXamFileName()).isNull();

        final var uploadedCRSStudents = courseStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedCRSStudents).hasSize(93);
    }

    @Test
    void testProcessGradFile_givenFiletypeCRS_BadDates_ShouldReturnOk() throws Exception {
        reportingPeriodRepository.save(createMockReportingPeriodEntity());
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/student-crs-file-bad-dates.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload verFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-crs-file.crs")
                .fileType("crs")
                .build();

        this.mockMvc.perform(post( BASE_URL + "/" + schoolTombstone.getSchoolId() + "/file")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(verFile))
                .contentType(APPLICATION_JSON)).andExpect(status().isPreconditionRequired());


        GradFileUpload verFile2 = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-crs-file.crs")
                .fileType("crs")
                .courseSessionOverride(true)
                .build();

        this.mockMvc.perform(post( BASE_URL + "/" + schoolTombstone.getSchoolId() + "/file")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(verFile2))
                .contentType(APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    void testProcessGradFile_givenFiletypeCRS_OldDatesWithOverride_ShouldReturnOk() throws Exception {
        reportingPeriodRepository.save(createMockReportingPeriodEntity());
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/student-crs-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload verFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .courseSessionOverride(true)
                .createUser("ABC")
                .fileName("student-crs-file.crs")
                .fileType("crs")
                .build();

        this.mockMvc.perform(post( BASE_URL + "/" + schoolTombstone.getSchoolId() + "/file")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(verFile))
                .contentType(APPLICATION_JSON)).andExpect(status().isOk());

        final var result =  incomingFilesetRepository.findAll();
        assertThat(result).hasSize(1);
        final var entity = result.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getCrsFileName()).isEqualTo("student-crs-file.crs");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getDemFileName()).isNull();
        assertThat(entity.getXamFileName()).isNull();

        final var uploadedCRSStudents = courseStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedCRSStudents).hasSize(93);
    }

    @Test
    void testProcessGradFile_givenFiletypeDEM_ShouldReturnOk() throws Exception {
        reportingPeriodRepository.save(createMockReportingPeriodEntity());
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/student-dem-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload verFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-dem-file.dem")
                .fileType("dem")
                .build();

        this.mockMvc.perform(post( BASE_URL + "/" + schoolTombstone.getSchoolId() + "/file")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(verFile))
                .contentType(APPLICATION_JSON)).andExpect(status().isOk());

        final var result =  incomingFilesetRepository.findAll();
        assertThat(result).hasSize(1);
        final var entity = result.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getDemFileName()).isEqualTo("student-dem-file.dem");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getCrsFileName()).isNull();
        assertThat(entity.getXamFileName()).isNull();

        final var uploadedDEMStudents = demographicStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedDEMStudents).hasSize(5);
    }

    @Test
    void testProcessGradFile_givenEmptyCourseFile_ShouldReturnOk() throws Exception {
        reportingPeriodRepository.save(createMockReportingPeriodEntity());
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/empty-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload verFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .courseSessionOverride(true)
                .fileName("empty-file.crs")
                .fileType("crs")
                .build();

        this.mockMvc.perform(post( BASE_URL + "/" + schoolTombstone.getSchoolId() + "/file")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(verFile))
                .contentType(APPLICATION_JSON)).andExpect(status().isOk());

        final var result =  incomingFilesetRepository.findAll();
        assertThat(result).hasSize(1);
        final var entity = result.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getCrsFileName()).isEqualTo("empty-file.crs");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getDemFileName()).isNull();
        assertThat(entity.getXamFileName()).isNull();
    }

    @Test
    void testProcessGradFile_givenEmptyXAMFile_ShouldReturnOk() throws Exception {
        reportingPeriodRepository.save(createMockReportingPeriodEntity());
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/empty-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload verFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("empty-file.xam")
                .fileType("xam")
                .build();

        this.mockMvc.perform(post( BASE_URL + "/" + schoolTombstone.getSchoolId() + "/file")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(verFile))
                .contentType(APPLICATION_JSON)).andExpect(status().isOk());

        final var result =  incomingFilesetRepository.findAll();
        assertThat(result).hasSize(1);
        final var entity = result.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getXamFileName()).isEqualTo("empty-file.xam");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getDemFileName()).isNull();
    }

    @Test
    void testProcessGradFile_givenEmptyDEMFile_ShouldReturnOk() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/empty-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload verFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("empty-file.dem")
                .fileType("dem")
                .build();

        this.mockMvc.perform(post( BASE_URL + "/" + schoolTombstone.getSchoolId() + "/file")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(verFile))
                .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    void testProcessGradFile_forDistrict_givenFiletypeXAM_ShouldReturnOk() throws Exception {
        reportingPeriodRepository.save(createMockReportingPeriodEntity());
        SchoolTombstone schoolTombstone = this.createMockSchool();
        var districtID = UUID.randomUUID();
        schoolTombstone.setMincode("07965039");
        schoolTombstone.setDistrictId(String.valueOf(districtID));
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));
        when(this.restUtils.getSchoolByMincode(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/student-xam-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload verFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-xam-file.xam")
                .fileType("xam")
                .build();

        this.mockMvc.perform(post( BASE_URL + "/district/" + districtID + "/file")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(verFile))
                .contentType(APPLICATION_JSON)).andExpect(status().isOk());

        final var result =  incomingFilesetRepository.findAll();
        assertThat(result).hasSize(1);
        final var entity = result.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getXamFileName()).isEqualTo("student-xam-file.xam");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getDemFileName()).isNull();

        final var uploadedCRSStudents = assessmentStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedCRSStudents).hasSize(206);
    }

    @Test
    void testProcessGradFile_forDistrict_givenFiletypeCRS_ShouldReturnOk() throws Exception {
        reportingPeriodRepository.save(createMockReportingPeriodEntity());
        SchoolTombstone schoolTombstone = this.createMockSchool();
        var districtID = UUID.randomUUID();
        schoolTombstone.setMincode("07965039");
        schoolTombstone.setDistrictId(String.valueOf(districtID));
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));
        when(this.restUtils.getSchoolByMincode(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/student-crs-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload verFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-crs-file.crs")
                .fileType("crs")
                .build();

        this.mockMvc.perform(post( BASE_URL + "/district/" + districtID + "/file")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(verFile))
                .contentType(APPLICATION_JSON)).andExpect(status().isOk());

        final var result =  incomingFilesetRepository.findAll();
        assertThat(result).hasSize(1);
        final var entity = result.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getCrsFileName()).isEqualTo("student-crs-file.crs");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getDemFileName()).isNull();
        assertThat(entity.getXamFileName()).isNull();

        final var uploadedCRSStudents = courseStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedCRSStudents).hasSize(93);
    }

    @Test
    void testProcessGradFile_forDistrict_givenFiletypeDEM_ShouldReturnOk() throws Exception {
        reportingPeriodRepository.save(createMockReportingPeriodEntity());
        SchoolTombstone schoolTombstone = this.createMockSchool();
        var districtID = UUID.randomUUID();
        schoolTombstone.setMincode("07965039");
        schoolTombstone.setDistrictId(String.valueOf(districtID));
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));
        when(this.restUtils.getSchoolByMincode(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/student-dem-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload verFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-dem-file.dem")
                .fileType("dem")
                .build();

        this.mockMvc.perform(post( BASE_URL + "/district/" + districtID + "/file")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(verFile))
                .contentType(APPLICATION_JSON)).andExpect(status().isOk());

        final var result =  incomingFilesetRepository.findAll();
        assertThat(result).hasSize(1);
        final var entity = result.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getDemFileName()).isEqualTo("student-dem-file.dem");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getCrsFileName()).isNull();
        assertThat(entity.getXamFileName()).isNull();

        final var uploadedDEMStudents = demographicStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedDEMStudents).hasSize(5);
    }

    @Test
    void testProcessGradFile_forDistrict_givenEmptyCourseFile_ShouldReturnOk() throws Exception {
        reportingPeriodRepository.save(createMockReportingPeriodEntity());
        SchoolTombstone schoolTombstone = this.createMockSchool();
        var districtID = UUID.randomUUID();
        schoolTombstone.setMincode("07965039");
        schoolTombstone.setDistrictId(String.valueOf(districtID));
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));
        when(this.restUtils.getSchoolByMincode(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/empty-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload verFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .courseSessionOverride(true)
                .fileName("empty-file.crs")
                .fileType("crs")
                .build();

        this.mockMvc.perform(post( BASE_URL + "/district/" + districtID + "/file")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(verFile))
                .contentType(APPLICATION_JSON)).andExpect(status().isOk());

        final var result =  incomingFilesetRepository.findAll();
        assertThat(result).hasSize(1);
        final var entity = result.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getCrsFileName()).isEqualTo("empty-file.crs");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getDemFileName()).isNull();
        assertThat(entity.getXamFileName()).isNull();
    }

    @Test
    void testProcessGradFile_forDistrict_givenEmptyXAMFile_ShouldReturnOk() throws Exception {
        reportingPeriodRepository.save(createMockReportingPeriodEntity());
        SchoolTombstone schoolTombstone = this.createMockSchool();
        var districtID = UUID.randomUUID();
        schoolTombstone.setMincode("07965039");
        schoolTombstone.setDistrictId(String.valueOf(districtID));
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));
        when(this.restUtils.getSchoolByMincode(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/empty-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload verFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("empty-file.xam")
                .fileType("xam")
                .build();

        this.mockMvc.perform(post( BASE_URL + "/district/" + districtID + "/file")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(verFile))
                .contentType(APPLICATION_JSON)).andExpect(status().isOk());

        final var result =  incomingFilesetRepository.findAll();
        assertThat(result).hasSize(1);
        final var entity = result.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getXamFileName()).isEqualTo("empty-file.xam");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getDemFileName()).isNull();
    }

    @Test
    void testProcessGradFile_forDistrict_givenEmptyDEMFile_ShouldReturnOk() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        var districtID = UUID.randomUUID();
        schoolTombstone.setMincode("07965039");
        schoolTombstone.setDistrictId(String.valueOf(districtID));
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));
        when(this.restUtils.getSchoolByMincode(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/empty-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload verFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("empty-file.dem")
                .fileType("dem")
                .build();

        this.mockMvc.perform(post( BASE_URL + "/district/" + districtID + "/file")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(verFile))
                .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    void testProcessSchoolXlsxFile_givenValidPayload_ShouldReturnStatusOk() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("02496099");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/summer-reporting.xlsx");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        assertThat(fileContents).isNotEmpty();
        val body = GradFileUpload.builder()
                .fileContents(fileContents)
                .fileType("xlsx")
                .createUser("test")
                .fileName("summer-reporting.xlsx")
                .build();

        this.mockMvc.perform(post(BASE_URL + "/" +schoolTombstone.getSchoolId() +"/excel-upload")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(body))
                .contentType(APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.summerStudents", hasSize(3)));
    }

    @Test
    void testProcessDistrictXlsxFile_givenValidPayload_ShouldReturnStatusOk() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        var districtID = UUID.randomUUID();
        schoolTombstone.setMincode("07965039");
        schoolTombstone.setDistrictId(String.valueOf(districtID));
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));
        when(this.restUtils.getSchoolByMincode(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/summer-reporting.xlsx");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        assertThat(fileContents).isNotEmpty();
        val body = GradFileUpload.builder()
                .fileContents(fileContents)
                .fileType("xlsx")
                .createUser("test")
                .fileName("summer-reporting.xlsx")
                .build();

        this.mockMvc.perform(post(BASE_URL + "/district/" + districtID +"/excel-upload")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                        .header("correlationID", UUID.randomUUID().toString())
                        .content(JsonUtil.getJsonStringFromObject(body))
                        .contentType(APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.summerStudents", hasSize(3)));
    }

    @ParameterizedTest
    @CsvSource({
            "src/test/resources/summer-reporting-missing-header.xlsx, Missing required header Legal Middle Name.",
            "src/test/resources/summer-reporting-header-blank.xlsx, Heading row has a blank cell at column 6.",
    })
    void testProcessSchoolXlsxFile_givenEncryptedFile_ShouldReturnStatusBadRequest(final String filePath, final String errorMessage) throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream(filePath);
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        assertThat(fileContents).isNotEmpty();
        val body = GradFileUpload.builder()
                .fileContents(fileContents)
                .fileType("xlsx")
                .createUser("test")
                .fileName("summer-reporting.xls")
                .build();

        this.mockMvc.perform(post(BASE_URL + "/" +schoolTombstone.getSchoolId() +"/excel-upload")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                        .header("correlationID", UUID.randomUUID().toString())
                        .content(JsonUtil.getJsonStringFromObject(body))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors[0].message", is(equalToIgnoringCase(errorMessage))));
    }

    @Test
    void testProcessSchoolXlsxFile_givenEmptyFile_WithNoHeaders_ShouldReturnStatusBadRequest() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/empty.xlsx");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        assertThat(fileContents).isNotEmpty();
        val body = GradFileUpload.builder()
                .fileContents(fileContents)
                .fileType("xlsx")
                .createUser("test")
                .fileName("empty.xlsx")
                .build();

        this.mockMvc.perform(post(BASE_URL + "/" +schoolTombstone.getSchoolId() +"/excel-upload")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                        .header("correlationID", UUID.randomUUID().toString())
                        .content(JsonUtil.getJsonStringFromObject(body))
                        .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors[0].message").value("The file does not contain any records."));
    }

    @Test
    void testProcessSchoolXlsxFile_givenFileWithInvalidPEN_ShouldReturnBadRequest() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("02496099");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/summer-reporting-invalid-pen.xlsx");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        assertThat(fileContents).isNotEmpty();
        val body = GradFileUpload.builder()
                .fileContents(fileContents)
                .fileType("xlsx")
                .createUser("test")
                .fileName("summer-reporting.xlsx")
                .build();

        this.mockMvc.perform(post(BASE_URL + "/" +schoolTombstone.getSchoolId() +"/excel-upload")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                        .header("correlationID", UUID.randomUUID().toString())
                        .content(JsonUtil.getJsonStringFromObject(body))
                        .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors[0].message").value("Submitted PENs cannot be more than 10 digits. Review the data on line 1."));
    }

    @Test
    void testProcessSchoolXlsxFile_givenFileWithMincodeMismatch_ShouldReturnBadRequest() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/summer-reporting.xlsx");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        assertThat(fileContents).isNotEmpty();
        val body = GradFileUpload.builder()
                .fileContents(fileContents)
                .fileType("xlsx")
                .createUser("test")
                .fileName("summer-reporting.xlsx")
                .build();

        this.mockMvc.perform(post(BASE_URL + "/" +schoolTombstone.getSchoolId() +"/excel-upload")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                        .header("correlationID", UUID.randomUUID().toString())
                        .content(JsonUtil.getJsonStringFromObject(body))
                        .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors[0].message").value("The school codes in your file do not match your school's code. Please ensure that all school codes in the file correspond to your school code."));
    }

    @Test
    void testProcessSchoolXlsxFile_givenFileWithInvalidSessionDate_ShouldReturnBadRequest() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("02496099");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/summer-reporting-invalid-sessionDate.xlsx");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        assertThat(fileContents).isNotEmpty();
        val body = GradFileUpload.builder()
                .fileContents(fileContents)
                .fileType("xlsx")
                .createUser("test")
                .fileName("summer-reporting.xlsx")
                .build();

        String sub1= LocalDate.now().getYear() +"07";
        String sub2= LocalDate.now().getYear() +"08";

        this.mockMvc.perform(post(BASE_URL + "/" +schoolTombstone.getSchoolId() +"/excel-upload")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                        .header("correlationID", UUID.randomUUID().toString())
                        .content(JsonUtil.getJsonStringFromObject(body))
                        .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors[0].message").value("Can only report courses in the "+sub1+" or "+sub2+" sessions. Review the data on line 1."));
    }

    @Test
    void testProcessSchoolXlsxFile_givenFileWithInvalidLegalSurname_ShouldReturnBadRequest() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("02496099");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/summer-reporting-invalid-surname.xlsx");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        assertThat(fileContents).isNotEmpty();
        val body = GradFileUpload.builder()
                .fileContents(fileContents)
                .fileType("xlsx")
                .createUser("test")
                .fileName("summer-reporting.xlsx")
                .build();

        this.mockMvc.perform(post(BASE_URL + "/" +schoolTombstone.getSchoolId() +"/excel-upload")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                        .header("correlationID", UUID.randomUUID().toString())
                        .content(JsonUtil.getJsonStringFromObject(body))
                        .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors[0].message").value("Legal Surnames cannot be longer than 25 characters. Review the data on line 1."));
    }

    @Test
    void testProcessSchoolXlsxFile_givenFileWithInvalidFirstName_ShouldReturnBadRequest() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("02496099");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/summer-reporting-invalid-firstName.xlsx");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        assertThat(fileContents).isNotEmpty();
        val body = GradFileUpload.builder()
                .fileContents(fileContents)
                .fileType("xlsx")
                .createUser("test")
                .fileName("summer-reporting.xlsx")
                .build();

        this.mockMvc.perform(post(BASE_URL + "/" +schoolTombstone.getSchoolId() +"/excel-upload")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                        .header("correlationID", UUID.randomUUID().toString())
                        .content(JsonUtil.getJsonStringFromObject(body))
                        .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors[0].message").value("Legal Given Names cannot be longer than 25 characters. Review the data on line 1."));
    }

    @Test
    void testProcessSchoolXlsxFile_givenFileWithInvalidMiddleName_ShouldReturnBadRequest() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("02496099");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/summer-reporting-invalid-MiddleName.xlsx");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        assertThat(fileContents).isNotEmpty();
        val body = GradFileUpload.builder()
                .fileContents(fileContents)
                .fileType("xlsx")
                .createUser("test")
                .fileName("summer-reporting.xlsx")
                .build();

        this.mockMvc.perform(post(BASE_URL + "/" +schoolTombstone.getSchoolId() +"/excel-upload")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                        .header("correlationID", UUID.randomUUID().toString())
                        .content(JsonUtil.getJsonStringFromObject(body))
                        .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors[0].message").value("Legal Middle Names cannot be longer than 25 characters. Review the data on line 1."));
    }

    @Test
    void testProcessSchoolXlsxFile_givenFileWithInvalidCourse_ShouldReturnBadRequest() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("02496099");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/summer-reporting-invalid-course.xlsx");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        assertThat(fileContents).isNotEmpty();
        val body = GradFileUpload.builder()
                .fileContents(fileContents)
                .fileType("xlsx")
                .createUser("test")
                .fileName("summer-reporting.xlsx")
                .build();

        this.mockMvc.perform(post(BASE_URL + "/" +schoolTombstone.getSchoolId() +"/excel-upload")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                        .header("correlationID", UUID.randomUUID().toString())
                        .content(JsonUtil.getJsonStringFromObject(body))
                        .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors[0].message").value("Course code and level cannot be longer than 8 characters. Review the data on line 1."));
    }

    @Test
    void testProcessSchoolXlsxFile_givenFileWithInvalidFinalPercent_ShouldReturnBadRequest() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("02496099");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/summer-reporting-invalid-final-percent.xlsx");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        assertThat(fileContents).isNotEmpty();
        val body = GradFileUpload.builder()
                .fileContents(fileContents)
                .fileType("xlsx")
                .createUser("test")
                .fileName("summer-reporting.xlsx")
                .build();

        this.mockMvc.perform(post(BASE_URL + "/" +schoolTombstone.getSchoolId() +"/excel-upload")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                        .header("correlationID", UUID.randomUUID().toString())
                        .content(JsonUtil.getJsonStringFromObject(body))
                        .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors[0].message").value("Final School Percent cannot be more than 3 digits. Review the data on line 1."));
    }

    @Test
    void testProcessSchoolXlsxFile_givenFileWithInvalidDOB_ShouldReturnBadRequest() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("02496099");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/summer-reporting-invalid-dob.xlsx");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        assertThat(fileContents).isNotEmpty();
        val body = GradFileUpload.builder()
                .fileContents(fileContents)
                .fileType("xlsx")
                .createUser("test")
                .fileName("summer-reporting.xlsx")
                .build();

        this.mockMvc.perform(post(BASE_URL + "/" +schoolTombstone.getSchoolId() +"/excel-upload")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                        .header("correlationID", UUID.randomUUID().toString())
                        .content(JsonUtil.getJsonStringFromObject(body))
                        .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors[0].message").value("Birthdate must be in the format YYYYMMDD. Review the data on line 1."));
    }

    @Test
    void testProcessSummerGradFile_ShouldReturnOk() throws Exception {
        reportingPeriodRepository.save(createMockReportingPeriodEntity());
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        SummerStudentData summerData = SummerStudentData.builder()
                .dob("20030516")
                .pen("123456789")
                .course("ENST 12")
                .finalPercent("72")
                .legalFirstName("firstName")
                .legalSurname("surname")
                .legalMiddleName("middleName")
                .noOfCredits("4")
                .schoolCode("07965039")
                .build();
        SummerFileUpload fileData = SummerFileUpload.builder()
                .createUser("ABC")
                .fileName("summer-reporting.xlsx")
                .summerStudents(List.of(summerData))
                .build();

        this.mockMvc.perform(post( BASE_URL + "/" + schoolTombstone.getSchoolId() + "/process")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(fileData))
                .contentType(APPLICATION_JSON));

        final var result =  incomingFilesetRepository.findAll();
        assertThat(result).hasSize(1);
        final var entity = result.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getCrsFileName()).isEqualTo("summer-reporting.CRS");
        assertThat(entity.getDemFileName()).isEqualTo("summer-reporting.DEM");
        assertThat(entity.getXamFileName()).isEqualTo("summer-reporting.XAM");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");

        final var uploadedDEMStudents = demographicStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedDEMStudents).hasSize(1);

        final var uploadedCRSStudents = courseStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedCRSStudents).hasSize(1);
    }

    @Test
    void testProcessSummerGradFile_ForDistrict_ShouldReturnOk() throws Exception {
        reportingPeriodRepository.save(createMockReportingPeriodEntity());
        SchoolTombstone schoolTombstone = this.createMockSchool();
        var districtID = UUID.randomUUID();
        schoolTombstone.setMincode("07965039");
        schoolTombstone.setDistrictId(String.valueOf(districtID));
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));
        when(this.restUtils.getSchoolByMincode(anyString())).thenReturn(Optional.of(schoolTombstone));

        SummerStudentData summerData = SummerStudentData.builder()
                .dob("20030516")
                .pen("123456789")
                .course("ENST 12")
                .finalPercent("72")
                .legalFirstName("firstName")
                .legalSurname("surname")
                .legalMiddleName("middleName")
                .noOfCredits("4")
                .schoolCode("07965039")
                .build();
        SummerFileUpload fileData = SummerFileUpload.builder()
                .createUser("ABC")
                .fileName("summer-reporting.xlsx")
                .summerStudents(List.of(summerData))
                .build();

        this.mockMvc.perform(post( BASE_URL + "/district/" + schoolTombstone.getDistrictId() + "/process")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(fileData))
                .contentType(APPLICATION_JSON));

        final var result =  incomingFilesetRepository.findAll();
        assertThat(result).hasSize(1);
        final var entity = result.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getCrsFileName()).isEqualTo("summer-reporting.CRS");
        assertThat(entity.getDemFileName()).isEqualTo("summer-reporting.DEM");
        assertThat(entity.getXamFileName()).isEqualTo("summer-reporting.XAM");
        assertThat(entity.getCsvFileName()).isEqualTo("summer-reporting.xlsx");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");

        final var uploadedDEMStudents = demographicStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedDEMStudents).hasSize(1);

        final var uploadedCRSStudents = courseStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedCRSStudents).hasSize(1);
    }

    @Test
    void testProcessSummerGradFile_WithTwoRecords_ForDistrict_ShouldReturnOk() throws Exception {
        reportingPeriodRepository.save(createMockReportingPeriodEntity());
        SchoolTombstone schoolTombstone = this.createMockSchool();
        var districtID = UUID.randomUUID();
        schoolTombstone.setMincode("07965039");
        schoolTombstone.setDistrictId(String.valueOf(districtID));
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));
        when(this.restUtils.getSchoolByMincode(anyString())).thenReturn(Optional.of(schoolTombstone));

        SummerStudentData summerData1 = SummerStudentData.builder()
                .dob("20030516")
                .pen("123456789")
                .course("ENST 12")
                .finalPercent("72")
                .legalFirstName("firstName")
                .legalSurname("surname")
                .legalMiddleName("middleName")
                .noOfCredits("4")
                .schoolCode("07965039")
                .build();
        SummerStudentData summerData2 = SummerStudentData.builder()
                .dob("20030516")
                .pen("123456789")
                .course("CNST 12")
                .finalPercent("62")
                .legalFirstName("firstName")
                .legalSurname("surname")
                .legalMiddleName("middleName")
                .noOfCredits("4")
                .schoolCode("07965039")
                .build();

        SummerFileUpload fileData = SummerFileUpload.builder()
                .createUser("ABC")
                .fileName("summer-reporting.xlsx")
                .summerStudents(List.of(summerData1, summerData2))
                .build();

        this.mockMvc.perform(post( BASE_URL + "/district/" + schoolTombstone.getDistrictId() + "/process")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(fileData))
                .contentType(APPLICATION_JSON));

        final var result =  incomingFilesetRepository.findAll();
        assertThat(result).hasSize(1);
        final var entity = result.get(0);
        assertThat(entity.getIncomingFilesetID()).isNotNull();
        assertThat(entity.getCrsFileName()).isEqualTo("summer-reporting.CRS");
        assertThat(entity.getDemFileName()).isEqualTo("summer-reporting.DEM");
        assertThat(entity.getXamFileName()).isEqualTo("summer-reporting.XAM");
        assertThat(entity.getCsvFileName()).isEqualTo("summer-reporting.xlsx");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");

        final var uploadedDEMStudents = demographicStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedDEMStudents).hasSize(1);

        final var uploadedCRSStudents = courseStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedCRSStudents).hasSize(2);
    }

}
