package ca.bc.gov.educ.graddatacollection.api.controller;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.AssessmentStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.CourseStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.FileUploadSummary;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.graddatacollection.api.constants.v1.URL.BASE_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    protected static final ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.demographicStudentRepository.deleteAll();
        this.incomingFilesetRepository.deleteAll();
        this.courseStudentRepository.deleteAll();
        this.assessmentStudentRepository.deleteAll();
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.subErrors[0].message").value("FILE_NOT_ALLOWED :: File type not allowed"));
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.subErrors[0].message").value("INVALID_ROW_LENGTH :: Detail record 0 has extraneous characters."));
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.subErrors[0].message").value("INVALID_ROW_LENGTH :: Detail record 0 has extraneous characters."));
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.subErrors[0].message").value("INVALID_ROW_LENGTH :: Detail record 0 is missing characters."));
    }

    @Test
    void testProcessGradFile_givenFiletypeXAM_ShouldReturnOk() throws Exception {
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
        assertThat(entity.getCrsFileStatusCode()).isEqualTo("NOTLOADED");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getDemFileStatusCode()).isEqualTo("NOTLOADED");
        assertThat(entity.getXamFileStatusCode()).isEqualTo("LOADED");

        final var uploadedCRSStudents = assessmentStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedCRSStudents).hasSize(206);
    }

    @Test
    void testProcessGradFile_givenFiletypeCRS_ShouldReturnOk() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/student-crs-file.txt");
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
        assertThat(entity.getCrsFileStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getDemFileStatusCode()).isEqualTo("NOTLOADED");
        assertThat(entity.getXamFileStatusCode()).isEqualTo("NOTLOADED");

        final var uploadedCRSStudents = courseStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedCRSStudents).hasSize(93);
    }

    @Test
    void testProcessGradFile_givenFiletypeDEM_ShouldReturnOk() throws Exception {
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
        assertThat(entity.getDemFileStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getCrsFileStatusCode()).isEqualTo("NOTLOADED");
        assertThat(entity.getXamFileStatusCode()).isEqualTo("NOTLOADED");

        final var uploadedDEMStudents = demographicStudentRepository.findAllByIncomingFileset_IncomingFilesetID(entity.getIncomingFilesetID());
        assertThat(uploadedDEMStudents).hasSize(5);
    }

    @Test
    void testProcessGradFile_givenEmptyCourseFile_ShouldReturnOk() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        schoolTombstone.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/empty-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload verFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
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
        assertThat(entity.getCrsFileStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getDemFileStatusCode()).isEqualTo("NOTLOADED");
        assertThat(entity.getXamFileStatusCode()).isEqualTo("NOTLOADED");
    }

    @Test
    void testProcessGradFile_givenEmptyXAMFile_ShouldReturnOk() throws Exception {
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
        assertThat(entity.getCrsFileStatusCode()).isEqualTo("NOTLOADED");
        assertThat(entity.getFilesetStatusCode()).isEqualTo("LOADED");
        assertThat(entity.getDemFileStatusCode()).isEqualTo("NOTLOADED");
        assertThat(entity.getXamFileStatusCode()).isEqualTo("LOADED");
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

}
