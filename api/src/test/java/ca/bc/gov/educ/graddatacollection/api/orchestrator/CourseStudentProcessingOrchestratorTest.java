package ca.bc.gov.educ.graddatacollection.api.orchestrator;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome;
import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.CourseStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.*;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CoregCoursesRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CourseAllowableCreditRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CourseCharacteristicsRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CourseCodeRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.EquivalencyChallengeCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GradCourseRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GradStudentCourseRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.LetterGrade;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentSagaData;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static ca.bc.gov.educ.graddatacollection.api.constants.EventType.VALIDATE_COURSE_STUDENT;
import static ca.bc.gov.educ.graddatacollection.api.constants.SagaStatusEnum.IN_PROGRESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class CourseStudentProcessingOrchestratorTest extends BaseGradDataCollectionAPITest {

    @MockBean
    protected RestUtils restUtils;
    @Autowired
    SagaRepository sagaRepository;
    @Autowired
    SagaEventRepository sagaEventRepository;
    @Autowired
    MessagePublisher messagePublisher;
    @Autowired
    CourseStudentRepository courseStudentRepository;
    @Autowired
    IncomingFilesetRepository incomingFilesetRepository;
    @Autowired
    CourseStudentProcessingOrchestrator courseStudentProcessingOrchestrator;
    @Autowired
    DemographicStudentRepository demographicStudentRepository;
    @Captor
    ArgumentCaptor<byte[]> eventCaptor;

    @BeforeEach
    public void setUp() {
        Mockito.reset(messagePublisher);
        Mockito.reset(restUtils);
        sagaEventRepository.deleteAll();
        sagaRepository.deleteAll();
        courseStudentRepository.deleteAll();
        incomingFilesetRepository.deleteAll();
        JsonMapper.builder()
                .findAndAddModules()
                .build();
        when(restUtils.getLetterGradeList()).thenReturn(
                List.of(
                        new LetterGrade("A", "4", "Y", "The student demonstrates excellent or outstanding performance in relation to expected learning outcomes for the course or subject and grade.", "A", 100, 86, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("B", "3", "Y", "", "B", 85, 73, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("C+", "2.5", "Y", "", "C+", 72, 67, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("F", "0", "N", "", "F", 49, 0, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("IE", "0", "N", "", "Insufficient Evidence", 0, 0, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("RM", "0", "Y", "", "Requirement Met", 0, 0, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
                )
        );
        when(restUtils.getEquivalencyChallengeCodeList()).thenReturn(
                List.of(
                        new EquivalencyChallengeCode("E", "Equivalency", "Indicates that the course credit was earned through an equivalency review.", "1", "1984-01-01 00:00:00.000", null, "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new EquivalencyChallengeCode("C", "Challenge", "Indicates that the course credit was earned through the challenge process.", "2", "1984-01-01 00:00:00.000", null, "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
                )
        );
        when(restUtils.getGradStudentCoursesByPEN(any(), any())).thenReturn(
                List.of(
                        new GradStudentCourseRecord(
                                "131411258", "CLE", "CAREER-LIFE EDUCATION", 4, "", "2021/06", "", null, 100.0, "A", 100.0, "", null, null, null, null, "", "", null, 4, null, "", null, "", "N", "", "", " ", null, null, "N", false, false, false,
                                new GradCourseRecord(
                                        "CLE", "", "CAREER-LIFE EDUCATION", "", "2018-06-30", "1858-11-16", " ", "", "3201860", 4
                                )
                        ),
                        new GradStudentCourseRecord(
                                "131411258", "CLC", "CAREER-LIFE CONNECTIONS", 4, "", "2023/06", "", null, 95.0, "A", 95.0, "", null, null, null, null, "", "", null, 4, null, "", null, "", "N", "", "", " ", null, null, "N", false, false, false,
                                new GradCourseRecord(
                                        "CLC", "", "CAREER-LIFE CONNECTIONS", "", "2018-06-30", "1858-11-16", " ", "", "3201862", 4
                                )
                        )
                )
        );
        CoregCoursesRecord coursesRecord = new CoregCoursesRecord();
        coursesRecord.setStartDate(LocalDateTime.of(1983, 2, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        coursesRecord.setCompletionEndDate(LocalDateTime.of(9999, 5, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        Set<CourseCodeRecord> courseCodes = new HashSet<>();
        CourseCodeRecord traxCode = new CourseCodeRecord();
        traxCode.setCourseID("856787");
        traxCode.setExternalCode("PH   11");
        traxCode.setOriginatingSystem("39"); // TRAX
        courseCodes.add(traxCode);
        CourseCodeRecord myEdBCCode = new CourseCodeRecord();
        myEdBCCode.setCourseID("856787");
        myEdBCCode.setExternalCode("MPH--11");
        myEdBCCode.setOriginatingSystem("38"); // MyEdBC
        courseCodes.add(myEdBCCode);
        coursesRecord.setCourseCode(courseCodes);
        Set<CourseAllowableCreditRecord> courseAllowableCredits = new HashSet<>();
        CourseAllowableCreditRecord courseAllowableCreditRecord = new CourseAllowableCreditRecord();
        courseAllowableCreditRecord.setCourseID("856787");
        courseAllowableCreditRecord.setCreditValue("3");
        courseAllowableCreditRecord.setCacID("2145166");
        courseAllowableCreditRecord.setStartDate("1970-01-01 00:00:00");
        courseAllowableCreditRecord.setEndDate(null);
        courseAllowableCredits.add(courseAllowableCreditRecord);
        coursesRecord.setCourseAllowableCredit(courseAllowableCredits);
        CourseCharacteristicsRecord courseCategory = new CourseCharacteristicsRecord();
        courseCategory.setId("2932");
        courseCategory.setType("CC");
        courseCategory.setCode("BA");
        courseCategory.setDescription("");
        coursesRecord.setCourseCategory(courseCategory);
        coursesRecord.setGenericCourseType("G");
        when(restUtils.getCoursesByExternalID(any(), any())).thenReturn(coursesRecord);
    }

    @SneakyThrows
    @Test
    void testHandleEvent_givenEventTypeInitiated_validateCourseStudentRecordWithEventOutCome_VALIDATE_COURSE_STUDENT_SUCCESS_WITH_NO_ERROR() {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var mockFileset = createMockIncomingFilesetEntityWithCRSFile(UUID.fromString(school.getSchoolId()));
        var savedFileSet = incomingFilesetRepository.save(mockFileset);

        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);

        var courseStudentEntity = createMockCourseStudent(savedFileSet);
        courseStudentEntity.setPen(demStudent.getPen());
        courseStudentEntity.setLocalID(demStudent.getLocalID());
        courseStudentEntity.setLastName(demStudent.getLastName());
        courseStudentEntity.setIncomingFileset(demStudent.getIncomingFileset());
        courseStudentEntity.setCourseStudentID(null);
        courseStudentEntity.setStudentStatusCode("LOADED");
        courseStudentEntity.setCreateDate(LocalDateTime.now().minusMinutes(14));
        courseStudentEntity.setUpdateDate(LocalDateTime.now());
        courseStudentEntity.setCreateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);
        courseStudentEntity.setUpdateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);

        courseStudentRepository.save(courseStudentEntity);

        Student stud1 = new Student();
        stud1.setStudentID(UUID.randomUUID().toString());
        stud1.setDob(demStudent.getBirthdate());
        stud1.setLegalLastName(demStudent.getLastName());
        stud1.setLegalFirstName(demStudent.getFirstName());
        stud1.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud1);

        val courseStudent = CourseStudentMapper.mapper.toCourseStudent(courseStudentEntity);
        val saga = this.createCourseMockSaga(courseStudent);
        saga.setSagaId(null);
        this.sagaRepository.save(saga);

        val sagaData = CourseStudentSagaData.builder().courseStudent(courseStudent).school(createMockSchool()).build();
        val event = Event.builder()
                .sagaId(saga.getSagaId())
                .eventType(EventType.INITIATED)
                .eventOutcome(EventOutcome.INITIATE_SUCCESS)
                .eventPayload(JsonUtil.getJsonStringFromObject(sagaData)).build();
        this.courseStudentProcessingOrchestrator.handleEvent(event);

        verify(this.messagePublisher, atMost(2)).dispatchMessage(eq(this.courseStudentProcessingOrchestrator.getTopicToSubscribe()), this.eventCaptor.capture());
        final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
        assertThat(newEvent.getEventType()).isEqualTo(VALIDATE_COURSE_STUDENT);
        assertThat(newEvent.getEventOutcome()).isEqualTo(EventOutcome.VALIDATE_COURSE_STUDENT_SUCCESS_WITH_NO_ERROR);

        val savedSagaInDB = this.sagaRepository.findById(saga.getSagaId());
        assertThat(savedSagaInDB).isPresent();
        assertThat(savedSagaInDB.get().getStatus()).isEqualTo(IN_PROGRESS.toString());
        assertThat(savedSagaInDB.get().getSagaState()).isEqualTo(VALIDATE_COURSE_STUDENT.toString());
    }

    @SneakyThrows
    @Test
    void testHandleEvent_givenEventTypeInitiated_validateCourseStudentRecordWithEventOutCome_VALIDATE_COURSE_STUDENT_SUCCESS_WITH_ERROR() {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var mockFileset = createMockIncomingFilesetEntityWithCRSFile(UUID.fromString(school.getSchoolId()));
        var savedFileset = incomingFilesetRepository.save(mockFileset);

        var demStudent = createMockDemographicStudent(savedFileset);
        demographicStudentRepository.save(demStudent);

        var courseStudentEntity = createMockCourseStudent(savedFileset);
        courseStudentEntity.setIncomingFileset(mockFileset);
        courseStudentEntity.setTransactionID("AB");
        courseStudentEntity.setCourseStudentID(null);
        courseStudentEntity.setStudentStatusCode("LOADED");
        courseStudentEntity.setCreateDate(LocalDateTime.now().minusMinutes(14));
        courseStudentEntity.setUpdateDate(LocalDateTime.now());
        courseStudentEntity.setCreateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);
        courseStudentEntity.setUpdateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);

        courseStudentRepository.save(courseStudentEntity);

        val courseStudent = CourseStudentMapper.mapper.toCourseStudent(courseStudentEntity);
        val saga = this.createCourseMockSaga(courseStudent);
        saga.setSagaId(null);
        this.sagaRepository.save(saga);

        val sagaData = CourseStudentSagaData.builder().courseStudent(courseStudent).school(createMockSchool()).build();
        val event = Event.builder()
                .sagaId(saga.getSagaId())
                .eventType(EventType.INITIATED)
                .eventOutcome(EventOutcome.INITIATE_SUCCESS)
                .eventPayload(JsonUtil.getJsonStringFromObject(sagaData)).build();
        this.courseStudentProcessingOrchestrator.handleEvent(event);

        verify(this.messagePublisher, atMost(2)).dispatchMessage(eq(this.courseStudentProcessingOrchestrator.getTopicToSubscribe()), this.eventCaptor.capture());
        final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
        assertThat(newEvent.getEventType()).isEqualTo(VALIDATE_COURSE_STUDENT);
        assertThat(newEvent.getEventOutcome()).isEqualTo(EventOutcome.VALIDATE_COURSE_STUDENT_SUCCESS_WITH_ERROR);

        val savedSagaInDB = this.sagaRepository.findById(saga.getSagaId());
        assertThat(savedSagaInDB).isPresent();
        assertThat(savedSagaInDB.get().getStatus()).isEqualTo(IN_PROGRESS.toString());
        assertThat(savedSagaInDB.get().getSagaState()).isEqualTo(VALIDATE_COURSE_STUDENT.toString());
    }
}
