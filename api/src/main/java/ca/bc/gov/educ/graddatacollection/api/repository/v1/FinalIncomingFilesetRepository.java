package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.FinalIncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SchoolSubmissionCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinalIncomingFilesetRepository extends JpaRepository<FinalIncomingFilesetEntity, UUID>, JpaSpecificationExecutor<FinalIncomingFilesetEntity> {

    @Query(value = """
    SELECT COUNT(*)
    FROM FinalIncomingFilesetEntity inFileset
    WHERE inFileset.filesetStatusCode != 'COMPLETED'
    AND inFileset.updateDate <= :updateDate
    AND inFileset.demFileName is not null
    AND inFileset.crsFileName is not null
    AND inFileset.xamFileName is not null
    AND ((select count(ds2) from FinalDemographicStudentEntity ds2 where ds2.studentStatusCode = 'LOADED' and ds2.incomingFileset.incomingFilesetID = inFileset.incomingFilesetID) > 0
    OR (select count(cs2) from FinalCourseStudentEntity cs2 where cs2.studentStatusCode = 'LOADED' and cs2.incomingFileset.incomingFilesetID = inFileset.incomingFilesetID) > 0
    OR (select count(as2) from FinalAssessmentStudentEntity as2 where as2.studentStatusCode = 'LOADED' and as2.incomingFileset.incomingFilesetID = inFileset.incomingFilesetID) > 0)
    """)
    long findPositionInQueueByUpdateDate(LocalDateTime updateDate);

    Optional<FinalIncomingFilesetEntity> findByIncomingFilesetID(UUID incomingFilesetID);

    @Query(value = """
    SELECT inFileset.school_id as schoolID,
    COUNT(inFileset.incoming_fileset_id) as submissionCount
    FROM final_incoming_fileset inFileset
    WHERE inFileset.fileset_status_code = 'COMPLETED'
    AND inFileset.create_date >= GREATEST(:reportingStartDate, (CURRENT_TIMESTAMP - INTERVAL '30' day))
    AND inFileset.reporting_period_id = :reportingPeriodID
    GROUP BY inFileset.school_id
    """, nativeQuery = true)
    List<SchoolSubmissionCount> findSchoolSubmissionsInLast30Days(UUID reportingPeriodID, LocalDateTime reportingStartDate);

    @Query(value = """
    SELECT inFileset.schoolID as schoolID,
    COUNT(inFileset.incomingFilesetID) as submissionCount,
    MAX(inFileset.createDate) as lastSubmissionDate
    FROM FinalIncomingFilesetEntity inFileset
    WHERE inFileset.filesetStatusCode = 'COMPLETED'
    AND inFileset.createDate >= :summerStartDate
    AND inFileset.createDate <= :summerEndDate
    AND inFileset.reportingPeriod.reportingPeriodID = :reportingPeriodID
    GROUP BY inFileset.schoolID
    """)
    List<SchoolSubmissionCount> findSchoolSubmissionsInSummerReportingPeriod(UUID reportingPeriodID, LocalDateTime summerStartDate, LocalDateTime summerEndDate);

    @Query(value = """
    SELECT inFileset.schoolID as schoolID,
    COUNT(inFileset.incomingFilesetID) as submissionCount,
    MAX(inFileset.createDate) as lastSubmissionDate
    FROM FinalIncomingFilesetEntity inFileset
    WHERE inFileset.filesetStatusCode = 'COMPLETED'
    AND inFileset.createDate >= :schoolStartDate
    AND inFileset.createDate <= :schoolEndDate
    AND inFileset.reportingPeriod.reportingPeriodID = :reportingPeriodID
    GROUP BY inFileset.schoolID
    """)
    List<SchoolSubmissionCount> findSchoolSubmissionsInSchoolReportingPeriod(UUID reportingPeriodID, LocalDateTime schoolStartDate, LocalDateTime schoolEndDate);
}
