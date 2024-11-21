package ca.bc.gov.educ.graddatacollection.api.service.reports;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.ErrorFilesetStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ErrorFilesetStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.service.v1.reports.CSVReportService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorFilesetStudent;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorFilesetStudentValidationIssue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CSVReportServiceTest extends BaseGradDataCollectionAPITest {
    private static final ErrorFilesetStudentMapper errorFilesetStudentMapper = ErrorFilesetStudentMapper.mapper;

    @Autowired
    RestUtils restUtils;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    IncomingFilesetRepository incomingFilesetRepository;
    @Autowired
    ErrorFilesetStudentRepository errorFilesetStudentRepository;
    @Autowired
    CSVReportService csvReportService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessValidationIssuesForFieldEmpty() {
        var incomingFileSet = incomingFilesetRepository.save(createMockIncomingFilesetEntityWithAllFilesLoaded());
        errorFilesetStudentRepository.save(createMockErrorFilesetEntity(incomingFileSet));
        var errorFileset2 = createMockErrorFilesetEntity(incomingFileSet);
        errorFileset2.setPen("422342342");
        errorFilesetStudentRepository.save(errorFileset2);
        var school = this.createMockSchool();
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));

        List<ErrorFilesetStudent> results = errorFilesetStudentRepository.findAllByIncomingFileset_IncomingFilesetID(incomingFileSet.getIncomingFilesetID())
                .stream()
                .map(errorFilesetStudentMapper::toStructure)
                .toList();

        String result = csvReportService.processValidationIssuesForField(results.getFirst().getErrorFilesetStudentValidationIssues());
        Assertions.assertEquals("", result);
    }

    @Test
    void testProcessValidationIssuesForFieldCourse() {
        ErrorFilesetStudentValidationIssue error = new ErrorFilesetStudentValidationIssue();
        error.setErrorFilesetValidationIssueTypeCode("COURSE");
        error.setValidationIssueSeverityCode("ERROR");
        error.setValidationIssueFieldCode("PEN");
        error.setValidationIssueCode("DEM_DATA_MISSING");
        List<ErrorFilesetStudentValidationIssue> results = new ArrayList<>();
        results.add(error);


        String result = csvReportService.processValidationIssuesForField(results);
        Assertions.assertEquals("COURSE ERROR This student is missing demographic data based on Student PEN, Surname and Local ID.", result);
    }

    @Test
    void testProcessValidationIssuesForFieldAssessment() {
        ErrorFilesetStudentValidationIssue error = new ErrorFilesetStudentValidationIssue();
        error.setErrorFilesetValidationIssueTypeCode("ASSESSMENT");
        error.setValidationIssueSeverityCode("ERROR");
        error.setValidationIssueFieldCode("PEN");
        error.setValidationIssueCode("DEM_DATA_MISSING");
        List<ErrorFilesetStudentValidationIssue> results = new ArrayList<>();
        results.add(error);


        String result = csvReportService.processValidationIssuesForField(results);
        Assertions.assertEquals("ASSESSMENT ERROR This student is missing demographic data based on Student PEN, Surname, Mincode and Local ID.", result);
    }
}
