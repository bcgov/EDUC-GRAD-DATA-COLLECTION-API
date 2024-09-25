package ca.bc.gov.educ.graddatacollection.api.struct.v1;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GradFileUpload {
    @NotNull
    String fileName;
    @NotNull
    String createUser;
    String updateUser;
    @NotNull
    @ToString.Exclude
    String fileContents;
    @NotNull
    String fileType; //dem,xam,crs
}
