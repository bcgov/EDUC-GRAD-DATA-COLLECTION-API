package ca.bc.gov.educ.graddatacollection.api.service.v1.events;

import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.orchestrator.base.EventHandler;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static ca.bc.gov.educ.graddatacollection.api.constants.TopicsEnum.GRAD_SCHOOL_EVENTS_TOPIC;

@Service
@Slf4j
public class GradSchoolUpdateEventDelegatorService implements EventHandler {

    private final RestUtils restUtils;
    public static final String PAYLOAD_LOG = "payload is :: {}";

    public GradSchoolUpdateEventDelegatorService(RestUtils restUtils) {
        this.restUtils = restUtils;
    }

    @Async("subscriberExecutor")
    @Override
    public void handleEvent(Event event) {
      try {
        if(event.getEventType() == EventType.UPDATE_GRAD_SCHOOL) {
           log.info("Received UPDATE_GRAD_SCHOOL event :: {}", event.getSagaId());
           log.trace(PAYLOAD_LOG, event.getEventPayload());
           this.handleGradSchoolUpdateEvent();
        } else {
            log.info("silently ignoring other events.");
        }
      } catch (final Exception e) {
         log.error("Exception", e);
      }
    }

    @Override
    public String getTopicToSubscribe() {
        return GRAD_SCHOOL_EVENTS_TOPIC.toString();
    }

    public void handleGradSchoolUpdateEvent() {
        restUtils.populateGradSchoolMap();
    }
}
