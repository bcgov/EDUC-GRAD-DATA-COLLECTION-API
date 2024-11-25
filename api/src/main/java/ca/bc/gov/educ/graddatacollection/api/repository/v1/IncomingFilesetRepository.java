package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncomingFilesetRepository extends JpaRepository<IncomingFilesetEntity, UUID>, JpaSpecificationExecutor<IncomingFilesetEntity> {
    Optional<IncomingFilesetEntity> findBySchoolIDAndFilesetStatusCode(UUID schoolID, String statusCode);
    Optional<IncomingFilesetEntity> findBySchoolID(UUID schoolID);

    @Query(value="""
    SELECT inFileset
    FROM DemographicStudentEntity dse, IncomingFilesetEntity inFileset, CourseStudentEntity cs, AssessmentStudentEntity assessment
    WHERE dse.incomingFileset.incomingFilesetID = inFileset.incomingFilesetID
    AND cs.incomingFileset.incomingFilesetID = inFileset.incomingFilesetID
    AND assessment.incomingFileset.incomingFilesetID = inFileset.incomingFilesetID
    AND (select count(ds2) from DemographicStudentEntity ds2 where ds2.studentStatusCode = 'LOADED' and ds2.incomingFileset.incomingFilesetID = inFileset.incomingFilesetID) = 0
    AND (select count(cs2) from CourseStudentEntity cs2 where cs2.studentStatusCode = 'LOADED' and cs2.incomingFileset.incomingFilesetID = inFileset.incomingFilesetID) = 0
    AND (select count(as2) from AssessmentStudentEntity as2 where as2.studentStatusCode = 'LOADED' and as2.incomingFileset.incomingFilesetID = inFileset.incomingFilesetID) = 0
    AND inFileset.incomingFilesetID
    IN (SELECT incoming.incomingFilesetID FROM IncomingFilesetEntity incoming 
    WHERE incoming.filesetStatusCode != 'COMPLETED'
    AND incoming.demFileStatusCode = 'LOADED'
    AND incoming.crsFileStatusCode = 'LOADED'
    AND incoming.xamFileStatusCode = 'LOADED')""")
    List<IncomingFilesetEntity> findCompletedCollectionsForStatusUpdate();

    @Query(value="""
    SELECT dse FROM DemographicStudentEntity dse WHERE dse.demographicStudentID
    NOT IN (SELECT saga.demographicStudentID FROM GradSagaEntity saga WHERE saga.status != 'COMPLETED'
    AND saga.demographicStudentID IS NOT NULL)
    AND dse.incomingFileset.crsFileStatusCode = 'LOADED'
    AND dse.incomingFileset.demFileStatusCode = 'LOADED'
    AND dse.incomingFileset.xamFileStatusCode = 'LOADED'
    AND dse.studentStatusCode = 'LOADED'
    order by dse.createDate
    LIMIT :numberOfStudentsToProcess""")
    List<DemographicStudentEntity> findTopLoadedDEMStudentForProcessing(String numberOfStudentsToProcess);

    @Query(value="""
    SELECT cse FROM CourseStudentEntity cse WHERE cse.courseStudentID
    NOT IN (SELECT saga.courseStudentID FROM GradSagaEntity saga WHERE saga.status != 'COMPLETED'
    AND saga.courseStudentID IS NOT NULL)
    AND cse.incomingFileset.crsFileStatusCode = 'LOADED'
    AND cse.incomingFileset.demFileStatusCode = 'LOADED'
    AND cse.incomingFileset.xamFileStatusCode = 'LOADED'
    AND cse.studentStatusCode = 'LOADED'
    order by cse.createDate
    LIMIT :numberOfStudentsToProcess""")
    List<CourseStudentEntity> findTopLoadedCRSStudentForProcessing(String numberOfStudentsToProcess);

    @Query(value="""
    SELECT ase FROM AssessmentStudentEntity ase WHERE ase.assessmentStudentID
    NOT IN (SELECT saga.assessmentStudentID FROM GradSagaEntity saga WHERE saga.status != 'COMPLETED'
    AND saga.assessmentStudentID IS NOT NULL)
    AND ase.incomingFileset.crsFileStatusCode = 'LOADED'
    AND ase.incomingFileset.demFileStatusCode = 'LOADED'
    AND ase.incomingFileset.xamFileStatusCode = 'LOADED'
    AND ase.studentStatusCode = 'LOADED'
    order by ase.createDate
    LIMIT :numberOfStudentsToProcess""")
    List<AssessmentStudentEntity> findTopLoadedAssessmentStudentForProcessing(String numberOfStudentsToProcess);
}
