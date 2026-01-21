package ca.bc.gov.educ.graddatacollection.api.schedulers;

import ca.bc.gov.educ.graddatacollection.api.service.v1.IncomingFilesetService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static lombok.AccessLevel.PRIVATE;

@Component
@Slf4j
public class PurgeStaleIncomingFilesetsScheduler {
    @Getter(PRIVATE)
    private final IncomingFilesetService incomingFilesetService;

    public PurgeStaleIncomingFilesetsScheduler(final IncomingFilesetService incomingFilesetService) {
        this.incomingFilesetService = incomingFilesetService;
    }

    /**
     * Run the job based on configured scheduler(a cron expression) and purge old records from DB.
     */
    @Scheduled(cron = "${scheduled.jobs.purge.stale.incoming.filesets.cron}")
    @SchedulerLock(name = "PurgeStaleIncomingFilesetsLock", lockAtLeastFor = "${scheduled.jobs.purge.stale.incoming.filesets.cron.lockAtLeastFor}", lockAtMostFor = "${scheduled.jobs.purge.stale.incoming.filesets.cron.lockAtMostFor}")
    public void purgeStaleFinalIncomingFilesetsRecords() {
        LockAssert.assertLocked();
        log.info("Purging stale Incoming Filesets records from EDUC-GRAD-DATA-COLLECTION-API.");
        this.incomingFilesetService.purgeStaleFinalIncomingFilesetRecords();
        log.info("Finished purging stale Incoming Filesets records from EDUC-GRAD-DATA-COLLECTION-API");
    }
}
