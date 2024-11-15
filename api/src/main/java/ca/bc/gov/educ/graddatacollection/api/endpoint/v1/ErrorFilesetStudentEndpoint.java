package ca.bc.gov.educ.graddatacollection.api.endpoint.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.URL;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorFilesetStudent;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.FileUploadSummary;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFileset;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping(URL.BASE_URL)
public interface ErrorFilesetStudentEndpoint {

    @GetMapping("/{incomingFileSetId}/errors")
    @PreAuthorize("hasAuthority('SCOPE_READ_FILESET_STUDENT_ERROR')")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    @Tag(name = "Endpoint to list errors on an IncomingFileset.", description = "Endpoint to list errors on an IncomingFileset.")
    ResponseEntity<ErrorFilesetStudent> findErrors(@PathVariable(name = "incomingFileSetId") String incomingFileSetId, @RequestHeader(name = "correlationID") String correlationID);

}
