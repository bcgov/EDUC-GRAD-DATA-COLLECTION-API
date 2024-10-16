package ca.bc.gov.educ.graddatacollection.api;

import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.*;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
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

  public DemographicStudentEntity createMockDemographicStudent() {
    return DemographicStudentEntity.builder()
            .demographicStudentID(UUID.randomUUID())
            .incomingFilesetID(UUID.randomUUID())
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
            .studentStatusCode("ACTIVE")
            .localID("8887555")
            .transactionID("D02")
            .build();
  }

  public CourseStudentEntity createMockCourseStudent() {
    return CourseStudentEntity.builder()
            .courseStudentID(UUID.randomUUID())
            .incomingFilesetID(UUID.randomUUID())
            .pen("123456789")
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .createUser("ABC")
            .updateUser("ABC")
            .courseMonth("01")
            .courseYear("2024")
            .studentStatusCode("ACTIVE")
            .courseStatus("ACTIVE")
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
            .incomingFilesetID(UUID.randomUUID())
            .assessmentID(UUID.randomUUID())
            .pen("123456789")
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .createUser("ABC")
            .updateUser("ABC")
            .courseMonth("01")
            .courseYear("2024")
            .studentStatusCode("ACTIVE")
            .courseStatus("ACTIVE")
            .lastName("JACKSON")
            .localCourseID("123")
            .isElectronicExam("N")
            .courseCode("LTE10")
            .provincialSpecialCase("N/A")
            .localID("8887555")
            .transactionID("E06")
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
}
