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
    Optional<DemographicStudentEntity> findFirstByIncomingFileset_SchoolIDAndIncomingFileset_FilesetStatusCodeAndPenAndStudentStatusCodeNotOrderByCreateDateDesc(UUID schoolID, String filesetStatusCode, String pen, String studentStatusCode);
    Optional<DemographicStudentEntity> findByIncomingFileset_IncomingFilesetIDAndPenAndIncomingFileset_SchoolIDAndIncomingFileset_FilesetStatusCodeAndStudentStatusCodeNot(UUID incomingFilesetID, String pen, UUID schoolID, String filesetStatusCode, String studentStatusCode);

    @Query("SELECT " +
            "   v.validationIssueSeverityCode, COUNT(v) " +
            "FROM DemographicStudentEntity d " +
            "JOIN d.demographicStudentValidationIssueEntities v " +
            "WHERE d.incomingFileset.incomingFilesetID = :incomingFilesetId " +
            "GROUP BY v.validationIssueSeverityCode")
    List<Object[]> countValidationIssuesBySeverity(@Param("incomingFilesetId") UUID incomingFilesetId);
}
