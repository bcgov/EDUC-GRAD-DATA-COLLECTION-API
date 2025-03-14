package ca.bc.gov.educ.graddatacollection.api.controller;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.URL;
import ca.bc.gov.educ.graddatacollection.api.filter.FilterOperation;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.service.v1.IncomingFilesetService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.Search;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SearchCriteria;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ValueType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static ca.bc.gov.educ.graddatacollection.api.struct.v1.Condition.AND;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IncomingFilesetControllerTest extends BaseGradDataCollectionAPITest {

    @Autowired
    RestUtils restUtils;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    IncomingFilesetRepository incomingFilesetRepository;

    @MockBean
    private IncomingFilesetService incomingFilesetService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.incomingFilesetRepository.deleteAll();
    }

    @Test
    void testReadIncomingFilesetStudentPaginated_Always_ShouldReturnStatusOk() throws Exception {
        incomingFilesetRepository.save(createMockIncomingFilesetEntityWithAllFilesLoaded());
        var school = this.createMockSchool();
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));

        final MvcResult result = this.mockMvc
                .perform(get(URL.BASE_URL_FILESET + URL.PAGINATED + "?pageSize=2")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_INCOMING_FILESET")))
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andDo(MvcResult::getAsyncResult)
                .andReturn();
        this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void testReadIncomingFilesetPaginated_withName_ShouldReturnStatusOk() throws Exception {
        var incomingFileset = incomingFilesetRepository.save(createMockIncomingFilesetEntityWithAllFilesLoaded());
        var school = this.createMockSchool();
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        final SearchCriteria criteria = SearchCriteria.builder().condition(AND).key("schoolID").operation(FilterOperation.EQUAL).value(incomingFileset.getSchoolID().toString()).valueType(ValueType.UUID).build();

        final List<SearchCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(criteria);

        final List<Search> searches = new LinkedList<>();
        searches.add(Search.builder().searchCriteriaList(criteriaList).build());

        final var objectMapper = new ObjectMapper();
        final String criteriaJSON = objectMapper.writeValueAsString(searches);
        final MvcResult result = this.mockMvc
                .perform(get(URL.BASE_URL_FILESET + URL.PAGINATED)
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_INCOMING_FILESET")))
                        .param("searchCriteriaList", criteriaJSON)
                        .contentType(APPLICATION_JSON))
                .andReturn();
        this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void testGetIncomingFileset_withValidParameters_shouldReturnFilesetx() throws Exception {
        var incomingFilesetEntity = createMockIncomingFilesetEntityWithAllFilesLoaded();
        UUID filesetId = UUID.randomUUID();
        UUID schoolId = UUID.randomUUID();
        UUID districtId = UUID.randomUUID();
        incomingFilesetEntity.setIncomingFilesetID(filesetId);
        incomingFilesetEntity.setSchoolID(schoolId);
        incomingFilesetEntity.setDistrictID(districtId);

        var demStudent = createMockDemographicStudent(incomingFilesetEntity);
        demStudent.setPen("123456789");
        incomingFilesetEntity.setDemographicStudentEntities(Set.of(demStudent));

        var courseStudent = createMockCourseStudent(incomingFilesetEntity);
        courseStudent.setPen("123456789");
        incomingFilesetEntity.setCourseStudentEntities(Set.of(courseStudent));

        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen("123456789");
        assessmentStudent.setIncomingFileset(incomingFilesetEntity);
        incomingFilesetEntity.setAssessmentStudentEntities(Set.of(assessmentStudent));

        when(incomingFilesetService.getErrorFilesetStudent(eq("123456789"), eq(filesetId), eq(schoolId), eq(districtId)))
                .thenReturn(incomingFilesetEntity);

        var school = this.createMockSchool();
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var district = this.createMockDistrict();
        when(this.restUtils.getDistrictByDistrictID(anyString())).thenReturn(Optional.of(district));

        String pen = "123456789";
        this.mockMvc.perform(get(URL.BASE_URL_FILESET + URL.GET_STUDENT_FILESETS)
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_INCOMING_FILESET")))
                        .param("pen", pen)
                        .param("incomingFilesetID", filesetId.toString())
                        .param("schoolID", schoolId.toString())
                        .param("districtID", districtId.toString())
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incomingFilesetID").value(filesetId.toString()));
    }
}
