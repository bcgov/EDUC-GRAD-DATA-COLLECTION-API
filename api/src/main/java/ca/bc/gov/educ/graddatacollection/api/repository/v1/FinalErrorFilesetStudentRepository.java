package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.FinalErrorFilesetStudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinalErrorFilesetStudentRepository extends JpaRepository<FinalErrorFilesetStudentEntity, UUID>, JpaSpecificationExecutor<FinalErrorFilesetStudentEntity> {
    Optional<FinalErrorFilesetStudentEntity> findByIncomingFileset_IncomingFilesetIDAndPen(UUID incomingFilesetId, String pen);
    List<FinalErrorFilesetStudentEntity> findAllByIncomingFileset_IncomingFilesetID(UUID incomingFilesetId);
    long countAllByIncomingFileset_IncomingFilesetID(UUID incomingFilesetId);
}
