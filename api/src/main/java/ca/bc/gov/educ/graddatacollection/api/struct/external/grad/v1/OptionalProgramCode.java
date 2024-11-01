package ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
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

    private LocalDateTime effectiveDate;

    private LocalDateTime expiryDate;

    private String graduationProgramCode;

    private String associatedCredential;

    private String createUser;

    private LocalDateTime createDate;

    private String updateUser;

    private LocalDateTime updateDate;
}
