package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseStudentRepository extends JpaRepository<CourseStudentEntity, UUID>, JpaSpecificationExecutor<CourseStudentEntity> {
    List<CourseStudentEntity> findAllByIncomingFileset_IncomingFilesetID(UUID incomingFilesetID);
    long countByIncomingFileset_SchoolIDAndStudentStatusCode(UUID schoolID, String studentStatusCode);

    List<CourseStudentEntity> findAllByIncomingFileset_IncomingFilesetIDAndPenEqualsIgnoreCase(UUID incomingFilesetID, String pen);

    long countByPenEqualsAndCourseCodeEqualsAndCourseMonthEqualsAndCourseYearEqualsAndCourseLevelEquals(String pen, String courseCode, String courseMonth, String courseYear, String courseLevel);
}
