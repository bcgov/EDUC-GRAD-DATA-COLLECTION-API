package ca.bc.gov.educ.graddatacollection.api.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IncomingFileset extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "incomingFilesetID cannot be null")
    String incomingFilesetID;
    @NotNull(message = "schoolID cannot be null")
    String schoolID;
    String demFileName;
    String demFileUploadDate;
    @Size(max = 10)
    String demFileStatusCode;
    String xamFileName;
    String xamFileUploadDate;
    @Size(max = 10)
    String xamFileStatusCode;
    String crsFileName;
    String crsFileUploadDate;
    @Size(max = 10)
    String crsFileStatusCode;
    @Size(max = 10)
    String filesetStatusCode;

}
