package ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1;

import ca.bc.gov.educ.graddatacollection.api.struct.v1.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(callSuper = true)
public class DemStudentSchoolOfRecordAndStatus extends BaseRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "schoolOfRecordID cannot be null")
    private String schoolOfRecordID;
    @NotNull(message = "pen cannot be null")
    private String pen;
    @NotNull(message = "status cannot be null")
    private String status;
}