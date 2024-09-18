package ca.bc.gov.educ.graddatacollection.api.orchestrator.base;

import ca.bc.gov.educ.graddatacollection.api.model.v1.GradSagaEntity;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * The interface Orchestrator.
 */
public interface Orchestrator {


  /**
   * Gets saga name.
   *
   * @return the saga name
   */
  String getSagaName();

  /**
   * Start saga.
   *
   * @param saga  the saga data
   */
  void startSaga(GradSagaEntity saga);

  /**
   * create saga.
   *
   * @param payload   the payload
   * @param sdcSchoolStudentID the student id
   * @param userName  the user who created the saga
   * @return the saga
   */
  GradSagaEntity createSaga(String payload, UUID sdcSchoolStudentID, UUID sdcSchoolCollectionID, String userName, UUID collectionID);

  /**
   * Replay saga.
   *
   * @param saga the saga
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   * @throws TimeoutException     the timeout exception
   */
  void replaySaga(GradSagaEntity saga) throws IOException, InterruptedException, TimeoutException;
}
