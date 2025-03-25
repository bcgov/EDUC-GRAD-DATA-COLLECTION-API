package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DemographicStudentRepository extends JpaRepository<DemographicStudentEntity, UUID>, JpaSpecificationExecutor<DemographicStudentEntity> {
    List<DemographicStudentEntity> findAllByIncomingFileset_IncomingFilesetID(UUID incomingFilesetID);
    List<DemographicStudentEntity> findAllByIncomingFileset_IncomingFilesetIDAndLastNameEqualsIgnoreCaseAndPenEqualsIgnoreCaseAndLocalIDEqualsIgnoreCase(UUID incomingFilesetID, String lastName, String pen, String localID);

    @Query(value = "SELECT d.* " +
            "FROM DEMOGRAPHIC_STUDENT d " +
            "JOIN INCOMING_FILESET i ON d.INCOMING_FILESET_ID = i.INCOMING_FILESET_ID " +
            "WHERE i.SCHOOL_ID = :schoolID " +
            "  AND i.FILESET_STATUS_CODE = :filesetStatusCode " +
            "  AND d.PEN = :pen " +
            "  AND d.STUDENT_STATUS_CODE <> 'LOADED' " +
            "ORDER BY i.CREATE_DATE DESC " +
            "LIMIT 1", nativeQuery = true)
    Optional<DemographicStudentEntity> findFirstBySchoolIDAndPen(UUID schoolID, String filesetStatusCode, String pen);

    @Query(value = "SELECT d.* " +
            "FROM DEMOGRAPHIC_STUDENT d " +
            "JOIN INCOMING_FILESET i ON d.INCOMING_FILESET_ID = i.INCOMING_FILESET_ID " +
            "WHERE i.INCOMING_FILESET_ID = :incomingFilesetID " +
            "  AND i.SCHOOL_ID = :schoolID " +
            "  AND i.FILESET_STATUS_CODE = :filesetStatusCode " +
            "  AND d.PEN = :pen " +
            "  AND d.STUDENT_STATUS_CODE <> 'LOADED'", nativeQuery = true)
    Optional<DemographicStudentEntity> findByIncomingFilesetIDAndSchoolID(UUID incomingFilesetID, String pen, UUID schoolID, String filesetStatusCode);

    @Query("SELECT " +
            "   v.validationIssueSeverityCode, COUNT(v) " +
            "FROM DemographicStudentEntity d " +
            "JOIN d.demographicStudentValidationIssueEntities v " +
            "WHERE d.incomingFileset.incomingFilesetID = :incomingFilesetId " +
            "GROUP BY v.validationIssueSeverityCode")
    List<Object[]> countValidationIssuesBySeverity(@Param("incomingFilesetId") UUID incomingFilesetId);
}
