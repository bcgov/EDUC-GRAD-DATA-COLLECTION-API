package ca.bc.gov.educ.graddatacollection.api.struct.v1;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(callSuper = true)
public class ErrorFilesetStudent extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "errorFilesetStudentId cannot be null")
    private String errorFilesetStudentId;

    @NotNull(message = "incomingFilesetID cannot be null")
    private String incomingFilesetId;

    @NotNull(message = "pen cannot be null")
    private String pen;

    private String localID;

    private String lastName;

    private String firstName;

    private String birthdate;

    private List<ErrorFilesetStudentValidationIssue> errorFilesetStudentValidationIssues;

}
