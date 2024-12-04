package ca.bc.gov.educ.graddatacollection.api.service;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome;
import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.StudentStatusCodes;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.AssessmentStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.CourseStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.DemographicStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.orchestrator.AssessmentStudentProcessingOrchestrator;
import ca.bc.gov.educ.graddatacollection.api.orchestrator.DemographicStudentProcessingOrchestrator;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.*;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.service.v1.SagaService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.events.EventHandlerService;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GradStudentRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.AssessmentStudentSagaData;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentSagaData;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentSagaData;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
    AssessmentStudentRepository assessmentStudentRepository;
    @Autowired
    AssessmentStudentProcessingOrchestrator assessmentStudentProcessingOrchestrator;
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

        Student studentApiStudent = new Student();
        studentApiStudent.setStudentID(UUID.randomUUID().toString());
        studentApiStudent.setPen("123456789");
        studentApiStudent.setLocalID("8887555");
        studentApiStudent.setStatusCode(StudentStatusCodes.A.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent);
        GradStudentRecord gradStudentRecord = new GradStudentRecord();
        gradStudentRecord.setSchoolOfRecord("03636018");
        gradStudentRecord.setStudentStatusCode("CUR");
        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(gradStudentRecord);

        var demographicStudentEntity = createMockDemographicStudent(mockFileset);
        demographicStudentEntity.setDemographicStudentID(null);
        demographicStudentEntity.setStudentStatusCode("LOADED");
        demographicStudentEntity.setCreateDate(LocalDateTime.now().minusMinutes(14));
        demographicStudentEntity.setUpdateDate(LocalDateTime.now());
        demographicStudentEntity.setCreateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);
        demographicStudentEntity.setUpdateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);

        demographicStudentRepository.save(demographicStudentEntity);

        val demographicStudent = DemographicStudentMapper.mapper.toDemographicStudent(demographicStudentEntity);

        val sagaData = DemographicStudentSagaData.builder().demographicStudent(demographicStudent).school(createMockSchool()).build();
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

        val sagaData = CourseStudentSagaData.builder().courseStudent(courseStudent).school(createMockSchool()).build();
        val event = Event.builder()
                .eventType(EventType.READ_COURSE_STUDENTS_FOR_PROCESSING)
                .eventOutcome(EventOutcome.READ_COURSE_STUDENTS_FOR_PROCESSING_SUCCESS)
                .eventPayload(JsonUtil.getJsonStringFromObject(sagaData)).build();

        eventHandlerService.handleProcessCourseStudentsEvent(event);

        var sagaEntity = sagaRepository.findByCourseStudentIDAndIncomingFilesetIDAndSagaNameAndStatusNot(courseStudentEntity.getCourseStudentID(), mockFileset.getIncomingFilesetID(), "PROCESS_COURSE_STUDENTS_SAGA", "COMPLETED");
        assertThat(sagaEntity).isPresent();
    }

    @Test
    void testHandleProcessAssessmentStudentsEvent() throws JsonProcessingException {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var mockFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        mockFileset.setSchoolID(UUID.fromString(school.getSchoolId()));
        incomingFilesetRepository.save(mockFileset);

        var assessmentStudentEntity = createMockAssessmentStudent();
        assessmentStudentEntity.setIncomingFileset(mockFileset);
        assessmentStudentEntity.setAssessmentStudentID(null);
        assessmentStudentEntity.setStudentStatusCode("LOADED");
        assessmentStudentEntity.setCreateDate(LocalDateTime.now().minusMinutes(14));
        assessmentStudentEntity.setUpdateDate(LocalDateTime.now());
        assessmentStudentEntity.setCreateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);
        assessmentStudentEntity.setUpdateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);

        assessmentStudentRepository.save(assessmentStudentEntity);

        val assessmentStudent = AssessmentStudentMapper.mapper.toAssessmentStudent(assessmentStudentEntity);

        val sagaData = AssessmentStudentSagaData.builder().assessmentStudent(assessmentStudent).school(createMockSchool()).build();
        val event = Event.builder()
                .eventType(EventType.READ_ASSESSMENT_STUDENTS_FOR_PROCESSING)
                .eventOutcome(EventOutcome.READ_ASSESSMENT_STUDENTS_FOR_PROCESSING_SUCCESS)
                .eventPayload(JsonUtil.getJsonStringFromObject(sagaData)).build();

        eventHandlerService.handleProcessAssessmentStudentsEvent(event);

        var sagaEntity = sagaRepository.findByAssessmentStudentIDAndIncomingFilesetIDAndSagaNameAndStatusNot(assessmentStudentEntity.getAssessmentStudentID(), mockFileset.getIncomingFilesetID(), "PROCESS_ASSESSMENT_STUDENTS_SAGA", "COMPLETED");
        assertThat(sagaEntity).isPresent();
    }
}
