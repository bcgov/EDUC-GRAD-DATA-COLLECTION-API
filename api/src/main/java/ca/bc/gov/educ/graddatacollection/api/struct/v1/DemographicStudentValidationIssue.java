package ca.bc.gov.educ.graddatacollection.api.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DemographicStudentValidationIssue extends BaseStudentValidationIssue implements Serializable {
  private static final long serialVersionUID = 1L;

  private String demographicStudentValidationIssueID;

  @NotNull(message = "demographicStudentID cannot be null")
  private String demographicStudentID;
}
