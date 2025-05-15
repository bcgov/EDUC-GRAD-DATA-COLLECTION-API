package ca.bc.gov.educ.graddatacollection.api.util;

import ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome;
import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.model.v1.GDCEvent;

import java.time.LocalDateTime;

import static ca.bc.gov.educ.graddatacollection.api.constants.EventStatus.DB_COMMITTED;


public class EventUtil {
  private EventUtil() {
  }

  public static GDCEvent createGDCEvent(String createUser, String updateUser, String jsonString, EventType eventType, EventOutcome eventOutcome) {
    return GDCEvent.builder()
      .createDate(LocalDateTime.now())
      .updateDate(LocalDateTime.now())
      .createUser(createUser)
      .updateUser(updateUser)
      .eventPayload(jsonString)
      .eventType(eventType.toString())
      .eventStatus(DB_COMMITTED.toString())
      .eventOutcome(eventOutcome.toString())
      .build();
  }
}
