package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.ReportingPeriodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ReportingPeriodRepository extends JpaRepository<ReportingPeriodEntity, UUID>, JpaSpecificationExecutor<ReportingPeriodEntity> {

    @Query("SELECT rp FROM ReportingPeriodEntity rp WHERE CURRENT_TIMESTAMP BETWEEN rp.schYrStart AND rp.summerEnd")
    Optional<ReportingPeriodEntity> findActiveReportingPeriod();

}
