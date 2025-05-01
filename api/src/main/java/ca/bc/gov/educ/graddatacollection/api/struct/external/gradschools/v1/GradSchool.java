package ca.bc.gov.educ.graddatacollection.api.struct.external.gradschools.v1;

import ca.bc.gov.educ.graddatacollection.api.struct.v1.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GradSchool extends BaseRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  private String gradSchoolID;

  @NotNull
  private String schoolID;

  @NotNull
  private String submissionModeCode;

  @NotNull
  private String canIssueTranscripts;

  @NotNull
  private String canIssueCertificates;

}
