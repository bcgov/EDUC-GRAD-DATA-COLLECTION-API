package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentLightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DemographicStudentLightRepository extends JpaRepository<DemographicStudentLightEntity, UUID>, JpaSpecificationExecutor<DemographicStudentLightEntity> {
    @Query(value = """
    SELECT ds.*
    FROM demographic_student ds
    WHERE ds.incoming_fileset_id = :incomingFilesetID
    AND ds.student_status_code = 'LOADED'
    AND NOT EXISTS (
        SELECT 1 FROM grad_saga saga
        WHERE saga.demographic_student_id = ds.demographic_student_id
        AND saga.status != 'COMPLETED'
    )
    LIMIT :numberOfStudentsToProcess
    """, nativeQuery = true)
    List<DemographicStudentLightEntity> findTopLoadedDEMStudentForProcessing(UUID incomingFilesetID, int numberOfStudentsToProcess);
}
