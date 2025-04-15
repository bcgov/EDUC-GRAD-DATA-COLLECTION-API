package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentLightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

}
