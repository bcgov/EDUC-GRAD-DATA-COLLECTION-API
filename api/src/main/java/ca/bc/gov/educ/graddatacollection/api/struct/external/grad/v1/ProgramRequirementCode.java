package ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProgramRequirementCode implements Serializable {
    private static final long serialVersionUID = 123456789012345678L;

    @Size(max = 10)
    private String proReqCode;

    @Size(max = 255)
    private String label;

    private String description;

    @Size(max = 10)
    private String requirementTypeCode;

    private String requiredCredits;

    private String notMetDesc;

    private String requiredLevel;

    private String languageOfInstruction;

    private String activeRequirement;

    private String requirementCategory;

    @Size(max = 10)
    private String traxReqNumber;

    @Size(max = 1)
    private String traxReqChar;

    private String createUser;

    private LocalDateTime createDate;

    private String updateUser;

    private LocalDateTime updateDate;
}
