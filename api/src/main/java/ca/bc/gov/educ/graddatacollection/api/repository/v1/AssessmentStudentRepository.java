package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssessmentStudentRepository extends JpaRepository<AssessmentStudentEntity, UUID>, JpaSpecificationExecutor<AssessmentStudentEntity> {
    List<AssessmentStudentEntity> findAllByIncomingFileset_IncomingFilesetID(UUID incomingFilesetID);
    long countByPenEqualsAndCourseCodeEqualsAndCourseMonthEqualsAndCourseYearEquals(String pen, String courseCode, String courseMonth, String courseYear);

    @Query(value = "SELECT a.* " +
            "FROM ASSESSMENT_STUDENT a " +
            "JOIN INCOMING_FILESET i ON a.INCOMING_FILESET_ID = i.INCOMING_FILESET_ID " +
            "WHERE i.INCOMING_FILESET_ID = :incomingFilesetID " +
            "  AND i.SCHOOL_ID = :schoolID " +
            "  AND i.FILESET_STATUS_CODE = :filesetStatusCode " +
            "  AND a.PEN = :pen " +
            "  AND a.STUDENT_STATUS_CODE <> 'LOADED' " +
            "ORDER BY i.CREATE_DATE DESC", nativeQuery = true)
    List<AssessmentStudentEntity> findByIncomingFilesetIDAndSchoolID(UUID incomingFilesetID, String pen, UUID schoolID, String filesetStatusCode);

    @Query("SELECT " +
            "   v.validationIssueSeverityCode, COUNT(v) " +
            "FROM AssessmentStudentEntity d " +
            "JOIN d.assessmentStudentValidationIssueEntities v " +
            "WHERE d.incomingFileset.incomingFilesetID = :incomingFilesetId " +
            "GROUP BY v.validationIssueSeverityCode")
    List<Object[]> countValidationIssuesBySeverity(@Param("incomingFilesetId") UUID incomingFilesetId);

}
