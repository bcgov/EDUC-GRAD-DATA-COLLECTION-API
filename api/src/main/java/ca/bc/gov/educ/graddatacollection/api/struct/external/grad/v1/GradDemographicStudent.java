package ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1;

import ca.bc.gov.educ.graddatacollection.api.struct.v1.BaseRequest;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
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
public class GradDemographicStudent extends BaseRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String mincode;

    private String schoolID;

    private String schoolReportingRequirementCode;

    private String birthdate;

    private String pen;

    private String citizenship;

    private String grade;

    private String programCode1;

    private String programCode2;

    private String programCode3;

    private String programCode4;

    private String programCode5;

    private String gradRequirementYear;

    private String schoolCertificateCompletionDate;

    private String studentStatus;

    private String isSummerCollection;

    private String vendorID;

}
