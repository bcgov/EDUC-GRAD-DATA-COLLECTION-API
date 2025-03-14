package ca.bc.gov.educ.graddatacollection.api.rest;

import ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome;
import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.constants.TopicsEnum;
import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.exception.SagaRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.struct.CHESEmail;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CoregCoursesRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1.AssessmentStudentDetailResponse;
import ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1.AssessmentStudentGet;
import ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1.Session;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.*;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.*;
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

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
  private static final String EXCEPTION = "exception";
  public static final String NO_RESPONSE_RECEIVED_WITHIN_TIMEOUT_FOR_CORRELATION_ID = "No response received within timeout for correlation ID ";
  private final Map<String, SchoolTombstone> schoolMap = new ConcurrentHashMap<>();
  private final Map<String, SchoolTombstone> schoolMincodeMap = new ConcurrentHashMap<>();
  private final Map<String, District> districtMap = new ConcurrentHashMap<>();
  private final Map<String, FacilityTypeCode> facilityTypeCodesMap = new ConcurrentHashMap<>();
  private final Map<String, SchoolCategoryCode> schoolCategoryCodesMap = new ConcurrentHashMap<>();
  private final Map<String, GradGrade> gradGradeMap = new ConcurrentHashMap<>();
  private final Map<String, LetterGrade> letterGradeMap = new ConcurrentHashMap<>();
  private final Map<String, CitizenshipCode> scholarshipsCitizenshipCodesMap = new ConcurrentHashMap<>();
  private final Map<String, CareerProgramCode> careerProgramCodesMap = new ConcurrentHashMap<>();
  private final Map<String, OptionalProgramCode> optionalProgramCodesMap = new ConcurrentHashMap<>();
  private final Map<String, ProgramRequirementCode> programRequirementCodeMap = new ConcurrentHashMap<>();
  private final Map<String, GraduationProgramCode> gradProgramCodeMap = new ConcurrentHashMap<>();
  private final Map<String, EquivalencyChallengeCode> equivalencyChallengeCodeMap = new ConcurrentHashMap<>();
  private final WebClient webClient;
  private final WebClient chesWebClient;
  private final MessagePublisher messagePublisher;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final ReadWriteLock facilityTypesLock = new ReentrantReadWriteLock();
  private final ReadWriteLock scholarshipsCitizenshipLock = new ReentrantReadWriteLock();
  private final ReadWriteLock schoolCategoriesLock = new ReentrantReadWriteLock();
  private final ReadWriteLock schoolLock = new ReentrantReadWriteLock();
  private final ReadWriteLock districtLock = new ReentrantReadWriteLock();
  private final ReadWriteLock assessmentSessionLock = new ReentrantReadWriteLock();
  private final ReadWriteLock gradGradeLock = new ReentrantReadWriteLock();
  private final ReadWriteLock letterGradeLock = new ReentrantReadWriteLock();
  private final ReadWriteLock careerProgramLock = new ReentrantReadWriteLock();
  private final ReadWriteLock optionalProgramLock = new ReentrantReadWriteLock();
  private final ReadWriteLock programRequirementLock = new ReentrantReadWriteLock();
  private final ReadWriteLock gradProgramLock = new ReentrantReadWriteLock();
  private final ReadWriteLock equivalencyChallengeCodeLock = new ReentrantReadWriteLock();
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
    this.populateAssessmentSessionMap();
    this.populateCitizenshipCodesMap();
    this.populateGradGradesMap();
    this.populateLetterGradeMap();
    this.populateCareerProgramsMap();
    this.populateOptionalProgramsMap();
    this.populateProgramRequirementCodesMap();
    this.populateEquivalencyChallengeCodeMap();
    this.populateGradProgramCodesMap();
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

  public void populateLetterGradeMap() {
    val writeLock = this.letterGradeLock.writeLock();
    try {
      writeLock.lock();
      for (val grade : this.getLetterGrades()) {
        this.letterGradeMap.put(grade.getGrade(), grade);
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache letter grade {}", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("Loaded  {} letter grades to memory", this.letterGradeMap.values().size());
  }

  public void populateGradGradesMap() {
    val writeLock = this.gradGradeLock.writeLock();
    try {
      writeLock.lock();
      for (val grade : this.getGradGrades()) {
        this.gradGradeMap.put(grade.getStudentGradeCode(), grade);
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache grad grade {}", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("Loaded  {} grad grades to memory", this.gradGradeMap.values().size());
  }

  public void populateCareerProgramsMap() {
    val writeLock = this.careerProgramLock.writeLock();
    try {
      writeLock.lock();
      for (val program : this.getCareerPrograms()) {
        this.careerProgramCodesMap.put(program.getCode(), program);
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache career program {}", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("Loaded  {} career programs to memory", this.careerProgramCodesMap.values().size());
  }

  public void populateOptionalProgramsMap() {
    val writeLock = this.optionalProgramLock.writeLock();
    try {
      writeLock.lock();
      for (val program : this.getOptionalPrograms()) {
        this.optionalProgramCodesMap.put(program.getOptProgramCode(), program);
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache optional program {}", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("Loaded  {} optional programs to memory", this.optionalProgramCodesMap.values().size());
  }

  public void populateProgramRequirementCodesMap() {
    val writeLock = this.programRequirementLock.writeLock();
    try {
      writeLock.lock();
      for (val program : this.getProgramRequirementCodes()) {
        this.programRequirementCodeMap.put(program.getProReqCode(), program);
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache program requirement codes {}", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("Loaded  {} program requirement codes to memory", this.programRequirementCodeMap.values().size());
    log.debug(this.programRequirementCodeMap.values().toString());
  }

  public void populateGradProgramCodesMap() {
    val writeLock = this.gradProgramLock.writeLock();
    try {
      writeLock.lock();
      for (val program : this.getGraduationProgramCodes()) {
        this.gradProgramCodeMap.put(program.getProgramCode(), program);
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache grad program codes {}", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("Loaded  {} grad program codes to memory", this.gradProgramCodeMap.values().size());
    log.debug(this.gradProgramCodeMap.values().toString());
  }

  public void populateEquivalencyChallengeCodeMap() {
    val writeLock = this.equivalencyChallengeCodeLock.writeLock();
    try {
      writeLock.lock();
      for (val equivalencyCode : this.getEquivalencyChallengeCodes()) {
        this.equivalencyChallengeCodeMap.put(equivalencyCode.getEquivalentOrChallengeCode(), equivalencyCode);
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache equivalent or challenge codes {}", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("Loaded  {} equivalent or challenge codes to memory", this.equivalencyChallengeCodeMap.values().size());
  }

  public List<EquivalencyChallengeCode> getEquivalencyChallengeCodeList() {
    if (this.equivalencyChallengeCodeMap.isEmpty()) {
      log.info("Equivalency Challenge map is empty reloading them");
      this.populateEquivalencyChallengeCodeMap();
    }
    return this.equivalencyChallengeCodeMap.values().stream().toList();
  }

  private List<EquivalencyChallengeCode> getEquivalencyChallengeCodes() {
    log.info("Calling Grad course api to load equivalent or challenge codes to memory");
    return this.webClient.get()
            .uri(this.props.getGradCourseApiURL() + "/equivalentOrChallengeCodes")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(EquivalencyChallengeCode.class)
            .collectList()
            .block();
  }

  public List<ProgramRequirementCode> getProgramRequirementCodeList() {
    if (this.programRequirementCodeMap.isEmpty()) {
      log.info("Program Requirement Code map is empty reloading them");
      this.populateProgramRequirementCodesMap();
    }
    return this.programRequirementCodeMap.values().stream().toList();
  }

  private List<ProgramRequirementCode> getProgramRequirementCodes() {
    log.info("Calling Grad api to load program requirement codes to memory");
    return this.webClient.get()
            .uri(this.props.getGradProgramApiURL() + "/programrequirementcode")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(ProgramRequirementCode.class)
            .collectList()
            .block();
  }

  public List<GraduationProgramCode> getGraduationProgramCodeList() {
    if (this.gradProgramCodeMap.isEmpty()) {
      log.info("Graduation Program Code map is empty reloading them");
      this.populateGradProgramCodesMap();
    }
    return this.gradProgramCodeMap.values().stream().toList();
  }

  private List<GraduationProgramCode> getGraduationProgramCodes() {
    log.info("Calling Grad api to load graduation program codes to memory");
    return this.webClient.get()
            .uri(this.props.getGradProgramApiURL() + "/programs")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(GraduationProgramCode.class)
            .collectList()
            .block();
  }

  public List<CitizenshipCode> getScholarshipsCitizenshipCodeList() {
    if (this.scholarshipsCitizenshipCodesMap.isEmpty()) {
      log.info("Citizenship Code map is empty reloading them");
      this.populateCitizenshipCodesMap();
    }
    return this.scholarshipsCitizenshipCodesMap.values().stream().toList();
  }

  private List<CitizenshipCode> getScholarshipsCitizenshipCodes() {
    log.info("Calling Scholarships api to load citizenship codes to memory");
    return this.webClient.get()
            .uri(this.props.getScholarshipsApiURL() + "/citizenship-codes")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(CitizenshipCode.class)
            .collectList()
            .block();
  }

  public List<CareerProgramCode> getCareerProgramCodeList() {
    if (this.careerProgramCodesMap.isEmpty()) {
      log.info("Career Program Code map is empty reloading them");
      this.populateCareerProgramsMap();
    }
    return this.careerProgramCodesMap.values().stream().toList();
  }

  private List<CareerProgramCode> getCareerPrograms() {
    log.info("Calling Grad api to load career programs to memory");
    return this.webClient.get()
            .uri(this.props.getGradProgramApiURL() + "/careerprogram")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(CareerProgramCode.class)
            .collectList()
            .block();
  }

  public List<OptionalProgramCode> getOptionalProgramCodeList() {
    if (this.optionalProgramCodesMap.isEmpty()) {
      log.info("Optional Program Code map is empty reloading them");
      this.populateOptionalProgramsMap();
    }
    return this.optionalProgramCodesMap.values().stream().toList();
  }

  private List<OptionalProgramCode> getOptionalPrograms() {
    log.info("Calling Grad api to load optional programs to memory");
    return this.webClient.get()
            .uri(this.props.getGradProgramApiURL() + "/optionalprograms")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(OptionalProgramCode.class)
            .collectList()
            .block();
  }

  public List<GradGrade> getGradGradeList(boolean activeOnly) {
    if (this.gradGradeMap.isEmpty()) {
      log.info("Grad Grade map is empty reloading them");
      this.populateGradGradesMap();
    }
    if(activeOnly){
      var codes = this.gradGradeMap.values().stream().filter(code -> StringUtils.isBlank(code.getExpiryDate()) || LocalDateTime.parse(code.getExpiryDate()).isAfter(LocalDateTime.now())).toList();
      codes.forEach(code -> {
        log.info("Active Grade Code " + code);
      });

      return codes;
    }
    return this.gradGradeMap.values().stream().toList();
  }

  private List<GradGrade> getGradGrades() {
    log.info("Calling Grad api to load grades to memory");
    return this.webClient.get()
            .uri(this.props.getGradStudentApiURL() + "/grade-codes")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(GradGrade.class)
            .collectList()
            .block();
  }

  public List<LetterGrade> getLetterGradeList(boolean activeOnly) {
    if (this.letterGradeMap.isEmpty()) {
      log.info("Letter Grade map is empty reloading them");
      this.populateLetterGradeMap();
    }
    if(activeOnly){
      return this.letterGradeMap.values().stream().filter(code -> StringUtils.isBlank(code.getExpiryDate()) || LocalDateTime.parse(code.getExpiryDate(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).isAfter(LocalDateTime.now())).toList();
    }
    return this.letterGradeMap.values().stream().toList();
  }

  private List<LetterGrade> getLetterGrades() {
    log.info("Calling Grad student graduation api to load grades to memory");
    return this.webClient.get()
            .uri(this.props.getGradStudentGraduationApiURL() + "/lettergrade")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(LetterGrade.class)
            .collectList()
            .block();
  }

  private List<SchoolTombstone> getSchools() {
    log.info("Calling Institute api to load schools to memory");
    return this.webClient.get()
            .uri(this.props.getInstituteApiURL() + "/school")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(SchoolTombstone.class)
            .collectList()
            .block();
  }

  public List<SchoolCategoryCode> getSchoolCategoryCodeList() {
    if (this.schoolCategoryCodesMap.isEmpty()) {
      log.info("School Category Code map is empty reloading them");
      this.populateSchoolCategoryCodesMap();
    }
    return this.schoolCategoryCodesMap.values().stream().toList();
  }

  private List<SchoolCategoryCode> getSchoolCategoryCodes() {
    log.info("Calling Institute api to load school categories to memory");
    return this.webClient.get()
            .uri(this.props.getInstituteApiURL() + "/category-codes")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(SchoolCategoryCode.class)
            .collectList()
            .block();
  }

  public List<FacilityTypeCode> getFacilityTypeCodeList() {
    if (this.facilityTypeCodesMap.isEmpty()) {
      log.info("Facility Type Code map is empty reloading them");
      this.populateFacilityTypeCodesMap();
    }
    return this.facilityTypeCodesMap.values().stream().toList();
  }

  private List<FacilityTypeCode> getFacilityTypeCodes() {
    log.info("Calling Institute api to load facility type codes to memory");
    return this.webClient.get()
            .uri(this.props.getInstituteApiURL() + "/facility-codes")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(FacilityTypeCode.class)
            .collectList()
            .block();
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

  private List<District> getDistricts() {
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

  @Retryable(retryFor = {Exception.class}, noRetryFor = {SagaRuntimeException.class, EntityNotFoundException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public Student getStudentByPEN(UUID correlationID, String assignedPEN) {
    try {
      final TypeReference<Event> refEvent = new TypeReference<>() {};
      final TypeReference<Student> refPenMatchResult = new TypeReference<>() {};
      Object event = Event.builder().sagaId(correlationID).eventType(EventType.GET_STUDENT).eventPayload(assignedPEN).build();
      val responseMessage = this.messagePublisher.requestMessage(TopicsEnum.STUDENT_API_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(event)).completeOnTimeout(null, 120, TimeUnit.SECONDS).get();
      if (responseMessage != null) {
        byte[] data = responseMessage.getData();
        if (data == null || data.length == 0) {
          log.info("Empty response data for getStudentByPEN; treating as student not found for PEN: {}", assignedPEN);
          throw new EntityNotFoundException(Student.class);
        }

        log.debug("Response message for getStudentByPen: {}", responseMessage);
        Event responseEvent = objectMapper.readValue(responseMessage.getData(), refEvent);

        if (EventOutcome.STUDENT_NOT_FOUND.equals(responseEvent.getEventOutcome())) {
          log.info("Student not found for PEN: {}", assignedPEN);
          throw new EntityNotFoundException(Student.class);
        }

        return objectMapper.readValue(responseMessage.getData(), refPenMatchResult);
      } else {
        throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT + correlationID);
      }

    } catch (EntityNotFoundException ex) {
      log.error("Entity Not Found occurred calling GET STUDENT service :: {}", ex.getMessage());
      throw ex;
    } catch (final Exception ex) {
      log.error("Error occurred calling GET STUDENT service :: {}", ex.getMessage());
      Thread.currentThread().interrupt();
      throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT + correlationID + ex.getMessage());
    }
  }

  @Retryable(retryFor = {Exception.class}, noRetryFor = {SagaRuntimeException.class, EntityNotFoundException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public GradStudentRecord getGradStudentRecordByStudentID(UUID correlationID, UUID studentID) {
    try {
      final TypeReference<GradStudentRecord> refGradStudentRecordResult = new TypeReference<>() {
      };
      Object event = Event.builder().sagaId(correlationID).eventType(EventType.GET_GRAD_STUDENT_RECORD).eventPayload(studentID.toString()).build();
      val responseMessage = this.messagePublisher.requestMessage(TopicsEnum.GRAD_STUDENT_API_FETCH_GRAD_STUDENT_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(event)).completeOnTimeout(null, 120, TimeUnit.SECONDS).get();
      if (responseMessage != null) {
        String responseData = new String(responseMessage.getData(), StandardCharsets.UTF_8);

        Map<String, String> response = objectMapper.readValue(responseData, new TypeReference<>() {});

        log.debug("getGradStudentRecordByStudentID response{}", response.toString());

        if ("not found".equals(response.get(EXCEPTION))) {
          log.error("A not found error occurred while fetching GradStudentRecord for Student ID {}", studentID);
          throw new EntityNotFoundException(GradStudentRecord.class);
        } else if ("error".equals(response.get(EXCEPTION))) {
          log.error("An exception error occurred while fetching GradStudentRecord for Student ID {}", studentID);
          throw new GradDataCollectionAPIRuntimeException("Error occurred while processing the request for correlation ID " + correlationID);
        }

        log.info("Success fetching GradStudentRecord for Student ID {}", studentID);
        return objectMapper.readValue(responseData, refGradStudentRecordResult);
      } else {
        throw new GradDataCollectionAPIRuntimeException(NO_RESPONSE_RECEIVED_WITHIN_TIMEOUT_FOR_CORRELATION_ID + correlationID);
      }

    } catch (EntityNotFoundException ex) {
      log.error("Entity Not Found occurred calling GET GRAD STUDENT RECORD service :: {}", ex.getMessage());
      throw ex;
    } catch (final Exception ex) {
      log.error("Error occurred calling GET GRAD STUDENT RECORD service :: {}", ex.getMessage());
      Thread.currentThread().interrupt();
      throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT + correlationID);
    }
  }

  @Retryable(retryFor = {Exception.class}, noRetryFor = {EntityNotFoundException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public CoregCoursesRecord getCoursesByExternalID(UUID correlationID, String externalID) {
    try {
      final TypeReference<CoregCoursesRecord> refCourseInformation = new TypeReference<>() {};

      Event event = Event.builder()
              .sagaId(correlationID)
              .eventType(EventType.GET_COURSE_FROM_EXTERNAL_ID)
              .eventPayload(externalID)
              .replyTo("coreg-response-topic")
              .build();

      val responseMessage = this.messagePublisher
              .requestMessage(TopicsEnum.COREG_API_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(event))
              .completeOnTimeout(null, 120, TimeUnit.SECONDS)
              .get();

      if (responseMessage == null) {
        throw new GradDataCollectionAPIRuntimeException(NO_RESPONSE_RECEIVED_WITHIN_TIMEOUT_FOR_CORRELATION_ID + correlationID);
      }

      byte[] responseData = responseMessage.getData();
      if (responseData.length == 0) {
        log.warn("No course information found for external ID {}", externalID);
        throw new EntityNotFoundException(CoregCoursesRecord.class);
      }

      log.debug("Received response from NATS: {}", new String(responseData, StandardCharsets.UTF_8));
      return objectMapper.readValue(responseData, refCourseInformation);

    } catch (EntityNotFoundException ex) {
      log.error("EntityNotFoundException occurred calling GET_COURSE_FROM_EXTERNAL_ID service :: {}", ex.getMessage());
      throw new EntityNotFoundException();
    } catch (final Exception ex) {
      log.error("Error occurred calling GET_COURSE_FROM_EXTERNAL_ID service :: {}", ex.getMessage());
      Thread.currentThread().interrupt();
      throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT + correlationID);
    }
  }

  @Retryable(retryFor = {Exception.class}, noRetryFor = {EntityNotFoundException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public List<GradStudentCourseRecord> getGradStudentCoursesByPEN(UUID correlationID, String pen) {
    try {
      final TypeReference<List<GradStudentCourseRecord>> refCourseInformation = new TypeReference<>() {};

      Event event = Event.builder()
              .sagaId(correlationID)
              .eventType(EventType.GET_STUDENT_COURSE)
              .eventPayload(pen)
              .replyTo("grad-course-response-topic")
              .build();

      val responseMessage = this.messagePublisher
              .requestMessage(TopicsEnum.GRAD_COURSE_API_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(event))
              .completeOnTimeout(null, 120, TimeUnit.SECONDS)
              .get();

      if (responseMessage == null) {
        throw new GradDataCollectionAPIRuntimeException(NO_RESPONSE_RECEIVED_WITHIN_TIMEOUT_FOR_CORRELATION_ID + correlationID);
      }

      byte[] responseData = responseMessage.getData();
      if (responseData.length == 0) {
        log.warn("No course information found for PEN {}", pen);
        throw new EntityNotFoundException(CoregCoursesRecord.class);
      }

      log.debug("Received response from NATS: {}", new String(responseData, StandardCharsets.UTF_8));
      return objectMapper.readValue(responseData, refCourseInformation);

    } catch (EntityNotFoundException ex) {
      log.error("EntityNotFoundException occurred calling GET_STUDENT_COURSE service :: {}", ex.getMessage());
      throw new EntityNotFoundException();
    } catch (final Exception ex) {
      log.error("Error occurred calling GET_STUDENT_COURSE service :: {}", ex.getMessage());
      Thread.currentThread().interrupt();
      throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT + correlationID);
    }
  }

  public void populateAssessmentSessionMap() {
    val writeLock = this.assessmentSessionLock.writeLock();
    try {
      writeLock.lock();
      List<Session> sessions = this.getAssessmentSessions();

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

  public Optional<Session> getAssessmentSessionByCourseMonthAndYear(String courseMonth, String courseYear) {
    if(sessionMap.isEmpty()) {
      log.info("Assessment session map is empty reloading schools");
      populateAssessmentSessionMap();
    }
    return sessionMap.values().stream().
            filter(session -> Objects.equals(session.getCourseMonth(), courseMonth) && Objects.equals(session.getCourseYear(), courseYear)).findFirst();
  }

  @Retryable(retryFor = {Exception.class}, noRetryFor = {SagaRuntimeException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public AssessmentStudentDetailResponse getAssessmentStudentDetail(UUID studentID, UUID assessmentID) {
    try {
      final TypeReference<AssessmentStudentDetailResponse> refPenMatchResult = new TypeReference<>() {
      };
      var assessmentStudent = new AssessmentStudentGet();
      assessmentStudent.setAssessmentID(assessmentID.toString());
      assessmentStudent.setStudentID(studentID.toString());
      Object event = Event.builder().eventType(EventType.GET_STUDENT_ASSESSMENT_DETAILS).eventPayload(JsonUtil.getJsonStringFromObject(assessmentStudent)).build();
      val responseMessage = this.messagePublisher.requestMessage(TopicsEnum.EAS_API_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(event)).completeOnTimeout(null, 120, TimeUnit.SECONDS).get();
      if (responseMessage != null) {
        return objectMapper.readValue(responseMessage.getData(), refPenMatchResult);
      } else {
        throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT);
      }

    } catch (final Exception ex) {
      log.error("Error occurred calling GET GET_STUDENT_ASSESSMENT_DETAILS service :: " + ex.getMessage());
      Thread.currentThread().interrupt();
      throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT + ex.getMessage());
    }
  }

  private List<Session> getAssessmentSessions() {
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
