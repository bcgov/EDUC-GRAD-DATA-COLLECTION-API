package ca.bc.gov.educ.graddatacollection.api.validator;

import ca.bc.gov.educ.graddatacollection.api.model.v1.ReportingPeriodEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ReportingPeriodRepository;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ReportingPeriod;
import ca.bc.gov.educ.graddatacollection.api.util.ValidationUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
@Component
public class ReportingPeriodValidator {

    public static final String REPORITNG_PERIOD_ID = "reportingPeriodId";
    public static final String SCHOOL_YEAR_START = "School Year Start";
    public static final String SUMMER_START = "Summer Start";

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
            return apiValidationErrors;
        }

        try {
            LocalDateTime schYrStart = LocalDateTime.parse(reportingPeriod.getSchYrStart());
            LocalDateTime schYrEnd = LocalDateTime.parse(reportingPeriod.getSchYrEnd());
            LocalDateTime summerStart = LocalDateTime.parse(reportingPeriod.getSummerStart());
            LocalDateTime summerEnd = LocalDateTime.parse(reportingPeriod.getSummerEnd());

            LocalDateTime cycleStart = LocalDateTime.of(schYrStart.getYear(), 10, 1, 0, 0);
            LocalDateTime cycleEnd = LocalDateTime.of(summerEnd.getYear(), 9, 30, 23, 59);

            if (schYrStart.isAfter(schYrEnd)) {
                apiValidationErrors.add(ValidationUtil.createFieldError(SCHOOL_YEAR_START, reportingPeriod.getSchYrStart(), "School Year start date must be before or equal to end date."));
            }
            if (summerStart.isAfter(summerEnd)) {
                apiValidationErrors.add(ValidationUtil.createFieldError(SUMMER_START, reportingPeriod.getSummerStart(), "Summer start date must be before or equal to end date."));
            }

            if (schYrStart.isBefore(cycleStart) || schYrEnd.isAfter(cycleEnd)) {
                apiValidationErrors.add(ValidationUtil.createFieldError(SCHOOL_YEAR_START, reportingPeriod.getSchYrStart(), "School Year must be within the reporting cycle (Oct 1 to Sep 30)."));
            }
            if (summerStart.isBefore(cycleStart) || summerEnd.isAfter(cycleEnd)) {
                apiValidationErrors.add(ValidationUtil.createFieldError(SUMMER_START, reportingPeriod.getSummerStart(), "Summer must be within the reporting cycle (Oct 1 to Sep 30)."));
            }

            if (dateTimeRangesOverlap(schYrStart, schYrEnd, summerStart, summerEnd)) {
                apiValidationErrors.add(ValidationUtil.createFieldError(SCHOOL_YEAR_START, reportingPeriod.getSchYrStart(), "School Year and Summer periods must not overlap."));
                apiValidationErrors.add(ValidationUtil.createFieldError(SUMMER_START, reportingPeriod.getSummerStart(), "School Year and Summer periods must not overlap."));
            }

            if (schYrStart.isAfter(summerStart)) {
                apiValidationErrors.add(ValidationUtil.createFieldError(SCHOOL_YEAR_START, reportingPeriod.getSchYrStart(), "School Year must start before Summer."));
            }
            if (summerStart.isBefore(schYrEnd)) {
                apiValidationErrors.add(ValidationUtil.createFieldError(SUMMER_START, reportingPeriod.getSummerStart(), "Summer must start after School Year ends."));
            }

        } catch (DateTimeParseException e) {
            apiValidationErrors.add(ValidationUtil.createFieldError("date", "Invalid", "Invalid date format. Expected ISO format (e.g. 2025-07-01T00:00:00)."));
        }

        return apiValidationErrors;
    }

    private boolean dateTimeRangesOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }
}
