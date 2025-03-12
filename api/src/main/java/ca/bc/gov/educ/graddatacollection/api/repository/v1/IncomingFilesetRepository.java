package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncomingFilesetRepository extends JpaRepository<IncomingFilesetEntity, UUID>, JpaSpecificationExecutor<IncomingFilesetEntity> {
    Optional<IncomingFilesetEntity> findBySchoolIDAndFilesetStatusCode(UUID schoolID, String statusCode);

    Optional<IncomingFilesetEntity> findBySchoolIDAndFilesetStatusCodeAndDemFileNameIsNotNullAndXamFileNameIsNotNullAndCrsFileNameIsNotNull(UUID schoolID, String statusCode);

    Optional<IncomingFilesetEntity> findFirstByFilesetStatusCodeAndDemographicStudentEntities_PenOrFilesetStatusCodeAndCourseStudentEntities_PenOrFilesetStatusCodeAndAssessmentStudentEntities_PenOrderByCreateDateDesc(
            String status1, String pen1, String status2, String pen2, String status3, String pen3);

    @Query(value = """
    SELECT DISTINCT i
    FROM IncomingFilesetEntity i
    LEFT JOIN FETCH i.demographicStudentEntities d
    LEFT JOIN FETCH i.courseStudentEntities c
    LEFT JOIN FETCH i.assessmentStudentEntities a
    WHERE i.incomingFilesetID = :incomingFilesetId
      AND i.filesetStatusCode = 'COMPLETED'
      AND (d.pen = :pen OR c.pen = :pen OR a.pen = :pen)
    """)
    Optional<IncomingFilesetEntity> findByIncomingFilesetIDAndPen(UUID incomingFilesetId, String pen);

    @Query(value="""
    SELECT inFileset
    FROM IncomingFilesetEntity inFileset
    WHERE inFileset.filesetStatusCode != 'COMPLETED'
    AND inFileset.demFileName is not null
    AND inFileset.crsFileName is not null
    AND inFileset.xamFileName is not null
    AND (select count(ds2) from DemographicStudentEntity ds2 where ds2.studentStatusCode = 'LOADED' and ds2.incomingFileset.incomingFilesetID = inFileset.incomingFilesetID) = 0
    AND (select count(cs2) from CourseStudentEntity cs2 where cs2.studentStatusCode = 'LOADED' and cs2.incomingFileset.incomingFilesetID = inFileset.incomingFilesetID) = 0
    AND (select count(as2) from AssessmentStudentEntity as2 where as2.studentStatusCode = 'LOADED' and as2.incomingFileset.incomingFilesetID = inFileset.incomingFilesetID) = 0
    """)
    List<IncomingFilesetEntity> findCompletedCollectionsForStatusUpdate();

    @Query(value="""
    SELECT dse FROM DemographicStudentEntity dse WHERE dse.demographicStudentID
    NOT IN (SELECT saga.demographicStudentID FROM GradSagaEntity saga WHERE saga.status != 'COMPLETED'
    AND saga.demographicStudentID IS NOT NULL)
    AND dse.incomingFileset.demFileName is not null
    AND dse.incomingFileset.crsFileName is not null
    AND dse.incomingFileset.xamFileName is not null
    AND dse.studentStatusCode = 'LOADED'
    order by dse.createDate
    LIMIT :numberOfStudentsToProcess""")
    List<DemographicStudentEntity> findTopLoadedDEMStudentForProcessing(String numberOfStudentsToProcess);

    @Query(value="""
    SELECT cse FROM CourseStudentEntity cse WHERE cse.courseStudentID
    NOT IN (SELECT saga.courseStudentID FROM GradSagaEntity saga WHERE saga.status != 'COMPLETED'
    AND saga.courseStudentID IS NOT NULL)
    AND cse.incomingFileset.demFileName is not null
    AND cse.incomingFileset.crsFileName is not null
    AND cse.incomingFileset.xamFileName is not null
    AND cse.studentStatusCode = 'LOADED'
    order by cse.createDate
    LIMIT :numberOfStudentsToProcess""")
    List<CourseStudentEntity> findTopLoadedCRSStudentForProcessing(String numberOfStudentsToProcess);

    @Query(value="""
    SELECT ase FROM AssessmentStudentEntity ase WHERE ase.assessmentStudentID
    NOT IN (SELECT saga.assessmentStudentID FROM GradSagaEntity saga WHERE saga.status != 'COMPLETED'
    AND saga.assessmentStudentID IS NOT NULL)
    AND ase.incomingFileset.demFileName is not null
    AND ase.incomingFileset.crsFileName is not null
    AND ase.incomingFileset.xamFileName is not null
    AND ase.studentStatusCode = 'LOADED'
    order by ase.createDate
    LIMIT :numberOfStudentsToProcess""")
    List<AssessmentStudentEntity> findTopLoadedAssessmentStudentForProcessing(String numberOfStudentsToProcess);

    @Transactional
    @Modifying
    @Query("DELETE FROM IncomingFilesetEntity WHERE updateDate <= :oldestIncomingFilesetTimestamp AND (demFileName is null OR crsFileName is null OR xamFileName is null)")
    void deleteStaleWithUpdateDateBefore(LocalDateTime oldestIncomingFilesetTimestamp);
}
