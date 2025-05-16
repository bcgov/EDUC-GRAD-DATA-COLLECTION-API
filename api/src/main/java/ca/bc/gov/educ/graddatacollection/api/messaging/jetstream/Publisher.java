package ca.bc.gov.educ.graddatacollection.api.messaging.jetstream;

import ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome;
import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.model.v1.GDCEvent;
import ca.bc.gov.educ.graddatacollection.api.model.v1.GradSagaEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ChoreographedEvent;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.api.StreamConfiguration;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static ca.bc.gov.educ.graddatacollection.api.constants.TopicsEnum.GDC_EVENTS_TOPIC;

@Component("publisher")
@Slf4j
public class Publisher {

    private final JetStream jetStream;

    public static final String STREAM_NAME= "GDC_EVENTS";

    /**
     * Instantiates a new Publisher.
     *
     * @param natsConnection the nats connection
     * @throws IOException           the io exception
     * @throws JetStreamApiException the jet stream api exception
     */
    @Autowired
    public Publisher(final Connection natsConnection) throws IOException, JetStreamApiException {
        this.jetStream = natsConnection.jetStream();
        this.createOrUpdateGDCEventStream(natsConnection);
    }

    /**
     * here only name and replicas and max messages are set, rest all are library default.
     *
     * @param natsConnection the nats connection
     * @throws IOException           the io exception
     * @throws JetStreamApiException the jet stream api exception
     */
    private void createOrUpdateGDCEventStream(final Connection natsConnection) throws IOException, JetStreamApiException {
        val streamConfiguration = StreamConfiguration.builder().name(STREAM_NAME).replicas(1).maxMessages(10000).addSubjects(GDC_EVENTS_TOPIC.toString()).build();
        try {
            natsConnection.jetStreamManagement().updateStream(streamConfiguration);
        } catch (final JetStreamApiException exception) {
            if (exception.getErrorCode() == 404) { // the stream does not exist , lets create it.
                natsConnection.jetStreamManagement().addStream(streamConfiguration);
            } else {
                log.info("exception", exception);
            }
        }

    }


    /**
     * Dispatch choreography event.
     *
     * @param event the event
     */
    public void dispatchChoreographyEvent(final GDCEvent event) {
        if (event != null && event.getEventId() != null) {
            val choreographedEvent = new ChoreographedEvent();
            choreographedEvent.setEventType(EventType.valueOf(event.getEventType()));
            choreographedEvent.setEventOutcome(EventOutcome.valueOf(event.getEventOutcome()));
            choreographedEvent.setEventPayload(event.getEventPayload());
            choreographedEvent.setEventID(event.getEventId().toString());
            choreographedEvent.setCreateUser(event.getCreateUser());
            choreographedEvent.setUpdateUser(event.getUpdateUser());
            try {
                log.info("Broadcasting event :: {}", choreographedEvent);
                val pub = this.jetStream.publishAsync(GDC_EVENTS_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(choreographedEvent));
                pub.thenAcceptAsync(result -> log.info("Event ID :: {} Published to JetStream :: {}", event.getSagaId(), result.getSeqno()));
            } catch (IOException e) {
                log.error("exception while broadcasting message to JetStream", e);
            }
        }
    }
}
