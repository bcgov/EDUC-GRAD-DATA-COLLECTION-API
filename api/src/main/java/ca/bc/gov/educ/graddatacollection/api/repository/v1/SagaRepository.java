package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.GradSagaEntity;
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

/**
 * The interface Saga repository.
 */
@Repository
public interface SagaRepository extends JpaRepository<GradSagaEntity, UUID>, JpaSpecificationExecutor<GradSagaEntity> {

  long countAllByStatusIn(List<String> statuses);

  @Transactional
  @Modifying
  @Query("delete from GradSagaEntity where createDate <= :createDate")
  void deleteByCreateDateBefore(LocalDateTime createDate);

  List<GradSagaEntity> findTop500ByStatusInOrderByCreateDate(List<String> statuses);

  Optional<GradSagaEntity>  findByDemographicStudentIDAndIncomingFilesetIDAndSagaNameAndStatusNot(UUID demographicStudentID, UUID incomingFilesetID, String sagaName, String status);

  Optional<GradSagaEntity>  findByCourseStudentIDAndIncomingFilesetIDAndSagaNameAndStatusNot(UUID courseStudentID, UUID incomingFilesetID, String sagaName, String status);
  Optional<GradSagaEntity>  findByAssessmentStudentIDAndIncomingFilesetIDAndSagaNameAndStatusNot(UUID assessmentStudentID, UUID incomingFilesetID, String sagaName, String status);
}
