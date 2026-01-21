package ca.bc.gov.educ.graddatacollection.api.schedulers;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

//Override the cron configuration for scheduled.jobs.purge.stale.incoming.filesets.cron for these tests to ensure the scheduler runs every second.
@SpringBootTest(properties = "scheduled.jobs.purge.stale.incoming.filesets.cron=* * * * * *")
class PurgeStaleIncomingFilesetsSchedulerTest extends BaseGradDataCollectionAPITest {
    @SpyBean
    PurgeStaleIncomingFilesetsScheduler purgeStaleIncomingFilesetsScheduler;
    @Test
    void purgeStaleIncomingFilesetsSchedulerIsTriggered() {
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> verify(purgeStaleIncomingFilesetsScheduler, atLeast(1)).purgeStaleFinalIncomingFilesetsRecords());
    }
}
