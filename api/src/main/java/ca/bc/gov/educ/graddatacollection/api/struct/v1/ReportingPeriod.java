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
public class ReportingPeriod extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "reportingPeriodID cannot be null")
    private String reportingPeriodID;

    @NotNull(message = "schYrStart cannot be null")
    private String schYrStart;

    @NotNull(message = "schYrEnd cannot be null")
    private String schYrEnd;

    @NotNull(message = "summerStart cannot be null")
    private String summerStart;

    @NotNull(message = "summerEnd cannot be null")
    private String summerEnd;

    @NotNull(message = "periodStart cannot be null")
    private String periodStart;

    @NotNull(message = "periodEnd cannot be null")
    private String periodEnd;

}
