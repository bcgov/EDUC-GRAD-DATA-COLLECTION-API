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
public class DemographicStudent extends BaseRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String demographicStudentID;

    @NotNull(message = "incomingFilesetID cannot be null")
    private String incomingFilesetID;

    private String studentStatusCode;

    private String transactionID;

    private String localID;

    private String vendorID;

    private String pen;

    private String lastName;

    private String middleName;

    private String firstName;

    private String addressLine1;

    private String addressLine2;

    private String city;

    private String provincialCode;

    private String countryCode;

    private String postalCode;

    private String birthdate;

    private String gender;

    private String citizenship;

    private String grade;

    private String programCode1;

    private String programCode2;

    private String programCode3;

    private String programCode4;

    private String programCode5;

    private String programCadreFlag;

    private String gradRequirementYear;

    private String schoolCertificateCompletionDate;

    private String studentStatus;

    private List<DemographicStudentValidationIssue> demographicStudentValidationIssue;

}
