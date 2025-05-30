package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetPurgeEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SchoolSubmissionCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncomingFilesetPurgeRepository extends JpaRepository<IncomingFilesetPurgeEntity, UUID>, JpaSpecificationExecutor<IncomingFilesetPurgeEntity> {

    @Transactional
    @Modifying
    @Query("DELETE FROM IncomingFilesetPurgeEntity WHERE updateDate <= :oldestIncomingFilesetTimestamp AND (demFileName is null OR crsFileName is null OR xamFileName is null)")
    void deleteStaleWithUpdateDateBefore(LocalDateTime oldestIncomingFilesetTimestamp);

    @Transactional
    @Modifying
    @Query("DELETE FROM IncomingFilesetPurgeEntity WHERE createDate <= :deleteDate")
    void deleteWithCreateDateBefore(LocalDateTime deleteDate);
}
