package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentLightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DemographicStudentLightRepository extends JpaRepository<DemographicStudentLightEntity, UUID>, JpaSpecificationExecutor<DemographicStudentLightEntity> {
    @Query(value="""
    SELECT dse FROM DemographicStudentLightEntity dse WHERE dse.demographicStudentID
    NOT IN (SELECT saga.demographicStudentID FROM GradSagaEntity saga WHERE saga.status != 'COMPLETED'
    AND saga.demographicStudentID IS NOT NULL)
    AND dse.incomingFileset.demFileName is not null
    AND dse.incomingFileset.crsFileName is not null
    AND dse.incomingFileset.xamFileName is not null
    AND dse.studentStatusCode = 'LOADED'
    order by dse.createDate
    LIMIT :numberOfStudentsToProcess""")
    List<DemographicStudentLightEntity> findTopLoadedDEMStudentForProcessing(String numberOfStudentsToProcess);
}
