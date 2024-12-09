package ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
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
@SuppressWarnings("squid:S1700")
public class LetterGrade implements Serializable {
    private static final long serialVersionUID = 8123456789012345678L;

    @Size(max = 2)
    @NotNull(message = "grade cannot be null.")
    private String grade;

    @Size(max = 100)
    @NotNull(message = "gpaMarkValue cannot be null.")
    private String gpaMarkValue;

    @Size(max = 4)
    @NotNull(message = "passFlag cannot be null.")
    private String passFlag;

    @Size(max = 550)
    @NotNull(message = "description cannot be null.")
    private String description;

    @Size(max = 45)
    @NotNull(message = "label cannot be null.")
    private String label;

    @NotNull(message = "percentRangeHigh cannot be null.")
    private Integer percentRangeHigh;

    @NotNull(message = "percentRangeLow cannot be null.")
    private Integer percentRangeLow;

    @NotNull(message = "expiryDate cannot be null.")
    private String expiryDate;

    @NotNull(message = "effectiveDate cannot be null.")
    private String effectiveDate;

    private String createUser;

    private String createDate;

    private String updateUser;

    private String updateDate;
}
