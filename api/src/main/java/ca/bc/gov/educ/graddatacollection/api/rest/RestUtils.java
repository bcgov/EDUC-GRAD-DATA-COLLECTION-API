package ca.bc.gov.educ.graddatacollection.api.rest;

import ca.bc.gov.educ.graddatacollection.api.constants.EventOutcome;
import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.constants.TopicsEnum;
import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.exception.SagaRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ReportingPeriodEntity;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.struct.CHESEmail;
import ca.bc.gov.educ.graddatacollection.api.struct.Event;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CoregCoursesRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1.*;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.*;
import ca.bc.gov.educ.graddatacollection.api.struct.external.gradschools.v1.GradSchool;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.*;
import ca.bc.gov.educ.graddatacollection.api.struct.external.scholarships.v1.CitizenshipCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.scholarships.v1.StudentAddress;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.AssessmentStudent;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudent;
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
  private final Map<UUID, OptionalProgramCode> optionalProgramCodesMap = new ConcurrentHashMap<>();
  private final Map<String, ProgramRequirementCode> programRequirementCodeMap = new ConcurrentHashMap<>();
  private final Map<String, GraduationProgramCode> gradProgramCodeMap = new ConcurrentHashMap<>();
  private final Map<String, EquivalencyChallengeCode> equivalencyChallengeCodeMap = new ConcurrentHashMap<>();
  private final Map<String, GradCourseCode> coreg38Map = new ConcurrentHashMap<>();
  private final Map<String, GradCourseCode> coreg39Map = new ConcurrentHashMap<>();
  private final Map<String, GradExaminableCourse> examinableCourseMap = new ConcurrentHashMap<>();
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
  private final ReadWriteLock coreg38Lock = new ReentrantReadWriteLock();
  private final ReadWriteLock coreg39Lock = new ReentrantReadWriteLock();
  private final ReadWriteLock examinableCourseLock = new ReentrantReadWriteLock();
  private final Map<String, Session> sessionMap = new ConcurrentHashMap<>();
  @Getter
  private final ApplicationProperties props;

  @Value("${initialization.background.enabled}")
  private Boolean isBackgroundInitializationEnabled;

  private final Map<String, List<UUID>> independentAuthorityToSchoolIDMap = new ConcurrentHashMap<>();
  private final ReadWriteLock gradSchoolLock = new ReentrantReadWriteLock();
  private final Map<String, GradSchool> gradSchoolMap = new ConcurrentHashMap<>();

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
    this.populateGradSchoolMap();
    this.populateAssessmentSessionMap();
    this.populateCitizenshipCodesMap();
    this.populateGradGradesMap();
    this.populateLetterGradeMap();
    this.populateCareerProgramsMap();
    this.populateOptionalProgramsMap();
    this.populateProgramRequirementCodesMap();
    this.populateEquivalencyChallengeCodeMap();
    this.populateGradProgramCodesMap();
    this.populateCoreg38Map();
    this.populateCoreg39Map();
    this.populateExaminableCourseMap();
  }

  @Scheduled(cron = "${schedule.jobs.load.school.cron}")
  public void scheduled() {
    this.init();
  }

  public void populateCitizenshipCodesMap() {
    val writeLock = this.scholarshipsCitizenshipLock.writeLock();
    try {
      writeLock.lock();
      this.scholarshipsCitizenshipCodesMap.clear();
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
      this.schoolCategoryCodesMap.clear();
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
      this.facilityTypeCodesMap.clear();
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
      this.schoolMap.clear();
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

  public void populateGradSchoolMap() {
    val writeLock = this.gradSchoolLock.writeLock();
    try {
      writeLock.lock();
      this.gradSchoolMap.clear();
      for (val school : this.getGradSchools()) {
        this.gradSchoolMap.put(school.getSchoolID(), school);
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache grad-school {}", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("Loaded  {} grad-schools to memory", this.gradSchoolMap.values().size());
  }

  public void populateSchoolMincodeMap() {
    val writeLock = this.schoolLock.writeLock();
    try {
      writeLock.lock();
      this.schoolMincodeMap.clear();
      this.independentAuthorityToSchoolIDMap.clear();
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
      this.letterGradeMap.clear();
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
      this.gradGradeMap.clear();
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
      this.careerProgramCodesMap.clear();
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
      this.optionalProgramCodesMap.clear();
      for (val program : this.getOptionalPrograms()) {
        this.optionalProgramCodesMap.put(program.getOptionalProgramID(), program);
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
      this.programRequirementCodeMap.clear();
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
      this.gradProgramCodeMap.clear();
      for (val program : this.getGraduationProgramCodes()) {
        program.setEffectiveDate(!StringUtils.isBlank(program.getEffectiveDate()) ? LocalDateTime.parse(program.getEffectiveDate(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toString() : null);
        program.setExpiryDate(!StringUtils.isBlank(program.getExpiryDate()) ? LocalDateTime.parse(program.getExpiryDate(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toString() : null);
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
      this.equivalencyChallengeCodeMap.clear();
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

  public void populateCoreg38Map() {
    val writeLock = this.coreg38Lock.writeLock();
    try {
      writeLock.lock();
      if (this.coreg38Map.isEmpty()) {
        log.info("Calling COREG API to load coreg38 courses to memory");
        this.coreg38Map.clear();
        var coreg38Courses = this.getCoreg38Courses();
        for (val courseCode : coreg38Courses) {
          this.coreg38Map.put(courseCode.getCourseID(), courseCode);
        }
        log.info("Loaded  {} coreg38 courses to memory", coreg38Courses.size());
      } else {
        log.debug("Coreg38 map already populated by another thread, skipping reload");
      }
    } catch (Exception ex) {
      log.error("Unable to load coreg38 courses to map cache ", ex);
    } finally {
      writeLock.unlock();
    }
  }

  public void populateCoreg39Map() {
    val writeLock = this.coreg39Lock.writeLock();
    try {
      writeLock.lock();
      if (this.coreg39Map.isEmpty()) {
        log.info("Calling COREG API to load coreg39 courses to memory");
        this.coreg39Map.clear();
        var coreg39Courses = this.getCoreg39Courses();
        for (val courseCode : coreg39Courses) {
          this.coreg39Map.put(courseCode.getCourseID(), courseCode);
        }
        log.info("Loaded  {} coreg39 courses to memory", coreg39Courses.size());
      } else {
        log.debug("Coreg39 map already populated by another thread, skipping reload");
      }
    } catch (Exception ex) {
      log.error("Unable to load coreg39 courses to map cache ", ex);
    } finally {
      writeLock.unlock();
    }
  }

  public void populateExaminableCourseMap() {
    val writeLock = this.examinableCourseLock.writeLock();
    try {
      writeLock.lock();
      this.examinableCourseMap.clear();
      for (val examinableCourse : this.getGradExaminableCourses()) {
        this.examinableCourseMap.put(String.valueOf(examinableCourse.getExaminableCourseID()), examinableCourse);
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache grad examinable courses ", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("Loaded  {} grad examinable courses to memory", this.examinableCourseMap.values().size());
  }

  public List<GradExaminableCourse> getGradExaminableCourses() {
    log.info("Calling GRAD COURSE API to load examinable courses to memory");
    return this.webClient.get()
            .uri(this.props.getGradCourseApiURL() + "/examinablecourses")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(GradExaminableCourse.class)
            .collectList()
            .block();
  }

  public List<GradCourseCode> getCoreg38Courses() {
    log.info("Calling COREG API to load courses to memory");
    return this.webClient.get()
            .uri(this.props.getCoregApiURL() + "/all/38")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(GradCourseCode.class)
            .collectList()
            .block();
  }

  public List<GradCourseCode> getCoreg39Courses() {
    log.info("Calling COREG API to load courses to memory");
    return this.webClient.get()
            .uri(this.props.getCoregApiURL() + "/all/39")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(GradCourseCode.class)
            .collectList()
            .block();
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

  public List<GraduationProgramCode> getGraduationProgramCodeList(boolean activeOnly) {
    if (this.gradProgramCodeMap.isEmpty()) {
      log.info("Graduation Program Code map is empty reloading them");
      this.populateGradProgramCodesMap();
    }
    if(activeOnly){
      return this.gradProgramCodeMap.values().stream().filter(code -> StringUtils.isBlank(code.getExpiryDate()) || LocalDateTime.parse(code.getExpiryDate()).isAfter(LocalDateTime.now())).toList();
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
      log.info("Grad Grade map is empty, reloading them");
      this.populateGradGradesMap();
    }
    if (activeOnly) {
      return this.gradGradeMap.values().stream()
              .filter(code ->
                      StringUtils.isBlank(code.getExpiryDate()) ||
                              LocalDateTime.parse(code.getExpiryDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                      .isAfter(LocalDateTime.now())
              ).toList();
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

  public List<LetterGrade> getLetterGradeList(LocalDateTime sessionDate) {
    if (this.letterGradeMap.isEmpty()) {
      log.info("Letter Grade map is empty reloading them");
      this.populateLetterGradeMap();
    }
    if (sessionDate != null) {
      return this.letterGradeMap.values().stream()
      .filter(code ->
        !LocalDateTime.parse(code.getEffectiveDate(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).isAfter(sessionDate)
          && (StringUtils.isBlank(code.getExpiryDate())
          || LocalDateTime.parse(code.getExpiryDate(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).isAfter(sessionDate))
      )
        .toList();
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

  public List<GradSchool> getGradSchools() {
    log.info("Calling Grad schools api to load schools to memory");
    return this.webClient.get()
            .uri(this.props.getGradSchoolApiURL())
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(GradSchool.class)
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
      this.districtMap.clear();
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

  public List<SchoolTombstone> getAllSchools() {
    if (this.schoolMap.isEmpty()) {
      log.info("School map is empty reloading schools");
      this.populateSchoolMap();
    }
    return this.schoolMap.values().stream().toList();
  }

  public Optional<GradSchool> getGradSchoolBySchoolID(final String schoolID) {
    if (this.gradSchoolMap.isEmpty()) {
      log.info("Grad School map is empty reloading schools");
      this.populateGradSchoolMap();
    }
    return Optional.ofNullable(this.gradSchoolMap.get(schoolID));
  }

  public Optional<SchoolTombstone> getSchoolByMincode(final String mincode) {
    if (this.schoolMincodeMap.isEmpty()) {
      log.info("School mincode map is empty reloading schools");
      this.populateSchoolMincodeMap();
    }
    return Optional.ofNullable(this.schoolMincodeMap.get(mincode));
  }

  public Optional<GradCourseCode> getCoreg38CourseByID(final String courseID) {
    if (this.coreg38Map.isEmpty()) {
      log.info("Coreg 38 course map is empty reloading courses");
      this.populateCoreg38Map();
    }
    return Optional.ofNullable(this.coreg38Map.get(courseID));
  }

  public Optional<GradCourseCode> getCoreg39CourseByID(final String courseID) {
    if (this.coreg39Map.isEmpty()) {
      log.info("Coreg 39 course map is empty reloading courses");
      this.populateCoreg39Map();
    }
    return Optional.ofNullable(this.coreg39Map.get(courseID));
  }

  public List<GradExaminableCourse> getExaminableCourseByExternalID(final String externalID) {
    if (this.examinableCourseMap.isEmpty()) {
      log.info("Examinable course map is empty reloading courses");
      this.populateExaminableCourseMap();
    }
    List<GradExaminableCourse> examinableCourses = new ArrayList<>();
    this.examinableCourseMap.forEach((key, value) -> {
        if (StringUtils.equals(String.format("%-5s", value.getCourseCode()) + value.getCourseLevel(), externalID)) {
            examinableCourses.add(value);
        }
    });
    return examinableCourses;
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
          log.debug("Empty response data for getStudentByPEN; treating as student not found for PEN: {}", assignedPEN);
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
      log.debug("Entity Not Found occurred calling GET STUDENT service :: {}", ex.getMessage());
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
      Object event = Event.builder().sagaId(correlationID).eventType(EventType.GET_GRAD_STUDENT_RECORD).eventPayload(studentID.toString()).build();
      val responseMessage = this.messagePublisher.requestMessage(TopicsEnum.GRAD_STUDENT_API_FETCH_GRAD_STUDENT_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(event)).completeOnTimeout(null, 120, TimeUnit.SECONDS).get();
      if (responseMessage != null) {
        String responseData = new String(responseMessage.getData(), StandardCharsets.UTF_8);

        final TypeReference<GradStudentRecord> refGradStudentRecordResult = new TypeReference<>() {
        };
        GradStudentRecord response = objectMapper.readValue(responseData, refGradStudentRecordResult);

        log.debug("getGradStudentRecordByStudentID response{}", response.toString());

        if ("not found".equals(response.getException())) {
          log.debug("A not found error occurred while fetching GradStudentRecord for Student ID {}", studentID);
          throw new EntityNotFoundException(GradStudentRecord.class);
        } else if ("error".equals(response.getException())) {
          log.error("An exception error occurred while fetching GradStudentRecord for Student ID {}", studentID);
          throw new GradDataCollectionAPIRuntimeException("Error occurred while processing the request for correlation ID " + correlationID);
        }

        log.debug("Success fetching GradStudentRecord for Student ID {}", studentID);
        return response;
      } else {
        throw new GradDataCollectionAPIRuntimeException(NO_RESPONSE_RECEIVED_WITHIN_TIMEOUT_FOR_CORRELATION_ID + correlationID);
      }

    } catch (EntityNotFoundException ex) {
      log.debug("Entity Not Found occurred calling GET GRAD STUDENT RECORD service :: {}", ex.getMessage());
      throw ex;
    } catch (final Exception ex) {
      log.error("Error occurred calling GET GRAD STUDENT RECORD service :: {}", ex.getMessage());
      Thread.currentThread().interrupt();
      throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT + correlationID);
    }
  }

  @Retryable(retryFor = {Exception.class}, noRetryFor = {EntityNotFoundException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public List<GradStudentCourseRecord> getGradStudentCoursesByStudentID(UUID correlationID, String studentID) {
    try {
      Event event = Event.builder().sagaId(correlationID).eventType(EventType.GET_GRAD_STUDENT_COURSE_RECORDS).eventPayload(studentID).build();
      val responseMessage = this.messagePublisher.requestMessage(TopicsEnum.GRAD_STUDENT_API_FETCH_GRAD_STUDENT_COURSES_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(event)).completeOnTimeout(null, 120, TimeUnit.SECONDS).get();

      if (responseMessage != null) {
        String responseData = new String(responseMessage.getData(), StandardCharsets.UTF_8);

        final TypeReference<GradStudentCourseRecordsPayload> payloadTypeRef = new TypeReference<>() {};
        GradStudentCourseRecordsPayload responsePayload = objectMapper.readValue(responseData, payloadTypeRef);

        log.debug("getGradStudentCourseRecordsByStudentID response{}", responsePayload.toString());

        if (responsePayload.getException() != null) {
          if ("not found".equals(responsePayload.getException())) {
            throw new EntityNotFoundException(GradStudentRecord.class);
          } else {
            log.error("An exception error occurred: {}", responsePayload.getException());
            throw new GradDataCollectionAPIRuntimeException("Error occurred for correlation ID " + correlationID);
          }
        }

        log.debug("Success fetching GradStudentCoursesRecord for Student ID {}", studentID);
        return responsePayload.getCourses();
      } else {
        throw new GradDataCollectionAPIRuntimeException(NO_RESPONSE_RECEIVED_WITHIN_TIMEOUT_FOR_CORRELATION_ID + correlationID);
      }
    } catch (EntityNotFoundException ex) {
      log.error("EntityNotFoundException occurred calling GET_STUDENT_COURSE service :: {}", ex.getMessage());
      throw new EntityNotFoundException();
    } catch (final Exception ex) {
      log.error("Error occurred calling GET_STUDENT_COURSE service :: {}", ex.getMessage());
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
        log.debug("No course information found for externalID {}", externalID);
        throw new EntityNotFoundException(CoregCoursesRecord.class);
      }

      log.debug("Received response from NATS: {}", new String(responseData, StandardCharsets.UTF_8));
      return objectMapper.readValue(responseData, refCourseInformation);

    } catch (EntityNotFoundException ex) {
      log.debug("EntityNotFoundException occurred calling GET_COURSE_FROM_EXTERNAL_ID service :: {}", ex.getMessage());
      throw new EntityNotFoundException();
    } catch (final Exception ex) {
      log.error("Error occurred calling GET_COURSE_FROM_EXTERNAL_ID service :: {}", ex.getMessage());
      Thread.currentThread().interrupt();
      throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT + correlationID);
    }
  }

  public void populateAssessmentSessionMap() {
    val writeLock = this.assessmentSessionLock.writeLock();
    try {
      writeLock.lock();
      this.sessionMap.clear();
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
      val responseMessage = this.messagePublisher.requestMessage(TopicsEnum.STUDENT_ASSESSMENT_API_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(event)).completeOnTimeout(null, 120, TimeUnit.SECONDS).get();
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

  @Retryable(retryFor = {Exception.class}, noRetryFor = {SagaRuntimeException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public EasEvent writeAssessmentStudentDetailInAssessmentService(AssessmentStudent student, String assessmentID, SchoolTombstone schoolTombstone, String gradeAtRegistration) {
    try {
      final TypeReference<EasEvent> eventResult = new TypeReference<>() {
      };

      var studFromAPI = getStudentByPEN(UUID.randomUUID(), student.getPen());

      var assessmentStudent = new EASAssessmentStudent();
      assessmentStudent.setAssessmentStudentID(null);
      assessmentStudent.setAssessmentID(assessmentID);
      assessmentStudent.setSchoolOfRecordSchoolID(schoolTombstone.getSchoolId());
      assessmentStudent.setAssessmentCenterSchoolID(student.getExamSchoolID());
      assessmentStudent.setStudentID(studFromAPI.getStudentID());
      assessmentStudent.setGivenName(studFromAPI.getLegalFirstName());
      assessmentStudent.setSurname(student.getLastName());
      assessmentStudent.setPen(student.getPen());
      assessmentStudent.setLocalID(student.getLocalID());
      assessmentStudent.setGradeAtRegistration(gradeAtRegistration);
      assessmentStudent.setLocalAssessmentID(student.getLocalCourseID());
      assessmentStudent.setIsElectronicAssessment(StringUtils.isNotBlank(student.getIsElectronicExam()) && student.getIsElectronicExam().equalsIgnoreCase("Y") ? "true" : "false");
      assessmentStudent.setProficiencyScore(null);
      assessmentStudent.setProvincialSpecialCaseCode(null);
      assessmentStudent.setCourseStatusCode(student.getCourseStatus());
      assessmentStudent.setNumberOfAttempts(null);
      assessmentStudent.setCreateUser(student.getCreateUser());
      assessmentStudent.setUpdateUser(student.getCreateUser());
      assessmentStudent.setCreateDate(null);
      assessmentStudent.setUpdateDate(null);

      log.info("Assessment Student Detail: " + assessmentStudent);

      Object event = Event.builder().eventType(EventType.PROCESS_STUDENT_REGISTRATION).eventPayload(JsonUtil.getJsonStringFromObject(assessmentStudent)).build();
      val responseMessage = this.messagePublisher.requestMessage(TopicsEnum.STUDENT_ASSESSMENT_API_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(event)).completeOnTimeout(null, 120, TimeUnit.SECONDS).get();
      if (responseMessage != null) {
        return objectMapper.readValue(responseMessage.getData(), eventResult);
      } else {
        throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT);
      }

    } catch (final Exception ex) {
      log.error("Error occurred calling PROCESS_STUDENT_REGISTRATION service :: " + ex.getMessage());
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
      val responseMessage = this.messagePublisher.requestMessage(TopicsEnum.STUDENT_ASSESSMENT_API_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(event)).completeOnTimeout(null, 60, TimeUnit.SECONDS).get();
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

  @Retryable(retryFor = {Exception.class}, noRetryFor = {SagaRuntimeException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public GradStatusEvent writeDEMStudentRecordInGrad(DemographicStudent student, SchoolTombstone schoolTombstone, ReportingPeriodEntity reportingPeriod) {
    try {
      final TypeReference<GradStatusEvent> eventResult = new TypeReference<>() {
      };
      LocalDateTime now = LocalDateTime.now();
      var isSummer =  (now.isEqual(reportingPeriod.getSummerStart()) || now.isAfter(reportingPeriod.getSummerStart()))
              && (now.isEqual(reportingPeriod.getSummerEnd()) || now.isBefore(reportingPeriod.getSummerEnd()));

      var incomingGrade = StringUtils.isNumeric(student.getGrade())  && student.getGrade().length() == 1
              ? "0" + student.getGrade()
              : student.getGrade();

      var demStudent = new GradDemographicStudent();
      demStudent.setMincode(schoolTombstone.getMincode());
      demStudent.setSchoolID(schoolTombstone.getSchoolId());
      demStudent.setSchoolReportingRequirementCode(schoolTombstone.getSchoolReportingRequirementCode());
      demStudent.setBirthdate(student.getBirthdate());
      demStudent.setPen(student.getPen());
      demStudent.setCitizenship(student.getCitizenship());
      demStudent.setGrade(incomingGrade);
      demStudent.setProgramCode1(student.getProgramCode1());
      demStudent.setProgramCode2(student.getProgramCode2());
      demStudent.setProgramCode3(student.getProgramCode3());
      demStudent.setProgramCode4(student.getProgramCode4());
      demStudent.setProgramCode5(student.getProgramCode5());
      demStudent.setGradRequirementYear(student.getGradRequirementYear());
      demStudent.setSchoolCertificateCompletionDate(student.getSchoolCertificateCompletionDate());
      demStudent.setStudentStatus(student.getStudentStatus());
      demStudent.setIsSummerCollection(isSummer ? "Y" : "N");
      demStudent.setCreateUser(student.getCreateUser());
      demStudent.setUpdateUser(student.getCreateUser());
      demStudent.setCreateDate(student.getCreateDate());
      demStudent.setUpdateDate(student.getUpdateDate());
      demStudent.setVendorID(student.getVendorID());

      log.debug("DEM Student Detail:: {}", demStudent);

      Object event = Event.builder().eventType(EventType.PROCESS_STUDENT_DEM_DATA).eventPayload(JsonUtil.getJsonStringFromObject(demStudent)).build();
      val responseMessage = this.messagePublisher.requestMessage(TopicsEnum.GRAD_STUDENT_API_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(event)).completeOnTimeout(null, 120, TimeUnit.SECONDS).get();
      if (responseMessage != null) {
        return objectMapper.readValue(responseMessage.getData(), eventResult);
      } else {
        throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT);
      }

    } catch (final Exception ex) {
      log.error("Error occurred calling PROCESS_STUDENT_DEM_DATA service :: {}", ex.getMessage());
      Thread.currentThread().interrupt();
      throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT + ex.getMessage());
    }
  }

  @Retryable(retryFor = {Exception.class}, noRetryFor = {SagaRuntimeException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public Event writeStudentAddressToScholarships(DemographicStudent student, String studentID) {
    try {
      final TypeReference<Event> eventResult = new TypeReference<>() {
      };

      StudentAddress address = new StudentAddress();
      address.setStudentID(studentID);
      address.setAddressLine1(student.getAddressLine1());
      address.setAddressLine2(student.getAddressLine2());
      address.setCity(student.getCity());
      address.setPostalZip(student.getPostalCode());
      address.setProvinceStateCode(student.getProvincialCode());
      address.setCountryCode(student.getCountryCode());
      address.setCreateUser(student.getCreateUser());
      address.setUpdateUser(student.getUpdateUser());
      
      log.debug("DEM address detail:: {}", address);

      Object event = Event.builder().eventType(EventType.UPDATE_STUDENT_SCHOLARSHIPS_ADDRESS).eventPayload(JsonUtil.getJsonStringFromObject(address)).build();
      val responseMessage = this.messagePublisher.requestMessage(TopicsEnum.SCHOLARSHIPS_API_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(event)).completeOnTimeout(null, 120, TimeUnit.SECONDS).get();
      if (responseMessage != null) {
        return objectMapper.readValue(responseMessage.getData(), eventResult);
      } else {
        throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT);
      }

    } catch (final Exception ex) {
      log.error("Error occurred calling UPDATE_STUDENT_SCHOLARSHIPS_ADDRESS service :: {}", ex.getMessage());
      Thread.currentThread().interrupt();
      throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT + ex.getMessage());
    }
  }
  
  @Retryable(retryFor = {Exception.class}, noRetryFor = {SagaRuntimeException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public GradStatusEvent writeCRSStudentRecordInGrad(List<CourseStudentEntity> courseStudentEntities, String pen, String schoolID, ReportingPeriodEntity reportingPeriod) {
    try {
      final TypeReference<GradStatusEvent> eventResult = new TypeReference<>() {
      };

      List<GradCourseStudentDetail> studentList = new ArrayList<>();
      var student = new GradCourseStudent();

      var gradSchool = getGradSchoolBySchoolID(schoolID);
      LocalDateTime now = LocalDateTime.now();
      var isSummer =  (now.isEqual(reportingPeriod.getSummerStart()) || now.isAfter(reportingPeriod.getSummerStart()))
              && (now.isEqual(reportingPeriod.getSummerEnd()) || now.isBefore(reportingPeriod.getSummerEnd()));

      student.setPen(pen);
      student.setIsSummerCollection(isSummer ? "Y" : "N");
      student.setSubmissionModeCode(gradSchool.get().getSubmissionModeCode());

      courseStudentEntities.forEach(courseStudentEntity -> {
        var courseStudent = new GradCourseStudentDetail();
        courseStudent.setPen(courseStudentEntity.getPen());
        courseStudent.setCourseCode(courseStudentEntity.getCourseCode());
        courseStudent.setCourseLevel(courseStudentEntity.getCourseLevel());
        courseStudent.setCourseYear(courseStudentEntity.getCourseYear());
        courseStudent.setCourseMonth(courseStudentEntity.getCourseMonth());
        courseStudent.setInterimPercentage(courseStudentEntity.getInterimPercentage());
        courseStudent.setInterimLetterGrade(courseStudentEntity.getInterimLetterGrade());
        courseStudent.setFinalPercentage(courseStudentEntity.getFinalPercentage());
        courseStudent.setFinalLetterGrade(courseStudentEntity.getFinalLetterGrade());
        courseStudent.setCourseStatus(courseStudentEntity.getCourseStatus());
        courseStudent.setNumberOfCredits(courseStudentEntity.getNumberOfCredits() != null ? courseStudentEntity.getNumberOfCredits() : "0");
        courseStudent.setRelatedCourse(courseStudentEntity.getRelatedCourse());
        courseStudent.setRelatedLevel(courseStudentEntity.getRelatedLevel());
        courseStudent.setCourseDescription(courseStudentEntity.getCourseDescription());
        courseStudent.setCourseType(courseStudentEntity.getCourseType());
        courseStudent.setCourseGraduationRequirement(courseStudentEntity.getCourseGraduationRequirement());
        courseStudent.setCreateUser(courseStudentEntity.getCreateUser());
        courseStudent.setUpdateUser(courseStudentEntity.getCreateUser());
        courseStudent.setCreateDate(courseStudentEntity.getCreateDate().toString());
        courseStudent.setUpdateDate(courseStudentEntity.getUpdateDate().toString());

        studentList.add(courseStudent);
      });
      student.getStudentDetails().addAll(studentList);

      Object event = Event.builder().eventType(EventType.PROCESS_STUDENT_COURSE_DATA).eventPayload(
              this.objectMapper.writeValueAsString(student)).build();
      val responseMessage = this.messagePublisher.requestMessage(TopicsEnum.GRAD_STUDENT_API_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(event)).completeOnTimeout(null, 120, TimeUnit.SECONDS).get();
      if (responseMessage != null) {
        return objectMapper.readValue(responseMessage.getData(), eventResult);
      } else {
        throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT);
      }

    } catch (final Exception ex) {
      log.error("Error occurred calling PROCESS_STUDENT_COURSE_DATA service :: {}", ex.getMessage());
      Thread.currentThread().interrupt();
      throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT + ex.getMessage());
    }
  }

  /**
   * Get school from Institute API.
   *
   */
  @Retryable(retryFor = {Exception.class}, noRetryFor = {GradDataCollectionAPIRuntimeException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public School getSchoolFromSchoolID(UUID schoolID, UUID correlationID) {
    try {

      final TypeReference<School> ref = new TypeReference<>() {
      };
      val event = Event.builder().sagaId(correlationID).eventType(EventType.GET_SCHOOL).eventPayload(String.valueOf(schoolID)).build();
      val responseMessage = this.messagePublisher.requestMessage(TopicsEnum.INSTITUTE_API_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(event)).completeOnTimeout(null, 60, TimeUnit.SECONDS).get();
      if (responseMessage != null) {
        return objectMapper.readValue(responseMessage.getData(), ref);
      } else {
        throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT + correlationID);
      }

    } catch (final Exception ex) {
      Thread.currentThread().interrupt();
      throw new GradDataCollectionAPIRuntimeException(NATS_TIMEOUT + correlationID + ex.getMessage());
    }
  }

  /**
   * Update school in Institute API.
   *
   */
  @Retryable(retryFor = {Exception.class}, noRetryFor = {GradDataCollectionAPIRuntimeException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public InstituteStatusEvent updateSchool(School school, UUID correlationID) {
    try {

      final TypeReference<InstituteStatusEvent> ref = new TypeReference<>() {
      };

      val event = Event.builder().sagaId(correlationID).eventType(EventType.UPDATE_SCHOOL).eventPayload(this.objectMapper.writeValueAsString(school)).build();
      val responseMessage = this.messagePublisher.requestMessage(TopicsEnum.INSTITUTE_API_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(event)).completeOnTimeout(null, 60, TimeUnit.SECONDS).get();
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

