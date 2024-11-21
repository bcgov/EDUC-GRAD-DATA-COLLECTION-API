package ca.bc.gov.educ.graddatacollection.api.endpoint.v1;

import ca.bc.gov.educ.graddatacollection.api.struct.v1.reports.DownloadableReportResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.URL;

import java.util.UUID;

@RequestMapping(URL.BASE_URL_REPORT_GENERATION)
public interface ReportGenerationEndpoint {
    @GetMapping("/errorReport/{schoolID}")
    @PreAuthorize("hasAuthority('SCOPE_READ_FILESET_STUDENT_ERROR')")
    @Transactional(readOnly = true)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
    DownloadableReportResponse generateErrorReport(@PathVariable("schoolID") UUID incomingFilesetId);
}
