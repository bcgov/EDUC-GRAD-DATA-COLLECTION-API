package ca.bc.gov.educ.graddatacollection.api.endpoint.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.URL;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFileset;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SummerStudentDataResponse;
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
    @Tag(name = "Endpoint to upload a GRAD file and convert to json structure.", description = "Endpoint to upload a GRAD file and convert to json structure")
    @Schema(name = "FileUpload", implementation = GradFileUpload.class)
    ResponseEntity<IncomingFileset> processSchoolBatchFile(@Validated @RequestBody GradFileUpload fileUpload, @PathVariable(name = "schoolID") String schoolID);

    @PostMapping("/district/{districtID}/file")
    @PreAuthorize("hasAuthority('SCOPE_WRITE_GRAD_COLLECTION')")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    @Tag(name = "Endpoint for the district to upload a GRAD file and convert to json structure.", description = "Endpoint for the district to upload a GRAD file and convert to json structure")
    @Schema(name = "FileUpload", implementation = GradFileUpload.class)
    ResponseEntity<IncomingFileset> processDistrictBatchFile(@Validated @RequestBody GradFileUpload fileUpload, @PathVariable(name = "districtID") String districtID);

    @PostMapping("/{schoolID}/excel-upload")
    @PreAuthorize("hasAuthority('SCOPE_WRITE_GRAD_COLLECTION')")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    @Tag(name = "Endpoint to Upload an excel file and convert to json structure.", description = "Endpoint to Upload an excel file and convert to json structure")
    @Schema(name = "FileUpload", implementation = GradFileUpload.class)
    ResponseEntity<SummerStudentDataResponse> processSchoolExcelFile(@Validated @RequestBody GradFileUpload fileUpload, @PathVariable(name = "schoolID") String schoolID);

}
