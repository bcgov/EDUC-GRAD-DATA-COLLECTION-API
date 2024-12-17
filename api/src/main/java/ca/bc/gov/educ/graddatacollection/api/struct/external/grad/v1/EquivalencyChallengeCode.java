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
public class EquivalencyChallengeCode implements Serializable {
    private static final long serialVersionUID = 987654321098765432L;

    @NotNull(message = "equivalentOrChallengeCode cannot be null.")
    private String equivalentOrChallengeCode;

    @Size(max = 50)
    @NotNull(message = "label cannot be null.")
    private String label;

    @Size(max = 355)
    @NotNull(message = "description cannot be null.")
    private String description;

    @NotNull(message = "displayOrder cannot be null.")
    private String displayOrder;

    @NotNull(message = "effectiveDate cannot be null.")
    private String effectiveDate;

    private String expiryDate;

    private String createUser;

    private String createDate;

    private String updateUser;

    private String updateDate;
}
