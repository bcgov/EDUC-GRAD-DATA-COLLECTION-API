package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseStudentRepository extends JpaRepository<CourseStudentEntity, UUID>, JpaSpecificationExecutor<CourseStudentEntity> {
    List<CourseStudentEntity> findAllByIncomingFileset_IncomingFilesetID(UUID incomingFilesetID);
    List<CourseStudentEntity> findAllByIncomingFileset_IncomingFilesetIDAndPenEqualsIgnoreCase(UUID incomingFilesetID, String pen);
    long countByPenEqualsAndCourseCodeEqualsAndCourseMonthEqualsAndCourseYearEqualsAndCourseLevelEquals(String pen, String courseCode, String courseMonth, String courseYear, String courseLevel);

    @Query(value = "SELECT c.* " +
            "FROM COURSE_STUDENT c " +
            "JOIN INCOMING_FILESET i ON c.INCOMING_FILESET_ID = i.INCOMING_FILESET_ID " +
            "WHERE i.INCOMING_FILESET_ID = :incomingFilesetID " +
            "  AND i.SCHOOL_ID = :schoolID " +
            "  AND i.FILESET_STATUS_CODE = :filesetStatusCode " +
            "  AND c.PEN = :pen " +
            "  AND c.STUDENT_STATUS_CODE <> 'LOADED' " +
            "ORDER BY i.CREATE_DATE DESC", nativeQuery = true)
    List<CourseStudentEntity> findByIncomingFilesetIDAndSchoolID(UUID incomingFilesetID, String pen, UUID schoolID, String filesetStatusCode);

    @Query("SELECT " +
            "   v.validationIssueSeverityCode, COUNT(v) " +
            "FROM CourseStudentEntity d " +
            "JOIN d.courseStudentValidationIssueEntities v " +
            "WHERE d.incomingFileset.incomingFilesetID = :incomingFilesetId " +
            "GROUP BY v.validationIssueSeverityCode")
    List<Object[]> countValidationIssuesBySeverity(@Param("incomingFilesetId") UUID incomingFilesetId);

}
