package ca.bc.gov.educ.graddatacollection.api.service;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome;
import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.CourseStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.DemographicStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.orchestrator.DemographicStudentProcessingOrchestrator;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.CourseStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.SagaRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.service.v1.SagaService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.events.EventHandlerService;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradCourseStudentSagaData;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradDemographicStudentSagaData;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EventHandlerServiceTest extends BaseGradDataCollectionAPITest {
    @MockBean
    protected RestUtils restUtils;
    @Autowired
    SagaService sagaService;
    @Autowired
    DemographicStudentRepository demographicStudentRepository;
    @Autowired
    IncomingFilesetRepository incomingFilesetRepository;
    @Autowired
    SagaRepository sagaRepository;
    @Autowired
    DemographicStudentProcessingOrchestrator demographicStudentProcessingOrchestrator;
    @Autowired
    CourseStudentRepository courseStudentRepository;
    @Autowired
    EventHandlerService eventHandlerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleProcessDemStudentsEvent() throws JsonProcessingException {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var mockFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        mockFileset.setSchoolID(UUID.fromString(school.getSchoolId()));
        incomingFilesetRepository.save(mockFileset);

        var demographicStudentEntity = createMockDemographicStudent(mockFileset);
        demographicStudentEntity.setDemographicStudentID(null);
        demographicStudentEntity.setStudentStatusCode("LOADED");
        demographicStudentEntity.setCreateDate(LocalDateTime.now().minusMinutes(14));
        demographicStudentEntity.setUpdateDate(LocalDateTime.now());
        demographicStudentEntity.setCreateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);
        demographicStudentEntity.setUpdateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);

        demographicStudentRepository.save(demographicStudentEntity);

        val demographicStudent = DemographicStudentMapper.mapper.toDemographicStudent(demographicStudentEntity);

        val sagaData = GradDemographicStudentSagaData.builder().demographicStudent(demographicStudent).school(createMockSchool()).build();
        val event = Event.builder()
                .eventType(EventType.READ_DEM_STUDENTS_FOR_PROCESSING)
                .eventOutcome(EventOutcome.READ_DEM_STUDENTS_FOR_PROCESSING_SUCCESS)
                .eventPayload(JsonUtil.getJsonStringFromObject(sagaData)).build();

        eventHandlerService.handleProcessDemStudentsEvent(event);

        var sagaEntity = sagaRepository.findByDemographicStudentIDAndIncomingFilesetIDAndSagaNameAndStatusNot(demographicStudentEntity.getDemographicStudentID(), mockFileset.getIncomingFilesetID(), "PROCESS_DEM_STUDENTS_SAGA", "COMPLETED");
        assertThat(sagaEntity).isPresent();
    }

    @Test
    void testHandleProcessCourseStudentsEvent() throws JsonProcessingException {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var mockFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        mockFileset.setSchoolID(UUID.fromString(school.getSchoolId()));
        incomingFilesetRepository.save(mockFileset);

        var courseStudentEntity = createMockCourseStudent();
        courseStudentEntity.setIncomingFileset(mockFileset);
        courseStudentEntity.setCourseStudentID(null);
        courseStudentEntity.setStudentStatusCode("LOADED");
        courseStudentEntity.setCreateDate(LocalDateTime.now().minusMinutes(14));
        courseStudentEntity.setUpdateDate(LocalDateTime.now());
        courseStudentEntity.setCreateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);
        courseStudentEntity.setUpdateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);

        courseStudentRepository.save(courseStudentEntity);

        val courseStudent = CourseStudentMapper.mapper.toCourseStudent(courseStudentEntity);

        val sagaData = GradCourseStudentSagaData.builder().courseStudent(courseStudent).school(createMockSchool()).build();
        val event = Event.builder()
                .eventType(EventType.READ_COURSE_STUDENTS_FOR_PROCESSING)
                .eventOutcome(EventOutcome.READ_COURSE_STUDENTS_FOR_PROCESSING_SUCCESS)
                .eventPayload(JsonUtil.getJsonStringFromObject(sagaData)).build();

        eventHandlerService.handleProcessCourseStudentsEvent(event);

        var sagaEntity = sagaRepository.findByCourseStudentIDAndIncomingFilesetIDAndSagaNameAndStatusNot(courseStudentEntity.getCourseStudentID(), mockFileset.getIncomingFilesetID(), "PROCESS_COURSE_STUDENTS_SAGA", "COMPLETED");
        assertThat(sagaEntity).isPresent();
    }
}
