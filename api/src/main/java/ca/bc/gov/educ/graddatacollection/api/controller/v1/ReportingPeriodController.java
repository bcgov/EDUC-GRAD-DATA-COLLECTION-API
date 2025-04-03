package ca.bc.gov.educ.graddatacollection.api.controller.v1;

import ca.bc.gov.educ.graddatacollection.api.endpoint.v1.ReportingPeriodEndpoint;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.ReportingPeriodMapper;
import ca.bc.gov.educ.graddatacollection.api.service.v1.ReportingPeriodService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ReportingPeriod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ReportingPeriodController implements ReportingPeriodEndpoint {
    private final ReportingPeriodService reportingPeriodService;

    private final ReportingPeriodMapper mapper =  ReportingPeriodMapper.mapper;

    public ReportingPeriodController(ReportingPeriodService reportingPeriodService) {
        this.reportingPeriodService = reportingPeriodService;
    }

    public ReportingPeriod getActiveReportingPeriod() {
        return mapper.toStructure(reportingPeriodService.getActiveReportingPeriod());
    }
}
