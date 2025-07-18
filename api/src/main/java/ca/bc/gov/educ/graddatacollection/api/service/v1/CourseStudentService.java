package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome;
import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.constants.TopicsEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolStudentStatus;
import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.CourseStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentLightEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentValidationIssueEntity;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.CourseStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentRulesProcessor;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.*;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseStudentService {
    private final MessagePublisher messagePublisher;
    private final IncomingFilesetRepository incomingFilesetRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final CourseRulesService courseRulesService;
    private final RestUtils restUtils;
    private final CourseStudentRulesProcessor courseStudentRulesProcessor;
    private final ErrorFilesetStudentService errorFilesetStudentService;
    private static final String COURSE_STUDENT_ID = "courseStudentID";
    private static final String EVENT_EMPTY_MSG = "Event String is empty, skipping the publish to topic :: {}";

    public List<CourseStudentEntity> getCrsStudents(String pen, UUID incomingFilesetId, UUID schoolID) {
        List<CourseStudentEntity> courseStudentList;

        if (schoolID != null) {
            courseStudentList = courseStudentRepository.findAllByIncomingFileset_IncomingFilesetIDAndPenAndIncomingFileset_SchoolIDAndIncomingFileset_FilesetStatusCodeAndStudentStatusCodeNot(incomingFilesetId, pen, schoolID, FilesetStatus.COMPLETED.getCode(), SchoolStudentStatus.LOADED.getCode());
        } else {
            throw new IllegalArgumentException("schoolID must be provided.");
        }

        log.info("getCrsStudents: {}", courseStudentList);
        return courseStudentList;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<CourseStudentValidationIssue> validateStudent(final UUID courseStudentID, SchoolTombstone schoolTombstone) {
        var currentStudentEntity = this.courseStudentRepository.findById(courseStudentID);
        if(currentStudentEntity.isPresent()) {
            var validationErrors = runValidationRules(currentStudentEntity.get(), schoolTombstone);
            saveSdcStudent(currentStudentEntity.get());
            return validationErrors;
        } else {
            throw new EntityNotFoundException(CourseStudentEntity.class, COURSE_STUDENT_ID, courseStudentID.toString());
        }
    }

    public void saveSdcStudent(CourseStudentEntity studentEntity) {
        studentEntity.setUpdateDate(LocalDateTime.now());
        this.courseStudentRepository.save(studentEntity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setStudentStatus(final UUID courseStudentID, final SchoolStudentStatus status) {
        var currentStudentEntity = this.courseStudentRepository.findById(courseStudentID);
        if(currentStudentEntity.isPresent()) {
            currentStudentEntity.get().setStudentStatusCode(status.getCode());
            saveSdcStudent(currentStudentEntity.get());
        } else {
            throw new EntityNotFoundException(CourseStudentEntity.class, COURSE_STUDENT_ID, courseStudentID.toString());
        }
    }

    public List<CourseStudentValidationIssue> runValidationRules(CourseStudentEntity courseStudentEntity, SchoolTombstone schoolTombstone) {
        StudentRuleData studentRuleData = new StudentRuleData();
        studentRuleData.setCourseStudentEntity(courseStudentEntity);
        studentRuleData.setSchool(schoolTombstone);

        val validationErrors = this.courseStudentRulesProcessor.processRules(studentRuleData);
        var entity = studentRuleData.getCourseStudentEntity();
        entity.getCourseStudentValidationIssueEntities().clear();
        entity.getCourseStudentValidationIssueEntities().addAll(populateValidationErrors(validationErrors, entity));
        if(validationErrors.stream().anyMatch(val -> val.getValidationIssueSeverityCode().equalsIgnoreCase(StudentValidationIssueSeverityCode.ERROR.toString()))){
            entity.setStudentStatusCode(SchoolStudentStatus.ERROR.getCode());
        }
        return validationErrors;
    }

    public Set<CourseStudentValidationIssueEntity> populateValidationErrors(final List<CourseStudentValidationIssue> issues, final CourseStudentEntity courseStudentEntity) {
        final Set<CourseStudentValidationIssueEntity> validationErrors = new HashSet<>();
        issues.forEach(issue -> {
            final CourseStudentValidationIssueEntity error = new CourseStudentValidationIssueEntity();
            error.setValidationIssueFieldCode(issue.getValidationIssueFieldCode());
            error.setValidationIssueSeverityCode(issue.getValidationIssueSeverityCode());
            error.setValidationIssueCode(issue.getValidationIssueCode());
            error.setValidationIssueDescription(issue.getValidationIssueDescription());
            error.setCourseStudent(courseStudentEntity);
            error.setCreateDate(LocalDateTime.now());
            error.setUpdateDate(LocalDateTime.now());
            error.setCreateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);
            error.setUpdateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);
            validationErrors.add(error);
        });
        return validationErrors;
    }

    @Async("publisherExecutor")
    public void prepareAndSendCourseStudentsForFurtherProcessing(final List<CourseStudentLightEntity> courseStudentEntities) {
        final List<CourseStudentSagaData> courseStudentSagaData = courseStudentEntities.stream()
                .map(el -> {
                    val gradCourseStudentSagaData = new CourseStudentSagaData();
                    var school = this.restUtils.getSchoolBySchoolID(el.getIncomingFileset().getSchoolID().toString());
                    gradCourseStudentSagaData.setSchool(school.get());
                    gradCourseStudentSagaData.setCourseStudent(CourseStudentMapper.mapper.toCourseStudent(el));
                    return gradCourseStudentSagaData;
                }).toList();
        this.publishUnprocessedStudentRecordsForProcessing(courseStudentSagaData);
    }

    public void publishUnprocessedStudentRecordsForProcessing(final List<CourseStudentSagaData> courseStudentSagaData) {
        courseStudentSagaData.forEach(this::sendIndividualStudentAsMessageToTopic);
    }

    private void sendIndividualStudentAsMessageToTopic(final CourseStudentSagaData courseStudentSagaData) {
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

    @Async("publisherExecutor")
    public void prepareAndSendCourseStudentsForDownstreamProcessing(final List<ICourseStudentUpdate> entities) {
        final List<CourseStudentUpdate> courseStudentUpdateList = entities.stream()
                .map(el -> {
                    val courseStudentUpdate = new CourseStudentUpdate();
                    courseStudentUpdate.setIncomingFilesetID(el.getIncomingFilesetID());
                    courseStudentUpdate.setPen(el.getPen());
                    return courseStudentUpdate;
                }).toList();
        courseStudentUpdateList.forEach(this::sendIndividualStudentAsMessageForDownstreamUpdateToTopic);
    }

    private void sendIndividualStudentAsMessageForDownstreamUpdateToTopic(final CourseStudentUpdate courseStudentUpdate) {
        final var eventPayload = JsonUtil.getJsonString(courseStudentUpdate);
        if (eventPayload.isPresent()) {
            final Event event = Event.builder().eventType(EventType.READ_COURSE_STUDENTS_FOR_DOWNSTREAM_UPDATE).eventOutcome(EventOutcome.READ_COURSE_STUDENTS_FOR_DOWNSTREAM_UPDATE_SUCCESS).eventPayload(eventPayload.get()).incomingFilesetID(String.valueOf(courseStudentUpdate.getIncomingFilesetID())).build();
            final var eventString = JsonUtil.getJsonString(event);
            if (eventString.isPresent()) {
                this.messagePublisher.dispatchMessage(TopicsEnum.READ_COURSE_STUDENTS_FOR_DOWNSTREAM_UPDATE_TOPIC.toString(), eventString.get().getBytes());
            } else {
                log.error(EVENT_EMPTY_MSG, courseStudentUpdate);
            }
        } else {
            log.error(EVENT_EMPTY_MSG, courseStudentUpdate);
        }
    }

    public void flagErrorOnStudent(final CourseStudent courseStudent) {
        try{
            var demographicStudentEntity = courseRulesService.getDemographicDataForStudent(UUID.fromString(courseStudent.getIncomingFilesetID()), courseStudent.getPen(), courseStudent.getLastName(), courseStudent.getLocalID());
            errorFilesetStudentService.flagErrorOnStudent(UUID.fromString(courseStudent.getIncomingFilesetID()), courseStudent.getPen(), demographicStudentEntity, courseStudent.getCreateUser(), LocalDateTime.parse(courseStudent.getCreateDate()), courseStudent.getUpdateUser(), LocalDateTime.parse(courseStudent.getUpdateDate()));
        } catch (Exception e) {
            log.info("Adding student to error fileset failed, will be retried :: {}", e);
            throw new GradDataCollectionAPIRuntimeException("Adding student to error fileset failed, will be retried");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStudentStatus(final CourseStudentUpdate courseStudentUpdate, final SchoolStudentStatus status) {
        List<CourseStudentEntity> currentStudentEntity = this.courseRulesService.findByIncomingFilesetIDAndPenAndStudentStatusCode(UUID.fromString(courseStudentUpdate.getIncomingFilesetID()), courseStudentUpdate.getPen());
        currentStudentEntity.forEach(courseStudentEntity -> courseStudentEntity.setStudentStatusCode(status.getCode()));
        courseStudentRepository.saveAll(currentStudentEntity);
    }

}
