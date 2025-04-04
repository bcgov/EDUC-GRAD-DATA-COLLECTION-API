package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ReportingPeriodEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ReportingPeriodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportingPeriodService {
    private final ReportingPeriodRepository reportingPeriodRepository;

    public ReportingPeriodEntity getActiveReportingPeriod() {
        Optional<ReportingPeriodEntity> reportingPeriodEntity = reportingPeriodRepository.findActiveReportingPeriod();
        if (reportingPeriodEntity.isPresent()) {
            return reportingPeriodEntity.get();
        } else {
            throw new EntityNotFoundException(ReportingPeriodEntity.class, "currentDate", String.valueOf(LocalDateTime.now()));
        }
    }

    public  ReportingPeriodEntity updateReportingPeriod(final ReportingPeriodEntity reportingPeriodEntity) {
        final Optional<ReportingPeriodEntity> curOptionalReportingPeriodEntity = reportingPeriodRepository.findById(reportingPeriodEntity.getReportingPeriodID());
        if (curOptionalReportingPeriodEntity.isPresent()) {
            ReportingPeriodEntity curReportingPeriodEntity = curOptionalReportingPeriodEntity.get();
            BeanUtils.copyProperties(reportingPeriodEntity, curReportingPeriodEntity, "reportingPeriodID", "createUser", "createDate", "incomingFilesets", "reportingPeriodID");
            return reportingPeriodRepository.save(curReportingPeriodEntity);
        } else {
            throw new EntityNotFoundException(ReportingPeriodEntity.class, "ReportingPeriodEntity", reportingPeriodEntity.getReportingPeriodID().toString());
        }
    }
}
