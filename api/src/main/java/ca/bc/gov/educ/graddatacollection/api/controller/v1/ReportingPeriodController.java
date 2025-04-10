package ca.bc.gov.educ.graddatacollection.api.controller.v1;

import ca.bc.gov.educ.graddatacollection.api.endpoint.v1.ReportingPeriodEndpoint;
import ca.bc.gov.educ.graddatacollection.api.exception.ReportingPeriodValidationException;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.ReportingPeriodMapper;
import ca.bc.gov.educ.graddatacollection.api.service.v1.ReportingPeriodService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.ReportingSummaryService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ReportingCycleSummary;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ReportingPeriod;
import ca.bc.gov.educ.graddatacollection.api.util.RequestUtil;
import ca.bc.gov.educ.graddatacollection.api.validator.ReportingPeriodValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
public class ReportingPeriodController implements ReportingPeriodEndpoint {
    private final ReportingPeriodService reportingPeriodService;
    private final ReportingSummaryService reportingSummaryService;
    private final ReportingPeriodMapper mapper =  ReportingPeriodMapper.mapper;
    private final ReportingPeriodValidator reportingPeriodValidator;

    public ReportingPeriodController(ReportingPeriodService reportingPeriodService, ReportingSummaryService reportingSummaryService, ReportingPeriodValidator reportingPeriodValidator) {
        this.reportingPeriodService = reportingPeriodService;
        this.reportingSummaryService = reportingSummaryService;
        this.reportingPeriodValidator = reportingPeriodValidator;
    }

    @Override
    public ReportingPeriod getActiveReportingPeriod() {
        return mapper.toStructure(reportingPeriodService.getActiveReportingPeriod());
    }

    @Override
    public ReportingPeriod getPreviousReportingPeriod() {
        return mapper.toStructure(reportingPeriodService.getPreviousReportingPeriod());
    }

    @Override
    public ReportingCycleSummary getReportingCycleSummary(UUID reportingPeriodID, String type) {
        return reportingSummaryService.getReportingSummary(reportingPeriodID, type);
    }

    @Override
    public ReportingPeriod updateReportingPeriod(ReportingPeriod reportingPeriod) {
        List<FieldError> errors = this.reportingPeriodValidator.validatePayload(reportingPeriod);
        if (!errors.isEmpty()) {
            throw new ReportingPeriodValidationException(errors);
        }
        RequestUtil.setAuditColumnsForUpdate(reportingPeriod);
        return mapper.toStructure(
                reportingPeriodService.updateReportingPeriod(mapper.toReportingPeriodEntity(reportingPeriod)));
    }
}
