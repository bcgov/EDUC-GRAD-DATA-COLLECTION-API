package ca.bc.gov.educ.graddatacollection.api.rest;

import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.constants.TopicsEnum;
import ca.bc.gov.educ.graddatacollection.api.exception.SagaRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1.Session;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GradGrade;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.*;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.struct.CHESEmail;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.external.scholarships.v1.CitizenshipCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

/**
 * This class is used for REST calls
 *
 * @author Marco Villeneuve
 */
@Component
@Slf4j
public class RestUtils {
  public static final String NATS_TIMEOUT = "Either NATS timed out or the response is null , correlationID :: ";
  private static final String CONTENT_TYPE = "Content-Type";
  private final Map<String, IndependentAuthority> authorityMap = new ConcurrentHashMap<>();
  private final Map<String, SchoolTombstone> schoolMap = new ConcurrentHashMap<>();
  private final Map<String, SchoolTombstone> schoolMincodeMap = new ConcurrentHashMap<>();
  private final Map<String, District> districtMap = new ConcurrentHashMap<>();
  private final Map<String, FacilityTypeCode> facilityTypeCodesMap = new ConcurrentHashMap<>();
  private final Map<String, SchoolCategoryCode> schoolCategoryCodesMap = new ConcurrentHashMap<>();
  private final Map<String, GradGrade> gradGradeMap = new ConcurrentHashMap<>();
  private final Map<String, CitizenshipCode> scholarshipsCitizenshipCodesMap = new ConcurrentHashMap<>();
  private final WebClient webClient;
  private final WebClient chesWebClient;
  private final MessagePublisher messagePublisher;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final ReadWriteLock facilityTypesLock = new ReentrantReadWriteLock();
  private final ReadWriteLock scholarshipsCitizenshipLock = new ReentrantReadWriteLock();
  private final ReadWriteLock schoolCategoriesLock = new ReentrantReadWriteLock();
  private final ReadWriteLock authorityLock = new ReentrantReadWriteLock();
  private final ReadWriteLock schoolLock = new ReentrantReadWriteLock();
  private final ReadWriteLock districtLock = new ReentrantReadWriteLock();
  private final ReadWriteLock assessmentSessionLock = new ReentrantReadWriteLock();
  private final ReadWriteLock gradGradeSessionLock = new ReentrantReadWriteLock();
  private final Map<String, Session> sessionMap = new ConcurrentHashMap<>();
  @Getter
  private final ApplicationProperties props;

  @Value("${initialization.background.enabled}")
  private Boolean isBackgroundInitializationEnabled;

  private final Map<String, List<UUID>> independentAuthorityToSchoolIDMap = new ConcurrentHashMap<>();

  @Autowired
  public RestUtils(@Qualifier("chesWebClient") final WebClient chesWebClient, WebClient webClient, final ApplicationProperties props, final MessagePublisher messagePublisher) {
    this.webClient = webClient;
    this.chesWebClient = chesWebClient;
    this.props = props;
    this.messagePublisher = messagePublisher;
  }

  @PostConstruct
  public void init() {
    if (this.isBackgroundInitializationEnabled != null && this.isBackgroundInitializationEnabled) {
      ApplicationProperties.bgTask.execute(this::initialize);
    }
  }

  private void initialize() {
    this.populateSchoolCategoryCodesMap();
    this.populateFacilityTypeCodesMap();
    this.populateSchoolMap();
    this.populateSchoolMincodeMap();
    this.populateDistrictMap();
    this.populateAuthorityMap();
    this.populateAssessmentSessionMap();
    this.populateCitizenshipCodesMap();
    this.populateGradGradesMap();
  }

  @Scheduled(cron = "${schedule.jobs.load.school.cron}")
  public void scheduled() {
    this.init();
  }

  public void populateCitizenshipCodesMap() {
    val writeLock = this.scholarshipsCitizenshipLock.writeLock();
    try {
      writeLock.lock();
      for (val citizenshipCode : this.getScholarshipsCitizenshipCodes()) {
        this.scholarshipsCitizenshipCodesMap.put(citizenshipCode.getCitizenshipCode(), citizenshipCode);
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache citizenship codes {}", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("Loaded  {} citizenship codes to memory", this.scholarshipsCitizenshipCodesMap.values().size());
  }

  public void populateAuthorityMap() {
    val writeLock = this.authorityLock.writeLock();
    try {
      writeLock.lock();
      for (val authority : this.getAuthorities()) {
        this.authorityMap.put(authority.getIndependentAuthorityId(), authority);
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache authorities {}", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("Loaded  {} authorities to memory", this.authorityMap.values().size());
  }

  public void populateSchoolCategoryCodesMap() {
    val writeLock = this.schoolCategoriesLock.writeLock();
    try {
      writeLock.lock();
      for (val categoryCode : this.getSchoolCategoryCodes()) {
        this.schoolCategoryCodesMap.put(categoryCode.getSchoolCategoryCode(), categoryCode);
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache school categories {}", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("Loaded  {} school categories to memory", this.schoolCategoryCodesMap.values().size());
  }

  public void populateFacilityTypeCodesMap() {
    val writeLock = this.facilityTypesLock.writeLock();
    try {
      writeLock.lock();
      for (val categoryCode : this.getFacilityTypeCodes()) {
        this.facilityTypeCodesMap.put(categoryCode.getFacilityTypeCode(), categoryCode);
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache facility types {}", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("Loaded  {} facility types to memory", this.facilityTypeCodesMap.values().size());
  }

  public void populateSchoolMap() {
    val writeLock = this.schoolLock.writeLock();
    try {
      writeLock.lock();
      for (val school : this.getSchools()) {
        this.schoolMap.put(school.getSchoolId(), school);
        if (StringUtils.isNotBlank(school.getIndependentAuthorityId())) {
          this.independentAuthorityToSchoolIDMap.computeIfAbsent(school.getIndependentAuthorityId(), k -> new ArrayList<>()).add(UUID.fromString(school.getSchoolId()));
        }
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache school {}", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("Loaded  {} schools to memory", this.schoolMap.values().size());
  }

  public void populateSchoolMincodeMap() {
    val writeLock = this.schoolLock.writeLock();
    try {
      writeLock.lock();
      for (val school : this.getSchools()) {
        this.schoolMincodeMap.put(school.getMincode(), school);
        if (StringUtils.isNotBlank(school.getIndependentAuthorityId())) {
          this.independentAuthorityToSchoolIDMap.computeIfAbsent(school.getIndependentAuthorityId(), k -> new ArrayList<>()).add(UUID.fromString(school.getSchoolId()));
        }
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache school mincodes {}", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("Loaded  {} school mincodes to memory", this.schoolMincodeMap.values().size());
  }

  public List<CitizenshipCode> getScholarshipsCitizenshipCodes() {
    log.info("Calling Scholarships api to load citizenship codes to memory");
    return this.webClient.get()
            .uri(this.props.getScholarshipsApiURL() + "/citizenship-codes")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(CitizenshipCode.class)
            .collectList()
            .block();
  }

  public void populateGradGradesMap() {
    val writeLock = this.gradGradeSessionLock.writeLock();
    try {
      writeLock.lock();
      for (val grade : this.getGradGrades()) {
        this.gradGradeMap.put(grade.getGrade(), grade);
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache grad grade {}", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("Loaded  {} grad grades to memory", this.gradGradeMap.values().size());
  }

  public List<GradGrade> getGradGrades() {
    log.info("Calling Grad api to load grades to memory");
    return this.webClient.get()
            .uri(this.props.getGradApiURL() + "/grades")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(GradGrade.class)
            .collectList()
            .block();
  }

  public List<SchoolTombstone> getSchools() {
    log.info("Calling Institute api to load schools to memory");
    return this.webClient.get()
            .uri(this.props.getInstituteApiURL() + "/school")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(SchoolTombstone.class)
            .collectList()
            .block();
  }

  public List<IndependentAuthority> getAuthorities() {
    log.info("Calling Institute api to load authority to memory");
    return this.webClient.get()
            .uri(this.props.getInstituteApiURL() + "/authority")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(IndependentAuthority.class)
            .collectList()
            .block();
  }

  public List<SchoolCategoryCode> getSchoolCategoryCodes() {
    log.info("Calling Institute api to load school categories to memory");
    return this.webClient.get()
            .uri(this.props.getInstituteApiURL() + "/category-codes")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(SchoolCategoryCode.class)
            .collectList()
            .block();
  }

  public List<FacilityTypeCode> getFacilityTypeCodes() {
    log.info("Calling Institute api to load facility type codes to memory");
    return this.webClient.get()
            .uri(this.props.getInstituteApiURL() + "/facility-codes")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(FacilityTypeCode.class)
            .collectList()
            .block();
  }

  public School getSchoolDetails(UUID schoolID) {
    log.debug("Retrieving school by ID: {}", schoolID);
    return this.webClient.get()
            .uri(this.props.getInstituteApiURL() + "/school/" + schoolID)
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(School.class)
            .blockFirst();
  }

  public void populateDistrictMap() {
    val writeLock = this.districtLock.writeLock();
    try {
      writeLock.lock();
      for (val district : this.getDistricts()) {
        this.districtMap.put(district.getDistrictId(), district);
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache district {}", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("Loaded  {} districts to memory", this.districtMap.values().size());
  }

  public List<District> getDistricts() {
    log.info("Calling Institute api to load districts to memory");
    return this.webClient.get()
            .uri(this.props.getInstituteApiURL() + "/district")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(District.class)
            .collectList()
            .block();
  }

  public Optional<SchoolCategoryCode> getSchoolCategoryCode(final String schoolCategoryCode) {
    if (this.schoolCategoryCodesMap.isEmpty()) {
      log.info("School categories map is empty reloading them");
      this.populateSchoolCategoryCodesMap();
    }
    return Optional.ofNullable(this.schoolCategoryCodesMap.get(schoolCategoryCode));
  }

  public Optional<FacilityTypeCode> getFacilityTypeCode(final String facilityTypeCode) {
    if (this.facilityTypeCodesMap.isEmpty()) {
      log.info("Facility types map is empty reloading them");
      this.populateFacilityTypeCodesMap();
    }
    return Optional.ofNullable(this.facilityTypeCodesMap.get(facilityTypeCode));
  }

  public Optional<SchoolTombstone> getSchoolBySchoolID(final String schoolID) {
    if (this.schoolMap.isEmpty()) {
      log.info("School map is empty reloading schools");
      this.populateSchoolMap();
    }
    return Optional.ofNullable(this.schoolMap.get(schoolID));
  }

  public Optional<IndependentAuthority> getAuthorityByAuthorityID(final String authorityID) {
    if (this.authorityMap.isEmpty()) {
      log.info("Authority map is empty reloading authorities");
      this.populateAuthorityMap();
    }
    return Optional.ofNullable(this.authorityMap.get(authorityID));
  }

  public Optional<SchoolTombstone> getSchoolByMincode(final String mincode) {
    if (this.schoolMincodeMap.isEmpty()) {
      log.info("School mincode map is empty reloading schools");
      this.populateSchoolMincodeMap();
    }
    return Optional.ofNullable(this.schoolMincodeMap.get(mincode));
  }

  public void sendEmail(final String fromEmail, final List<String> toEmail, final String body, final String subject) {
    this.sendEmail(this.getChesEmail(fromEmail, toEmail, body, subject));
  }

  public void sendEmail(final CHESEmail chesEmail) {
    this.chesWebClient
            .post()
            .uri(this.props.getChesEndpointURL())
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(Mono.just(chesEmail), CHESEmail.class)
            .retrieve()
            .bodyToMono(String.class)
            .doOnError(error -> this.logError(error, chesEmail))
            .doOnSuccess(success -> this.onSendEmailSuccess(success, chesEmail))
            .block();
  }

  private void logError(final Throwable throwable, final CHESEmail chesEmailEntity) {
    log.error("Error from CHES API call :: {} ", chesEmailEntity, throwable);
  }

  private void onSendEmailSuccess(final String s, final CHESEmail chesEmailEntity) {
    log.info("Email sent success :: {} :: {}", chesEmailEntity, s);
  }

  public CHESEmail getChesEmail(final String fromEmail, final List<String> toEmail, final String body, final String subject) {
    final CHESEmail chesEmail = new CHESEmail();
    chesEmail.setBody(body);
    chesEmail.setBodyType("html");
    chesEmail.setDelayTS(0);
    chesEmail.setEncoding("utf-8");
    chesEmail.setFrom(fromEmail);
    chesEmail.setPriority("normal");
    chesEmail.setSubject(subject);
    chesEmail.setTag("tag");
    chesEmail.getTo().addAll(toEmail);
    return chesEmail;
  }

  public Optional<District> getDistrictByDistrictID(final String districtID) {
    if (this.districtMap.isEmpty()) {
      log.info("District map is empty reloading schools");
      this.populateDistrictMap();
    }
    return Optional.ofNullable(this.districtMap.get(districtID));
  }

  public Optional<List<UUID>> getSchoolIDsByIndependentAuthorityID(final String independentAuthorityID) {
    if (this.independentAuthorityToSchoolIDMap.isEmpty()) {
      log.info("The map is empty reloading schools");
      this.populateSchoolMap();
    }
    return Optional.ofNullable(this.independentAuthorityToSchoolIDMap.get(independentAuthorityID));
  }

  @Retryable(retryFor = {Exception.class}, noRetryFor = {SagaRuntimeException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public Student getStudentByPEN(UUID correlationID, String assignedPEN) {
    try {
      final TypeReference<Student> refPenMatchResult = new TypeReference<>() {
      };
      Object event = Event.builder().sagaId(correlationID).eventType(EventType.GET_STUDENT).eventPayload(assignedPEN).build();
      val responseMessage = this.messagePublisher.requestMessage(TopicsEnum.STUDENT_API_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(event)).completeOnTimeout(null, 120, TimeUnit.SECONDS).get();
      if (responseMessage != null) {
        return objectMapper.readValue(responseMessage.getData(), refPenMatchResult);
      } else {
        throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT + correlationID);
      }

    } catch (final Exception ex) {
      log.error("Error occurred calling GET STUDENT service :: " + ex.getMessage());
      Thread.currentThread().interrupt();
      throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT + correlationID + ex.getMessage());
    }
  }

  public void populateAssessmentSessionMap() {
    val writeLock = this.assessmentSessionLock.writeLock();
    try {
      writeLock.lock();
      List<Session> sessions = this.getAssessmentSession();

      for (val session : sessions) {
        this.sessionMap.put(session.getSessionID(), session);
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache for Assessment session map {}", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("Loaded  {} Assessment session map to memory", this.sessionMap.values().size());
  }

  public Optional<Session> getAssessmentSessionByCourseMonthAndYear(Integer courseMonth, Integer courseYear) {
    if(sessionMap.isEmpty()) {
      log.info("Assessment session map is empty reloading schools");
      populateAssessmentSessionMap();
    }
    return sessionMap.values().stream().
            filter(session -> Objects.equals(session.getCourseMonth(), courseMonth) && Objects.equals(session.getCourseYear(), courseYear)).findFirst();
  }

  public List<Session> getAssessmentSession() {
    UUID correlationID = UUID.randomUUID();
    try {
      log.info("Calling EAS API to load assessment sessions to memory");
      final TypeReference<List<Session>> ref = new TypeReference<>() {
      };
      val event = Event.builder().sagaId(correlationID).eventType(EventType.GET_OPEN_ASSESSMENT_SESSIONS).build();
      val responseMessage = this.messagePublisher.requestMessage(TopicsEnum.EAS_API_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(event)).completeOnTimeout(null, 60, TimeUnit.SECONDS).get();
      if (null != responseMessage) {
        return objectMapper.readValue(responseMessage.getData(), ref);
      } else {
        throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT + correlationID);
      }
    } catch (final Exception ex) {
      Thread.currentThread().interrupt();
      throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT + correlationID + ex.getMessage());
    }
  }
}
