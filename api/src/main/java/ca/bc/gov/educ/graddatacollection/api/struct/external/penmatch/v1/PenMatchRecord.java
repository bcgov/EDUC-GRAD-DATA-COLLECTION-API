package ca.bc.gov.educ.graddatacollection.api.struct.external.penmatch.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;

/**
 * The type Pen match record.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PenMatchRecord implements Serializable {
  private static final long serialVersionUID = 3445788842074331571L;
  /**
   * The Matching pen.
   */
  private String matchingPEN;
  /**
   * The Student id.
   */
  private String studentID;
}
