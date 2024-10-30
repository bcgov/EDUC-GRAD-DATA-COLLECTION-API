package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.GradSagaEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.SagaEventRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.SagaRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static ca.bc.gov.educ.graddatacollection.api.constants.EventType.INITIATED;
import static ca.bc.gov.educ.graddatacollection.api.constants.SagaStatusEnum.STARTED;
import static lombok.AccessLevel.PRIVATE;

/**
 * The type Saga service.
 */
@Service
@Slf4j
public class SagaService {
  /**
   * The Saga repository.
   */
  @Getter(AccessLevel.PRIVATE)
  private final SagaRepository sagaRepository;
  /**
   * The Saga event repository.
   */
  @Getter(PRIVATE)
  private final SagaEventRepository sagaEventRepository;

  /**
   * Instantiates a new Saga service.
   *
   * @param sagaRepository      the saga repository
   * @param sagaEventRepository the saga event repository
   */
  @Autowired
  public SagaService(final SagaRepository sagaRepository, final SagaEventRepository sagaEventRepository) {
    this.sagaRepository = sagaRepository;
    this.sagaEventRepository = sagaEventRepository;
  }


  /**
   * Create saga record saga.
   *
   * @param saga the saga
   * @return the saga
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public GradSagaEntity createSagaRecord(final GradSagaEntity saga) {
    return this.getSagaRepository().save(saga);
  }

  /**
   * Create saga records.
   *
   * @param sagas the sagas
   * @return the saga
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public List<GradSagaEntity> createSagaRecords(final List<GradSagaEntity> sagas) {
    return this.getSagaRepository().saveAll(sagas);
  }

  /**
   * no need to do a get here as it is an attached entity
   * first find the child record, if exist do not add. this scenario may occur in replay process,
   * so dont remove this check. removing this check will lead to duplicate records in the child table.
   *
   * @param saga            the saga object.
   * @param sagaEventStates the saga event
   */
  @Retryable(maxAttempts = 5, backoff = @Backoff(multiplier = 2, delay = 2000))
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateAttachedSagaWithEvents(final GradSagaEntity saga, final SagaEventStatesEntity sagaEventStates) {
    saga.setUpdateDate(LocalDateTime.now());
    this.getSagaRepository().save(saga);
    val result = this.getSagaEventRepository()
      .findBySagaAndSagaEventOutcomeAndSagaEventStateAndSagaStepNumber(saga, sagaEventStates.getSagaEventOutcome(), sagaEventStates.getSagaEventState(), sagaEventStates.getSagaStepNumber() - 1); //check if the previous step was same and had same outcome, and it is due to replay.
    if (result.isEmpty()) {
      this.getSagaEventRepository().save(sagaEventStates);
    }
  }

  /**
   * Find saga by id optional.
   *
   * @param sagaId the saga id
   * @return the optional
   */
  public Optional<GradSagaEntity> findSagaById(final UUID sagaId) {
    return this.getSagaRepository().findById(sagaId);
  }

  /**
   * Find all saga states list.
   *
   * @param saga the saga
   * @return the list
   */
  public List<SagaEventStatesEntity> findAllSagaStates(final GradSagaEntity saga) {
    return this.getSagaEventRepository().findBySaga(saga);
  }


  /**
   * Update saga record.
   *
   * @param saga the saga
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public void updateSagaRecord(final GradSagaEntity saga) { // saga here MUST be an attached entity
    this.getSagaRepository().save(saga);
  }

  /**
   * Create saga record in db saga.
   *
   * @param sagaName             the saga name
   * @param userName             the username
   * @param payload              the payload
   * @return the saga
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public GradSagaEntity createSagaRecordInDB(final String sagaName, final String userName, final String payload,
                                             final UUID incomingFilesetID, final UUID demographicStudentID, final UUID assessmentStudentID, final UUID courseStudentID) {
    final var saga = GradSagaEntity
      .builder()
      .payload(payload)
      .incomingFilesetID(incomingFilesetID)
      .demographicStudentID(demographicStudentID)
      .assessmentStudentID(assessmentStudentID)
      .courseStudentID(courseStudentID)
      .sagaName(sagaName)
      .status(STARTED.toString())
      .sagaState(INITIATED.toString())
      .createDate(LocalDateTime.now())
      .createUser(userName)
      .updateUser(userName)
      .updateDate(LocalDateTime.now())
      .build();
    return this.createSagaRecord(saga);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public List<GradSagaEntity> createSagaRecordsInDB(final List<GradSagaEntity> gradSagaEntities) {
    return this.createSagaRecords(gradSagaEntities);
  }

  public Optional<GradSagaEntity> findByDemographicStudentIDAndIncomingFilesetIDAndSagaNameAndStatusNot(final UUID demographicStudentID, final UUID incomingFilesetID, final String sagaName, final String status) {
    return this.getSagaRepository().findByDemographicStudentIDAndIncomingFilesetIDAndSagaNameAndStatusNot(demographicStudentID, incomingFilesetID, sagaName, status);
  }

  public Optional<GradSagaEntity> findByCourseStudentIDAndIncomingFilesetIDAndSagaNameAndStatusNot(final UUID courseStudentID, final UUID incomingFilesetID, final String sagaName, final String status) {
    return this.getSagaRepository().findByCourseStudentIDAndIncomingFilesetIDAndSagaNameAndStatusNot(courseStudentID, incomingFilesetID, sagaName, status);
  }

  /**
   * Find all completable future.
   *
   * @param specs      the saga specs
   * @param pageNumber the page number
   * @param pageSize   the page size
   * @param sorts      the sorts
   * @return the completable future
   */
  @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
  public CompletableFuture<Page<GradSagaEntity>> findAll(final Specification<GradSagaEntity> specs, final Integer pageNumber, final Integer pageSize, final List<Sort.Order> sorts) {
    return CompletableFuture.supplyAsync(() -> {
      final Pageable paging = PageRequest.of(pageNumber, pageSize, Sort.by(sorts));
      try {
        return this.sagaRepository.findAll(specs, paging);
      } catch (final Exception ex) {
        throw new CompletionException(ex);
      }
    });
  }
}
