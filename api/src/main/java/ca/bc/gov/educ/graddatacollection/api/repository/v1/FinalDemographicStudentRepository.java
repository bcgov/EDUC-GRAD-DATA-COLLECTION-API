package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.FinalDemographicStudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinalDemographicStudentRepository extends JpaRepository<FinalDemographicStudentEntity, UUID>, JpaSpecificationExecutor<FinalDemographicStudentEntity> {

    List<FinalDemographicStudentEntity> findAllByIncomingFileset_IncomingFilesetID(UUID incomingFilesetID);
    Optional<FinalDemographicStudentEntity> findFirstByIncomingFileset_SchoolIDAndIncomingFileset_FilesetStatusCodeAndPenAndStudentStatusCodeNotOrderByCreateDateDesc(UUID schoolID, String filesetStatusCode, String pen, String studentStatusCode);
    Optional<FinalDemographicStudentEntity> findByIncomingFileset_IncomingFilesetIDAndPenAndIncomingFileset_SchoolIDAndIncomingFileset_FilesetStatusCodeAndStudentStatusCodeNot(UUID incomingFilesetID, String pen, UUID schoolID, String filesetStatusCode, String studentStatusCode);

    @Query("SELECT " +
            "   v.validationIssueSeverityCode, COUNT(v) " +
            "FROM FinalDemographicStudentEntity d " +
            "JOIN d.demographicStudentValidationIssueEntities v " +
            "WHERE d.incomingFileset.incomingFilesetID = :incomingFilesetId " +
            "GROUP BY v.validationIssueSeverityCode")
    List<Object[]> countValidationIssuesBySeverity(@Param("incomingFilesetId") UUID incomingFilesetId);
}
