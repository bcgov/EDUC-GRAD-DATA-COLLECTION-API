package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome;
import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.constants.TopicsEnum;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.CourseStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradCourseStudentSagaData;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradDemographicStudentSagaData;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseStudentService {
    private final MessagePublisher messagePublisher;
    private final IncomingFilesetRepository incomingFilesetRepository;
    private final RestUtils restUtils;
    private static final String EVENT_EMPTY_MSG = "Event String is empty, skipping the publish to topic :: {}";


    @Async("publisherExecutor")
    public void prepareAndSendCourseStudentsForFurtherProcessing(final List<CourseStudentEntity> courseStudentEntities) {
        final List<GradCourseStudentSagaData> courseStudentSagaData = courseStudentEntities.stream()
                .map(el -> {
                    val gradCourseStudentSagaData = new GradCourseStudentSagaData();
                    Optional<IncomingFilesetEntity> incomingFilesetEntity = this.incomingFilesetRepository.findById(el.getIncomingFileset().getIncomingFilesetID());
                    if(incomingFilesetEntity.isPresent()) {
                        var school = this.restUtils.getSchoolBySchoolID(incomingFilesetEntity.get().getSchoolID().toString());
                        gradCourseStudentSagaData.setSchool(school.get());
                    }
                    gradCourseStudentSagaData.setCourseStudent(CourseStudentMapper.mapper.toCourseStudent(el));
                    return gradCourseStudentSagaData;
                }).toList();
        this.publishUnprocessedStudentRecordsForProcessing(courseStudentSagaData);
    }

    public void publishUnprocessedStudentRecordsForProcessing(final List<GradCourseStudentSagaData> courseStudentSagaData) {
        courseStudentSagaData.forEach(this::sendIndividualStudentAsMessageToTopic);
    }

    private void sendIndividualStudentAsMessageToTopic(final GradCourseStudentSagaData courseStudentSagaData) {
        final var eventPayload = JsonUtil.getJsonString(courseStudentSagaData);
        if (eventPayload.isPresent()) {
            final Event event = Event.builder().eventType(EventType.READ_COURSE_STUDENTS_FOR_PROCESSING).eventOutcome(EventOutcome.READ_COURSE_STUDENTS_FOR_PROCESSING_SUCCESS).eventPayload(eventPayload.get()).courseStudentID(String.valueOf(courseStudentSagaData.getCourseStudent().getCourseStudentID())).build();
            final var eventString = JsonUtil.getJsonString(event);
            if (eventString.isPresent()) {
                this.messagePublisher.dispatchMessage(TopicsEnum.READ_COURSE_STUDENTS_FROM_TOPIC.toString(), eventString.get().getBytes());
            } else {
                log.error(EVENT_EMPTY_MSG, courseStudentSagaData);
            }
        } else {
            log.error(EVENT_EMPTY_MSG, courseStudentSagaData);
        }
    }

}
