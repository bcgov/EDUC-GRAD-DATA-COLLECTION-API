package ca.bc.gov.educ.graddatacollection.api.endpoint.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.URL;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorAndWarningSummary;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFileset;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@RequestMapping(URL.METRICS)
public interface MetricsEndpoint {

    @GetMapping("/submission")
    @PreAuthorize("hasAuthority('SCOPE_READ_INCOMING_FILESET')")
    @Transactional(readOnly = true)
    @ApiResponses(value={@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")})
    ResponseEntity<IncomingFileset> generateSubmissionMetrics(@RequestParam(name = "schoolID") UUID schoolID);

    @GetMapping("/errors")
    @PreAuthorize("hasAuthority('SCOPE_READ_INCOMING_FILESET')")
    @Transactional(readOnly = true)
    @ApiResponses(value={@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")})
    ResponseEntity<ErrorAndWarningSummary> generateErrorAndWarningMetrics(@RequestParam(name = "schoolID") UUID schoolID);
}
