package ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GradStatusEvent {

  private UUID eventId;
  private byte[] eventPayloadBytes;
  private String eventStatus;
  private String eventType;
  String createUser;
  LocalDateTime createDate;
  String updateUser;
  LocalDateTime updateDate;
  private UUID sagaId;
  private String eventOutcome;
  private String replyChannel;
  private String activityCode;

  public String getEventPayload() {
    return new String(getEventPayloadBytes(), StandardCharsets.UTF_8);
  }

  public void setEventPayload(String eventPayload) {
    setEventPayloadBytes(eventPayload.getBytes(StandardCharsets.UTF_8));
  }

}
