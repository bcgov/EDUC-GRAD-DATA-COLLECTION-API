package ca.bc.gov.educ.graddatacollection.api.service.v1.events.schedulers;

import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static lombok.AccessLevel.PRIVATE;

@Service
@Slf4j
public class EventPublisherService {

  /**
   * The constant RESPONDING_BACK_TO_NATS_ON_CHANNEL.
   */
  public static final String RESPONDING_BACK_TO_NATS_ON_CHANNEL = "responding back to NATS on {} channel ";

  @Getter(PRIVATE)
  private final MessagePublisher messagePublisher;

  @Autowired
  public EventPublisherService(final MessagePublisher messagePublisher) {
    this.messagePublisher = messagePublisher;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void send(final Event event) throws JsonProcessingException {
    if (event.getReplyTo() != null) {
      log.debug(RESPONDING_BACK_TO_NATS_ON_CHANNEL, event.getReplyTo());
      this.getMessagePublisher().dispatchMessage(event.getReplyTo(), this.gradEventProcessed(event));
    }
  }

  private byte[] gradEventProcessed(final Event gradEvent) throws JsonProcessingException {
    final Event event = Event.builder()
        .sagaId(gradEvent.getSagaId())
        .eventType(gradEvent.getEventType())
        .eventOutcome(gradEvent.getEventOutcome())
        .eventPayload(gradEvent.getEventPayload()).build();
    return JsonUtil.getJsonStringFromObject(event).getBytes();
  }

}
