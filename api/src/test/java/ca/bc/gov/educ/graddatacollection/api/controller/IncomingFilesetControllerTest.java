package ca.bc.gov.educ.graddatacollection.api.controller;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.URL;
import ca.bc.gov.educ.graddatacollection.api.filter.FilterOperation;
import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.service.v1.AssessmentStudentService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseStudentService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.DemographicStudentService;
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
    DemographicStudentService demographicStudentService;
    @MockBean
    CourseStudentService courseStudentService;
    @MockBean
    AssessmentStudentService assessmentStudentService;

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
        // Arrange
        final String pen = "123456789";
        UUID filesetId = UUID.randomUUID();
        UUID schoolId = UUID.randomUUID();
        UUID districtId = UUID.randomUUID();

        IncomingFilesetEntity incomingFilesetEntity = buildIncomingFilesetEntity(pen, filesetId, schoolId, districtId);

        DemographicStudentEntity demStudent = incomingFilesetEntity.getDemographicStudentEntities().iterator().next();
        List<AssessmentStudentEntity> xamStuds = List.copyOf(incomingFilesetEntity.getAssessmentStudentEntities());
        List<CourseStudentEntity> crsStuds = List.copyOf(incomingFilesetEntity.getCourseStudentEntities());

        when(demographicStudentService.getDemStudent(pen, filesetId, schoolId, districtId))
                .thenReturn(demStudent);
        when(assessmentStudentService.getXamStudents(pen, filesetId, schoolId, districtId))
                .thenReturn(xamStuds);
        when(courseStudentService.getCrsStudents(pen, filesetId, schoolId, districtId))
                .thenReturn(crsStuds);

        when(restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(createMockSchool()));
        when(restUtils.getDistrictByDistrictID(anyString())).thenReturn(Optional.of(createMockDistrict()));

        // Act & Assert
        mockMvc.perform(get(URL.BASE_URL_FILESET + URL.GET_STUDENT_FILESETS)
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

    /**
     * Helper method to create and populate an IncomingFilesetEntity.
     */
    private IncomingFilesetEntity buildIncomingFilesetEntity(String pen, UUID filesetId, UUID schoolId, UUID districtId) {
        IncomingFilesetEntity entity = createMockIncomingFilesetEntityWithAllFilesLoaded();
        entity.setIncomingFilesetID(filesetId);
        entity.setSchoolID(schoolId);
        entity.setDistrictID(districtId);

        var demStudent = createMockDemographicStudent(entity);
        demStudent.setPen(pen);
        entity.setDemographicStudentEntities(Set.of(demStudent));

        var courseStudent = createMockCourseStudent(entity);
        courseStudent.setPen(pen);
        entity.setCourseStudentEntities(Set.of(courseStudent));

        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(pen);
        assessmentStudent.setIncomingFileset(entity);
        entity.setAssessmentStudentEntities(Set.of(assessmentStudent));

        return entity;
    }
}
