package ca.bc.gov.educ.graddatacollection.api.controller.v1;

import ca.bc.gov.educ.graddatacollection.api.endpoint.v1.ReportingPeriodEndpoint;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.ReportingPeriodMapper;
import ca.bc.gov.educ.graddatacollection.api.service.v1.ReportingPeriodService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.ReportingSummaryService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ReportingCycleSummary;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ReportingPeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ReportingPeriodController implements ReportingPeriodEndpoint {
    private final ReportingPeriodService reportingPeriodService;
    private final ReportingSummaryService reportingSummaryService;
    private final ReportingPeriodMapper mapper =  ReportingPeriodMapper.mapper;

    @Override
    public ReportingPeriod getActiveReportingPeriod() {
        return mapper.toStructure(reportingPeriodService.getActiveReportingPeriod());
    }

    @Override
    public ReportingCycleSummary getReportingCycleSummary(UUID reportingPeriodID, String type) {
        return reportingSummaryService.getReportingSummary(reportingPeriodID, type);
    }
}
