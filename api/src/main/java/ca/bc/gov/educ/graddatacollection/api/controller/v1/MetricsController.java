package ca.bc.gov.educ.graddatacollection.api.controller.v1;

import ca.bc.gov.educ.graddatacollection.api.endpoint.v1.MetricsEndpoint;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.IncomingFilesetMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.service.v1.MetricsService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorAndWarningSummary;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFileset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MetricsController implements MetricsEndpoint {
    private final MetricsService metricsService;

    @Override
    public ResponseEntity<IncomingFileset> generateSubmissionMetrics(UUID schoolID) {
        log.debug("Retrieving submission metrics for schoolID: {}", schoolID.toString());
        IncomingFilesetEntity submissionMetrics = metricsService.getFilesetData(schoolID);

        return ResponseEntity.ok(IncomingFilesetMapper.mapper.toStructure(submissionMetrics));
    }

    @Override
    public ResponseEntity<ErrorAndWarningSummary> generateErrorAndWarningMetrics(UUID schoolID){
        log.debug("Retrieving error and warning summary metrics for schoolID: {}", schoolID.toString());
        ErrorAndWarningSummary errorAndWarningSummary = metricsService.getErrorAndWarningSummary(schoolID);

        return ResponseEntity.ok(errorAndWarningSummary);
    }
}
