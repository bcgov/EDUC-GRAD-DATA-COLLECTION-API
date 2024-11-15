package ca.bc.gov.educ.graddatacollection.api.struct.v1;


import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public class ErrorFilesetStudent extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "errorFilesetStudentId cannot be null")
    String errorFilesetStudentId;
    @NotNull(message = "incomingFilesetID cannot be null")
    String incomingFilesetId;
    @NotNull(message = "pen cannot be null")
    String pen;
}
