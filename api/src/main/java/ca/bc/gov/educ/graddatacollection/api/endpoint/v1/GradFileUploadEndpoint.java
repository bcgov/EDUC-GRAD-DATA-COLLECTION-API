package ca.bc.gov.educ.graddatacollection.api.endpoint.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.URL;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFileset;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping(URL.BASE_URL)
public interface GradFileUploadEndpoint {

    @PostMapping("/{schoolID}/file")
    @PreAuthorize("hasAuthority('SCOPE_WRITE_GRAD_COLLECTION')")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    @Tag(name = "Endpoint to Upload an SDC file and convert to json structure.", description = "Endpoint to Upload an SDC file and convert to json structure")
    @Schema(name = "FileUpload", implementation = GradFileUpload.class)
    ResponseEntity<IncomingFileset> processSdcBatchFile(@Validated @RequestBody GradFileUpload fileUpload, @PathVariable(name = "schoolID") String schoolID, @RequestHeader(name = "correlationID") String correlationID);

}
