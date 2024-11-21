package ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OptionalProgramCode implements Serializable {
    private static final long serialVersionUID = 523456789012345679L;

    private UUID optionalProgramID;

    @Size(max = 10)
    private String optProgramCode;

    private String optionalProgramName;

    private String description;

    private Integer displayOrder;

    private String effectiveDate;

    private String expiryDate;

    private String graduationProgramCode;

    private String associatedCredential;

    private String createUser;

    private String createDate;

    private String updateUser;

    private String updateDate;
}
