package ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("squid:S1700")
public class SchoolGradeCode implements Serializable {

  private static final long serialVersionUID = 6118916290604876032L;

  private String schoolGradeCode;

  private String label;

  private String description;

  private Integer displayOrder;

  private String effectiveDate;

  private String expiryDate;
}
