package ca.bc.gov.educ.graddatacollection.api.struct;

import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationEvent extends Event {
  private String sagaStatus;
  private String sagaName;
  private String sdcSchoolStudentID;
  private String sdcSchoolBatchID;
}
