package ca.bc.gov.educ.graddatacollection.api.controller.v1;

import ca.bc.gov.educ.graddatacollection.api.endpoint.v1.ReportingPeriodEndpoint;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.ReportingPeriodMapper;
import ca.bc.gov.educ.graddatacollection.api.service.v1.ReportingPeriodService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ReportingPeriod;
import ca.bc.gov.educ.graddatacollection.api.util.RequestUtil;
import ca.bc.gov.educ.graddatacollection.api.util.ValidationUtil;
import ca.bc.gov.educ.graddatacollection.api.validator.ReportingPeriodValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ReportingPeriodController implements ReportingPeriodEndpoint {
    private final ReportingPeriodService reportingPeriodService;
    private final ReportingPeriodValidator reportingPeriodValidator;
    private final ReportingPeriodMapper mapper =  ReportingPeriodMapper.mapper;

    public ReportingPeriodController(ReportingPeriodService reportingPeriodService, ReportingPeriodValidator reportingPeriodValidator) {
        this.reportingPeriodService = reportingPeriodService;
        this.reportingPeriodValidator = reportingPeriodValidator;
    }

    public ReportingPeriod getActiveReportingPeriod() {
        return mapper.toStructure(reportingPeriodService.getActiveReportingPeriod());
    }

    public ReportingPeriod updateReportingPeriod(ReportingPeriod reportingPeriod) {
        ValidationUtil.validatePayload(() -> this.reportingPeriodValidator.validatePayload(reportingPeriod));
        RequestUtil.setAuditColumnsForUpdate(reportingPeriod);
        return mapper.toStructure(
                reportingPeriodService.updateReportingPeriod(mapper.toReportingPeriodEntity(reportingPeriod)));
    }
}
