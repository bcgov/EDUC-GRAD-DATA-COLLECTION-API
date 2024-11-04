package ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
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
public class CareerProgramCode implements Serializable {
    private static final long serialVersionUID = 623456789012345681L;

    @Size(max = 10)
    private String code;

    private String name;

    private String description;

    private Integer displayOrder;

    private String startDate;

    private String endDate;
}
