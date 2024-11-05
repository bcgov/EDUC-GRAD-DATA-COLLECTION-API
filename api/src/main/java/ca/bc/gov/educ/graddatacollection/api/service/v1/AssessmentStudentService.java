package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome;
import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.constants.TopicsEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolStudentStatus;
import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.AssessmentStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.model.v1.*;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.AssessmentStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentRulesProcessor;
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
public class AssessmentStudentService {
    private final MessagePublisher messagePublisher;
    private final IncomingFilesetRepository incomingFilesetRepository;
    private final RestUtils restUtils;
    private final AssessmentStudentRepository assessmentStudentRepository;
    private final AssessmentStudentRulesProcessor assessmentStudentRulesProcessor;
    private static final String ASSESSMENT_STUDENT_ID = "assessmentStudentID";
    private static final String EVENT_EMPTY_MSG = "Event String is empty, skipping the publish to topic :: {}";

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<AssessmentStudentValidationIssue> validateStudent(final UUID assessmentStudentID, SchoolTombstone schoolTombstone) {
        var currentStudentEntity = this.assessmentStudentRepository.findById(assessmentStudentID);
        if(currentStudentEntity.isPresent()) {
            var validationErrors = runValidationRules(currentStudentEntity.get(), schoolTombstone);
            saveSdcStudent(currentStudentEntity.get());
            return validationErrors;
        } else {
            throw new EntityNotFoundException(AssessmentStudentEntity.class, ASSESSMENT_STUDENT_ID, assessmentStudentID.toString());
        }
    }

    public void saveSdcStudent(AssessmentStudentEntity studentEntity) {
        studentEntity.setUpdateDate(LocalDateTime.now());
        this.assessmentStudentRepository.save(studentEntity);
    }

    public List<AssessmentStudentValidationIssue> runValidationRules(AssessmentStudentEntity assessmentStudentEntity, SchoolTombstone schoolTombstone) {
        StudentRuleData studentRuleData = new StudentRuleData();
        studentRuleData.setAssessmentStudentEntity(assessmentStudentEntity);
        studentRuleData.setSchool(schoolTombstone);

        val validationErrors = this.assessmentStudentRulesProcessor.processRules(studentRuleData);
        var entity = studentRuleData.getAssessmentStudentEntity();
        entity.getAssessmentStudentValidationIssueEntities().clear();
        entity.getAssessmentStudentValidationIssueEntities().addAll(populateValidationErrors(validationErrors, entity));
        if(validationErrors.stream().anyMatch(val -> val.getValidationIssueSeverityCode().equalsIgnoreCase(StudentValidationIssueSeverityCode.ERROR.toString()))){
            entity.setStudentStatusCode(SchoolStudentStatus.ERROR.getCode());
        }
        return validationErrors;
    }

    public static Set<AssessmentStudentValidationIssueEntity> populateValidationErrors(final List<AssessmentStudentValidationIssue> issues, final AssessmentStudentEntity assessmentStudentEntity) {
        final Set<AssessmentStudentValidationIssueEntity> validationErrors = new HashSet<>();
        issues.forEach(issue -> {
            final AssessmentStudentValidationIssueEntity error = new AssessmentStudentValidationIssueEntity();
            error.setValidationIssueFieldCode(issue.getValidationIssueFieldCode());
            error.setValidationIssueSeverityCode(issue.getValidationIssueSeverityCode());
            error.setValidationIssueCode(issue.getValidationIssueCode());
            error.setAssessmentStudent(assessmentStudentEntity);
            error.setCreateDate(LocalDateTime.now());
            error.setUpdateDate(LocalDateTime.now());
            error.setCreateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);
            error.setUpdateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);
            validationErrors.add(error);
        });
        return validationErrors;
    }

    @Async("publisherExecutor")
    public void prepareAndSendAssessmentStudentsForFurtherProcessing(final List<AssessmentStudentEntity> assessmentStudentEntity) {
        final List<GradAssessmentStudentSagaData> assessmentStudentSagaData = assessmentStudentEntity.stream()
                .map(el -> {
                    val gradAssessmentStudentSagaData = new GradAssessmentStudentSagaData();
                    Optional<IncomingFilesetEntity> incomingFilesetEntity = this.incomingFilesetRepository.findById(el.getIncomingFileset().getIncomingFilesetID());
                    if(incomingFilesetEntity.isPresent()) {
                        var school = this.restUtils.getSchoolBySchoolID(incomingFilesetEntity.get().getSchoolID().toString());
                        gradAssessmentStudentSagaData.setSchool(school.get());
                    }
                    gradAssessmentStudentSagaData.setAssessmentStudent(AssessmentStudentMapper.mapper.toAssessmentStudent(el));
                    return gradAssessmentStudentSagaData;
                }).toList();
        this.publishUnprocessedStudentRecordsForProcessing(assessmentStudentSagaData);
    }

    public void publishUnprocessedStudentRecordsForProcessing(final List<GradAssessmentStudentSagaData> assessmentStudentSagaData) {
        assessmentStudentSagaData.forEach(this::sendIndividualStudentAsMessageToTopic);
    }

    private void sendIndividualStudentAsMessageToTopic(final GradAssessmentStudentSagaData assessmentStudentSagaData) {
        final var eventPayload = JsonUtil.getJsonString(assessmentStudentSagaData);
        if (eventPayload.isPresent()) {
            final Event event = Event.builder().eventType(EventType.READ_ASSESSMENT_STUDENTS_FOR_PROCESSING).eventOutcome(EventOutcome.READ_ASSESSMENT_STUDENTS_FOR_PROCESSING_SUCCESS).eventPayload(eventPayload.get()).assessmentStudentID(String.valueOf(assessmentStudentSagaData.getAssessmentStudent().getAssessmentStudentID())).build();
            final var eventString = JsonUtil.getJsonString(event);
            if (eventString.isPresent()) {
                this.messagePublisher.dispatchMessage(TopicsEnum.READ_ASSESSMENT_STUDENTS_FROM_TOPIC.toString(), eventString.get().getBytes());
            } else {
                log.error(EVENT_EMPTY_MSG, assessmentStudentSagaData);
            }
        } else {
            log.error(EVENT_EMPTY_MSG, assessmentStudentSagaData);
        }
    }

}
