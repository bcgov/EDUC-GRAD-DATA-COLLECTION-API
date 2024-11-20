package ca.bc.gov.educ.graddatacollection.api.controller;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ErrorFilesetStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportGenerationControllerTest extends BaseGradDataCollectionAPITest {

    @Autowired
    RestUtils restUtils;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    IncomingFilesetRepository incomingFilesetRepository;
    @Autowired
    ErrorFilesetStudentRepository errorFilesetStudentRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetStudentErrorReport_invalidUUID_ShouldReturn400() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_FILESET_STUDENT_ERROR";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);
        this.mockMvc.perform(get(URL.BASE_URL_REPORT_GENERATION + "/" + "errorReport" + "/" + "invalid").with(mockAuthority)).andDo(print()).andExpect(status().is4xxClientError());
    }

    @Test
    void testGetStudentErrorReport_validUUID_ShouldReturnReportData() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_FILESET_STUDENT_ERROR";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

        var incomingFileSet = incomingFilesetRepository.save(createMockIncomingFilesetEntityWithAllFilesLoaded());
        errorFilesetStudentRepository.save(createMockErrorFilesetEntity(incomingFileSet));
        var errorFileset2 = createMockErrorFilesetEntity(incomingFileSet);
        errorFileset2.setPen("422342342");
        errorFilesetStudentRepository.save(errorFileset2);
        var school = this.createMockSchool();
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));

        this.mockMvc.perform(get(URL.BASE_URL_REPORT_GENERATION + "/" + "errorReport" + "/" + incomingFileSet.getIncomingFilesetID()).with(mockAuthority)).andDo(print()).andExpect(status().isOk());
    }

    @Test
    void testGetStudentErrorReport_noMatchUUID_ShouldReturnOk() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_FILESET_STUDENT_ERROR";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);
        this.mockMvc.perform(get(URL.BASE_URL_REPORT_GENERATION + "/" + "errorReport" + "/" + UUID.randomUUID()).with(mockAuthority)).andDo(print()).andExpect(status().isOk());
    }
}
