package ca.bc.gov.educ.graddatacollection.api.schedulers;

import ca.bc.gov.educ.graddatacollection.api.service.v1.events.schedulers.EventTaskSchedulerAsyncService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static lombok.AccessLevel.PRIVATE;

/**
 * The type Event task scheduler.
 */
@Component
@Slf4j
public class EventTaskScheduler {
  /**
   * The Task scheduler async service.
   */
  @Getter(PRIVATE)
  private final EventTaskSchedulerAsyncService taskSchedulerAsyncService;

  /**
   * Instantiates a new Event task scheduler.
   *
   * @param taskSchedulerAsyncService the task scheduler async service
   */
  @Autowired
  public EventTaskScheduler(final EventTaskSchedulerAsyncService taskSchedulerAsyncService) {
    this.taskSchedulerAsyncService = taskSchedulerAsyncService;
  }

  @Scheduled(cron = "${scheduled.jobs.extract.uncompleted.sagas.cron}") // 1 * * * * *
  @SchedulerLock(name = "EXTRACT_UNCOMPLETED_SAGAS",
    lockAtLeastFor = "${scheduled.jobs.extract.uncompleted.sagas.cron.lockAtLeastFor}", lockAtMostFor = "${scheduled.jobs.extract.uncompleted.sagas.cron.lockAtMostFor}")
  public void findAndProcessPendingSagaEvents() {
    LockAssert.assertLocked();
    log.debug("Started findAndProcessPendingSagaEvents scheduler");
    this.getTaskSchedulerAsyncService().findAndProcessUncompletedSagas();
    log.debug("Scheduler findAndProcessPendingSagaEvents complete");
  }

  @Scheduled(cron = "${scheduled.jobs.process.loaded.grad.students.cron}")
  @SchedulerLock(name = "PROCESS_LOADED_STUDENTS", lockAtLeastFor = "${scheduled.jobs.process.loaded.grad.students.cron.lockAtLeastFor}", lockAtMostFor = "${scheduled.jobs.process.loaded.grad.students.cron.lockAtMostFor}")
  public void processLoadedStudents() {
    LockAssert.assertLocked();
    log.debug("Started processLoadedStudents scheduler");
    this.getTaskSchedulerAsyncService().findAndPublishLoadedStudentRecordsForProcessing();
    log.debug("Scheduler processLoadedStudents complete");
  }

  @Scheduled(cron = "${scheduled.jobs.setup.reporting.period.cron}")
  @SchedulerLock(name = "SETUP_REPORTING_PERIOD", lockAtLeastFor = "${scheduled.jobs.setup.reporting.period.cron.lockAtLeastFor}", lockAtMostFor = "${scheduled.jobs.setup.reporting.period.cron.lockAtMostFor}")
  public void setupReportingPeriodForUpcomingYear() {
    LockAssert.assertLocked();
    log.debug("Started setupReportingPeriodForUpcomingYear scheduler");
    this.getTaskSchedulerAsyncService().createReportingPeriodForYearAndPurge5YearOldFilesets();
    log.debug("Scheduler setupReportingPeriodForYear complete");
  }
}
