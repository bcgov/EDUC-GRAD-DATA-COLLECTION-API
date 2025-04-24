package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.ReportingPeriodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReportingPeriodRepository extends JpaRepository<ReportingPeriodEntity, UUID>, JpaSpecificationExecutor<ReportingPeriodEntity> {

    @Query("SELECT rp FROM ReportingPeriodEntity rp WHERE CURRENT_TIMESTAMP BETWEEN rp.periodStart AND rp.periodEnd")
    Optional<ReportingPeriodEntity> findActiveReportingPeriod();

    @Query("""
           SELECT prev_rp
           FROM ReportingPeriodEntity prev_rp
           WHERE prev_rp.periodStart = (
               SELECT MAX(inner_prev_rp.periodStart)
               FROM ReportingPeriodEntity inner_prev_rp
               WHERE inner_prev_rp.periodStart < (
                   SELECT active_rp.periodStart
                   FROM ReportingPeriodEntity active_rp
                   WHERE CURRENT_TIMESTAMP BETWEEN active_rp.periodStart AND active_rp.periodEnd
               )
           )
           """)
    Optional<ReportingPeriodEntity> findPreviousReportingPeriod();

    @Query("""
            SELECT COUNT(rp) = 0
            FROM ReportingPeriodEntity rp
            WHERE EXTRACT(YEAR FROM rp.periodStart) = :schoolYearStart
        """)
    boolean upcomingReportingPeriodDoesNotExist(@Param("schoolYearStart") int schoolYearStart);

}
