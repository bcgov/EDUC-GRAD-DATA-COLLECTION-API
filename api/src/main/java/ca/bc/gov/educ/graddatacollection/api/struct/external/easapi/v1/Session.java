package ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1;

import ca.bc.gov.educ.graddatacollection.api.struct.v1.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Session extends BaseRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    private String sessionID;
    private String schoolYear;
    private Integer courseMonth;
    private Integer courseYear;

    @NotNull(message = "activeFromDate cannot be null")
    private LocalDateTime activeFromDate;

    @NotNull(message = "activeUntilDate cannot be null")
    private LocalDateTime activeUntilDate;
}
