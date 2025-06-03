package ca.bc.gov.educ.graddatacollection.api;

import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.constants.SagaEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.SagaStatusEnum;
import ca.bc.gov.educ.graddatacollection.api.model.v1.*;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1.Assessment;
import ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1.Session;
import ca.bc.gov.educ.graddatacollection.api.struct.external.gradschools.v1.GradSchool;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.*;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.*;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@SpringBootTest(classes = {GradDataCollectionApiApplication.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class BaseGradDataCollectionAPITest {

  @BeforeEach
  public void before() {

  }

  @AfterEach
  public void resetState() {

  }

  public Session createMockSession() {
    LocalDateTime currentDate = LocalDateTime.now();

    var assessments = new ArrayList<Assessment>();
    var mockAssessment = createMockAssessment("LTE10");
    assessments.add(mockAssessment);

    return Session.builder()
            .sessionID(UUID.randomUUID().toString())
            .schoolYear(String.valueOf(currentDate.getYear()))
            .courseYear(Integer.toString(currentDate.getYear()))
            .courseMonth(Integer.toString(currentDate.getMonthValue()))
            .activeFromDate(currentDate.minusMonths(2).toString())
            .activeUntilDate(currentDate.plusMonths(2).toString())
            .assessments(assessments)
            .build();
  }

  public ReportingPeriodEntity createMockReportingPeriodEntity() {
    LocalDateTime currentDate = LocalDateTime.now();
    int currentMonthValue = currentDate.getMonthValue();
    int startingSchYear = currentMonthValue > 8 ? currentDate.getYear() : currentDate.getYear() - 1;

    LocalDateTime schYearStart = LocalDate.of(startingSchYear, 10, 8).atStartOfDay();
    LocalDateTime schYearEnd = LocalDate.of(startingSchYear + 1, 7, 20).atStartOfDay();
    LocalDateTime summerStart =  LocalDate.of(startingSchYear + 1, 8, 8).atStartOfDay();
    LocalDateTime summerEnd =  LocalDate.of(startingSchYear + 1, 9, 20).atStartOfDay();

    return ReportingPeriodEntity.builder()
            .reportingPeriodID(UUID.randomUUID())
            .schYrStart(schYearStart)
            .schYrEnd(schYearEnd)
            .summerStart(summerStart)
            .summerEnd(summerEnd)
            .periodStart(schYearStart)
            .periodEnd(summerEnd)
            .createDate(LocalDateTime.now().minusMonths(2))
            .updateDate(LocalDateTime.now().minusMonths(2))
            .createUser(ApplicationProperties.GRAD_DATA_COLLECTION_API)
            .updateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API)
            .build();
  }

  public Assessment createMockAssessment(String assessmentTypeCode) {
    return Assessment.builder()
            .sessionID(UUID.randomUUID().toString())
            .assessmentTypeCode(assessmentTypeCode)
            .createUser(ApplicationProperties.GRAD_DATA_COLLECTION_API)
            .createDate(LocalDateTime.now().toString())
            .updateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API)
            .updateDate(LocalDateTime.now().toString())
            .build();
  }

  public IncomingFilesetEntity createMockIncomingFilesetEntityWithDEMFile(UUID schoolID, ReportingPeriodEntity reportingPeriod) {

    return IncomingFilesetEntity.builder()
            .reportingPeriod(reportingPeriod)
            .schoolID(schoolID)
            .demFileUploadDate(LocalDateTime.now())
            .crsFileUploadDate(null)
            .xamFileUploadDate(null)
            .demFileName("Test.dem")
            .crsFileName(null)
            .xamFileName(null)
            .filesetStatusCode("LOADED")
            .build();
  }

  public IncomingFilesetEntity createMockIncomingFilesetEntityWithCRSFile(UUID schoolID, ReportingPeriodEntity reportingPeriod) {
    return IncomingFilesetEntity.builder()
            .reportingPeriod(reportingPeriod)
            .schoolID(schoolID)
            .demFileUploadDate(null)
            .crsFileUploadDate(LocalDateTime.now())
            .xamFileUploadDate(null)
            .demFileName(null)
            .crsFileName("Test.crs")
            .xamFileName(null)
            .filesetStatusCode("LOADED")
            .build();
  }

  public IncomingFilesetEntity createMockIncomingFilesetEntityWithAllFilesLoaded(ReportingPeriodEntity reportingPeriod) {
    return IncomingFilesetEntity.builder()
            .reportingPeriod(reportingPeriod)
            .schoolID(UUID.randomUUID())
            .demFileUploadDate(LocalDateTime.now())
            .crsFileUploadDate(LocalDateTime.now())
            .xamFileUploadDate(LocalDateTime.now())
            .demFileName("Test.dem")
            .crsFileName("Test.crs")
            .xamFileName("Test.xam")
            .filesetStatusCode("LOADED")
            .createUser(ApplicationProperties.GRAD_DATA_COLLECTION_API)
            .createDate(LocalDateTime.now())
            .updateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API)
            .updateDate(LocalDateTime.now())
            .build();
  }

  public ErrorFilesetStudentEntity createMockErrorFilesetStudentEntity(IncomingFilesetEntity incomingFileset) {
    return ErrorFilesetStudentEntity.builder()
            .incomingFileset(incomingFileset)
            .firstName("Jane")
            .lastName("Smith")
            .localID("123456789")
            .pen("123459987")
            .birthdate("19000101")
            .createUser(ApplicationProperties.GRAD_DATA_COLLECTION_API)
            .createDate(LocalDateTime.now())
            .updateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API)
            .updateDate(LocalDateTime.now())
            .build();
  }


  public DemographicStudentEntity createMockDemographicStudent(IncomingFilesetEntity incomingFileset) {
    return DemographicStudentEntity.builder()
            .demographicStudentID(UUID.randomUUID())
            .incomingFileset(incomingFileset)
            .pen("123456789")
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .createUser("ABC")
            .updateUser("ABC")
            .addressLine1("123 SOMEPLACE")
            .city("SOMEWHERE")
            .provincialCode("BC")
            .countryCode("CA")
            .postalCode("A1A1A1")
            .grade("08")
            .birthdate("19900101")
            .firstName("JIM")
            .lastName("JACKSON")
            .citizenship("C")
            .programCadreFlag("N")
            .studentStatusCode("LOADED")
            .localID("8887555")
            .transactionID("D02")
            .gradRequirementYear("2023")
            .programCode1("AA")
            .programCode2("AB")
            .programCode3("AC")
            .programCode4("FR")
            .programCode5("DD")
            .studentStatus("A")
            .build();
  }

  public CourseStudentEntity createMockCourseStudent(IncomingFilesetEntity incomingFileset) {
    return CourseStudentEntity.builder()
            .courseStudentID(UUID.randomUUID())
            .incomingFileset(incomingFileset)
            .pen("123456789")
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .createUser("ABC")
            .updateUser("ABC")
            .courseMonth("01")
            .courseYear("2024")
            .studentStatusCode("LOADED")
            .courseStatus("A")
            .lastName("JACKSON")
            .courseType("E")
            .courseDescription("COMP")
            .courseGraduationRequirement(null)
            .finalLetterGrade("A")
            .finalPercentage("92")
            .numberOfCredits("3")
            .interimPercentage("70")
            .interimLetterGrade("C+")
            .courseCode("PH")
            .courseLevel("12")
            .localID("8887555")
            .transactionID("E08")
            .build();
  }

  public AssessmentStudentEntity createMockAssessmentStudent() {
    ReportingPeriodEntity reportingPeriod = createMockReportingPeriodEntity();

    return AssessmentStudentEntity.builder()
            .assessmentStudentID(UUID.randomUUID())
            .incomingFileset(createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod))
            .assessmentID(UUID.randomUUID())
            .pen("123456789")
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .createUser("ABC")
            .updateUser("ABC")
            .courseMonth("01")
            .courseYear("2024")
            .studentStatusCode("LOADED")
            .courseStatus("A")
            .lastName("JACKSON")
            .localCourseID("123")
            .isElectronicExam("N")
            .courseCode("LTE10")
            .localID("8887555")
            .transactionID("E06")
            .examSchoolID(UUID.randomUUID())
            .build();
  }

  public StudentRuleData createMockStudentRuleData(final DemographicStudentEntity demographicStudentEntity, final CourseStudentEntity courseStudentEntity, final AssessmentStudentEntity assessmentStudent, final SchoolTombstone schoolTombstone) {
    final StudentRuleData studentRuleData = new StudentRuleData();
    studentRuleData.setSchool(schoolTombstone);
    studentRuleData.setDemographicStudentEntity(demographicStudentEntity);
    studentRuleData.setCourseStudentEntity(courseStudentEntity);
    studentRuleData.setAssessmentStudentEntity(assessmentStudent);
    return studentRuleData;
  }

  public SchoolTombstone createMockSchoolTombstone() {
    return SchoolTombstone.builder()
            .schoolId(UUID.randomUUID().toString())
            .mincode("123456")
            .schoolNumber("01001")
            .displayName("Mock School Tombstone 01001")
            .schoolOrganizationCode("QUARTER")
            .schoolCategoryCode("PUBLIC")
            .facilityTypeCode("STANDARD")
            .schoolReportingRequirementCode("REGULAR")
            .openedDate("2018-07-01 00:00:00.000")
            .closedDate(null)
            .build();
  }


  public SchoolTombstone createMockSchool() {
    final SchoolTombstone schoolTombstone = new SchoolTombstone();
    schoolTombstone.setSchoolId(UUID.randomUUID().toString());
    schoolTombstone.setDistrictId(UUID.randomUUID().toString());
    schoolTombstone.setDisplayName("Marco's school");
    schoolTombstone.setMincode("03636018");
    schoolTombstone.setOpenedDate("1964-09-01T00:00:00");
    schoolTombstone.setSchoolCategoryCode("PUBLIC");
    schoolTombstone.setSchoolReportingRequirementCode("REGULAR");
    schoolTombstone.setFacilityTypeCode("STANDARD");
    return schoolTombstone;
  }

  public GradSchool createMockGradSchool() {
    final GradSchool gradSchool = new GradSchool();
    gradSchool.setSchoolID(UUID.randomUUID().toString());
    gradSchool.setCanIssueTranscripts("Y");
    gradSchool.setCanIssueCertificates("Y");
    gradSchool.setSubmissionModeCode("Append");
    return gradSchool;
  }

  public School createMockSchoolDetail() {
    final School school = new School();
    school.setSchoolId(UUID.randomUUID().toString());
    school.setDistrictId(UUID.randomUUID().toString());
    school.setDisplayName("Marco's school");
    school.setMincode("03636018");
    school.setOpenedDate("1964-09-01T00:00:00");
    school.setSchoolCategoryCode("PUBLIC");
    school.setSchoolReportingRequirementCode("REGULAR");
    school.setFacilityTypeCode("STANDARD");

    var contactList = new ArrayList<SchoolContact>();
    SchoolContact contact1 = new SchoolContact();
    contact1.setEmail("abc@acb.com");
    contact1.setSchoolContactTypeCode("PRINCIPAL");
    contactList.add(contact1);
    school.setContacts(contactList);

    var gradesList = new ArrayList<SchoolGrade>();
    SchoolGrade grade1 = new SchoolGrade();
    grade1.setSchoolGradeCode("GRADE01");
    gradesList.add(grade1);
    school.setGrades(gradesList);
    return school;
  }
  public District createMockDistrict() {
    final District district = District.builder().build();
    district.setDistrictId(UUID.randomUUID().toString());
    district.setDisplayName("Marco's district");
    district.setDistrictNumber("036");
    district.setDistrictStatusCode("ACTIVE");
    district.setPhoneNumber("123456789");
    return district;
  }

  public IndependentAuthority createMockAuthority() {
    final IndependentAuthority independentAuthority = IndependentAuthority.builder().build();
    independentAuthority.setIndependentAuthorityId(UUID.randomUUID().toString());
    independentAuthority.setDisplayName("Marco's authority");
    independentAuthority.setAuthorityNumber("777");
    independentAuthority.setAuthorityTypeCode("INDEPENDNT");
    independentAuthority.setPhoneNumber("123456789");
    return independentAuthority;
  }

  @SneakyThrows
  protected GradSagaEntity createDemMockSaga(final DemographicStudent demographicStudent) {
    return GradSagaEntity.builder()
            .updateDate(LocalDateTime.now().minusMinutes(15))
            .createUser(ApplicationProperties.GRAD_DATA_COLLECTION_API)
            .updateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API)
            .createDate(LocalDateTime.now().minusMinutes(15))
            .sagaName(SagaEnum.PROCESS_DEM_STUDENTS_SAGA.toString())
            .status(SagaStatusEnum.IN_PROGRESS.toString())
            .sagaState(EventType.INITIATED.toString())
            .payload(JsonUtil.getJsonStringFromObject(DemographicStudentSagaData.builder().demographicStudent(demographicStudent).school(createMockSchool()).build()))
            .build();
  }

  @SneakyThrows
  protected GradSagaEntity createCourseMockSaga(final CourseStudent courseStudent) {
    return GradSagaEntity.builder()
            .updateDate(LocalDateTime.now().minusMinutes(15))
            .createUser(ApplicationProperties.GRAD_DATA_COLLECTION_API)
            .updateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API)
            .createDate(LocalDateTime.now().minusMinutes(15))
            .sagaName(SagaEnum.PROCESS_COURSE_STUDENTS_SAGA.toString())
            .status(SagaStatusEnum.IN_PROGRESS.toString())
            .sagaState(EventType.INITIATED.toString())
            .payload(JsonUtil.getJsonStringFromObject(CourseStudentSagaData.builder().courseStudent(courseStudent).school(createMockSchool()).build()))
            .build();
  }

  @SneakyThrows
  protected GradSagaEntity createUpdateCourseMockSaga(final CourseStudentUpdate courseStudentUpdate) {
    return GradSagaEntity.builder()
            .updateDate(LocalDateTime.now().minusMinutes(15))
            .createUser(ApplicationProperties.GRAD_DATA_COLLECTION_API)
            .updateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API)
            .createDate(LocalDateTime.now().minusMinutes(15))
            .sagaName(SagaEnum.PROCESS_COURSE_STUDENTS_SAGA.toString())
            .status(SagaStatusEnum.IN_PROGRESS.toString())
            .sagaState(EventType.INITIATED.toString())
            .payload(JsonUtil.getJsonStringFromObject(courseStudentUpdate))
            .build();
  }

  @SneakyThrows
  protected GradSagaEntity createAssessmentMockSaga(final AssessmentStudent assessmentStudent) {
    return GradSagaEntity.builder()
            .updateDate(LocalDateTime.now().minusMinutes(15))
            .createUser(ApplicationProperties.GRAD_DATA_COLLECTION_API)
            .updateUser(ApplicationProperties.GRAD_DATA_COLLECTION_API)
            .createDate(LocalDateTime.now().minusMinutes(15))
            .sagaName(SagaEnum.PROCESS_COURSE_STUDENTS_SAGA.toString())
            .status(SagaStatusEnum.IN_PROGRESS.toString())
            .sagaState(EventType.INITIATED.toString())
            .payload(JsonUtil.getJsonStringFromObject(AssessmentStudentSagaData.builder().assessmentStudent(assessmentStudent).school(createMockSchool()).build()))
            .build();
  }
}
