package ca.bc.gov.educ.graddatacollection.api.schedulers;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.constants.TopicsEnum;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ReportingPeriodEntity;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.*;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.service.v1.events.schedulers.EventTaskSchedulerAsyncService;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EventTaskSchedulerTest extends BaseGradDataCollectionAPITest {

    @Autowired
    EventTaskScheduler eventTaskScheduler;
    @Autowired
    EventTaskSchedulerAsyncService eventTaskSchedulerAsyncService;
    @Autowired
    SagaRepository sagaRepository;
    @Autowired
    SagaEventRepository sagaEventRepository;
    @Autowired
    MessagePublisher messagePublisher;
    @MockBean
    protected RestUtils restUtils;
    @Autowired
    DemographicStudentRepository demographicStudentRepository;
    @Autowired
    IncomingFilesetRepository incomingFilesetRepository;
    @Autowired
    CourseStudentRepository courseStudentRepository;
    @Autowired
    AssessmentStudentRepository assessmentStudentRepository;
    @Autowired
    ReportingPeriodRepository reportingPeriodRepository;
    @Captor
    ArgumentCaptor<byte[]> eventCaptor;

    @BeforeEach
    public void setUp() {
        this.demographicStudentRepository.deleteAll();
        this.incomingFilesetRepository.deleteAll();
        this.courseStudentRepository.deleteAll();
        this.assessmentStudentRepository.deleteAll();
        this.reportingPeriodRepository.deleteAll();
    }

    @Test
    void testFindAndPublishLoadedStudentRecordsForProcessing_DemographicStudents() throws JsonProcessingException {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        mockFileset.setSchoolID(UUID.fromString(school.getSchoolId()));
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
        eventTaskScheduler.processLoadedStudents();

        verify(this.messagePublisher, atMost(1)).dispatchMessage(eq(TopicsEnum.READ_DEM_STUDENTS_FROM_TOPIC.toString()), this.eventCaptor.capture());
        final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
        assertThat(newEvent.getEventType()).isEqualTo(EventType.READ_DEM_STUDENTS_FOR_PROCESSING);
    }

    @Test
    void testFindAndPublishLoadedStudentRecordsForProcessing_CourseStudents() throws JsonProcessingException {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        mockFileset.setSchoolID(UUID.fromString(school.getSchoolId()));
        var savedFileset = incomingFilesetRepository.save(mockFileset);

        var courseStudentEntity = createMockCourseStudent(savedFileset);
        courseStudentEntity.setCourseStudentID(null);
        courseStudentEntity.setStudentStatusCode("LOADED");
        courseStudentEntity.setIncomingFileset(mockFileset);
        courseStudentEntity.setCreateDate(LocalDateTime.now().minusMinutes(14));
        courseStudentEntity.setUpdateDate(LocalDateTime.now());
        courseStudentEntity.setCreateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);
        courseStudentEntity.setUpdateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);

        courseStudentRepository.save(courseStudentEntity);
        eventTaskSchedulerAsyncService.findAndPublishLoadedStudentRecordsForProcessing();

        verify(this.messagePublisher, atMost(1)).dispatchMessage(eq(TopicsEnum.READ_COURSE_STUDENTS_FROM_TOPIC.toString()), this.eventCaptor.capture());
        final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
        assertThat(newEvent.getEventType()).isEqualTo(EventType.READ_COURSE_STUDENTS_FOR_PROCESSING);
    }

    @Test
    void testFindAndPublishLoadedStudentRecordsForProcessing_AssessmentStudents() throws JsonProcessingException {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var mockFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        mockFileset.setSchoolID(UUID.fromString(school.getSchoolId()));
        incomingFilesetRepository.save(mockFileset);

        var assessmentStudentEntity = createMockAssessmentStudent();
        assessmentStudentEntity.setAssessmentStudentID(null);
        assessmentStudentEntity.setStudentStatusCode("LOADED");
        assessmentStudentEntity.setIncomingFileset(mockFileset);
        assessmentStudentEntity.setCreateDate(LocalDateTime.now().minusMinutes(14));
        assessmentStudentEntity.setUpdateDate(LocalDateTime.now());
        assessmentStudentEntity.setCreateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);
        assessmentStudentEntity.setUpdateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);

        assessmentStudentRepository.save(assessmentStudentEntity);
        eventTaskSchedulerAsyncService.findAndPublishLoadedStudentRecordsForProcessing();

        verify(this.messagePublisher, atMost(1)).dispatchMessage(eq(TopicsEnum.READ_ASSESSMENT_STUDENTS_FROM_TOPIC.toString()), this.eventCaptor.capture());
        final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
        assertThat(newEvent.getEventType()).isEqualTo(EventType.READ_ASSESSMENT_STUDENTS_FOR_PROCESSING);
    }

    @Test
    @Transactional
    void testSetupReportingPeriodForUpcomingYear() {
        int currentYear = LocalDate.now().getYear();

        LocalDate dateInOctober = LocalDate.of(currentYear, Month.OCTOBER, 1);
        LocalDate firstOctoberMondayDate = dateInOctober.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
        LocalDateTime schoolYearStart = firstOctoberMondayDate.atStartOfDay();

        LocalDate dateInJuly = LocalDate.of(currentYear + 1, Month.JULY, 1);
        LocalDate thirdJulyFridayDate = dateInJuly.with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.FRIDAY));
        LocalDateTime schoolYearEnd = thirdJulyFridayDate.atStartOfDay();

        LocalDate dateInAugust = LocalDate.of(currentYear + 1, Month.AUGUST, 1);
        LocalDate firstAugustMondayDate = dateInAugust.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
        LocalDateTime summerStart = firstAugustMondayDate.atStartOfDay();

        LocalDate dateInSeptember = LocalDate.of(currentYear + 1, Month.SEPTEMBER, 1);
        LocalDate thirdSeptemberFridayDate = dateInSeptember.with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.FRIDAY));
        LocalDateTime summerEnd = thirdSeptemberFridayDate.atStartOfDay();

        eventTaskSchedulerAsyncService.createReportingPeriodForYear();

        List<ReportingPeriodEntity> reportingPeriods = reportingPeriodRepository.findAll();
        assertThat(reportingPeriods)
                .hasSize(1)
                .anySatisfy(period -> {
                    assertThat(period.getSchYrStart()).isEqualTo(schoolYearStart);
                    assertThat(period.getSchYrEnd()).isEqualTo(schoolYearEnd);
                    assertThat(period.getSummerStart()).isEqualTo(summerStart);
                    assertThat(period.getSummerEnd()).isEqualTo(summerEnd);
                });
    }
}
