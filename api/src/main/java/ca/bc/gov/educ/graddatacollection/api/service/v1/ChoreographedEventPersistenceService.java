package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.exception.BusinessError;
import ca.bc.gov.educ.graddatacollection.api.exception.BusinessException;
import ca.bc.gov.educ.graddatacollection.api.model.v1.GDCEvent;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.GDCEventRepository;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ChoreographedEvent;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static ca.bc.gov.educ.graddatacollection.api.constants.EventStatus.DB_COMMITTED;


/**
 * The type Choreographed event persistence service.
 */
@Service
@Slf4j
public class ChoreographedEventPersistenceService {
  private final GDCEventRepository eventRepository;

  /**
   * Instantiates a new Choreographed event persistence service.
   *
   * @param eventRepository the event repository
   */
  @Autowired
  public ChoreographedEventPersistenceService(final GDCEventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public GDCEvent persistEventToDB(final ChoreographedEvent choreographedEvent) throws BusinessException {
    final var eventOptional = this.eventRepository.findByEventId(UUID.fromString(choreographedEvent.getEventID()));
    if (eventOptional.isPresent()) {
      throw new BusinessException(BusinessError.EVENT_ALREADY_PERSISTED, choreographedEvent.getEventID().toString());
    }
    val event = GDCEvent.builder()
      .eventType(choreographedEvent.getEventType().toString())
      .eventId(UUID.fromString(choreographedEvent.getEventID()))
      .eventOutcome(choreographedEvent.getEventOutcome().toString())
      .eventPayload(choreographedEvent.getEventPayload())
      .eventStatus(DB_COMMITTED.toString())
      .createUser(StringUtils.isBlank(choreographedEvent.getCreateUser()) ? "GRAD-DATA-COLLECTION-API" : choreographedEvent.getCreateUser())
      .updateUser(StringUtils.isBlank(choreographedEvent.getUpdateUser()) ? "GRAD-DATA-COLLECTION-API" : choreographedEvent.getUpdateUser())
      .createDate(LocalDateTime.now())
      .updateDate(LocalDateTime.now())
      .build();
    return this.eventRepository.save(event);
  }
}
