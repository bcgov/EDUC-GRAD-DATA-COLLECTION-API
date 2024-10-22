package ca.bc.gov.educ.graddatacollection.api.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileUploadSummary {
    private String schoolID;
    private List<FileUploadCounts> counts;
}
