package ca.bc.gov.educ.graddatacollection.api.controller;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.URL;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MetricsControllerTest extends BaseGradDataCollectionAPITest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private FinalIncomingFilesetRepository incomingFilesetRepository;
    @Autowired
    private FinalErrorFilesetStudentRepository errorFilesetStudentRepository;
    @Autowired
    private ReportingPeriodRepository reportingPeriodRepository;

    @MockBean
    private FinalDemographicStudentRepository demographicStudentRepository;
    @MockBean
    private FinalAssessmentStudentRepository assessmentStudentRepository;
    @MockBean
    private FinalCourseStudentRepository courseStudentRepository;

    @BeforeEach
    void setUp() {
        incomingFilesetRepository.deleteAll();
        errorFilesetStudentRepository.deleteAll();
        reportingPeriodRepository.deleteAll();
        demographicStudentRepository.deleteAll();
        assessmentStudentRepository.deleteAll();
        courseStudentRepository.deleteAll();
    }

    @Test
    void testGenerateSubmissionMetrics_withValidSchoolID_ReturnsMetrics() throws Exception {
        UUID schoolID = UUID.randomUUID();
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockFinalIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        mockFileset.setSchoolID(schoolID);
        mockFileset.setFilesetStatusCode("LOADED");
        incomingFilesetRepository.save(mockFileset);

        mockMvc.perform(get(URL.METRICS + "/" + mockFileset.getIncomingFilesetID() + "/submission")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_INCOMING_FILESET")))
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testGenerateSubmissionMetrics_withInvalidSchoolID_ReturnsNotFound() throws Exception {
        mockMvc.perform(get(URL.METRICS + "/" + UUID.randomUUID()+"/submission")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_INCOMING_FILESET")))
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testGenerateErrorAndWarningMetrics_withValidSchoolID_ReturnsSummary() throws Exception {
        UUID schoolID = UUID.randomUUID();
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var filesetEntity = createMockFinalIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        filesetEntity.setSchoolID(schoolID);
        filesetEntity.setFilesetStatusCode(FilesetStatus.LOADED.getCode());

        var savedEntity = incomingFilesetRepository.save(filesetEntity);

        errorFilesetStudentRepository.save(createMockFinalErrorFilesetStudentEntity(savedEntity));

        List<Object[]> demResults = Arrays.asList(
                new Object[]{"ERROR", 2L},
                new Object[]{"WARNING", 1L}
        );

        List<Object[]> assessmentResults = new ArrayList<>();
        assessmentResults.add(new Object[]{"ERROR", 1L});

        List<Object[]> courseResults = new ArrayList<>();
        courseResults.add(new Object[]{"WARNING", 3L});

        doReturn(demResults).when(demographicStudentRepository).countValidationIssuesBySeverity(savedEntity.getIncomingFilesetID());
        doReturn(assessmentResults).when(assessmentStudentRepository).countValidationIssuesBySeverity(savedEntity.getIncomingFilesetID());
        doReturn(courseResults).when(courseStudentRepository).countValidationIssuesBySeverity(savedEntity.getIncomingFilesetID());

        mockMvc.perform(get(URL.METRICS + "/" + filesetEntity.getIncomingFilesetID() + "/errors")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_INCOMING_FILESET")))
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schoolID").value(schoolID.toString()))
                .andExpect(jsonPath("$.filesetID").value(savedEntity.getIncomingFilesetID().toString()))
                .andExpect(jsonPath("$.totalStudents").value("1"))
                .andExpect(jsonPath("$.totalErrors").value("3"))
                .andExpect(jsonPath("$.totalWarnings").value("4"))
                .andExpect(jsonPath("$.demCounts.errorCount").value("2"))
                .andExpect(jsonPath("$.demCounts.warningCount").value("1"))
                .andExpect(jsonPath("$.xamCounts.errorCount").value("1"))
                .andExpect(jsonPath("$.xamCounts.warningCount").value("0"))
                .andExpect(jsonPath("$.crsCounts.errorCount").value("0"))
                .andExpect(jsonPath("$.crsCounts.warningCount").value("3"));
    }

    @Test
    void testGenerateErrorAndWarningMetrics_withInvalidSchoolID_ReturnsNotFound() throws Exception {
        UUID schoolID = UUID.randomUUID();

        mockMvc.perform(get(URL.METRICS + "/errors")
                    .param("schoolID", schoolID.toString())
                    .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_INCOMING_FILESET")))
                    .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testGenerateErrorAndWarningMetrics_noIssuesFound_ReturnsZeroCounts() throws Exception {
        UUID schoolID = UUID.randomUUID();
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var filesetEntity = createMockFinalIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        filesetEntity.setSchoolID(schoolID);
        filesetEntity.setFilesetStatusCode(FilesetStatus.LOADED.getCode());
        var savedEntity = incomingFilesetRepository.save(filesetEntity);

        when(demographicStudentRepository.countValidationIssuesBySeverity(savedEntity.getIncomingFilesetID()))
                .thenReturn(List.of());
        when(assessmentStudentRepository.countValidationIssuesBySeverity(savedEntity.getIncomingFilesetID()))
                .thenReturn(List.of());
        when(courseStudentRepository.countValidationIssuesBySeverity(savedEntity.getIncomingFilesetID()))
                .thenReturn(List.of());

        mockMvc.perform(get(URL.METRICS + "/" + filesetEntity.getIncomingFilesetID()+ "/errors")
                        .param("schoolID", schoolID.toString())
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_INCOMING_FILESET")))
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schoolID").value(schoolID.toString()))
                .andExpect(jsonPath("$.totalStudents").value("0"))
                .andExpect(jsonPath("$.totalErrors").value("0"))
                .andExpect(jsonPath("$.totalWarnings").value("0"))
                .andExpect(jsonPath("$.demCounts.errorCount").value("0"))
                .andExpect(jsonPath("$.demCounts.warningCount").value("0"))
                .andExpect(jsonPath("$.xamCounts.errorCount").value("0"))
                .andExpect(jsonPath("$.xamCounts.warningCount").value("0"))
                .andExpect(jsonPath("$.crsCounts.errorCount").value("0"))
                .andExpect(jsonPath("$.crsCounts.warningCount").value("0"));
    }

}