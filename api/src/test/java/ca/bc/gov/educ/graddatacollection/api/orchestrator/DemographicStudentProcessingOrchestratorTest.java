package ca.bc.gov.educ.graddatacollection.api.orchestrator;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome;
import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.StudentStatusCodes;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.DemographicStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.*;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.*;
import ca.bc.gov.educ.graddatacollection.api.struct.external.scholarships.v1.CitizenshipCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentSagaData;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.graddatacollection.api.constants.EventType.VALIDATE_DEM_STUDENT;
import static ca.bc.gov.educ.graddatacollection.api.constants.SagaStatusEnum.IN_PROGRESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class DemographicStudentProcessingOrchestratorTest extends BaseGradDataCollectionAPITest {

    @MockBean
    protected RestUtils restUtils;
    @Autowired
    SagaRepository sagaRepository;
    @Autowired
    SagaEventRepository sagaEventRepository;
    @Autowired
    MessagePublisher messagePublisher;
    @Autowired
    DemographicStudentRepository demographicStudentRepository;
    @Autowired
    CourseStudentRepository courseStudentRepository;
    @Autowired
    IncomingFilesetRepository incomingFilesetRepository;
    @Autowired
    DemographicStudentProcessingOrchestrator demographicStudentProcessingOrchestrator;
    @Captor
    ArgumentCaptor<byte[]> eventCaptor;

    @BeforeEach
    void setUp() {
        Mockito.reset(messagePublisher);
        Mockito.reset(restUtils);
        sagaEventRepository.deleteAll();
        sagaRepository.deleteAll();
        demographicStudentRepository.deleteAll();
        incomingFilesetRepository.deleteAll();
        JsonMapper.builder()
                .findAndAddModules()
                .build();
        when(restUtils.getScholarshipsCitizenshipCodeList()).thenReturn(
                List.of(
                        new CitizenshipCode("C", "Canadian", "Valid Citizenship Code", 1, "2020-01-01", "2099-12-31"),
                        new CitizenshipCode("O", "Other", "Valid Citizenship Code", 2, "2020-01-01", "2099-12-31"),
                        new CitizenshipCode("", "Blank", "Valid for Blank Citizenship", 3, "2020-01-01", "2099-12-31")
                )
        );
        when(restUtils.getGradGradeList(true)).thenReturn(
                List.of(
                        new GradGrade("08", "Grade 8", "", 1, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "8", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new GradGrade("09", "Grade 9", "", 2, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "9", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new GradGrade("10", "Grade 10", "", 3, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "10", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new GradGrade("11", "Grade 11", "", 4, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "11", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new GradGrade("12", "Grade 12", "", 5, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "12", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new GradGrade("AD", "Adult", "", 6, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "AD", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new GradGrade("AN", "Adult Non-Graduate", "", 7, "2020-01-01T00:00:00","2099-12-31T23:59:59", "AN", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new GradGrade("HS", "Home School", "", 8, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "HS", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new GradGrade("SU", "Secondary Ungraded", "", 9, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "SU", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new GradGrade("GA", "Graduated Adult", "", 10, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "GA", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
                )
        );
        when(restUtils.getCareerProgramCodeList()).thenReturn(
                List.of(
                        new CareerProgramCode("AA", "Art Careers", "", 1, "20200101", "20990101"),
                        new CareerProgramCode("AB", "Autobody", "", 2, "20200101", "20990101"),
                        new CareerProgramCode("AC", "Agribusiness", "", 3, "20200101", "20990101")
                )
        );
        when(restUtils.getOptionalProgramCodeList()).thenReturn(
                List.of(
                        new OptionalProgramCode(UUID.randomUUID(), "FR", "SCCP French Certificate", "", 1, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new OptionalProgramCode(UUID.randomUUID(), "AD", "Advanced Placement", "", 2, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new OptionalProgramCode(UUID.randomUUID(), "DD", "Dual Dogwood", "", 3, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
                )
        );
        when(restUtils.getProgramRequirementCodeList()).thenReturn(
                List.of(
                        new ProgramRequirementCode("1950", "Adult Graduation Program", "Description for 1950", RequirementTypeCode.builder().reqTypeCode("REQ_TYPE").build(), "4", "Not met description", "12", "English", "Y", "CATEGORY", "1", "A", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new ProgramRequirementCode("2023", "B.C. Graduation Program", "Description for 2023", RequirementTypeCode.builder().reqTypeCode("REQ_TYPE").build(), "4", "Not met description", "12", "English", "Y", "CATEGORY", "2", "B", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new ProgramRequirementCode("2018", "B.C. Graduation Program 2018", "Description for 2018", RequirementTypeCode.builder().reqTypeCode("REQ_TYPE").build(), "4", "Not met description", "12", "English", "Y", "CATEGORY", "3", "C", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new ProgramRequirementCode("2004", "B.C. Graduation Program 2004", "Description for 2004", RequirementTypeCode.builder().reqTypeCode("REQ_TYPE").build(), "4", "Not met description", "12", "English", "Y", "CATEGORY", "4", "D", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new ProgramRequirementCode("1996", "B.C. Graduation Program 1996", "Description for 1996", RequirementTypeCode.builder().reqTypeCode("REQ_TYPE").build(), "4", "Not met description", "12", "English", "Y", "CATEGORY", "5", "E", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new ProgramRequirementCode("1986", "B.C. Graduation Program 1986", "Description for 1986", RequirementTypeCode.builder().reqTypeCode("REQ_TYPE").build(), "4", "Not met description", "12", "English", "Y", "CATEGORY", "6", "F", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new ProgramRequirementCode("SCCP", "School Completion Certificate Program", "Description for SCCP", RequirementTypeCode.builder().reqTypeCode("REQ_TYPE").build(), "4", "Not met description", "12", "English", "Y", "CATEGORY", "7", "G", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
                )
        );
        when(restUtils.getGraduationProgramCodeList(true)).thenReturn(
                List.of(
                        new GraduationProgramCode("1950", "Adult Graduation Program", "Description for 1950", 4, LocalDate.now().toString(), LocalDate.now().minusYears(2).toString(), "associatedCred"),
                        new GraduationProgramCode("2023", "B.C. Graduation Program", "Description for 2023", 4, LocalDate.now().toString(), LocalDate.now().minusYears(2).toString(), "associatedCred"),
                        new GraduationProgramCode("2018-EN", "B.C. Graduation Program 2018", "Description for 2018", 4, LocalDate.now().toString(), LocalDate.now().minusYears(2).toString(), "associatedCred"),
                        new GraduationProgramCode("2004-PF", "B.C. Graduation Program 2004", "Description for 2004", 4, LocalDate.now().toString(), LocalDate.now().minusYears(2).toString(), "associatedCred"),
                        new GraduationProgramCode("1996-EN", "B.C. Graduation Program 1996", "Description for 1996", 4, LocalDate.now().toString(), LocalDate.now().minusYears(2).toString(), "associatedCred"),
                        new GraduationProgramCode("1986-PF", "B.C. Graduation Program 1986", "Description for 1986", 4, LocalDate.now().toString(), LocalDate.now().minusYears(2).toString(), "associatedCred"),
                        new GraduationProgramCode("SCCP", "School Completion Certificate Program", "Description for SCCP", 4, LocalDate.now().toString(), LocalDate.now().minusYears(2).toString(), "associatedCred"),
                        new GraduationProgramCode("NONPROG", "Expired Program", "Description for Expired", 4, LocalDate.now().toString(), LocalDate.now().minusYears(2).toString(), "associatedCred")
                )
        );
        Student studentApiStudent = new Student();
        studentApiStudent.setStudentID(UUID.randomUUID().toString());
        studentApiStudent.setPen("123456789");
        studentApiStudent.setLocalID("8887555");
        studentApiStudent.setLegalLastName("JACKSON");
        studentApiStudent.setLegalFirstName("JIM");
        studentApiStudent.setDob("19900101");
        studentApiStudent.setStatusCode(StudentStatusCodes.A.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent);
        GradStudentRecord gradStudentRecord = new GradStudentRecord();
        gradStudentRecord.setSchoolOfRecordId("03636018");
        gradStudentRecord.setStudentStatusCode("CUR");
        gradStudentRecord.setGraduated("false");
        gradStudentRecord.setProgramCompletionDate("2023-06-30 00:00:00.000");
        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(gradStudentRecord);
    }

    @SneakyThrows
    @Test
    void testHandleEvent_givenEventTypeInitiated_validateDEMStudentRecordWithEventOutCome_VALIDATE_DEM_STUDENT_SUCCESS_WITH_NO_ERROR() {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var mockFileset = createMockIncomingFilesetEntityWithCRSFile(UUID.fromString(school.getSchoolId()));
        incomingFilesetRepository.save(mockFileset);

        var courseStudent = createMockCourseStudent(mockFileset);
        courseStudentRepository.save(courseStudent);

        var demographicStudentEntity = createMockDemographicStudent(mockFileset);
        demographicStudentEntity.setDemographicStudentID(null);
        demographicStudentEntity.setPen(courseStudent.getPen());
        demographicStudentEntity.setFirstName("JIM");
        demographicStudentEntity.setLastName("JACKSON");
        demographicStudentEntity.setCreateDate(LocalDateTime.now().minusMinutes(14));
        demographicStudentEntity.setUpdateDate(LocalDateTime.now());
        demographicStudentEntity.setCreateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);
        demographicStudentEntity.setUpdateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);

        demographicStudentRepository.save(demographicStudentEntity);

        val demographicStudent = DemographicStudentMapper.mapper.toDemographicStudent(demographicStudentEntity);
        val saga = this.createDemMockSaga(demographicStudent);
        saga.setSagaId(null);
        this.sagaRepository.save(saga);

        val sagaData = DemographicStudentSagaData.builder().demographicStudent(demographicStudent).school(createMockSchool()).build();
        val event = Event.builder()
                .sagaId(saga.getSagaId())
                .eventType(EventType.INITIATED)
                .eventOutcome(EventOutcome.INITIATE_SUCCESS)
                .eventPayload(JsonUtil.getJsonStringFromObject(sagaData)).build();
        this.demographicStudentProcessingOrchestrator.handleEvent(event);

        verify(this.messagePublisher, atMost(2)).dispatchMessage(eq(this.demographicStudentProcessingOrchestrator.getTopicToSubscribe()), this.eventCaptor.capture());
        final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
        assertThat(newEvent.getEventType()).isEqualTo(VALIDATE_DEM_STUDENT);
        assertThat(newEvent.getEventOutcome()).isEqualTo(EventOutcome.VALIDATE_DEM_STUDENT_SUCCESS_WITH_NO_ERROR);

        val savedSagaInDB = this.sagaRepository.findById(saga.getSagaId());
        assertThat(savedSagaInDB).isPresent();
        assertThat(savedSagaInDB.get().getStatus()).isEqualTo(IN_PROGRESS.toString());
        assertThat(savedSagaInDB.get().getSagaState()).isEqualTo(VALIDATE_DEM_STUDENT.toString());
    }

    @SneakyThrows
    @Test
    void testHandleEvent_givenEventTypeInitiated_validateDEMStudentRecordWithEventOutCome_VALIDATE_DEM_STUDENT_SUCCESS_WITH_ERROR() {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var mockFileset = createMockIncomingFilesetEntityWithCRSFile(UUID.fromString(school.getSchoolId()));
        incomingFilesetRepository.save(mockFileset);

        var demographicStudentEntity = createMockDemographicStudent(mockFileset);
        demographicStudentEntity.setDemographicStudentID(null);
        demographicStudentEntity.setCitizenship("A");
        demographicStudentEntity.setStudentStatusCode("LOADED");
        demographicStudentEntity.setCreateDate(LocalDateTime.now().minusMinutes(14));
        demographicStudentEntity.setUpdateDate(LocalDateTime.now());
        demographicStudentEntity.setCreateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);
        demographicStudentEntity.setUpdateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);

        demographicStudentRepository.save(demographicStudentEntity);

        val demographicStudent = DemographicStudentMapper.mapper.toDemographicStudent(demographicStudentEntity);
        val saga = this.createDemMockSaga(demographicStudent);
        saga.setSagaId(null);
        this.sagaRepository.save(saga);

        val sagaData = DemographicStudentSagaData.builder().demographicStudent(demographicStudent).school(createMockSchool()).build();
        val event = Event.builder()
                .sagaId(saga.getSagaId())
                .eventType(EventType.INITIATED)
                .eventOutcome(EventOutcome.INITIATE_SUCCESS)
                .eventPayload(JsonUtil.getJsonStringFromObject(sagaData)).build();
        this.demographicStudentProcessingOrchestrator.handleEvent(event);

        verify(this.messagePublisher, atMost(2)).dispatchMessage(eq(this.demographicStudentProcessingOrchestrator.getTopicToSubscribe()), this.eventCaptor.capture());
        final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
        assertThat(newEvent.getEventType()).isEqualTo(VALIDATE_DEM_STUDENT);
        assertThat(newEvent.getEventOutcome()).isEqualTo(EventOutcome.VALIDATE_DEM_STUDENT_SUCCESS_WITH_ERROR);

        val savedSagaInDB = this.sagaRepository.findById(saga.getSagaId());
        assertThat(savedSagaInDB).isPresent();
        assertThat(savedSagaInDB.get().getStatus()).isEqualTo(IN_PROGRESS.toString());
        assertThat(savedSagaInDB.get().getSagaState()).isEqualTo(VALIDATE_DEM_STUDENT.toString());
    }
}
