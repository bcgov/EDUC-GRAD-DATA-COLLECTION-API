package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ReportingPeriodEntity;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetPurgeRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ReportingPeriodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportingPeriodService {
    private final ReportingPeriodRepository reportingPeriodRepository;
    private final IncomingFilesetPurgeRepository incomingFilesetPurgeRepository;

    public ReportingPeriodEntity getActiveReportingPeriod() {
        Optional<ReportingPeriodEntity> reportingPeriodEntity = reportingPeriodRepository.findActiveReportingPeriod();
        if (reportingPeriodEntity.isPresent()) {
            return reportingPeriodEntity.get();
        } else {
            throw new EntityNotFoundException(ReportingPeriodEntity.class, "currentDate", String.valueOf(LocalDateTime.now()));
        }
    }

    public ReportingPeriodEntity getPreviousReportingPeriod() {
        Optional<ReportingPeriodEntity> reportingPeriodEntity = reportingPeriodRepository.findPreviousReportingPeriod();
        if (reportingPeriodEntity.isPresent()) {
            return reportingPeriodEntity.get();
        } else  {
            throw new EntityNotFoundException(ReportingPeriodEntity.class, "currentDate", String.valueOf(LocalDateTime.now()));
        }
    }

    public ReportingPeriodEntity getReportingPeriod(UUID reportingPeriodID) {
        Optional<ReportingPeriodEntity> reportingPeriodEntity = reportingPeriodRepository.findById(reportingPeriodID);
        return reportingPeriodEntity.orElseThrow(() -> new EntityNotFoundException(ReportingPeriodEntity.class, "reportingPeriodID", reportingPeriodID.toString()));
    }

    public  ReportingPeriodEntity updateReportingPeriod(final ReportingPeriodEntity reportingPeriodEntity) {
        final Optional<ReportingPeriodEntity> curOptionalReportingPeriodEntity = reportingPeriodRepository.findById(reportingPeriodEntity.getReportingPeriodID());
        if (curOptionalReportingPeriodEntity.isPresent()) {
            ReportingPeriodEntity curReportingPeriodEntity = curOptionalReportingPeriodEntity.get();
            BeanUtils.copyProperties(reportingPeriodEntity, curReportingPeriodEntity, "reportingPeriodID", "createUser", "createDate", "incomingFilesets", "reportingPeriodID", "periodStart", "periodEnd");
            return reportingPeriodRepository.save(curReportingPeriodEntity);
        } else {
            throw new EntityNotFoundException(ReportingPeriodEntity.class, "ReportingPeriodEntity", reportingPeriodEntity.getReportingPeriodID().toString());
        }
    }

    public void createReportingPeriodForYear(){
        int currentYear = Year.now().getValue();

        ReportingPeriodEntity newReportingPeriod = ReportingPeriodEntity.builder()
                .schYrStart(getSchoolYearStart(currentYear))
                .schYrEnd(getSchoolYearEnd(currentYear))
                .summerStart(getSummerStart(currentYear))
                .summerEnd(getSummerEnd(currentYear))
                .periodStart(getPeriodStart(currentYear))
                .periodEnd(getPeriodEnd(currentYear))
                .createUser(ApplicationProperties.GRAD_DATA_COLLECTION_API)
                .createDate(LocalDateTime.now())
                .updateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API)
                .updateDate(LocalDateTime.now())
                .build();

        reportingPeriodRepository.save(newReportingPeriod);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void purgeReportingPeriodFor5YearsAgo(){
        int octoberMonth = Month.OCTOBER.getValue();
        LocalDateTime october1stFiveYearsAgo = LocalDateTime.now().withDayOfMonth(1).withMonth(octoberMonth).minusYears(5);
        incomingFilesetPurgeRepository.deleteWithCreateDateBefore(october1stFiveYearsAgo);
    }

    private LocalDateTime getSchoolYearStart(int startYear) {
        LocalDate dateInOctober = LocalDate.of(startYear, Month.OCTOBER, 1);
        LocalDate firstOctoberMondayDate = dateInOctober.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
        return firstOctoberMondayDate.atStartOfDay();
    }

    private LocalDateTime getSchoolYearEnd(int startYear) {
        LocalDate dateInJuly = LocalDate.of(startYear + 1, Month.JULY, 1);
        LocalDate thirdJulyFridayDate = dateInJuly.with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.FRIDAY));
        return thirdJulyFridayDate.atTime(23,59,59, 0);
    }

    private LocalDateTime getSummerStart(int startYear) {
        LocalDate dateInAugust = LocalDate.of(startYear + 1, Month.AUGUST, 1);
        LocalDate firstAugustMondayDate = dateInAugust.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
        return firstAugustMondayDate.atStartOfDay();
    }

    private LocalDateTime getSummerEnd(int startYear) {
        LocalDate dateInSeptember = LocalDate.of(startYear + 1, Month.SEPTEMBER, 1);
        LocalDate thirdSeptemberFridayDate = dateInSeptember.with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.FRIDAY));
        return thirdSeptemberFridayDate.atTime(23,59,59, 0);
    }

    private LocalDateTime getPeriodStart(int startYear) {
        LocalDate dateInOctober = LocalDate.of(startYear, Month.OCTOBER, 1);
        return dateInOctober.atStartOfDay();
    }

    private LocalDateTime getPeriodEnd(int startYear) {
        LocalDate dateInSeptember = LocalDate.of(startYear + 1, Month.SEPTEMBER, 30);
        return dateInSeptember.atTime(23, 59, 59, 0);
    }
}
