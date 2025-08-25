package ca.bc.gov.educ.graddatacollection.api.service.v1.events.schedulers;


import ca.bc.gov.educ.graddatacollection.api.constants.SagaStatusEnum;
import ca.bc.gov.educ.graddatacollection.api.helpers.LogHelper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.GradSagaEntity;
import ca.bc.gov.educ.graddatacollection.api.orchestrator.base.Orchestrator;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.*;
import ca.bc.gov.educ.graddatacollection.api.service.v1.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class EventTaskSchedulerAsyncService {
  private final SagaRepository sagaRepository;
  private final Map<String, Orchestrator> sagaOrchestrators = new HashMap<>();
  private final IncomingFilesetRepository incomingFilesetRepository;
  private final DemographicStudentLightRepository demographicStudentLightRepository;
  private final AssessmentStudentLightRepository assessmentStudentLightRepository;
  private final CourseStudentLightRepository courseStudentLightRepository;
  private final ReportingPeriodRepository reportingPeriodRepository;
  @Setter
  private List<String> statusFilters;
  @Value("${number.students.process.saga}")
  private String numberOfStudentsToProcess;
  private final DemographicStudentService demographicStudentService;
  private final AssessmentStudentService assessmentStudentService;
  private final CourseStudentService courseStudentService;
  private final ReportingPeriodService reportingPeriodService;
  private final IncomingFilesetService incomingFilesetService;

  public EventTaskSchedulerAsyncService(final List<Orchestrator> orchestrators, final SagaRepository sagaRepository, IncomingFilesetRepository incomingFilesetRepository, DemographicStudentLightRepository demographicStudentLightRepository, AssessmentStudentLightRepository assessmentStudentLightRepository, CourseStudentLightRepository courseStudentLightRepository, ReportingPeriodRepository reportingPeriodRepository, DemographicStudentService demographicStudentService, AssessmentStudentService assessmentStudentService, CourseStudentService courseStudentService, ReportingPeriodService reportingPeriodService, IncomingFilesetService incomingFilesetService) {
      this.sagaRepository = sagaRepository;
      this.incomingFilesetRepository = incomingFilesetRepository;
      this.demographicStudentLightRepository = demographicStudentLightRepository;
      this.assessmentStudentLightRepository = assessmentStudentLightRepository;
      this.courseStudentLightRepository = courseStudentLightRepository;
      this.reportingPeriodRepository = reportingPeriodRepository;
      this.demographicStudentService = demographicStudentService;
      this.assessmentStudentService = assessmentStudentService;
      this.courseStudentService = courseStudentService;
      this.reportingPeriodService = reportingPeriodService;
      this.incomingFilesetService = incomingFilesetService;
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
    log.info("Querying for loaded students to process");
    if (this.sagaRepository.countAllByStatusIn(this.getStatusFilters()) > 100) { // at max there will be 100 parallel sagas.
      log.info("Saga count is greater than 100, so not processing student records");
      return;
    }

    var completedFilesets = this.incomingFilesetRepository.findCompletedCollectionsForStatusUpdate();
    if (!completedFilesets.isEmpty()) {
      this.incomingFilesetService.prepareAndSendCompletedFilesetsForFurtherProcessing(completedFilesets);
      return;
    }

    final var demographicStudentEntities = this.demographicStudentLightRepository.findTopLoadedDEMStudentForProcessing(numberOfStudentsToProcess);
    log.debug("Found :: {} demographic records in loaded status", demographicStudentEntities.size());
    if (!demographicStudentEntities.isEmpty()) {
      this.demographicStudentService.prepareAndSendDemStudentsForFurtherProcessing(demographicStudentEntities);
      return;
    }

    final var assessmentStudentEntities = this.assessmentStudentLightRepository.findTopLoadedAssessmentStudentForProcessing(numberOfStudentsToProcess);
    log.debug("Found :: {} assessment records in loaded status", assessmentStudentEntities.size());
    if (!assessmentStudentEntities.isEmpty()) {
      this.assessmentStudentService.prepareAndSendAssessmentStudentsForFurtherProcessing(assessmentStudentEntities);
      return;
    }

    final var courseStudentEntities = this.courseStudentLightRepository.findTopLoadedCRSStudentForProcessing(numberOfStudentsToProcess);
    log.debug("Found :: {} course records in loaded status", courseStudentEntities.size());
    if (!courseStudentEntities.isEmpty()) {
      this.courseStudentService.prepareAndSendCourseStudentsForFurtherProcessing(courseStudentEntities);
      return;
    }

    final var courseStudentEntitiesToUpdate = this.courseStudentLightRepository.findTopLoadedCRSStudentForDownstreamUpdate(Integer.parseInt(numberOfStudentsToProcess));
    log.debug("Found :: {} course student packages in loaded status", courseStudentEntitiesToUpdate.size());
    if (!courseStudentEntitiesToUpdate.isEmpty()) {
      this.courseStudentService.prepareAndSendCourseStudentsForDownstreamProcessing(courseStudentEntitiesToUpdate);
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createReportingPeriodForYearAndPurge5YearOldFilesets(){
    int schoolYearStart = LocalDate.now().getYear();
    try {
      if (this.reportingPeriodRepository.upcomingReportingPeriodDoesNotExist(schoolYearStart)) {
        log.debug("Creating reporting period for {}/{}", schoolYearStart, schoolYearStart + 1);
        this.reportingPeriodService.createReportingPeriodForYear();
      }
      log.info("Purging incoming filesets which are 5 years old");
      this.reportingPeriodService.purgeReportingPeriodFor5YearsAgo();
    } catch (Exception e) {
      log.error("Error creating reporting period for {}/{}: ", schoolYearStart, schoolYearStart + 1, e);
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
