package ca.bc.gov.educ.graddatacollection.api.repository.v1;


import ca.bc.gov.educ.graddatacollection.api.model.v1.GradSagaEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.SagaEventStatesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The interface Saga event repository.
 */
@Repository
public interface SagaEventRepository extends JpaRepository<SagaEventStatesEntity, UUID> {
  /**
   * Find by saga list.
   *
   * @param saga the saga
   * @return the list
   */
  List<SagaEventStatesEntity> findBySaga(GradSagaEntity saga);

  /**
   * Find by saga and saga event outcome and saga event state and saga step number optional.
   *
   * @param saga         the saga
   * @param eventOutcome the event outcome
   * @param eventState   the event state
   * @param stepNumber   the step number
   * @return the optional
   */
  Optional<SagaEventStatesEntity> findBySagaAndSagaEventOutcomeAndSagaEventStateAndSagaStepNumber(GradSagaEntity saga, String eventOutcome, String eventState, int stepNumber);

  @Transactional
  @Modifying
  @Query(value = "delete from GRAD_SAGA_EVENT_STATES e where exists(select 1 from GRAD_SAGA s where s.SAGA_ID = e.SAGA_ID and s.CREATE_DATE <= :createDate)", nativeQuery = true)
  void deleteBySagaCreateDateBefore(LocalDateTime createDate);
}
