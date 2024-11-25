package ca.bc.gov.educ.graddatacollection.api.service.v1.events.schedulers;


import ca.bc.gov.educ.graddatacollection.api.constants.SagaStatusEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.helpers.LogHelper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.GradSagaEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.orchestrator.base.Orchestrator;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.SagaRepository;
import ca.bc.gov.educ.graddatacollection.api.service.v1.AssessmentStudentService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseStudentService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.DemographicStudentService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class EventTaskSchedulerAsyncService {
  private final SagaRepository sagaRepository;
  private final Map<String, Orchestrator> sagaOrchestrators = new HashMap<>();
  private final IncomingFilesetRepository incomingFilesetRepository;
  @Setter
  private List<String> statusFilters;
  @Value("${number.students.process.saga}")
  private String numberOfStudentsToProcess;
  private final DemographicStudentService demographicStudentService;
  private final AssessmentStudentService assessmentStudentService;
  private final CourseStudentService courseStudentService;

  public EventTaskSchedulerAsyncService(final List<Orchestrator> orchestrators, final SagaRepository sagaRepository, IncomingFilesetRepository incomingFilesetRepository, DemographicStudentService demographicStudentService, AssessmentStudentService assessmentStudentService, CourseStudentService courseStudentService) {
      this.sagaRepository = sagaRepository;
      this.incomingFilesetRepository = incomingFilesetRepository;
      this.demographicStudentService = demographicStudentService;
      this.assessmentStudentService = assessmentStudentService;
      this.courseStudentService = courseStudentService;
      orchestrators.forEach(orchestrator -> this.sagaOrchestrators.put(orchestrator.getSagaName(), orchestrator));
  }

  @Async("processUncompletedSagasTaskExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void findAndProcessUncompletedSagas() {
    log.debug("Processing uncompleted sagas");
    final var sagas = this.sagaRepository.findTop500ByStatusInOrderByCreateDate(this.getStatusFilters());
    log.debug("Found {} sagas to be retried", sagas.size());
    if (!sagas.isEmpty()) {
      this.processUncompletedSagas(sagas);
    }
  }

  private void processUncompletedSagas(final List<GradSagaEntity> sagas) {
    for (val saga : sagas) {
      if (saga.getUpdateDate().isBefore(LocalDateTime.now().minusMinutes(2))
        && this.sagaOrchestrators.containsKey(saga.getSagaName())) {
        try {
          this.setRetryCountAndLog(saga);
          this.sagaOrchestrators.get(saga.getSagaName()).replaySaga(saga);
        } catch (final InterruptedException ex) {
          Thread.currentThread().interrupt();
          log.error("InterruptedException while findAndProcessPendingSagaEvents :: for saga :: {} :: {}", saga, ex);
        } catch (final IOException | TimeoutException e) {
          log.error("Exception while findAndProcessPendingSagaEvents :: for saga :: {} :: {}", saga, e);
        }
      }
    }
  }

  @Async("processLoadedStudentsTaskExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void findAndPublishLoadedStudentRecordsForProcessing() {
    log.debug("Querying for loaded students to process");
    if (this.sagaRepository.countAllByStatusIn(this.getStatusFilters()) > 100) { // at max there will be 100 parallel sagas.
      log.debug("Saga count is greater than 100, so not processing student records");
      return;
    }

    var completedFilesets = this.incomingFilesetRepository.findCompletedCollectionsForStatusUpdate();
    completedFilesets.forEach(completedFileset -> {
      completedFileset.setFilesetStatusCode(FilesetStatus.COMPLETED.getCode());
      completedFileset.setDemFileStatusCode(FilesetStatus.COMPLETED.getCode());
      completedFileset.setCrsFileStatusCode(FilesetStatus.COMPLETED.getCode());
      completedFileset.setXamFileStatusCode(FilesetStatus.COMPLETED.getCode());
    });
    incomingFilesetRepository.saveAll(completedFilesets);

    final var demographicStudentEntities = this.incomingFilesetRepository.findTopLoadedDEMStudentForProcessing(numberOfStudentsToProcess);
    log.debug("Found :: {} demographic records in loaded status", demographicStudentEntities.size());
    if (!demographicStudentEntities.isEmpty()) {
      this.demographicStudentService.prepareAndSendDemStudentsForFurtherProcessing(demographicStudentEntities);
      return;
    }

    final var assessmentStudentEntities = this.incomingFilesetRepository.findTopLoadedAssessmentStudentForProcessing(numberOfStudentsToProcess);
    log.debug("Found :: {} assessment records in loaded status", assessmentStudentEntities.size());
    if (!assessmentStudentEntities.isEmpty()) {
      this.assessmentStudentService.prepareAndSendAssessmentStudentsForFurtherProcessing(assessmentStudentEntities);
      return;
    }

    final var courseStudentEntities = this.incomingFilesetRepository.findTopLoadedCRSStudentForProcessing(numberOfStudentsToProcess);
    log.debug("Found :: {} course records in loaded status", courseStudentEntities.size());
    if (!courseStudentEntities.isEmpty()) {
      this.courseStudentService.prepareAndSendCourseStudentsForFurtherProcessing(courseStudentEntities);
    }
  }

  public List<String> getStatusFilters() {
    if (this.statusFilters != null && !this.statusFilters.isEmpty()) {
      return this.statusFilters;
    } else {
      final var statuses = new ArrayList<String>();
      statuses.add(SagaStatusEnum.IN_PROGRESS.toString());
      statuses.add(SagaStatusEnum.STARTED.toString());
      return statuses;
    }
  }

  private void setRetryCountAndLog(final GradSagaEntity saga) {
    Integer retryCount = saga.getRetryCount();
    if (retryCount == null || retryCount == 0) {
      retryCount = 1;
    } else {
      retryCount += 1;
    }
    saga.setRetryCount(retryCount);
    this.sagaRepository.save(saga);
    LogHelper.logSagaRetry(saga);
  }
}
