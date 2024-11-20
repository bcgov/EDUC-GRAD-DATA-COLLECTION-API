package ca.bc.gov.educ.graddatacollection.api.controller.v1;

import ca.bc.gov.educ.graddatacollection.api.endpoint.v1.ReportGenerationEndpoint;
import ca.bc.gov.educ.graddatacollection.api.service.v1.reports.CSVReportService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.reports.DownloadableReportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ReportGenerationController implements ReportGenerationEndpoint {
    private final CSVReportService csvReportService;

    @Override
    public DownloadableReportResponse generateErrorReport() {
        return csvReportService.generateErrorReport();
    }
}
