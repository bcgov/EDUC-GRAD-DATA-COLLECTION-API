package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome;
import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.constants.TopicsEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolStudentStatus;
import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.DemographicStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentValidationIssueEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentRulesProcessor;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudent;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentSagaData;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
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
public class DemographicStudentService {
    private static final String DEMOGRAPHIC_STUDENT_ID = "demographicStudentID";
    private final MessagePublisher messagePublisher;
    private final IncomingFilesetRepository incomingFilesetRepository;
    private final RestUtils restUtils;
    private final DemographicStudentRepository demographicStudentRepository;
    private final DemographicRulesService demographicRulesService;
    private final DemographicStudentRulesProcessor demographicStudentRulesProcessor;
    private final ErrorFilesetStudentService errorFilesetStudentService;
    private static final String EVENT_EMPTY_MSG = "Event String is empty, skipping the publish to topic :: {}";

    public DemographicStudentEntity getDemStudent(String pen, UUID incomingFilesetId, UUID schoolID) {
        Optional<DemographicStudentEntity> optionalDemographicStudentEntity;

        if (incomingFilesetId != null) {
            if (schoolID != null) {
                optionalDemographicStudentEntity = demographicStudentRepository.findByIncomingFileset_IncomingFilesetIDAndPenAndIncomingFileset_SchoolIDAndIncomingFileset_FilesetStatusCodeAndStudentStatusCode(incomingFilesetId, pen, schoolID, FilesetStatus.COMPLETED.getCode(), SchoolStudentStatus.VERIFIED.getCode());
            } else {
                throw new IllegalArgumentException("schoolID must be provided.");
            }
        } else {
            if (schoolID != null) {
                optionalDemographicStudentEntity = demographicStudentRepository.findFirstByIncomingFileset_SchoolIDAndIncomingFileset_FilesetStatusCodeAndPenAndStudentStatusCodeOrderByCreateDateDesc(schoolID, SchoolStudentStatus.VERIFIED.getCode(), pen, FilesetStatus.COMPLETED.getCode());
            } else {
                throw new IllegalArgumentException("schoolID must be provided.");
            }
        }

        String incomingFilesetIdString = Objects.toString(incomingFilesetId, null);
        String incomingSchoolIdString = Objects.toString(schoolID, null);
        return optionalDemographicStudentEntity.orElseThrow(() -> new EntityNotFoundException(DemographicStudentEntity.class, "pen: ", pen, "incomingFilesetId: ", incomingFilesetIdString, "incomingSchoolId: ", incomingSchoolIdString)
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<DemographicStudentValidationIssue> validateStudent(final UUID demographicStudentID, SchoolTombstone schoolTombstone) {
        var currentStudentEntity = this.demographicStudentRepository.findById(demographicStudentID);
        if(currentStudentEntity.isPresent()) {
            var validationErrors = runValidationRules(currentStudentEntity.get(), schoolTombstone);
            saveSdcStudent(currentStudentEntity.get());
            return validationErrors;
        } else {
            throw new EntityNotFoundException(DemographicStudentEntity.class, DEMOGRAPHIC_STUDENT_ID, demographicStudentID.toString());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setStudentStatus(final UUID demographicStudentID, final SchoolStudentStatus status) {
        var currentStudentEntity = this.demographicStudentRepository.findById(demographicStudentID);
        if(currentStudentEntity.isPresent()) {
            currentStudentEntity.get().setStudentStatusCode(status.getCode());
            saveSdcStudent(currentStudentEntity.get());
        } else {
            throw new EntityNotFoundException(DemographicStudentEntity.class, DEMOGRAPHIC_STUDENT_ID, demographicStudentID.toString());
        }
    }

    public List<DemographicStudentValidationIssue> runValidationRules(DemographicStudentEntity demographicStudentEntity, SchoolTombstone schoolTombstone) {
        StudentRuleData studentRuleData = new StudentRuleData();
        studentRuleData.setDemographicStudentEntity(demographicStudentEntity);
        studentRuleData.setSchool(schoolTombstone);

        val validationErrors = this.demographicStudentRulesProcessor.processRules(studentRuleData);
        var entity = studentRuleData.getDemographicStudentEntity();
        entity.getDemographicStudentValidationIssueEntities().clear();
        entity.getDemographicStudentValidationIssueEntities().addAll(populateValidationErrors(validationErrors, entity));
        if(validationErrors.stream().anyMatch(val -> val.getValidationIssueSeverityCode().equalsIgnoreCase(StudentValidationIssueSeverityCode.ERROR.toString()))){
            entity.setStudentStatusCode(SchoolStudentStatus.ERROR.getCode());
        }
        return validationErrors;
    }

    public Set<DemographicStudentValidationIssueEntity> populateValidationErrors(final List<DemographicStudentValidationIssue> issues, final DemographicStudentEntity demographicStudent) {
        final Set<DemographicStudentValidationIssueEntity> validationErrors = new HashSet<>();
        issues.forEach(issue -> {
            final DemographicStudentValidationIssueEntity error = new DemographicStudentValidationIssueEntity();
            error.setValidationIssueFieldCode(issue.getValidationIssueFieldCode());
            error.setValidationIssueSeverityCode(issue.getValidationIssueSeverityCode());
            error.setValidationIssueCode(issue.getValidationIssueCode());
            error.setValidationIssueDescription(issue.getValidationIssueDescription());
            error.setDemographicStudent(demographicStudent);
            error.setCreateDate(LocalDateTime.now());
            error.setUpdateDate(LocalDateTime.now());
            error.setCreateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);
            error.setUpdateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API);
            validationErrors.add(error);
        });
        return validationErrors;
    }

    public void saveSdcStudent(DemographicStudentEntity studentEntity) {
        studentEntity.setUpdateDate(LocalDateTime.now());
        this.demographicStudentRepository.save(studentEntity);
    }

    @Async("publisherExecutor")
    public void prepareAndSendDemStudentsForFurtherProcessing(final List<DemographicStudentEntity> demographicStudentEntities) {
        final List<DemographicStudentSagaData> demographicStudentSagaData = demographicStudentEntities.stream()
                .map(el -> {
                    val gradDemographicStudentSagaData = new DemographicStudentSagaData();
                    Optional<IncomingFilesetEntity> incomingFilesetEntity = this.incomingFilesetRepository.findById(el.getIncomingFileset().getIncomingFilesetID());
                    if(incomingFilesetEntity.isPresent()) {
                        var school = this.restUtils.getSchoolBySchoolID(incomingFilesetEntity.get().getSchoolID().toString());
                        gradDemographicStudentSagaData.setSchool(school.get());
                    }
                    gradDemographicStudentSagaData.setDemographicStudent(DemographicStudentMapper.mapper.toDemographicStudent(el));
                    return gradDemographicStudentSagaData;
                }).toList();
        this.publishUnprocessedStudentRecordsForProcessing(demographicStudentSagaData);
    }

    public void publishUnprocessedStudentRecordsForProcessing(final List<DemographicStudentSagaData> demographicStudentSagaData) {
        demographicStudentSagaData.forEach(this::sendIndividualStudentAsMessageToTopic);
    }

    private void sendIndividualStudentAsMessageToTopic(final DemographicStudentSagaData demographicStudentSagaData) {
        final var eventPayload = JsonUtil.getJsonString(demographicStudentSagaData);
        if (eventPayload.isPresent()) {
            final Event event = Event.builder().eventType(EventType.READ_DEM_STUDENTS_FOR_PROCESSING).eventOutcome(EventOutcome.READ_DEM_STUDENTS_FOR_PROCESSING_SUCCESS).eventPayload(eventPayload.get()).demographicStudentID(String.valueOf(demographicStudentSagaData.getDemographicStudent().getDemographicStudentID())).build();
            final var eventString = JsonUtil.getJsonString(event);
            if (eventString.isPresent()) {
                this.messagePublisher.dispatchMessage(TopicsEnum.READ_DEM_STUDENTS_FROM_TOPIC.toString(), eventString.get().getBytes());
            } else {
                log.error(EVENT_EMPTY_MSG, demographicStudentSagaData);
            }
        } else {
            log.error(EVENT_EMPTY_MSG, demographicStudentSagaData);
        }
    }

    public void flagErrorOnStudent(final DemographicStudent demographicStudent) {
        try{
            var demographicStudentEntity = demographicRulesService.getDemographicDataForStudent(UUID.fromString(demographicStudent.getIncomingFilesetID()), demographicStudent.getPen(), demographicStudent.getLastName(), demographicStudent.getLocalID());
            errorFilesetStudentService.flagErrorOnStudent(UUID.fromString(demographicStudent.getIncomingFilesetID()), demographicStudent.getPen(), demographicStudentEntity, demographicStudent.getCreateUser(), LocalDateTime.parse(demographicStudent.getCreateDate()), demographicStudent.getUpdateUser(), LocalDateTime.parse(demographicStudent.getUpdateDate()));
        } catch (Exception e) {
            log.info("Adding student to error fileset failed, will be retried :: {}", e);
            throw new GradDataCollectionAPIRuntimeException("Adding student to error fileset failed, will be retried");
        }
    }

}
