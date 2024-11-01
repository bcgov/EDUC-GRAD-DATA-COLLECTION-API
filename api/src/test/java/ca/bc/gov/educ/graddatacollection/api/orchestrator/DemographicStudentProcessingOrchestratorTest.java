package ca.bc.gov.educ.graddatacollection.api.orchestrator;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome;
import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.DemographicStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.SagaEventRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.SagaRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.CareerProgramCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GradGrade;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.OptionalProgramCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.scholarships.v1.CitizenshipCode;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradDemographicStudentSagaData;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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
    IncomingFilesetRepository incomingFilesetRepository;
    @Autowired
    DemographicStudentProcessingOrchestrator demographicStudentProcessingOrchestrator;
    @Captor
    ArgumentCaptor<byte[]> eventCaptor;

    @BeforeEach
    public void setUp() {
        Mockito.reset(messagePublisher);
        Mockito.reset(restUtils);
        sagaEventRepository.deleteAll();
        sagaRepository.deleteAll();
        demographicStudentRepository.deleteAll();
        incomingFilesetRepository.deleteAll();
        JsonMapper.builder()
                .findAndAddModules()
                .build();
        when(restUtils.getScholarshipsCitizenshipCodes()).thenReturn(
                List.of(
                        new CitizenshipCode("C", "Canadian", "Valid Citizenship Code", 1, "2020-01-01", "2099-12-31"),
                        new CitizenshipCode("O", "Other", "Valid Citizenship Code", 2, "2020-01-01", "2099-12-31"),
                        new CitizenshipCode("", "Blank", "Valid for Blank Citizenship", 3, "2020-01-01", "2099-12-31")
                )
        );
        when(restUtils.getGradGrades()).thenReturn(
                List.of(
                        new GradGrade("08", "Grade 8", "", 1, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), null, "8", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new GradGrade("09", "Grade 9", "", 2, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), null, "9", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new GradGrade("10", "Grade 10", "", 3, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), null, "10", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new GradGrade("11", "Grade 11", "", 4, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), null, "11", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new GradGrade("12", "Grade 12", "", 5, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), null, "12", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new GradGrade("AD", "Adult", "", 6, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), null, "AD", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new GradGrade("AN", "Adult Non-Graduate", "", 7, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), null, "AN", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new GradGrade("HS", "Home School", "", 8, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), null, "HS", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new GradGrade("SU", "Secondary Ungraded", "", 9, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), null, "SU", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new GradGrade("GA", "Graduated Adult", "", 10, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), null, "GA", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now())
                )
        );
        when(restUtils.getCareerPrograms()).thenReturn(
                List.of(
                        new CareerProgramCode("AA", "Art Careers", "", 1, "20200101", "20990101"),
                        new CareerProgramCode("AB", "Autobody", "", 2, "20200101", "20990101"),
                        new CareerProgramCode("AC", "Agribusiness", "", 3, "20200101", "20990101")
                )
        );
        when(restUtils.getOptionalPrograms()).thenReturn(
                List.of(
                        new OptionalProgramCode(UUID.randomUUID(), "FR", "SCCP French Certificate", "", 1, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), "", "", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new OptionalProgramCode(UUID.randomUUID(), "AD", "Advanced Placement", "", 2, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), "", "", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new OptionalProgramCode(UUID.randomUUID(), "DD", "Dual Dogwood", "", 3, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), "", "", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now())
                )
        );
    }

    @SneakyThrows
    @Test
    void testHandleEvent_givenEventTypeInitiated_validateDEMStudentRecordWithEventOutCome_VALIDATE_DEM_STUDENT_SUCCESS_WITH_NO_ERROR() {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var mockFileset = createMockIncomingFilesetEntityWithCRSFile(UUID.fromString(school.getSchoolId()));
        incomingFilesetRepository.save(mockFileset);

        var demographicStudentEntity = createMockDemographicStudent(mockFileset);
        demographicStudentEntity.setDemographicStudentID(null);
        demographicStudentEntity.setCreateDate(LocalDateTime.now().minusMinutes(14));
        demographicStudentEntity.setUpdateDate(LocalDateTime.now());
        demographicStudentEntity.setCreateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);
        demographicStudentEntity.setUpdateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);

        demographicStudentRepository.save(demographicStudentEntity);

        val demographicStudent = DemographicStudentMapper.mapper.toDemographicStudent(demographicStudentEntity);
        val saga = this.createMockSaga(demographicStudent);
        saga.setSagaId(null);
        this.sagaRepository.save(saga);

        val sagaData = GradDemographicStudentSagaData.builder().demographicStudent(demographicStudent).school(createMockSchool()).build();
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
        val saga = this.createMockSaga(demographicStudent);
        saga.setSagaId(null);
        this.sagaRepository.save(saga);

        val sagaData = GradDemographicStudentSagaData.builder().demographicStudent(demographicStudent).school(createMockSchool()).build();
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
