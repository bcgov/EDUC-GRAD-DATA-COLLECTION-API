package ca.bc.gov.educ.graddatacollection.api.controller;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.URL;
import ca.bc.gov.educ.graddatacollection.api.filter.FilterOperation;
import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentValidationIssueEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentValidationIssueEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.*;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.Search;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SearchCriteria;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ValueType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static ca.bc.gov.educ.graddatacollection.api.struct.v1.Condition.AND;
import static ca.bc.gov.educ.graddatacollection.api.struct.v1.Condition.OR;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ErrorFilesetStudentControllerTest extends BaseGradDataCollectionAPITest {

    @Autowired
    RestUtils restUtils;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    IncomingFilesetRepository incomingFilesetRepository;
    @Autowired
    ErrorFilesetStudentRepository errorFilesetStudentRepository;
    @Autowired
    DemographicStudentRepository demographicStudentRepository;
    @Autowired
    AssessmentStudentRepository assessmentStudentRepository;
    @Autowired
    ReportingPeriodRepository reportingPeriodRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.demographicStudentRepository.deleteAll();
        this.assessmentStudentRepository.deleteAll();
        this.errorFilesetStudentRepository.deleteAll();
        this.incomingFilesetRepository.deleteAll();
        this.reportingPeriodRepository.deleteAll();
    }

    @Test
    void testReadErrorFilesetStudentPaginated_Always_ShouldReturnStatusOk() throws Exception {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileSet = incomingFilesetRepository.save(createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod));
        errorFilesetStudentRepository.save(createMockErrorFilesetStudentEntity(incomingFileSet));
        var errorFileset2 = createMockErrorFilesetStudentEntity(incomingFileSet);
        errorFileset2.setPen("422342342");
        errorFilesetStudentRepository.save(errorFileset2);
        var school = this.createMockSchoolTombstone();
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));

        final MvcResult result = this.mockMvc
                .perform(get(URL.BASE_URL_ERROR_FILESET + URL.PAGINATED + "?pageSize=2")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_FILESET_STUDENT_ERROR")))
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andDo(MvcResult::getAsyncResult)
                .andReturn();
        this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void testReadErrorFilesetStudentsPaginated_withName_ShouldReturnStatusOk() throws Exception {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileSet = incomingFilesetRepository.save(createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod));
        errorFilesetStudentRepository.save(createMockErrorFilesetStudentEntity(incomingFileSet));
        var errorFileset2 = createMockErrorFilesetStudentEntity(incomingFileSet);
        errorFileset2.setLastName("PETERS");
        errorFileset2.setPen("422342342");
        errorFilesetStudentRepository.save(errorFileset2);
        var school = this.createMockSchoolTombstone();
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        final SearchCriteria criteria = SearchCriteria.builder().condition(AND).key("lastName").operation(FilterOperation.EQUAL).value("PETERS").valueType(ValueType.STRING).build();

        final List<SearchCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(criteria);

        final List<Search> searches = new LinkedList<>();
        searches.add(Search.builder().searchCriteriaList(criteriaList).build());

        final var objectMapper = new ObjectMapper();
        final String criteriaJSON = objectMapper.writeValueAsString(searches);
        final MvcResult result = this.mockMvc
                .perform(get(URL.BASE_URL_ERROR_FILESET + URL.PAGINATED)
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_FILESET_STUDENT_ERROR")))
                        .param("searchCriteriaList", criteriaJSON)
                        .contentType(APPLICATION_JSON))
                .andReturn();
        this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void testReadErrorFilesetStudentsPaginated_withDemErrors_ShouldReturnStatusOk() throws Exception {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileSet = incomingFilesetRepository.save(createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod));
        errorFilesetStudentRepository.save(createMockErrorFilesetStudentEntity(incomingFileSet));
        var errorFileset2 = createMockErrorFilesetStudentEntity(incomingFileSet);
        errorFileset2.setLastName("PETERS");
        errorFileset2.setPen("422342342");
        errorFilesetStudentRepository.save(errorFileset2);
        var school = this.createMockSchoolTombstone();
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));

        var mockDem = createMockDemographicStudent(incomingFileSet);
        mockDem.setPen("422342342");
        mockDem.setLastName("PETERS");

        var demValidation = DemographicStudentValidationIssueEntity.builder()
                .demographicStudent(mockDem)
                .demographicStudentValidationIssueID(UUID.randomUUID())
                .validationIssueCode("TEST")
                .validationIssueDescription("TEST")
                .validationIssueFieldCode("TEST")
                .validationIssueSeverityCode("ERROR")
                .build();

        mockDem.getDemographicStudentValidationIssueEntities().add(demValidation);
        demographicStudentRepository.save(mockDem);

        var mockAssessment = createMockAssessmentStudent();
        mockAssessment.setIncomingFileset(incomingFileSet);
        mockAssessment.setPen("122342342");
        mockAssessment.setLastName("PETERS");
        assessmentStudentRepository.save(mockAssessment);
        final SearchCriteria criteria = SearchCriteria.builder().condition(AND).key("demographicStudentEntities.demographicStudentValidationIssueEntities")
                .operation(FilterOperation.CUSTOM_CHILD_JOIN).value("DEM-ERROR").valueType(ValueType.STRING).build();

        final List<SearchCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(criteria);

        final List<Search> searches = new LinkedList<>();
        searches.add(Search.builder().searchCriteriaList(criteriaList).build());

        final var objectMapper = new ObjectMapper();
        final String criteriaJSON = objectMapper.writeValueAsString(searches);
        final MvcResult result = this.mockMvc
                .perform(get(URL.BASE_URL_ERROR_FILESET + URL.PAGINATED)
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_FILESET_STUDENT_ERROR")))
                        .param("searchCriteriaList", criteriaJSON)
                        .contentType(APPLICATION_JSON))
                .andReturn();
        this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void testReadErrorFilesetStudentsPaginated_withErrors_ShouldReturnStatusOk() throws Exception {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileSet = incomingFilesetRepository.save(createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod));
        errorFilesetStudentRepository.save(createMockErrorFilesetStudentEntity(incomingFileSet));
        var errorFileset2 = createMockErrorFilesetStudentEntity(incomingFileSet);
        errorFileset2.setLastName("PETERS");
        errorFileset2.setPen("422342342");
        errorFilesetStudentRepository.save(errorFileset2);
        var school = this.createMockSchoolTombstone();
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));

        var mockDem = createMockDemographicStudent(incomingFileSet);
        mockDem.setPen("422342342");
        mockDem.setLastName("PETERS");

        var demValidation = DemographicStudentValidationIssueEntity.builder()
                .demographicStudent(mockDem)
                .demographicStudentValidationIssueID(UUID.randomUUID())
                .validationIssueCode("TEST")
                .validationIssueDescription("TEST")
                .validationIssueFieldCode("TEST")
                .validationIssueSeverityCode("ERROR")
                .build();

        mockDem.getDemographicStudentValidationIssueEntities().add(demValidation);
        demographicStudentRepository.save(mockDem);

        var mockAssessment = createMockAssessmentStudent();
        mockAssessment.setIncomingFileset(incomingFileSet);
        mockAssessment.setPen("122342342");
        mockAssessment.setLastName("PETERS");
        assessmentStudentRepository.save(mockAssessment);
        final SearchCriteria criteria = SearchCriteria.builder().condition(AND).key("demographicStudentEntities.demographicStudentValidationIssueEntities.validationIssueSeverityCode")
                .operation(FilterOperation.IN).value("ERROR").valueType(ValueType.STRING).build();

        final List<SearchCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(criteria);

        final List<Search> searches = new LinkedList<>();
        searches.add(Search.builder().searchCriteriaList(criteriaList).build());

        final var objectMapper = new ObjectMapper();
        final String criteriaJSON = objectMapper.writeValueAsString(searches);
        final MvcResult result = this.mockMvc
                .perform(get(URL.BASE_URL_ERROR_FILESET + URL.PAGINATED)
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_FILESET_STUDENT_ERROR")))
                        .param("searchCriteriaList", criteriaJSON)
                        .contentType(APPLICATION_JSON))
                .andReturn();
        this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void testReadErrorFilesetStudentsPaginated_withFieldCode_ShouldReturnStatusOk() throws Exception {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileSet = incomingFilesetRepository.save(createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod));
        errorFilesetStudentRepository.save(createMockErrorFilesetStudentEntity(incomingFileSet));
        var errorFileset2 = createMockErrorFilesetStudentEntity(incomingFileSet);
        errorFileset2.setLastName("PETERS");
        errorFileset2.setPen("422342342");
        errorFilesetStudentRepository.save(errorFileset2);
        var school = this.createMockSchoolTombstone();
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));

        var mockDem = createMockDemographicStudent(incomingFileSet);
        mockDem.setPen("422342342");
        mockDem.setLastName("PETERS");

        var demValidation1 = DemographicStudentValidationIssueEntity.builder()
                .demographicStudent(mockDem)
                .demographicStudentValidationIssueID(UUID.randomUUID())
                .validationIssueCode("TEST")
                .validationIssueDescription("TEST")
                .validationIssueFieldCode("STUDENT_STATUS")
                .validationIssueSeverityCode("ERROR")
                .build();

        var demValidation2 = DemographicStudentValidationIssueEntity.builder()
                .demographicStudent(mockDem)
                .demographicStudentValidationIssueID(UUID.randomUUID())
                .validationIssueCode("TEST")
                .validationIssueDescription("TEST")
                .validationIssueFieldCode("TEST")
                .validationIssueSeverityCode("ERROR")
                .build();

        mockDem.getDemographicStudentValidationIssueEntities().addAll(List.of(demValidation1, demValidation2));
        demographicStudentRepository.save(mockDem);

        var mockAssessment = createMockAssessmentStudent();
        mockAssessment.setIncomingFileset(incomingFileSet);
        mockAssessment.setPen("122342342");
        mockAssessment.setLastName("PETERS");
        assessmentStudentRepository.save(mockAssessment);
        final SearchCriteria criteria1 = SearchCriteria.builder().condition(OR).key("demographicStudentEntities.demographicStudentValidationIssueEntities.validationIssueFieldCode")
                .operation(FilterOperation.EQUAL).value("STUDENT_STATUS").valueType(ValueType.STRING).build();


        final List<SearchCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(criteria1);

        final List<Search> searches = new LinkedList<>();
        searches.add(Search.builder().searchCriteriaList(criteriaList).build());

        final var objectMapper = new ObjectMapper();
        final String criteriaJSON = objectMapper.writeValueAsString(searches);
        final MvcResult result = this.mockMvc
                .perform(get(URL.BASE_URL_ERROR_FILESET + URL.PAGINATED)
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_FILESET_STUDENT_ERROR")))
                        .param("searchCriteriaList", criteriaJSON)
                        .contentType(APPLICATION_JSON))
                .andReturn();
        this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void testReadErrorFilesetStudentsPaginated_withErrors_ShouldReturnStatusOkAndReturnErrorContext() throws Exception {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileSet = incomingFilesetRepository.save(createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod));
        var errorFileset2 = createMockErrorFilesetStudentEntity(incomingFileSet);
        errorFileset2.setLastName("PETERS");
        errorFileset2.setPen("422342342");
        errorFilesetStudentRepository.save(errorFileset2);
        var school = this.createMockSchoolTombstone();
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));

        var mockDem = createMockDemographicStudent(incomingFileSet);
        mockDem.setPen("422342342");
        mockDem.setLastName("PETERS");
        demographicStudentRepository.save(mockDem);

        var mockAssessment = createMockAssessmentStudent();
        mockAssessment.setIncomingFileset(incomingFileSet);
        mockAssessment.setPen("422342342");
        mockAssessment.setLastName("PETERS");
        var assessmentValidation = AssessmentStudentValidationIssueEntity.builder()
                .assessmentStudent(mockAssessment)
                .assessmentStudentValidationIssueID(UUID.randomUUID())
                .validationIssueCode("TEST")
                .validationIssueDescription("TEST")
                .validationIssueFieldCode("TEST")
                .validationIssueSeverityCode("ERROR")
                .build();
        mockAssessment.getAssessmentStudentValidationIssueEntities().add(assessmentValidation);
        assessmentStudentRepository.save(mockAssessment);
        final SearchCriteria criteria = SearchCriteria.builder().condition(AND).key("incomingFileset.incomingFilesetID")
                .operation(FilterOperation.EQUAL).value(String.valueOf(incomingFileSet.getIncomingFilesetID())).valueType(ValueType.UUID).build();

        final List<SearchCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(criteria);

        final List<Search> searches = new LinkedList<>();
        searches.add(Search.builder().searchCriteriaList(criteriaList).build());

        final var objectMapper = new ObjectMapper();
        final String criteriaJSON = objectMapper.writeValueAsString(searches);
        final MvcResult result = this.mockMvc
                .perform(get(URL.BASE_URL_ERROR_FILESET + URL.PAGINATED)
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_FILESET_STUDENT_ERROR")))
                        .param("searchCriteriaList", criteriaJSON)
                        .contentType(APPLICATION_JSON))
                .andReturn();
        this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].errorFilesetStudentValidationIssues", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].errorFilesetStudentValidationIssues[0].errorContext").value("LTE10 - %s/01".formatted(LocalDate.now().plusYears(1).format(DateTimeFormatter.ofPattern("yyyy")))));
    }

}
