package ca.bc.gov.educ.graddatacollection.api.endpoint.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.URL;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ReportingCycleSummary;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ReportingPeriod;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@RequestMapping(URL.REPORTING_PERIOD_URL)
public interface ReportingPeriodEndpoint {

    @GetMapping(URL.ACTIVE)
    @PreAuthorize("hasAnyAuthority('SCOPE_READ_REPORTING_PERIOD')")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
    ReportingPeriod getActiveReportingPeriod();

    @GetMapping("/{reportingPeriodID}/summary")
    @PreAuthorize("hasAnyAuthority('SCOPE_READ_REPORTING_PERIOD')")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
    ReportingCycleSummary getReportingCycleSummary(@PathVariable("reportingPeriodID") UUID reportingPeriodID, @RequestParam("type") String type);
}
