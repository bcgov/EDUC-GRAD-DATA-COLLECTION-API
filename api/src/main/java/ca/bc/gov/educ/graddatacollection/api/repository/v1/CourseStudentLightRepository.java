package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentLightEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ICourseStudentUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseStudentLightRepository extends JpaRepository<CourseStudentLightEntity, UUID>, JpaSpecificationExecutor<CourseStudentLightEntity> {
    @Query(value="""
    SELECT cse FROM CourseStudentLightEntity cse WHERE cse.courseStudentID
    NOT IN (SELECT saga.courseStudentID FROM GradSagaEntity saga WHERE saga.status != 'COMPLETED'
    AND saga.courseStudentID IS NOT NULL)
    AND cse.incomingFileset.demFileName is not null
    AND cse.incomingFileset.crsFileName is not null
    AND cse.incomingFileset.xamFileName is not null
    AND cse.studentStatusCode = 'LOADED'
    and (select count(ds2) from DemographicStudentEntity ds2 where ds2.studentStatusCode = 'LOADED' and ds2.incomingFileset.incomingFilesetID = cse.incomingFileset.incomingFilesetID) = 0
    order by cse.createDate
    LIMIT :numberOfStudentsToProcess""")
    List<CourseStudentLightEntity> findTopLoadedCRSStudentForProcessing(String numberOfStudentsToProcess);

    @Query(value="""
    SELECT cse.incoming_fileset_id as incomingFilesetID, cse.pen as pen
    FROM course_student cse WHERE cse.incoming_fileset_id
    NOT IN (SELECT saga.incoming_fileset_id FROM grad_saga saga WHERE saga.status != 'COMPLETED'
    AND saga.saga_name = 'PROCESS_COURSE_STUDENTS_FOR_DOWNSTREAM_UPDATE_SAGA'
    AND saga.incoming_fileset_id IS NOT NULL)
    AND cse.student_status_code = 'UPDATE_CRS'
    GROUP BY cse.incoming_fileset_id, cse.pen
    LIMIT :numberOfStudentsToProcess""", nativeQuery = true)
    List<ICourseStudentUpdate> findTopLoadedCRSStudentForDownstreamUpdate(String numberOfStudentsToProcess);

}
