package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetLightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncomingFilesetLightRepository extends JpaRepository<IncomingFilesetLightEntity, UUID>, JpaSpecificationExecutor<IncomingFilesetLightEntity> {
    @Query(value="""
    SELECT inFileset.*
    FROM incoming_fileset inFileset
    WHERE inFileset.fileset_status_code != 'COMPLETED'
    AND inFileset.dem_file_name IS NOT NULL
    AND inFileset.crs_file_name IS NOT NULL
    AND inFileset.xam_file_name IS NOT NULL
    AND NOT EXISTS (
        SELECT 1 FROM grad_saga saga
        WHERE saga.incoming_fileset_id = inFileset.incoming_fileset_id
        AND saga.status != 'COMPLETED'
    )
    ORDER BY inFileset.create_date ASC
    LIMIT 1
    """, nativeQuery=true)
    Optional<IncomingFilesetLightEntity> findNextReadyCollectionForProcessing();

    @Query(value = """
    SELECT inFileset.incoming_fileset_id
    FROM incoming_fileset inFileset
    WHERE inFileset.fileset_status_code != 'COMPLETED'
    AND inFileset.dem_file_name IS NOT NULL
    AND inFileset.crs_file_name IS NOT NULL
    AND inFileset.xam_file_name IS NOT NULL
    AND NOT EXISTS (
        SELECT 1 FROM grad_saga saga 
        WHERE saga.incoming_fileset_id = inFileset.incoming_fileset_id
        AND saga.status != 'COMPLETED'
        AND saga.saga_name = 'PROCESS_COMPLETED_FILESETS_SAGA'
    )
    AND NOT EXISTS (
        SELECT 1 FROM demographic_student ds 
        WHERE ds.incoming_fileset_id = inFileset.incoming_fileset_id
        AND ds.student_status_code = 'LOADED'
    )
    AND NOT EXISTS (
        SELECT 1 FROM course_student cs 
        WHERE cs.incoming_fileset_id = inFileset.incoming_fileset_id
        AND cs.student_status_code IN ('LOADED', 'UPDATE_CRS')
    )
    AND NOT EXISTS (
        SELECT 1 FROM assessment_student as_ 
        WHERE as_.incoming_fileset_id = inFileset.incoming_fileset_id
        AND as_.student_status_code = 'LOADED'
    )
    """, nativeQuery=true)    
    List<UUID> findCompletedCollectionsForStatusUpdate();

}
