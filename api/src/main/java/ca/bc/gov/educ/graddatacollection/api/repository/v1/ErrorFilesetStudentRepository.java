package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.ErrorFilesetStudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ErrorFilesetStudentRepository extends JpaRepository<ErrorFilesetStudentEntity, UUID>, JpaSpecificationExecutor<ErrorFilesetStudentEntity> {
    Optional<ErrorFilesetStudentEntity> findByIncomingFileset_IncomingFilesetIDAndPen(UUID incomingFilesetId, String pen);
    Optional<ErrorFilesetStudentEntity> findFirstByPenOrderByIncomingFileset_CreateDateDesc(String pen);
    List<ErrorFilesetStudentEntity> findAllByIncomingFileset_IncomingFilesetID(UUID incomingFilesetId);
}
