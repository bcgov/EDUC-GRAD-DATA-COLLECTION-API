package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetPurgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface IncomingFilesetPurgeRepository extends JpaRepository<IncomingFilesetPurgeEntity, UUID>, JpaSpecificationExecutor<IncomingFilesetPurgeEntity> {

    @Transactional
    @Modifying
    @Query("DELETE FROM IncomingFilesetEntity WHERE updateDate <= :oldestIncomingFilesetTimestamp AND (demFileName is null OR crsFileName is null OR xamFileName is null)")
    void deleteStaleWithUpdateDateBefore(LocalDateTime oldestIncomingFilesetTimestamp);

    @Transactional
    @Modifying
    @Query("DELETE FROM IncomingFilesetEntity WHERE createDate <= :deleteDate")
    void deleteWithCreateDateBefore(LocalDateTime deleteDate);
}
