package ca.bc.gov.educ.graddatacollection.api.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(callSuper = true)
public class ErrorAndWarningSummary {
    private String schoolID;
    private String filesetID;
    private String totalStudents;
    private String totalErrors;
    private String totalWarnings;
    private FileWarningErrorCounts demCounts;
    private FileWarningErrorCounts xamCounts;
    private FileWarningErrorCounts crsCounts;
}
