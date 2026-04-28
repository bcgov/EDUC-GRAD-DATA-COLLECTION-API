package ca.bc.gov.educ.graddatacollection.api.service.v1.events;

import ca.bc.gov.educ.graddatacollection.api.choreographer.ChoreographEventHandler;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.service.v1.ChoreographedEventPersistenceService;
import io.nats.client.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventHandlerDelegatorServiceTest {

    @Mock
    private ChoreographedEventPersistenceService choreographedEventPersistenceService;
    @Mock
    private ChoreographEventHandler choreographer;
    @Mock
    private RestUtils restUtils;
    @Mock
    private Message message;
    @InjectMocks
    private EventHandlerDelegatorService eventHandlerDelegatorService;

    @Test
    void handleRefreshChoreographyEventRefreshesGdcCaches() throws IOException {
        eventHandlerDelegatorService.handleRefreshChoreographyEvent(message);

        verify(message).ack();
        verify(restUtils).populateGradSchoolMap();
        verify(restUtils).populateAssessmentSessionMap();
    }
}
