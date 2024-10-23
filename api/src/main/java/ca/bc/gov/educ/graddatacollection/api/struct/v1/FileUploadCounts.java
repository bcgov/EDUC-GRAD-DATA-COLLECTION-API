package ca.bc.gov.educ.graddatacollection.api.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileUploadCounts {
    private String fileName;
    private String uploadDate;
    private String percentageStudentsProcessed;
}
