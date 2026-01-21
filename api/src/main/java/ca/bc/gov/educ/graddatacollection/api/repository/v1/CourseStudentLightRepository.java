package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentLightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseStudentLightRepository extends JpaRepository<CourseStudentLightEntity, UUID>, JpaSpecificationExecutor<CourseStudentLightEntity> {
    @Query(value="""
    SELECT cse.*
    FROM course_student cse
    WHERE cse.incoming_fileset_id = :incomingFilesetID
    AND cse.student_status_code = 'LOADED'
    AND NOT EXISTS (
        SELECT 1 FROM grad_saga saga
        WHERE saga.course_student_id = cse.course_student_id
        AND saga.status != 'COMPLETED'
    )
    AND NOT EXISTS (
        SELECT 1 FROM demographic_student ds
        WHERE ds.incoming_fileset_id = cse.incoming_fileset_id
        AND ds.student_status_code = 'LOADED'
    )
    LIMIT :numberOfStudentsToProcess""", nativeQuery = true)
    List<CourseStudentLightEntity> findTopLoadedCRSStudentForProcessing(UUID incomingFilesetID, int numberOfStudentsToProcess);

    @Query(value="""
    SELECT cse.pen
    FROM course_student cse
    WHERE NOT EXISTS (
        SELECT 1
        FROM grad_saga saga
        WHERE saga.status != 'COMPLETED'
        AND saga.saga_name = 'PROCESS_COURSE_STUDENTS_FOR_DOWNSTREAM_UPDATE_SAGA'
        AND saga.incoming_fileset_id = :incomingFilesetID
    )
    AND NOT EXISTS (
        SELECT 1
        FROM course_student cs2
        WHERE cs2.student_status_code = 'LOADED'
        AND cs2.incoming_fileset_id = :incomingFilesetID
    )
    AND cse.student_status_code = 'UPDATE_CRS'
    GROUP BY cse.pen
    LIMIT :numberOfStudentsToProcess""", nativeQuery = true)
    List<String> findTopLoadedCRSStudentForDownstreamUpdate(UUID incomingFilesetID, int numberOfStudentsToProcess);

}
