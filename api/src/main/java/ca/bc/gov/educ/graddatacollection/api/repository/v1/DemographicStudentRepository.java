package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DemographicStudentRepository extends JpaRepository<DemographicStudentEntity, UUID>, JpaSpecificationExecutor<DemographicStudentEntity> {
    List<DemographicStudentEntity> findAllByIncomingFileset_IncomingFilesetID(UUID incomingFilesetID);
    long countByIncomingFileset_SchoolIDAndStudentStatusCode(UUID schoolID, String studentStatusCode);
    long countByIncomingFileset_IncomingFilesetID(UUID incomingFilesetID);
    long countByStudentStatusCodeAndAndIncomingFileset_IncomingFilesetID(String studentStatusCode, UUID incomingFilesetID);

    List<DemographicStudentEntity> findAllByIncomingFileset_IncomingFilesetIDAndLastNameEqualsIgnoreCaseAndPenEqualsIgnoreCaseAndLocalIDEqualsIgnoreCase(UUID incomingFilesetID, String lastName, String pen, String localID);
}
