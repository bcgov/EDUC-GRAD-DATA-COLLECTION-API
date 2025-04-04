package ca.bc.gov.educ.graddatacollection.api.endpoint.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.URL;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ReportingPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping(URL.REPORTING_PERIOD_URL)
public interface ReportingPeriodEndpoint {

    @GetMapping(URL.ACTIVE)
    @PreAuthorize("hasAnyAuthority('SCOPE_READ_REPORTING_PERIOD')")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
    ReportingPeriod getActiveReportingPeriod();

    @PutMapping()
    @PreAuthorize("hasAuthority('SCOPE_WRITE_REPORTING_PERIOD')")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
    @Transactional
    @Tag(name = "Reporting Period", description = "Endpoints update reporting period.")
    @Schema(name = "ReportingPeriod", implementation = ReportingPeriod.class)
    ReportingPeriod updateReportingPeriod(@Validated @RequestBody ReportingPeriod reportingPeriod);
}
