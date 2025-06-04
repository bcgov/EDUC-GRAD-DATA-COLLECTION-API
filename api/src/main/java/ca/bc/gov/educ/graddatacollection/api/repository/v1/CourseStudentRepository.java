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
    long countByIncomingFileset_IncomingFilesetIDAndPenEqualsAndCourseCodeEqualsAndCourseMonthEqualsAndCourseYearEqualsAndCourseLevelEquals(UUID incomingFilesetID, String pen, String courseCode, String courseMonth, String courseYear, String courseLevel);

    List<CourseStudentEntity> findAllByIncomingFileset_IncomingFilesetIDAndPenAndIncomingFileset_SchoolIDAndIncomingFileset_FilesetStatusCodeAndStudentStatusCodeNot(UUID incomingFilesetID, String pen, UUID schoolID, String filesetStatusCode, String studentStatusCode);
    List<CourseStudentEntity> findAllByIncomingFileset_IncomingFilesetIDAndPenAndStudentStatusCode(UUID incomingFilesetID, String pen, String studentStatusCode);

    @Query("SELECT " +
            "   v.validationIssueSeverityCode, COUNT(v) " +
            "FROM CourseStudentEntity d " +
            "JOIN d.courseStudentValidationIssueEntities v " +
            "WHERE d.incomingFileset.incomingFilesetID = :incomingFilesetId " +
            "GROUP BY v.validationIssueSeverityCode")
    List<Object[]> countValidationIssuesBySeverity(@Param("incomingFilesetId") UUID incomingFilesetId);

}
