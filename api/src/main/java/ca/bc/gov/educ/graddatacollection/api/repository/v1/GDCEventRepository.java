package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.GDCEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GDCEventRepository extends JpaRepository<GDCEvent, UUID> {

  Optional<GDCEvent> findByEventId(UUID eventId);

  /**
   * Find by saga id and event type optional.
   *
   * @param sagaId    the saga id
   * @param eventType the event type
   * @return the optional
   */
  Optional<GDCEvent> findBySagaIdAndEventType(UUID sagaId, String eventType);


  List<GDCEvent> findByEventStatusAndEventTypeNotIn(String eventStatus, List<String> eventTypes);

  @Query(value = "select event.* from INSTITUTE_EVENT event where event.EVENT_STATUS = :eventStatus " +
          "AND event.CREATE_DATE < :createDate " +
          "AND event.EVENT_TYPE in :eventTypes " +
          "ORDER BY event.CREATE_DATE asc " +
          "FETCH FIRST :limit ROWS ONLY", nativeQuery=true)
  List<GDCEvent> findAllByEventStatusAndCreateDateBeforeAndEventTypeInOrderByCreateDate(String eventStatus, LocalDateTime createDate, int limit, List<String> eventTypes);

}
