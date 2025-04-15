package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentLightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssessmentStudentLightRepository extends JpaRepository<AssessmentStudentLightEntity, UUID>, JpaSpecificationExecutor<AssessmentStudentLightEntity> {
    @Query(value="""
    SELECT ase FROM AssessmentStudentLightEntity ase WHERE ase.assessmentStudentID
    NOT IN (SELECT saga.assessmentStudentID FROM GradSagaEntity saga WHERE saga.status != 'COMPLETED'
    AND saga.assessmentStudentID IS NOT NULL)
    AND ase.incomingFileset.demFileName is not null
    AND ase.incomingFileset.crsFileName is not null
    AND ase.incomingFileset.xamFileName is not null
    and (select count(ds2) from DemographicStudentEntity ds2 where ds2.studentStatusCode = 'LOADED' and ds2.incomingFileset.incomingFilesetID = ase.incomingFileset.incomingFilesetID) = 0
    AND ase.studentStatusCode = 'LOADED'
    order by ase.createDate
    LIMIT :numberOfStudentsToProcess""")
    List<AssessmentStudentLightEntity> findTopLoadedAssessmentStudentForProcessing(String numberOfStudentsToProcess);
}
