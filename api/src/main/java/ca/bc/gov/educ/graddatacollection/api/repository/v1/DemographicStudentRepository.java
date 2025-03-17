package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DemographicStudentRepository extends JpaRepository<DemographicStudentEntity, UUID>, JpaSpecificationExecutor<DemographicStudentEntity> {
    List<DemographicStudentEntity> findAllByIncomingFileset_IncomingFilesetID(UUID incomingFilesetID);
    long countByIncomingFileset_SchoolIDAndStudentStatusCode(UUID schoolID, String studentStatusCode);
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
            "WHERE i.DISTRICT_ID = :districtID " +
            "  AND i.FILESET_STATUS_CODE = :filesetStatusCode " +
            "  AND d.PEN = :pen " +
            "  AND d.STUDENT_STATUS_CODE <> 'LOADED' " +
            "ORDER BY i.CREATE_DATE DESC " +
            "LIMIT 1", nativeQuery = true)
    Optional<DemographicStudentEntity> findFirstByDistrictIDAndPen(UUID districtID, String filesetStatusCode, String pen);

    @Query(value = "SELECT d.* " +
            "FROM DEMOGRAPHIC_STUDENT d " +
            "JOIN INCOMING_FILESET i ON d.INCOMING_FILESET_ID = i.INCOMING_FILESET_ID " +
            "WHERE i.INCOMING_FILESET_ID = :incomingFilesetID " +
            "  AND i.SCHOOL_ID = :schoolID " +
            "  AND i.FILESET_STATUS_CODE = :filesetStatusCode " +
            "  AND d.PEN = :pen " +
            "  AND d.STUDENT_STATUS_CODE <> 'LOADED'", nativeQuery = true)
    Optional<DemographicStudentEntity> findByIncomingFilesetIDAndSchoolID(UUID incomingFilesetID, String pen, UUID schoolID, String filesetStatusCode);

    @Query(value = "SELECT d.* " +
            "FROM DEMOGRAPHIC_STUDENT d " +
            "JOIN INCOMING_FILESET i ON d.INCOMING_FILESET_ID = i.INCOMING_FILESET_ID " +
            "WHERE i.INCOMING_FILESET_ID = :incomingFilesetID " +
            "  AND i.DISTRICT_ID = :districtID " +
            "  AND i.FILESET_STATUS_CODE = :filesetStatusCode " +
            "  AND d.PEN = :pen " +
            "  AND d.STUDENT_STATUS_CODE <> 'LOADED'", nativeQuery = true)
    Optional<DemographicStudentEntity> findByIncomingFilesetIDAndDistrictID(UUID incomingFilesetID, String pen, UUID districtID, String filesetStatusCode);
}
