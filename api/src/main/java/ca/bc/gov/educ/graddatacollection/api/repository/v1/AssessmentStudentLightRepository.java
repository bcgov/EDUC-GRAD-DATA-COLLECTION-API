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
    SELECT ase.*
    FROM assessment_student ase
    WHERE ase.incoming_fileset_id = :incomingFilesetID
    AND ase.student_status_code = 'LOADED'
    AND NOT EXISTS (
        SELECT 1 FROM grad_saga saga
        WHERE saga.assessment_student_id = ase.assessment_student_id
        AND saga.status != 'COMPLETED'
    )
    AND NOT EXISTS (
        SELECT 1 FROM demographic_student ds
        WHERE ds.incoming_fileset_id = ase.incoming_fileset_id
        AND ds.student_status_code = 'LOADED'
    )
    LIMIT :numberOfStudentsToProcess""", nativeQuery = true)
    List<AssessmentStudentLightEntity> findTopLoadedAssessmentStudentForProcessing(UUID incomingFilesetID, int numberOfStudentsToProcess);
}
