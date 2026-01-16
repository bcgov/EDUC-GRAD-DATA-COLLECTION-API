package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.FinalIncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
}
