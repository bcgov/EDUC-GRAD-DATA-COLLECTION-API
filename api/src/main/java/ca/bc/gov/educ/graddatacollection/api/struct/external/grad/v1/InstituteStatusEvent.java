package ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstituteStatusEvent {

    private UUID eventId;
    private String eventPayload;
    private String eventStatus;
    private String eventType;
    String createUser;
    LocalDateTime createDate;
    String updateUser;
    LocalDateTime updateDate;
    private UUID sagaId;
    private String eventOutcome;
    private String replyChannel;
}
