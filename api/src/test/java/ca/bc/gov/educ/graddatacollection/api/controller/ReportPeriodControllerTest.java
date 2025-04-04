package ca.bc.gov.educ.graddatacollection.api.controller;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.URL;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ReportingPeriodRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportPeriodControllerTest extends BaseGradDataCollectionAPITest {

    @Autowired
    RestUtils restUtils;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    IncomingFilesetRepository incomingFilesetRepository;
    @Autowired
    ReportingPeriodRepository reportingPeriodRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.incomingFilesetRepository.deleteAll();
        this.reportingPeriodRepository.deleteAll();
    }

    @Test
    void testGetReportingCycleSummary_ShouldReturnReportData() throws Exception {
        var school = this.createMockSchool();
        school.setCanIssueTranscripts(true);

        var listOfSchools = new ArrayList<SchoolTombstone>();
        listOfSchools.add(school);
        when(this.restUtils.getTranscriptEligibleSchools()).thenReturn(listOfSchools);

        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        IncomingFilesetEntity fileSet = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        fileSet.setSchoolID(UUID.fromString(school.getSchoolId()));
        incomingFilesetRepository.save(fileSet);

        this.mockMvc.perform(get(URL.REPORTING_PERIOD_URL + "/" + reportingPeriod.getReportingPeriodID() + "/summary")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_REPORTING_PERIOD")))
                .param("type", "SchoolYear")
                .contentType(APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk());
    }

}
