package ca.bc.gov.educ.graddatacollection.api;

import ca.bc.gov.educ.graddatacollection.api.constants.EventType;
import ca.bc.gov.educ.graddatacollection.api.constants.SagaEnum;
import ca.bc.gov.educ.graddatacollection.api.constants.SagaStatusEnum;
import ca.bc.gov.educ.graddatacollection.api.model.v1.*;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1.Assessment;
import ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1.Session;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.*;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.*;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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

  public IncomingFilesetEntity createMockIncomingFilesetEntityWithDEMFile(UUID schoolID) {
    return IncomingFilesetEntity.builder()
            .schoolID(schoolID)
            .demFileUploadDate(LocalDateTime.now())
            .crsFileUploadDate(null)
            .xamFileUploadDate(null)
            .demFileName("Test.stddem")
            .crsFileName(null)
            .xamFileName(null)
            .demFileStatusCode("LOADED")
            .crsFileStatusCode("NOTLOADED")
            .xamFileStatusCode("NOTLOADED")
            .filesetStatusCode("LOADED")
            .build();
  }

  public IncomingFilesetEntity createMockIncomingFilesetEntityWithCRSFile(UUID schoolID) {
    return IncomingFilesetEntity.builder()
            .schoolID(schoolID)
            .demFileUploadDate(null)
            .crsFileUploadDate(LocalDateTime.now())
            .xamFileUploadDate(null)
            .demFileName(null)
            .crsFileName("Test.stdcrs")
            .xamFileName(null)
            .demFileStatusCode("NOTLOADED")
            .crsFileStatusCode("LOADED")
            .xamFileStatusCode("NOTLOADED")
            .filesetStatusCode("LOADED")
            .build();
  }

  public IncomingFilesetEntity createMockIncomingFilesetEntityWithAllFilesLoaded() {
    return IncomingFilesetEntity.builder()
            .schoolID(UUID.randomUUID())
            .demFileUploadDate(LocalDateTime.now())
            .crsFileUploadDate(LocalDateTime.now())
            .xamFileUploadDate(LocalDateTime.now())
            .demFileName("Test.stddem")
            .crsFileName("Test.stdcrs")
            .xamFileName("Test.stdxam")
            .demFileStatusCode("LOADED")
            .crsFileStatusCode("LOADED")
            .xamFileStatusCode("LOADED")
            .filesetStatusCode("LOADED")
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
            .studentStatusCode("A")
            .localID("8887555")
            .transactionID("D02")
            .schoolCertificateCompletionDate("20240202")
            .gradRequirementYear("SCCP")
            .programCode1("AA")
            .programCode2("AB")
            .programCode3("AC")
            .programCode4("FR")
            .programCode5("DD")
            .build();
  }

  public CourseStudentEntity createMockCourseStudent() {
    return CourseStudentEntity.builder()
            .courseStudentID(UUID.randomUUID())
            .incomingFileset(createMockIncomingFilesetEntityWithAllFilesLoaded())
            .pen("123456789")
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .createUser("ABC")
            .updateUser("ABC")
            .courseMonth("01")
            .courseYear("2024")
            .studentStatusCode("ACTIVE")
            .courseStatus("A")
            .lastName("JACKSON")
            .courseType("BIG")
            .courseDescription("COMP")
            .courseGraduationRequirement("5")
            .finalGrade("15")
            .finalPercentage("40")
            .numberOfCredits("0")
            .interimPercentage("60")
            .courseCode("123")
            .localID("8887555")
            .transactionID("E08")
            .build();
  }

  public AssessmentStudentEntity createMockAssessmentStudent() {
    return AssessmentStudentEntity.builder()
            .assessmentStudentID(UUID.randomUUID())
            .incomingFileset(createMockIncomingFilesetEntityWithAllFilesLoaded())
            .assessmentID(UUID.randomUUID())
            .pen("123456789")
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .createUser("ABC")
            .updateUser("ABC")
            .courseMonth("01")
            .courseYear("2024")
            .studentStatusCode("ACTIVE")
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
            .payload(JsonUtil.getJsonStringFromObject(GradDemographicStudentSagaData.builder().demographicStudent(demographicStudent).school(createMockSchool()).build()))
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
            .payload(JsonUtil.getJsonStringFromObject(GradCourseStudentSagaData.builder().courseStudent(courseStudent).school(createMockSchool()).build()))
            .build();
  }
}
