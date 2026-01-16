package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.FinalCourseStudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FinalCourseStudentRepository extends JpaRepository<FinalCourseStudentEntity, UUID>, JpaSpecificationExecutor<FinalCourseStudentEntity> {

    List<FinalCourseStudentEntity> findAllByIncomingFileset_IncomingFilesetID(UUID incomingFilesetID);
    List<FinalCourseStudentEntity> findAllByIncomingFileset_IncomingFilesetIDAndPenAndIncomingFileset_SchoolIDAndIncomingFileset_FilesetStatusCodeAndStudentStatusCodeNot(UUID incomingFilesetID, String pen, UUID schoolID, String filesetStatusCode, String studentStatusCode);

    @Query("SELECT " +
            "   v.validationIssueSeverityCode, COUNT(v) " +
            "FROM FinalCourseStudentEntity d " +
            "JOIN d.courseStudentValidationIssueEntities v " +
            "WHERE d.incomingFileset.incomingFilesetID = :incomingFilesetId " +
            "GROUP BY v.validationIssueSeverityCode")
    List<Object[]> countValidationIssuesBySeverity(@Param("incomingFilesetId") UUID incomingFilesetId);
}
