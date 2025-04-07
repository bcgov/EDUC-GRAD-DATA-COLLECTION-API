package ca.bc.gov.educ.graddatacollection.api.validator;

import ca.bc.gov.educ.graddatacollection.api.model.v1.ReportingPeriodEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ReportingPeriodRepository;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ReportingPeriod;
import ca.bc.gov.educ.graddatacollection.api.util.ValidationUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
@Component
public class ReportingPeriodValidator {

    public static final String REPORITNG_PERIOD_ID = "reportingPeriodId";

    private final ReportingPeriodRepository reportingPeriodRepository;

    @Autowired
    public ReportingPeriodValidator(ReportingPeriodRepository reportingPeriodRepository) {
        this.reportingPeriodRepository = reportingPeriodRepository;
    }

    public List<FieldError> validatePayload(ReportingPeriod reportingPeriod) {
        final List<FieldError> apiValidationErrors = new ArrayList<>();

        Optional<ReportingPeriodEntity> reportingPeriodEntity = this.reportingPeriodRepository.findById(UUID.fromString(reportingPeriod.getReportingPeriodID()));
        if (reportingPeriodEntity.isEmpty()) {
            apiValidationErrors.add(ValidationUtil.createFieldError(REPORITNG_PERIOD_ID, reportingPeriod.getReportingPeriodID(), "Invalid reporting period ID."));
        }

        return apiValidationErrors;
    }
}
