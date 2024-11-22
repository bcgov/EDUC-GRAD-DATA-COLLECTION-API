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
public class GradGrade implements Serializable {
    private static final long serialVersionUID = 8123456789012345678L;

    @Size(max = 10)
    @NotNull(message = "studentGradeCode cannot be null.")
    private String studentGradeCode;

    @Size(max = 100)
    @NotNull(message = "label cannot be null.")
    private String label;

    @Size(max = 255)
    @NotNull(message = "description cannot be null.")
    private String description;

    @NotNull(message = "displayOrder cannot be null.")
    private Integer displayOrder;

    @NotNull(message = "effectiveDate cannot be null.")
    private String effectiveDate;

    @NotNull(message = "expiryDate cannot be null.")
    private String expiryDate;

    private String expected;

    private String createUser;

    private String createDate;

    private String updateUser;

    private String updateDate;
}
