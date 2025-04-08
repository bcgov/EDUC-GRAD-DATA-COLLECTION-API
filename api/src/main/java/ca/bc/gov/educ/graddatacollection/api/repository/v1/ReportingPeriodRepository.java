package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.ReportingPeriodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReportingPeriodRepository extends JpaRepository<ReportingPeriodEntity, UUID>, JpaSpecificationExecutor<ReportingPeriodEntity> {

    @Query("SELECT rp FROM ReportingPeriodEntity rp WHERE CURRENT_TIMESTAMP BETWEEN rp.schYrStart AND rp.summerEnd")
    Optional<ReportingPeriodEntity> findActiveReportingPeriod();

    @Query("""
           SELECT prev_rp
           FROM ReportingPeriodEntity prev_rp
           WHERE prev_rp.schYrStart = (
               SELECT MAX(inner_prev_rp.schYrStart)
               FROM ReportingPeriodEntity inner_prev_rp
               WHERE inner_prev_rp.schYrStart < (
                   SELECT active_rp.schYrStart
                   FROM ReportingPeriodEntity active_rp
                   WHERE CURRENT_TIMESTAMP BETWEEN active_rp.schYrStart AND active_rp.summerEnd
               )
           )
           """)
    Optional<ReportingPeriodEntity> findPreviousReportingPeriod();

    @Query("""
            SELECT COUNT(rp) = 0
            FROM ReportingPeriodEntity rp
            WHERE YEAR(rp.schYrStart) = :schoolYearStart
        """)
    boolean upcomingReportingPeriodDoesNotExist(@Param("schoolYearStart") int schoolYearStart);

}
