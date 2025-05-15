package ca.bc.gov.educ.graddatacollection.api.service.v1.events;

import ca.bc.gov.educ.graddatacollection.api.repository.v1.GDCEventRepository;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ChoreographedEvent;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static ca.bc.gov.educ.graddatacollection.api.constants.EventStatus.MESSAGE_PUBLISHED;

/**
 * This class will process events from Jet Stream, which is used in choreography pattern, where messages are published if a student is created or updated.
 */
@Service
@Slf4j
public class JetStreamEventHandlerService {

  private final GDCEventRepository gdcEventRepository;


  /**
   * Instantiates a new Stan event handler service.
   *
   * @param gdcEventRepository the gdc event repository
   */
  @Autowired
  public JetStreamEventHandlerService(GDCEventRepository gdcEventRepository) {
    this.gdcEventRepository = gdcEventRepository;
  }

  /**
   * Update event status.
   *
   * @param choreographedEvent the choreographed event
   */
  @Transactional
  public void updateEventStatus(ChoreographedEvent choreographedEvent) {
    if (choreographedEvent != null && choreographedEvent.getEventID() != null) {
      var eventID = UUID.fromString(choreographedEvent.getEventID());
      var eventOptional = gdcEventRepository.findById(eventID);
      if (eventOptional.isPresent()) {
        var studentEvent = eventOptional.get();
        studentEvent.setEventStatus(MESSAGE_PUBLISHED.toString());
        gdcEventRepository.save(studentEvent);
      }
    }
  }
}
