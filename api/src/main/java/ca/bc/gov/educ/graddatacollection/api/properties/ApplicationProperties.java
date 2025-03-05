package ca.bc.gov.educ.graddatacollection.api.properties;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.Setter;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * Class holds all application properties
 *
 * @author Marco Villeneuve
 */
@Component
@Getter
@Setter
public class ApplicationProperties {
  public static final Executor bgTask = new EnhancedQueueExecutor.Builder()
    .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("bg-task-executor-%d").build())
    .setCorePoolSize(1).setMaximumPoolSize(1).setKeepAliveTime(Duration.ofSeconds(60)).build();
  public static final String GRAD_DATA_COLLECTION_API = "GRAD_DATA_COLLECTION_API";
  public static final String CORRELATION_ID = "correlationID";
  /**
   * The Client id.
   */
  @Value("${client.id}")
  private String clientID;
  /**
   * The Client secret.
   */
  @Value("${client.secret}")
  private String clientSecret;
  /**
   * The Token url.
   */
  @Value("${url.token}")
  private String tokenURL;

  @Value("${nats.server}")
  private String server;

  @Value("${nats.maxReconnect}")
  private int maxReconnect;

  @Value("${nats.connectionName}")
  private String connectionName;

  @Value("${threads.min.subscriber}")
  private Integer minSubscriberThreads;
  @Value("${threads.max.subscriber}")
  private Integer maxSubscriberThreads;
  @Value("${sagas.max.pending}")
  private Integer maxPendingSagas;
  @Value("${sagas.max.parallel}")
  private Integer maxParallelSagas;
  @Value("${url.api.institute}")
  private String instituteApiURL;
  @Value("${url.api.scholarships}")
  private String scholarshipsApiURL;
  @Value("${url.api.grad.student}")
  private String gradStudentApiURL;
  @Value("${url.api.grad.student.graduation}")
  private String gradStudentGraduationApiURL;
  @Value("${url.api.grad.program}")
  private String gradProgramApiURL;
  @Value("${url.api.grad.course}")
  private String gradCourseApiURL;
  @Value("${number.students.process.saga}")
  private String numberOfStudentsToProcessInSaga;
  @Value("${ches.endpoint.url}")
  private String chesEndpointURL;
  @Value("${ches.client.id}")
  private String chesClientID;
  @Value("${ches.client.secret}")
  private String chesClientSecret;
  @Value("${ches.token.url}")
  private String chesTokenURL;
  @Value("${url.api.student}")
  private String studentApiURL;
  @Value("${incoming.fileset.stale.in.hours}")
  private Integer incomingFilesetStaleInHours;
  @Value("${edx.base.url}")
  private String edxBaseUrl;
}
