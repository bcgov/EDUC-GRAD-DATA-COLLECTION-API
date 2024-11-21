package ca.bc.gov.educ.graddatacollection.api.controller.v1;

import ca.bc.gov.educ.graddatacollection.api.endpoint.v1.ReportGenerationEndpoint;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.service.v1.reports.CSVReportService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.reports.DownloadableReportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ReportGenerationController implements ReportGenerationEndpoint {
    private final CSVReportService csvReportService;
    private final IncomingFilesetRepository incomingFilesetRepository;

    @Override
    public DownloadableReportResponse generateErrorReport(UUID schoolID) {
        var filesetOptional = incomingFilesetRepository.findBySchoolID(schoolID);
        if (filesetOptional.isPresent()) {
            var fileset = filesetOptional.get();
            return csvReportService.generateErrorReport(fileset.getIncomingFilesetID());
        } else {
            throw new IllegalArgumentException("No incoming fileset found for the given school ID: " + schoolID);
        }
    }
}
