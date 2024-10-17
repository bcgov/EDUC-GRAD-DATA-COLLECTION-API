package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncomingFilesetRepository extends JpaRepository<IncomingFilesetEntity, UUID>, JpaSpecificationExecutor<IncomingFilesetEntity> {
    Optional<IncomingFilesetEntity> findBySchoolID(UUID schoolID);
}
