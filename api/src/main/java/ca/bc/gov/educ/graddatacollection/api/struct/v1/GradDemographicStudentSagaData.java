package ca.bc.gov.educ.graddatacollection.api.struct.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GradDemographicStudentSagaData implements Serializable {
  private static final long serialVersionUID = -2329245910142215178L;
  private DemographicStudentEntity demographicStudentEntity;
  private SchoolTombstone school;
}
