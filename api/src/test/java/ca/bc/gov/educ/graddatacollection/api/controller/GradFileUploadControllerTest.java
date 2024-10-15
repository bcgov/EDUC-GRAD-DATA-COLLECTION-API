package ca.bc.gov.educ.graddatacollection.api.controller;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.io.FileInputStream;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.graddatacollection.api.constants.v1.URL.BASE_URL;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GradFileUploadControllerTest extends BaseGradDataCollectionAPITest {

    @Autowired
    RestUtils restUtils;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testProcessGradFile_givenVerFileAndFiletypeDEM_ShouldReturnBadRequest() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/student-dem-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload verFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-dem-file.ver")
                .fileType("dem")
                .build();

        this.mockMvc.perform(post( BASE_URL + "/" + UUID.randomUUID() + "/file")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(verFile))
                .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());

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
                .contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    void testProcessGradFile_givenFiletypeDEM_ShouldReturnOk() throws Exception {
        SchoolTombstone schoolTombstone = this.createMockSchool();
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(schoolTombstone));

        final FileInputStream fis = new FileInputStream("src/test/resources/student-dem-file.txt");
        final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
        GradFileUpload verFile = GradFileUpload.builder()
                .fileContents(fileContents)
                .createUser("ABC")
                .fileName("student-dem-file.dem")
                .fileType("dem")
                .build();

        this.mockMvc.perform(post( BASE_URL + "/" + UUID.randomUUID() + "/file")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "WRITE_GRAD_COLLECTION")))
                .header("correlationID", UUID.randomUUID().toString())
                .content(JsonUtil.getJsonStringFromObject(verFile))
                .contentType(APPLICATION_JSON)).andExpect(status().isOk());
    }
}
