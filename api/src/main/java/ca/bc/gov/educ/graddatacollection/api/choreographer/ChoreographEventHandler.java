package ca.bc.gov.educ.graddatacollection.api.choreographer;

import ca.bc.gov.educ.graddatacollection.api.constants.EventStatus;
import ca.bc.gov.educ.graddatacollection.api.messaging.jetstream.Publisher;
import ca.bc.gov.educ.graddatacollection.api.model.v1.GDCEvent;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.GDCEventRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.service.v1.EventService;
import ca.bc.gov.educ.graddatacollection.api.struct.external.gradschools.v1.GradSchool;
import ca.bc.gov.educ.graddatacollection.api.util.EventUtil;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome.GDC_CACHE_UPDATED;
import static ca.bc.gov.educ.graddatacollection.api.constants.EventType.REFRESH_GDC_CACHE;


/**
 * The type Choreograph event handler.
 */
@Component
@Slf4j
public class ChoreographEventHandler {
  private final Executor singleTaskExecutor = new EnhancedQueueExecutor.Builder()
    .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("task-executor-%d").build())
    .setCorePoolSize(2).setMaximumPoolSize(2).build();
  private final Map<String, EventService<?>> eventServiceMap;
  private final GDCEventRepository eventRepository;
  private final Publisher publisher;

  /**
   * Instantiates a new Choreograph event handler.
   *
   * @param eventServices   the event services
   * @param eventRepository the event repository
   */
  public ChoreographEventHandler(final List<EventService<?>> eventServices, final GDCEventRepository eventRepository, Publisher publisher) {
    this.eventRepository = eventRepository;
      this.eventServiceMap = new HashMap<>();
    eventServices.forEach(eventService -> this.eventServiceMap.put(eventService.getEventType(), eventService));
    this.publisher = publisher;
  }

  /**
   * Handle event.
   *
   * @param event the event
   */
  public void handleEvent(@NonNull final GDCEvent event) {
    this.singleTaskExecutor.execute(() -> {
      val eventFromDBOptional = this.eventRepository.findById(event.getEventId());
      if (eventFromDBOptional.isPresent()) {
        val eventFromDB = eventFromDBOptional.get();
        if (eventFromDB.getEventStatus().equals(EventStatus.DB_COMMITTED.toString())) {
          log.info("Processing event with event ID :: {}", event.getEventId());
          try {
            switch (event.getEventType()) {
              case "UPDATE_GRAD_SCHOOL":
                log.info("Processing UPDATE_GRAD_SCHOOL event record :: {} ", event);
                final GDCEvent gdcEvent = EventUtil.createGDCEvent(
                        ApplicationProperties.GRAD_DATA_COLLECTION_API,
                        ApplicationProperties.GRAD_DATA_COLLECTION_API,
                        event.getEventPayload(),
                        REFRESH_GDC_CACHE,
                        GDC_CACHE_UPDATED
                        );
                eventRepository.save(gdcEvent);
                publisher.dispatchChoreographyEvent(gdcEvent);
                updateEvent(event);
                break;
              default:
                log.warn("Silently ignoring event: {}", event);
                updateEvent(event);
                break;
            }
            log.info("Event was processed, ID :: {}", event.getEventId());
          } catch (final Exception exception) {
            log.error("Exception while processing event :: {}", event, exception);
          }
        }
      }

    });
  }

  private void updateEvent(final GDCEvent event) {
    this.eventRepository.findByEventId(event.getEventId()).ifPresent(existingEvent -> {
      existingEvent.setEventStatus(EventStatus.PROCESSED.toString());
      existingEvent.setUpdateDate(LocalDateTime.now());
      this.eventRepository.save(existingEvent);
    });
  }
}
