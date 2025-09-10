package ca.bc.gov.educ.graddatacollection.api.rules;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.*;
import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.CourseStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ReportingPeriodRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentRulesProcessor;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.*;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.graddatacollection.api.struct.external.scholarships.v1.CitizenshipCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
class DemographicRulesProcessorTest extends BaseGradDataCollectionAPITest {

    @Autowired
    private DemographicStudentRulesProcessor rulesProcessor;

    @Autowired
    private IncomingFilesetRepository incomingFilesetRepository;

    @Autowired
    private CourseStudentRepository courseStudentRepository;

    @Autowired
    private ReportingPeriodRepository reportingPeriodRepository;

    @MockBean
    private RestUtils restUtils;

    @BeforeEach
    void setUp() {
        when(restUtils.getScholarshipsCitizenshipCodeList()).thenReturn(
                List.of(
                        new CitizenshipCode("C", "Canadian", "Valid Citizenship Code", 1, "2020-01-01", "2099-12-31"),
                        new CitizenshipCode("O", "Other", "Valid Citizenship Code", 2, "2020-01-01", "2099-12-31"),
                        new CitizenshipCode("", "Blank", "Valid for Blank Citizenship", 3, "2020-01-01", "2099-12-31")
                )
        );
        when(restUtils.getGradGradeList(true)).thenReturn(
                List.of(
                        new GradGrade("KH", "KH", "", 1, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "N", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new GradGrade("01", "01", "", 1, "2020-01-01T00:00:00", null, "Y", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new GradGrade("08", "Grade 8", "", 1, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "8", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new GradGrade("09", "Grade 9", "", 2, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "9", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new GradGrade("10", "Grade 10", "", 3, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "10", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new GradGrade("11", "Grade 11", "", 4, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "11", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new GradGrade("12", "Grade 12", "", 5, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "12", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new GradGrade("AD", "Adult", "", 6, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "AD", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new GradGrade("AN", "Adult Non-Graduate", "", 7, "2020-01-01T00:00:00","2099-12-31T23:59:59", "AN", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new GradGrade("HS", "Home School", "", 8, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "HS", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new GradGrade("SU", "Secondary Ungraded", "", 9, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "SU", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new GradGrade("GA", "Graduated Adult", "", 10, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "GA", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
                )
        );
        when(restUtils.getCareerProgramCodeList()).thenReturn(
                List.of(
                        new CareerProgramCode("AA", "Art Careers", "", 1, "20200101", "20990101"),
                        new CareerProgramCode("AB", "Autobody", "", 2, "20200101", "20990101"),
                        new CareerProgramCode("AC", "Agribusiness", "", 3, "20200101", "20990101")
                )
        );
        when(restUtils.getOptionalProgramCodeList()).thenReturn(
                List.of(
                        new OptionalProgramCode(UUID.randomUUID(), "FR", "SCCP French Certificate", "", 1, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new OptionalProgramCode(UUID.randomUUID(), "AD", "Advanced Placement", "", 2, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new OptionalProgramCode(UUID.randomUUID(), "DD", "Dual Dogwood", "", 3, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
                )
        );
        when(restUtils.getProgramRequirementCodeList()).thenReturn(
                List.of(
                        new ProgramRequirementCode("1950", "Adult Graduation Program", "Description for 1950", RequirementTypeCode.builder().reqTypeCode("REQ_TYPE").expiryDate(Date.valueOf("2222-01-01")).build(), "4", "Not met description", "12", "English", "Y", "CATEGORY", "1", "A", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new ProgramRequirementCode("2023", "B.C. Graduation Program", "Description for 2023", RequirementTypeCode.builder().reqTypeCode("REQ_TYPE").expiryDate(Date.valueOf("2222-01-01")).build(), "4", "Not met description", "12", "English", "Y", "CATEGORY", "2", "B", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new ProgramRequirementCode("2018", "B.C. Graduation Program 2018", "Description for 2018", RequirementTypeCode.builder().reqTypeCode("REQ_TYPE").expiryDate(Date.valueOf("2222-01-01")).build(), "4", "Not met description", "12", "English", "Y", "CATEGORY", "3", "C", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new ProgramRequirementCode("2004", "B.C. Graduation Program 2004", "Description for 2004", RequirementTypeCode.builder().reqTypeCode("REQ_TYPE").expiryDate(Date.valueOf("2222-01-01")).build(), "4", "Not met description", "12", "English", "Y", "CATEGORY", "4", "D", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new ProgramRequirementCode("1996", "B.C. Graduation Program 1996", "Description for 1996", RequirementTypeCode.builder().reqTypeCode("REQ_TYPE").expiryDate(Date.valueOf("2222-01-01")).build(), "4", "Not met description", "12", "English", "Y", "CATEGORY", "5", "E", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new ProgramRequirementCode("1986", "B.C. Graduation Program 1986", "Description for 1986", RequirementTypeCode.builder().reqTypeCode("REQ_TYPE").expiryDate(Date.valueOf("2222-01-01")).build(), "4", "Not met description", "12", "English", "Y", "CATEGORY", "6", "F", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new ProgramRequirementCode("SCCP", "School Completion Certificate Program", "Description for SCCP", RequirementTypeCode.builder().reqTypeCode("REQ_TYPE").expiryDate(Date.valueOf("2222-01-01")).build(), "4", "Not met description", "12", "English", "Y", "CATEGORY", "7", "G", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new ProgramRequirementCode("EXP", "Expired Program", "Description for Expired", RequirementTypeCode.builder().reqTypeCode("REQ_TYPE").expiryDate(Date.valueOf("2003-01-01")).build(), "4", "Not met description", "12", "English", "Y", "CATEGORY", "7", "G", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
                )
        );
        when(restUtils.getGraduationProgramCodeList(true)).thenReturn(
                List.of(
                        new GraduationProgramCode("1950", "Adult Graduation Program", "Description for 1950", 4, LocalDate.now().toString(), null, "associatedCred"),
                        new GraduationProgramCode("2023", "B.C. Graduation Program", "Description for 2023", 4, LocalDate.now().toString(), null, "associatedCred"),
                        new GraduationProgramCode("2023-EN", "B.C. Graduation Program", "Description for 2023", 4, LocalDate.now().toString(), null, "associatedCred"),
                        new GraduationProgramCode("SCCP", "School Completion Certificate Program", "Description for SCCP", 4, LocalDate.now().toString(), null, "associatedCred")
                )
        );
        when(restUtils.getGraduationProgramCodeList(false)).thenReturn(
                List.of(
                        new GraduationProgramCode("1950", "Adult Graduation Program", "Description for 1950", 4, LocalDate.now().toString(), null , "associatedCred"),
                        new GraduationProgramCode("2023", "B.C. Graduation Program", "Description for 2023", 4, LocalDate.now().toString(), null, "associatedCred"),
                        new GraduationProgramCode("2018-EN", "B.C. Graduation Program 2018", "Description for 2018", 4, LocalDate.now().toString(), LocalDate.now().minusYears(2).toString(), "associatedCred"),
                        new GraduationProgramCode("2004-PF", "B.C. Graduation Program 2004", "Description for 2004", 4, LocalDate.now().toString(), null, "associatedCred"),
                        new GraduationProgramCode("1996-EN", "B.C. Graduation Program 1996", "Description for 1996", 4, LocalDate.now().toString(), LocalDate.now().minusYears(2).toString(), "associatedCred"),
                        new GraduationProgramCode("1986-PF", "B.C. Graduation Program 1986", "Description for 1986", 4, LocalDate.now().toString(), LocalDate.now().minusYears(2).toString(), "associatedCred"),
                        new GraduationProgramCode("SCCP", "School Completion Certificate Program", "Description for SCCP", 4, LocalDate.now().toString(), null, "associatedCred"),
                        new GraduationProgramCode("NONPROG", "Expired Program", "Description for Expired", 4, LocalDate.now().toString(), LocalDate.now().minusYears(2).toString(), "associatedCred")
                )
        );
        Student studentApiStudent = new Student();
        studentApiStudent.setStudentID(UUID.randomUUID().toString());
        studentApiStudent.setPen("123456789");
        studentApiStudent.setLocalID("8887555");
        studentApiStudent.setLegalFirstName("JIM");
        studentApiStudent.setLegalLastName("JACKSON");
        studentApiStudent.setDob("1990-01-01");
        studentApiStudent.setStatusCode(StudentStatusCodes.A.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent);
        GradStudentRecord gradStudentRecord = new GradStudentRecord();
        gradStudentRecord.setSchoolOfRecordId("03636018");
        gradStudentRecord.setStudentStatusCode("CUR");
        gradStudentRecord.setProgramCompletionDate("2023-06-30");
        gradStudentRecord.setGraduated("false");
        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(gradStudentRecord);
    }

    @Test
    void testD03DemographicStudentPEN() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        Student studentApiStudent2 = new Student();
        studentApiStudent2.setStudentID(UUID.randomUUID().toString());
        studentApiStudent2.setPen(null);
        studentApiStudent2.setLocalID("8887555");
        studentApiStudent2.setLegalLastName("JACKSON");
        studentApiStudent2.setLegalFirstName("JIM");
        studentApiStudent2.setDob("1990-01-01");
        studentApiStudent2.setStatusCode(StudentStatusCodes.A.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent2);

        StudentRuleData studentRuleData2 = createMockStudentRuleData(createMockDemographicStudent(savedFileSet), createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone());
        val validationError2 = rulesProcessor.processRules(studentRuleData2);
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PEN_NOT_FOUND.getCode());
    }

    @Test
    void testD11DemographicStudentPEN() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        var incomingFileset2 = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet2 = incomingFilesetRepository.save(incomingFileset2);
        var demStudent2 = createMockDemographicStudent(savedFileSet2);
        var courseStudent2 = createMockCourseStudent(savedFileSet2);
        demStudent2.setIncomingFileset(courseStudent2.getIncomingFileset());
        StudentRuleData studentRuleData2 = createMockStudentRuleData(demStudent2, courseStudent2, createMockAssessmentStudent(), createMockSchoolTombstone());
        val validationError2 = rulesProcessor.processRules(studentRuleData2);
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PEN_MISMATCH.getCode());
    }

    @Test
    void testD10DemographicStudentName() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        var demStudent2 = createMockDemographicStudent(savedFileSet);
        demStudent2.setLastName("A");
        StudentRuleData studentRuleData2 = createMockStudentRuleData(demStudent2, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone());
        val validationError2 = rulesProcessor.processRules(studentRuleData2);
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.LAST_NAME.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_SURNAME_MISMATCH.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(
    "SURNAME mismatch. School submitted: A and the Ministry PEN system has: JACKSON. If the submitted SURNAME is correct, request a PEN update through <a href=\"https://dev.educationdataexchange.gov.bc.ca/inbox\">EDX Secure Messaging </a>");

        demStudent2.setLastName("");
        StudentRuleData studentRuleData5 = createMockStudentRuleData(demStudent2, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone());
        val validationError5 = rulesProcessor.processRules(studentRuleData5);
        assertThat(validationError5.size()).isNotZero();
        assertThat(validationError5.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.LAST_NAME.getCode());
        assertThat(validationError5.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_SURNAME_MISMATCH.getCode());
        assertThat(validationError5.getFirst().getValidationIssueDescription()).isEqualTo(
                "SURNAME mismatch. School submitted a blank surname and the Ministry PEN system has: JACKSON. If the submitted SURNAME is correct, request a PEN update through <a href=\"https://dev.educationdataexchange.gov.bc.ca/inbox\">EDX Secure Messaging </a>");

        demStudent2.setLastName(null);
        StudentRuleData studentRuleDat6 = createMockStudentRuleData(demStudent2, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone());
        val validationError6 = rulesProcessor.processRules(studentRuleDat6);
        assertThat(validationError6.size()).isNotZero();
        assertThat(validationError6.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.LAST_NAME.getCode());
        assertThat(validationError6.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_SURNAME_MISMATCH.getCode());
        assertThat(validationError6.getFirst().getValidationIssueDescription()).isEqualTo(
                "SURNAME mismatch. School submitted a blank surname and the Ministry PEN system has: JACKSON. If the submitted SURNAME is correct, request a PEN update through <a href=\"https://dev.educationdataexchange.gov.bc.ca/inbox\">EDX Secure Messaging </a>");

        var demStudent3 = createMockDemographicStudent(savedFileSet);
        demStudent3.setMiddleName("A");
        StudentRuleData studentRuleData3 = createMockStudentRuleData(demStudent3, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone());
        val validationError3 = rulesProcessor.processRules(studentRuleData3);
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.MIDDLE_NAME.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_MIDDLE_MISMATCH.getCode());
        assertThat(validationError3.getFirst().getValidationIssueDescription()).isEqualTo(
                "MIDDLE NAME mismatch. School submitted: A but the Ministry PEN system is blank. If the submitted MIDDLE NAME is correct, request a PEN update through <a href=\"https://dev.educationdataexchange.gov.bc.ca/inbox\">EDX Secure Messaging </a>");

        var demStudent4 = createMockDemographicStudent(savedFileSet);
        demStudent4.setFirstName("A");
        StudentRuleData studentRuleData4 = createMockStudentRuleData(demStudent4, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone());
        val validationError4 = rulesProcessor.processRules(studentRuleData4);
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FIRST_NAME.getCode());
        assertThat(validationError4.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_GIVEN_MISMATCH.getCode());
        assertThat(validationError4.getFirst().getValidationIssueDescription()).isEqualTo(
    "FIRST NAME mismatch. School submitted: A and the Ministry PEN system has: JIM. If the submitted FIRST NAME is correct, request a PEN update through <a href=\"https://dev.educationdataexchange.gov.bc.ca/inbox\">EDX Secure Messaging </a>");
    }

    @Test
    void testD10DemographicStudentName_withNotAllowedHtmlChars() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        var demStudent2 = createMockDemographicStudent(savedFileSet);
        demStudent2.setLastName("<script>alert('Hello!');</script> and <a href=\"https://dev.educationdataexchange.gov.bc.ca/inbox\">badLink</a>");
        StudentRuleData studentRuleData2 = createMockStudentRuleData(demStudent2, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone());
        val validationError2 = rulesProcessor.processRules(studentRuleData2);
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.LAST_NAME.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_SURNAME_MISMATCH.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(
                "SURNAME mismatch. School submitted: &lt;script&gt;alert('Hello!');&lt;/script&gt; and &lt;a href=&quot;https://dev.educationdataexchange.gov.bc.ca/inbox&quot;&gt;badLink&lt;/a&gt; and the Ministry PEN system has: JACKSON. If the submitted SURNAME is correct, request a PEN update through <a href=\"https://dev.educationdataexchange.gov.bc.ca/inbox\">EDX Secure Messaging </a>");
    }

    @Test
    void testD16DemographicStudentBirthdate() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        var demStudent2 = createMockDemographicStudent(savedFileSet);
        demStudent2.setBirthdate("12341212");
        StudentRuleData studentRuleData2 = createMockStudentRuleData(demStudent2, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone());
        val validationError2 = rulesProcessor.processRules(studentRuleData2);
        assertThat(validationError2.size()).isNotZero();
        var issueCode = validationError2.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.BIRTHDATE.getCode()));
        var errorCode = validationError2.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_MISMATCH.getCode()));
        assertThat(issueCode).isTrue();
        assertThat(errorCode).isTrue();
    }

    @Test
    void testD23DemographicStudentAdultBirthdate() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        Student studentApiStudent = new Student();
        studentApiStudent.setStudentID(UUID.randomUUID().toString());
        studentApiStudent.setPen("123456789");
        studentApiStudent.setLocalID("8887555");
        studentApiStudent.setLegalFirstName("JIM");
        studentApiStudent.setLegalLastName("JACKSON");
        studentApiStudent.setDob("2020-01-01");
        studentApiStudent.setStatusCode(StudentStatusCodes.A.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent);

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setGradRequirementYear(GradRequirementYearCodes.YEAR_1950.getCode());
        demographicStudent.setBirthdate("20200101");
        demographicStudent.setGrade("AD");
        demographicStudent.setSchoolCertificateCompletionDate("");
        assertThat(demographicStudent.getGradRequirementYear()).isEqualTo(GradRequirementYearCodes.YEAR_1950.getCode());

        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(null);

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        var issueCode = validationError2.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.BIRTHDATE.getCode()));
        var errorCode = validationError2.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_ADULT.getCode()));
        assertThat(issueCode).isTrue();
        assertThat(errorCode).isTrue();
    }

    @Test
    void testD02DemographicStudentCitizenship() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setCitizenship("Z");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.CITIZENSHIP.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_CITIZENSHIP_CODE_INVALID.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_CITIZENSHIP_CODE_INVALID.getMessage().formatted(demographicStudent.getCitizenship()));

        demographicStudent.setCitizenship(null);
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));

        var issueCode = validationError3.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.CITIZENSHIP.getCode()));
        var errorCode = validationError3.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_CITIZENSHIP_CODE_INVALID.getCode()));
        assertThat(issueCode).isFalse();
        assertThat(errorCode).isFalse();

        demographicStudent.setCitizenship("");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        var issueCode4 = validationError4.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.CITIZENSHIP.getCode()));
        var errorCode4 = validationError4.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_CITIZENSHIP_CODE_INVALID.getCode()));
        assertThat(issueCode4).isFalse();
        assertThat(errorCode4).isFalse();
    }

    @Test
    void testD07DemographicValidGradeRule() {
        var mockReportingPeriod = createMockReportingPeriodEntity();
        mockReportingPeriod.setSummerStart(LocalDate.now().minusDays(2).atStartOfDay());
        mockReportingPeriod.setSummerEnd(LocalDate.now().minusDays(1).atStartOfDay());
        var reportingPeriod = reportingPeriodRepository.save(mockReportingPeriod);
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setGrade("22");
        val validationError = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError.size()).isNotZero();
        var issueCode = validationError.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.GRADE.getCode()));
        var errorCode = validationError.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.GRADE_INVALID.getCode()));
        var errorMessage = validationError.stream().anyMatch(val -> val.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.GRADE_INVALID.getMessage().formatted(demographicStudent.getGrade())));
        assertThat(issueCode).isTrue();
        assertThat(errorCode).isTrue();
        assertThat(errorMessage).isTrue();

        var demographicStudent2 = createMockDemographicStudent(savedFileSet);
        demographicStudent2.setGrade(null);
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        var issueCode2 = validationError2.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.GRADE.getCode()));
        var errorCode2 = validationError2.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.GRADE_INVALID.getCode()));
        var errorMessage2 = validationError2.stream().anyMatch(val -> val.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.GRADE_INVALID.getMessage().formatted(demographicStudent2.getGrade())));
        assertThat(issueCode2).isTrue();
        assertThat(errorCode2).isTrue();
        assertThat(errorMessage2).isTrue();

        var demographicStudent3 = createMockDemographicStudent(savedFileSet);
        demographicStudent3.setGrade("02");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent3, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError3.size()).isNotZero();
        var issueCode3 = validationError3.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.GRADE.getCode()));
        var errorCode3 = validationError3.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.GRADE_INVALID.getCode()));
        var errorMessage3 = validationError3.stream().anyMatch(val -> val.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.GRADE_INVALID.getMessage().formatted(demographicStudent3.getGrade())));
        assertThat(issueCode3).isTrue();
        assertThat(errorCode3).isTrue();
        assertThat(errorMessage3).isTrue();
    }

    @Test
    void testD15DemographicValidGradeRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setGrade("KH");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();

        var issueCode = validationError2.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.GRADE.getCode()));
        var errorCode = validationError2.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.GRADE_NOT_EXPECTED.getCode()));
        var errorMessage = validationError2.stream().anyMatch(val -> val.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.GRADE_NOT_EXPECTED.getMessage().formatted(demographicStudent.getGrade())));
        assertThat(issueCode).isTrue();
        assertThat(errorCode).isTrue();
        assertThat(errorMessage).isTrue();

        demographicStudent.setGrade("1");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError3.size()).isZero();
    }

    @Test
    void testD26DemographicValidGradeProgramRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setGradRequirementYear("1950");
        demographicStudent.setGrade("08");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();

        var issueCode = validationError2.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.GRADE.getCode()));
        var errorCode = validationError2.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.GRADE_AG_INVALID.getCode()));
        assertThat(issueCode).isTrue();
        assertThat(errorCode).isTrue();
    }

    @Test
    void testD27SCCPCompletionDateAlreadyReportedRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());
        demStudent.setSchoolCertificateCompletionDate("2025-01-16");

        var gradStudentRecord = new GradStudentRecord();
        gradStudentRecord.setProgramCompletionDate("2025-01-15");
        gradStudentRecord.setSchoolAtGradId(UUID.randomUUID().toString());
        gradStudentRecord.setGraduated("false");
        gradStudentRecord.setStudentID(demStudent.getDemographicStudentID().toString());

        when(restUtils.getGradStudentRecordByStudentID(any(UUID.class), any(UUID.class))).thenReturn(gradStudentRecord);

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setGradRequirementYear("SCCP");
        demographicStudent.setGrade("08");
        demographicStudent.setSchoolCertificateCompletionDate("20250116");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();

        var issueCode = validationError2.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.SCHOOL_CERTIFICATE_COMPLETION_DATE.getCode()));
        var errorCode = validationError2.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_STUDENT_PROGRAM_ALREADY_REPORTED.getCode()));
        assertThat(issueCode).isTrue();
        assertThat(errorCode).isTrue();
    }

    @Test
    void testD24DemographicValidGradeProgramRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setGradRequirementYear("SCCP");
        demographicStudent.setGrade("AD");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();

        var issueCode = validationError2.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.GRADE.getCode()));
        var errorCode = validationError2.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.GRADE_OG_INVALID.getCode()));
        assertThat(issueCode).isTrue();
        assertThat(errorCode).isTrue();
    }

    @Test
    void testD14DemographicProgramCodeRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setProgramCode1("10ZE");
        val validationError = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError.size()).isNotZero();
        assertThat(validationError.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PROGRAM_CODE_1.getCode());
        assertThat(validationError.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getCode());
        assertThat(validationError.getFirst().getValidationIssueDescription()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getMessage().formatted(demographicStudent.getProgramCode1()));

        var demographicStudent2 = createMockDemographicStudent(savedFileSet);
        demographicStudent2.setProgramCode1("1");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PROGRAM_CODE_1.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getMessage().formatted(demographicStudent2.getProgramCode1()));

        var demographicStudent3 = createMockDemographicStudent(savedFileSet);
        demographicStudent3.setProgramCode1("10AA");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent3, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        var issueCode = validationError3.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.PROGRAM_CODE_1.getCode()));
        assertThat(issueCode).isFalse();

        var demographicStudent4 = createMockDemographicStudent(savedFileSet);
        demographicStudent4.setProgramCode2("10ZE");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent4, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PROGRAM_CODE_2.getCode());
        assertThat(validationError4.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getCode());
        assertThat(validationError4.getFirst().getValidationIssueDescription()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getMessage().formatted(demographicStudent4.getProgramCode2()));

        var demographicStudent5 = createMockDemographicStudent(savedFileSet);
        demographicStudent5.setProgramCode3("10ZE");
        demographicStudent5.setProgramCode4("10ZE");
        demographicStudent5.setProgramCode5("1");
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent5, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError5.stream().anyMatch(err ->
                err.getValidationIssueFieldCode().equals(ValidationFieldCode.PROGRAM_CODE_3.getCode()) &&
                err.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getCode()) &&
                err.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getMessage().formatted(demographicStudent5.getProgramCode3()))
        )).isTrue();
        assertThat(validationError5.stream().anyMatch(err ->
                err.getValidationIssueFieldCode().equals(ValidationFieldCode.PROGRAM_CODE_4.getCode()) &&
                err.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getCode()) &&
                err.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getMessage().formatted(demographicStudent5.getProgramCode4()))
        )).isTrue();
        assertThat(validationError5.stream().anyMatch(err ->
                err.getValidationIssueFieldCode().equals(ValidationFieldCode.PROGRAM_CODE_5.getCode()) &&
                err.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getCode()) &&
                err.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getMessage().formatted(demographicStudent5.getProgramCode5()))
        )).isTrue();

        var demographicStudent6 = createMockDemographicStudent(savedFileSet);
        demographicStudent6.setProgramCode3("10ZE");
        demographicStudent6.setProgramCode5("10AA");
        val validationError6 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent6, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError6.stream().anyMatch(err ->
                err.getValidationIssueFieldCode().equals(ValidationFieldCode.PROGRAM_CODE_3.getCode()) &&
                err.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getCode()) &&
                err.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getMessage().formatted(demographicStudent6.getProgramCode3()))
        )).isTrue();
        assertThat(validationError6.stream().anyMatch(err ->
                err.getValidationIssueFieldCode().equals(ValidationFieldCode.PROGRAM_CODE_5.getCode()) &&
                err.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getCode()) &&
                err.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getMessage().formatted(demographicStudent6.getProgramCode5()))
        )).isFalse();
    }

    @Test
    void testD21DemographicStudentStatus() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());
        var school = createMockSchoolTombstone();
        school.setSchoolId("03636018");

        var demographicStudentMerged = createMockDemographicStudent(savedFileSet);
        demographicStudentMerged.setStudentStatus(StudentStatusCodes.A.getCode());
        Student studentApiStudentMerged = new Student();
        studentApiStudentMerged.setStudentID(UUID.randomUUID().toString());
        studentApiStudentMerged.setPen("123456789");
        studentApiStudentMerged.setStatusCode(StudentStatusCodes.M.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudentMerged);
        StudentRuleData studentRuleDataMerged = createMockStudentRuleData(demographicStudentMerged, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), school);
        val validationErrorMerged = rulesProcessor.processRules(studentRuleDataMerged);
        assertThat(validationErrorMerged.size()).isNotZero();
        assertThat(validationErrorMerged.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.PEN.getCode()))).isTrue();
        assertThat(validationErrorMerged.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_MERGED.getCode()))).isTrue();

        var demographicStudentDeceased = createMockDemographicStudent(savedFileSet);
        demographicStudentDeceased.setStudentStatus(StudentStatusCodes.T.getCode());
        Student studentApiStudentDeceased = new Student();
        studentApiStudentDeceased.setStudentID(UUID.randomUUID().toString());
        studentApiStudentDeceased.setPen("123456789");
        studentApiStudentDeceased.setStatusCode(StudentStatusCodes.D.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudentDeceased);
        StudentRuleData studentRuleDataDeceased = createMockStudentRuleData(demographicStudentDeceased, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), school);
        val validationErrorDeceased = rulesProcessor.processRules(studentRuleDataDeceased);
        assertThat(validationErrorDeceased.size()).isNotZero();
        assertThat(validationErrorDeceased.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.STUDENT_STATUS.getCode()))).isTrue();
        assertThat(validationErrorDeceased.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_PEN_MISMATCH.getCode()))).isTrue();

        var demographicStudentD = createMockDemographicStudent(savedFileSet);
        demographicStudentD.setStudentStatus(StudentStatusCodes.D.getCode());
        Student studentApiStudentNotDeceased = new Student();
        studentApiStudentNotDeceased.setStudentID(UUID.randomUUID().toString());
        studentApiStudentNotDeceased.setPen("123456789");
        studentApiStudentNotDeceased.setStatusCode(StudentStatusCodes.A.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudentNotDeceased);
        StudentRuleData studentRuleDataD = createMockStudentRuleData(demographicStudentD, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), school);
        val validationErrorD = rulesProcessor.processRules(studentRuleDataD);
        assertThat(validationErrorD.size()).isNotZero();
        assertThat(validationErrorD.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.STUDENT_STATUS.getCode()))).isTrue();
        assertThat(validationErrorD.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_PEN_MISMATCH.getCode()))).isTrue();

        var demographicStudentD2 = createMockDemographicStudent(savedFileSet);
        demographicStudentD2.setStudentStatus(StudentStatusCodes.D.getCode());
        Student studentApiStudentDeceased2 = new Student();
        studentApiStudentDeceased2.setStudentID(UUID.randomUUID().toString());
        studentApiStudentDeceased2.setPen("123456789");
        studentApiStudentDeceased2.setStatusCode(StudentStatusCodes.D.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudentDeceased2);
        StudentRuleData studentRuleDataD2 = createMockStudentRuleData(demographicStudentD2, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), school);
        val validationErrorD2 = rulesProcessor.processRules(studentRuleDataD2);
        assertThat(validationErrorD2.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.STUDENT_STATUS.getCode()) &&
                val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_PEN_MISMATCH.getCode()))).isFalse();
    }

    @Test
    void testD19DemographicStudentStatus() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setStudentStatus("T");
        SchoolTombstone schoolTombstone = createMockSchoolTombstone();
        schoolTombstone.setSchoolId("03636012");
        StudentRuleData studentRuleData2 = createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), schoolTombstone);
        val validationError2 = rulesProcessor.processRules(studentRuleData2);
        assertThat(validationError2.size()).isNotZero();

        var issueCode = validationError2.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.STUDENT_STATUS.getCode()));
        var errorCode = validationError2.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_SCHOOL_OF_RECORD_MISMATCH.getCode()));
        var errorMessage = validationError2.stream().anyMatch(val -> val.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_SCHOOL_OF_RECORD_MISMATCH.getMessage()));
        assertThat(issueCode).isTrue();
        assertThat(errorCode).isTrue();
        assertThat(errorMessage).isTrue();

        var demographicStudent3 = createMockDemographicStudent(savedFileSet);
        demographicStudent3.setStudentStatus("T");
        SchoolTombstone schoolTombstone3 = createMockSchoolTombstone();
        schoolTombstone3.setSchoolId("03636018");

        GradStudentRecord gradStudentRecord3 = new GradStudentRecord();
        gradStudentRecord3.setSchoolOfRecordId("03636018");
        gradStudentRecord3.setStudentStatusCode("ARC");
        gradStudentRecord3.setGraduated("false");
        gradStudentRecord3.setStudentID(demographicStudent3.getDemographicStudentID().toString());
        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(gradStudentRecord3);

        StudentRuleData studentRuleData3 = createMockStudentRuleData(demographicStudent3, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), schoolTombstone3);
        val validationError3 = rulesProcessor.processRules(studentRuleData3);
        assertThat(validationError3.size()).isNotZero();

        var issueCode3 = validationError3.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.STUDENT_STATUS.getCode()));
        var errorCode3 = validationError3.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_NOT_CURRENT_IN_GRAD.getCode()));
        var errorMessage3 = validationError3.stream().anyMatch(val -> val.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_NOT_CURRENT_IN_GRAD.getMessage()));
        assertThat(issueCode3).isTrue();
        assertThat(errorCode3).isTrue();
        assertThat(errorMessage3).isTrue();

        var demographicStudent4 = createMockDemographicStudent(savedFileSet);
        demographicStudent4.setStudentStatus("T");
        SchoolTombstone schoolTombstone4 = createMockSchoolTombstone();
        schoolTombstone4.setSchoolId("03636018");

        GradStudentRecord gradStudentRecord4 = new GradStudentRecord();
        gradStudentRecord4.setSchoolOfRecordId("03636018");
        gradStudentRecord4.setStudentStatusCode("CUR");
        gradStudentRecord4.setGraduated("false");
        gradStudentRecord4.setStudentID(demographicStudent4.getDemographicStudentID().toString());
        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(gradStudentRecord4);

        StudentRuleData studentRuleData4 = createMockStudentRuleData(demographicStudent4, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), schoolTombstone4);
        val validationError4 = rulesProcessor.processRules(studentRuleData4);
        var d19Error = validationError4.stream().anyMatch(val ->
            val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_NOT_CURRENT_IN_GRAD.getCode()) ||
            val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_SCHOOL_OF_RECORD_MISMATCH.getCode())
        );
        assertThat(d19Error).isFalse();
    }

    @Test
    void testD20DemographicStudentStatus() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        Student studentApiStudent = new Student();
        studentApiStudent.setStudentID(UUID.randomUUID().toString());
        studentApiStudent.setPen("123456789");
        studentApiStudent.setLocalID("8887555");
        studentApiStudent.setLegalFirstName("JIM");
        studentApiStudent.setLegalLastName("JACKSON");
        studentApiStudent.setDob("1990-01-01");
        studentApiStudent.setStatusCode(StudentStatusCodes.D.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent);

        when(restUtils.getGradStudentRecordByStudentID(any(UUID.class), any(UUID.class)))
                .thenThrow(new EntityNotFoundException(GradStudentRecord.class));

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setStudentStatus("T");
        StudentRuleData studentRuleData = createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone());
        val validationError = rulesProcessor.processRules(studentRuleData);
        assertThat(validationError.size()).isNotZero();

        var issueCode = validationError.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.STUDENT_STATUS.getCode()));
        var errorCode = validationError.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_INCORRECT_NEW_STUDENT.getCode()));
        var errorMessage = validationError.stream().anyMatch(val -> val.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_INCORRECT_NEW_STUDENT.getMessage()));
        assertThat(issueCode).isTrue();
        assertThat(errorCode).isTrue();
        assertThat(errorMessage).isTrue();

        var demographicStudent2 = createMockDemographicStudent(savedFileSet);
        demographicStudent2.setStudentStatus("A");
        StudentRuleData studentRuleData2 = createMockStudentRuleData(demographicStudent2, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone());
        val validationError2 = rulesProcessor.processRules(studentRuleData2);
        assertThat(validationError2.stream().anyMatch(err ->
                err.getValidationIssueFieldCode().equals(ValidationFieldCode.STUDENT_STATUS.getCode()) &&
                err.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_INCORRECT_NEW_STUDENT.getCode()) &&
                err.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_INCORRECT_NEW_STUDENT.getMessage())
        )).isFalse();

        var demographicStudent3 = createMockDemographicStudent(savedFileSet);
        demographicStudent3.setStudentStatus("D");
        StudentRuleData studentRuleData3 = createMockStudentRuleData(demographicStudent3, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone());
        val validationError3 = rulesProcessor.processRules(studentRuleData3);
        assertThat(validationError3.stream().anyMatch(err ->
                err.getValidationIssueFieldCode().equals(ValidationFieldCode.STUDENT_STATUS.getCode()) &&
                err.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_INCORRECT_NEW_STUDENT.getCode()) &&
                err.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_INCORRECT_NEW_STUDENT.getMessage())
        )).isFalse();
    }

    @Test
    void testD05DemographicStudentProgramRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setGradRequirementYear("1332");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.GRAD_REQUIREMENT_YEAR.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getMessage().formatted(demographicStudent.getGradRequirementYear()));
     }

    @Test
    void testD13DemographicStudentProgramRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setGradRequirementYear(GradRequirementYearCodes.YEAR_1950.getCode());
        demographicStudent.setGrade("AD");
        var school = createMockSchoolTombstone();
        school.setSchoolCategoryCode(SchoolCategoryCodes.OFFSHORE.getCode());
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), school));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.GRAD_REQUIREMENT_YEAR.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_SCHOOL_CATEGORY_CODE_INVALID.getCode());

        var demographicStudent2 = createMockDemographicStudent(savedFileSet);
        demographicStudent2.setGradRequirementYear(GradRequirementYearCodes.SCCP.getCode());
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), school));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.GRAD_REQUIREMENT_YEAR.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_SCHOOL_CATEGORY_CODE_INVALID.getCode());
    }

    @Test
    void testD18DemographicStudentProgramRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        GraduationProgramCode expiredProgram = new GraduationProgramCode();
        expiredProgram.setProgramCode("2023");
        expiredProgram.setExpiryDate(LocalDateTime.now().minusDays(2).toString());
        when(restUtils.getGraduationProgramCodeList(false)).thenReturn(List.of(expiredProgram));

        var demStudent2 = createMockDemographicStudent(savedFileSet);
        demStudent2.setPen(courseStudent.getPen());
        demStudent2.setIncomingFileset(courseStudent.getIncomingFileset());
        GradStudentRecord gradStudentRecord2 = new GradStudentRecord();
        gradStudentRecord2.setSchoolOfRecordId(UUID.randomUUID().toString());
        gradStudentRecord2.setStudentStatusCode("CUR");
        gradStudentRecord2.setGraduated("false");
        gradStudentRecord2.setProgram("2023");
        gradStudentRecord2.setProgramCompletionDate(null);
        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(gradStudentRecord2);

        GraduationProgramCode nonExpiredProgram = new GraduationProgramCode();
        nonExpiredProgram.setProgramCode("2024");
        nonExpiredProgram.setExpiryDate(null);
        when(restUtils.getGraduationProgramCodeList(true)).thenReturn(List.of(nonExpiredProgram));
        var validationErrors2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent2, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationErrors2).isNotEmpty();

        var issueCode = validationErrors2.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.GRAD_REQUIREMENT_YEAR.getCode()));
        var errorCode = validationErrors2.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_PROGRAM_CLOSED.getCode()));
        assertThat(issueCode).isTrue();
        assertThat(errorCode).isTrue();
    }

    @Test
    void testD17DemographicStudentProgramRule() {
        var mockReportingPeriod = createMockReportingPeriodEntity();
        mockReportingPeriod.setSummerStart(LocalDate.now().minusDays(2).atStartOfDay());
        mockReportingPeriod.setSummerEnd(LocalDate.now().minusDays(1).atStartOfDay());
        var reportingPeriod = reportingPeriodRepository.save(mockReportingPeriod);
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        GradStudentRecord gradStudentRecord = new GradStudentRecord();
        gradStudentRecord.setSchoolOfRecordId(UUID.randomUUID().toString());
        gradStudentRecord.setStudentStatusCode("CUR");
        gradStudentRecord.setGraduated("true");
        gradStudentRecord.setProgramCompletionDate("2023-01-01");
        gradStudentRecord.setProgram("2018");

        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(gradStudentRecord);

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(savedFileSet), createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();

        var issueCode = validationError2.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.PEN.getCode()));
        var errorCode = validationError2.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_ALREADY_GRADUATED.getCode()));
        var errorMessage = validationError2.stream().anyMatch(val -> val.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_ALREADY_GRADUATED.getMessage().formatted(gradStudentRecord.getProgram())));
        assertThat(issueCode).isTrue();
        assertThat(errorCode).isTrue();
        assertThat(errorMessage).isTrue();
    }


    @Test
    void testD08DemographicSCCPCompletionDate() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setSchoolCertificateCompletionDate("20041312");
        val validationError = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError.size()).isNotZero();
        assertThat(validationError.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.SCHOOL_CERTIFICATE_COMPLETION_DATE.getCode());
        assertThat(validationError.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getCode());
        assertThat(validationError.getFirst().getValidationIssueDescription()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getMessage().formatted(demographicStudent.getSchoolCertificateCompletionDate()));

        var demographicStudent2 = createMockDemographicStudent(savedFileSet);
        demographicStudent2.setSchoolCertificateCompletionDate("20042");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.SCHOOL_CERTIFICATE_COMPLETION_DATE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getMessage().formatted(demographicStudent2.getSchoolCertificateCompletionDate()));

        var demographicStudent3 = createMockDemographicStudent(savedFileSet);
        demographicStudent3.setSchoolCertificateCompletionDate("abc");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent3, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.SCHOOL_CERTIFICATE_COMPLETION_DATE.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getCode());
        assertThat(validationError3.getFirst().getValidationIssueDescription()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getMessage().formatted(demographicStudent3.getSchoolCertificateCompletionDate()));

        var demographicStudent4 = createMockDemographicStudent(savedFileSet);
        demographicStudent4.setSchoolCertificateCompletionDate("19900101");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent4, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.SCHOOL_CERTIFICATE_COMPLETION_DATE.getCode());
        assertThat(validationError4.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_DATE_TOO_EARLY.getCode());
        assertThat(validationError4.getFirst().getValidationIssueDescription()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_DATE_TOO_EARLY.getMessage().formatted(demographicStudent4.getSchoolCertificateCompletionDate()));

        var demographicStudent5 = createMockDemographicStudent(savedFileSet);
        demographicStudent5.setSchoolCertificateCompletionDate(null);
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent5, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));

        var issueCode = validationError5.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.SCHOOL_CERTIFICATE_COMPLETION_DATE.getCode()));
        var errorCode = validationError5.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getCode()) || val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.SCCP_DATE_TOO_EARLY.getCode()));
        var errorMessage = validationError5.stream().anyMatch(val -> val.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getMessage().formatted(demographicStudent5.getSchoolCertificateCompletionDate())) || val.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.SCCP_DATE_TOO_EARLY.getMessage().formatted(demographicStudent5.getSchoolCertificateCompletionDate())));
        assertThat(issueCode).isFalse();
        assertThat(errorCode).isFalse();
        assertThat(errorMessage).isFalse();

        var demographicStudent6 = createMockDemographicStudent(savedFileSet);
        demographicStudent6.setSchoolCertificateCompletionDate("");
        val validationError6 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent6, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));

        var issueCode1 = validationError6.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.SCHOOL_CERTIFICATE_COMPLETION_DATE.getCode()));
        var errorCode1 = validationError6.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getCode()) || val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.SCCP_DATE_TOO_EARLY.getCode()));
        var errorMessage1 = validationError6.stream().anyMatch(val -> val.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getMessage().formatted(demographicStudent6.getSchoolCertificateCompletionDate())) || val.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.SCCP_DATE_TOO_EARLY.getMessage().formatted(demographicStudent6.getSchoolCertificateCompletionDate())));
        assertThat(issueCode1).isFalse();
        assertThat(errorCode1).isFalse();
        assertThat(errorMessage1).isFalse();

        var demographicStudent7 = createMockDemographicStudent(savedFileSet);
        demographicStudent7.setSchoolCertificateCompletionDate("20240425");
        demographicStudent7.setGradRequirementYear(GradRequirementYearCodes.SCCP.getCode());
        val validationError7 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent7, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        var issueCode2 = validationError7.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.SCHOOL_CERTIFICATE_COMPLETION_DATE.getCode()));
        var errorCode2 = validationError7.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getCode()) || val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.SCCP_DATE_TOO_EARLY.getCode()));
        var errorMessage2 = validationError7.stream().anyMatch(val -> val.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getMessage().formatted(demographicStudent7.getSchoolCertificateCompletionDate())) || val.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.SCCP_DATE_TOO_EARLY.getMessage().formatted(demographicStudent7.getSchoolCertificateCompletionDate())));
        assertThat(issueCode2).isFalse();
        assertThat(errorCode2).isFalse();
        assertThat(errorMessage2).isFalse();
    }

    @Test
    void testDemographicSCCPCompletionDate() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setGradRequirementYear(GradRequirementYearCodes.YEAR_2023.getCode());
        assertThat(demographicStudent.getGradRequirementYear()).isEqualTo(GradRequirementYearCodes.YEAR_2023.getCode());

        var demographicStudent2 = createMockDemographicStudent(savedFileSet);
        demographicStudent2.setSchoolCertificateCompletionDate("19900701");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));

        assertThat(validationError3.size()).isNotZero();
        var issueCode = validationError3.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.SCHOOL_CERTIFICATE_COMPLETION_DATE.getCode()));
        var errorCode = validationError3.stream().anyMatch(val -> val.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.SCCP_DATE_TOO_EARLY.getCode()));
        var errorMessage = validationError3.stream().anyMatch(val -> val.getValidationIssueDescription().equals(DemographicStudentValidationIssueTypeCode.SCCP_DATE_TOO_EARLY.getMessage().formatted(demographicStudent2.getSchoolCertificateCompletionDate())));
        assertThat(issueCode).isTrue();
        assertThat(errorCode).isTrue();
        assertThat(errorMessage).isTrue();
    }

    @Test
    void testD04DemographicStudentBirthdate() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setBirthdate("");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.BIRTHDATE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getMessage().formatted("(EMPTY)"));

        var demographicStudent2 = createMockDemographicStudent(savedFileSet);
        demographicStudent2.setBirthdate("2222");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.BIRTHDATE.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getCode());
        assertThat(validationError3.getFirst().getValidationIssueDescription()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getMessage().formatted(demographicStudent2.getBirthdate()));

        var demographicStudent3 = createMockDemographicStudent(savedFileSet);
        demographicStudent3.setBirthdate("1990010");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent3, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.BIRTHDATE.getCode());
        assertThat(validationError4.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getCode());
        assertThat(validationError4.getFirst().getValidationIssueDescription()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getMessage().formatted(demographicStudent3.getBirthdate()));

        var demographicStudent4 = createMockDemographicStudent(savedFileSet);
        demographicStudent4.setBirthdate("199001011");
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent4, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError5.size()).isNotZero();
        assertThat(validationError5.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.BIRTHDATE.getCode());
        assertThat(validationError5.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getCode());
        assertThat(validationError5.getFirst().getValidationIssueDescription()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getMessage().formatted(demographicStudent4.getBirthdate()));

        var demographicStudent5 = createMockDemographicStudent(savedFileSet);
        demographicStudent5.setBirthdate(null);
        val validationError6 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent5, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError6.size()).isNotZero();
        assertThat(validationError6.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.BIRTHDATE.getCode());
        assertThat(validationError6.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getCode());
        assertThat(validationError6.getFirst().getValidationIssueDescription()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getMessage().formatted("(EMPTY)"));
    }

    @Test
    void testD12BlankGradRequirement_Error_When_BlankYear_NotGA_NotFNS_IsGraduated_UsingDirectDataInjection() {
        var mockReportingPeriod = createMockReportingPeriodEntity();
        mockReportingPeriod.setSummerStart(LocalDate.now().minusDays(2).atStartOfDay());
        mockReportingPeriod.setSummerEnd(LocalDate.now().minusDays(1).atStartOfDay());
        var reportingPeriod = reportingPeriodRepository.save(mockReportingPeriod);
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        var assessmentStudent = createMockAssessmentStudent();
        var demStudent = createMockDemographicStudent(savedFileSet);
        var school = createMockSchoolTombstone();

        var gradStudentRecord = new GradStudentRecord();
        demStudent.setGradRequirementYear("");
        demStudent.setGrade(SchoolGradeCodes.GRADE12.getCode());
        school.setSchoolCategoryCode(SchoolCategoryCodes.PUBLIC.getCode());
        gradStudentRecord.setProgramCompletionDate("2025-01-15");
        gradStudentRecord.setGraduated("true");
        gradStudentRecord.setStudentID(demStudent.getDemographicStudentID().toString());
        gradStudentRecord.setStudentStatusCode("CUR");

        StudentRuleData studentRuleDataError = new StudentRuleData();
        studentRuleDataError.setDemographicStudentEntity(demStudent);
        studentRuleDataError.setCourseStudentEntity(courseStudent);
        studentRuleDataError.setAssessmentStudentEntity(assessmentStudent);
        studentRuleDataError.setSchool(school);
        studentRuleDataError.setGradStudentRecord(gradStudentRecord);

        val validationErrorsError = rulesProcessor.processRules(studentRuleDataError);

        var d12ErrorOptional = validationErrorsError.stream()
                .filter(e -> e.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL.getCode()))
                .findFirst();

        assertThat(d12ErrorOptional)
                .isPresent();

        d12ErrorOptional.ifPresent(err -> {
            assertThat(err.getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.GRAD_REQUIREMENT_YEAR.getCode());
            assertThat(err.getValidationIssueSeverityCode()).isEqualTo(StudentValidationIssueSeverityCode.ERROR.getCode());
            assertThat(err.getValidationIssueDescription()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL.getMessage().formatted("2023-EN"));
        });
    }

    @Test
    void testD12BlankGradRequirement_Error_When_BlankYear_NotSummer_NotFNS_IsGraduated() {
        var reportingPeriodEntity = createMockReportingPeriodEntity();
        reportingPeriodEntity.setSchYrStart(LocalDateTime.now().minusDays(2));
        reportingPeriodEntity.setSchYrEnd(LocalDateTime.now().plusDays(2));
        reportingPeriodEntity.setSummerStart(LocalDate.now().minusDays(2).atStartOfDay());
        reportingPeriodEntity.setSummerEnd(LocalDate.now().minusDays(1).atStartOfDay());
        
        var reportingPeriod = reportingPeriodRepository.save(reportingPeriodEntity);
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        var assessmentStudent = createMockAssessmentStudent();
        var demStudent = createMockDemographicStudent(savedFileSet);
        var school = createMockSchoolTombstone();

        var gradStudentRecord = new GradStudentRecord();
        demStudent.setGradRequirementYear("");
        demStudent.setGrade(SchoolGradeCodes.GRADUATED_ADULT.getCode());
        school.setSchoolCategoryCode(SchoolCategoryCodes.PUBLIC.getCode());
        gradStudentRecord.setProgramCompletionDate("2025-01-15");
        gradStudentRecord.setGraduated("true");
        gradStudentRecord.setStudentID(demStudent.getDemographicStudentID().toString());
        gradStudentRecord.setStudentStatusCode("CUR");

        StudentRuleData studentRuleDataError = new StudentRuleData();
        studentRuleDataError.setDemographicStudentEntity(demStudent);
        studentRuleDataError.setCourseStudentEntity(courseStudent);
        studentRuleDataError.setAssessmentStudentEntity(assessmentStudent);
        studentRuleDataError.setSchool(school);
        studentRuleDataError.setGradStudentRecord(gradStudentRecord);

        val validationErrorsError = rulesProcessor.processRules(studentRuleDataError);

        var d12ErrorOptional = validationErrorsError.stream()
                .filter(e -> e.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL.getCode()))
                .findFirst();

        assertThat(d12ErrorOptional)
                .isPresent();

        d12ErrorOptional.ifPresent(err -> {
            assertThat(err.getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.GRAD_REQUIREMENT_YEAR.getCode());
            assertThat(err.getValidationIssueSeverityCode()).isEqualTo(StudentValidationIssueSeverityCode.ERROR.getCode());
        });
    }

    @Test
    void testD12BlankGradRequirement_Valid_When_NotGraduated() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        var assessmentStudent = createMockAssessmentStudent();
        var demStudent = createMockDemographicStudent(savedFileSet);
        var school = createMockSchoolTombstone();
        var gradStudentRecord = new GradStudentRecord();

        demStudent.setGradRequirementYear("2025");
        demStudent.setGrade(SchoolGradeCodes.GRADE12.getCode());
        school.setSchoolCategoryCode(SchoolCategoryCodes.PUBLIC.getCode());

        gradStudentRecord.setProgramCompletionDate(null);
        gradStudentRecord.setGraduated("false");
        gradStudentRecord.setStudentID(demStudent.getDemographicStudentID().toString());

        when(restUtils.getGradStudentRecordByStudentID(any(UUID.class), any(UUID.class))).thenReturn(gradStudentRecord);

        StudentRuleData studentRuleDataValid = createMockStudentRuleData(demStudent, courseStudent, assessmentStudent, school);
        val validationErrorsValid = rulesProcessor.processRules(studentRuleDataValid);

        log.info("TEST: Valid NotGraduated scenario. Errors received: {}", validationErrorsValid);

        var d12ErrorOptional = validationErrorsValid.stream()
                .filter(e -> e.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL.getCode()))
                .findFirst();

        assertThat(d12ErrorOptional)
                .as("Expecting NO D12 error when student is not graduated, even if other conditions met")
                .isNotPresent();
    }

    @Test
    void testD12BlankGradRequirement_Valid_When_GradYearNotBlank() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        var assessmentStudent = createMockAssessmentStudent();
        var demStudent = createMockDemographicStudent(savedFileSet);
        var school = createMockSchoolTombstone();
        var gradStudentRecord = new GradStudentRecord();

        demStudent.setGradRequirementYear("2023");
        demStudent.setGrade(SchoolGradeCodes.GRADE12.getCode());
        school.setSchoolCategoryCode(SchoolCategoryCodes.PUBLIC.getCode());

        gradStudentRecord.setProgramCompletionDate("2024-01-15");
        gradStudentRecord.setGraduated("true");
        gradStudentRecord.setStudentID(demStudent.getDemographicStudentID().toString());

        when(restUtils.getGradStudentRecordByStudentID(any(UUID.class), any(UUID.class))).thenReturn(gradStudentRecord);

        StudentRuleData studentRuleDataValid = createMockStudentRuleData(demStudent, courseStudent, assessmentStudent, school);
        val validationErrorsValid = rulesProcessor.processRules(studentRuleDataValid);

        log.info("TEST: Valid GradYearNotBlank scenario. Errors received: {}", validationErrorsValid);

        var d12ErrorOptional = validationErrorsValid.stream()
                .filter(e -> e.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL.getCode()))
                .findFirst();

        assertThat(d12ErrorOptional)
                .as("Expecting NO D12 error when Grad Requirement Year is NOT blank")
                .isNotPresent();
    }

    @Test
    void testD12BlankGradRequirement_Warning_When_GradProgramChanged_IsGraduated_NotSCCP() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        var assessmentStudent = createMockAssessmentStudent();
        var demStudent = createMockDemographicStudent(savedFileSet);
        var school = createMockSchoolTombstone();

        demStudent.setGradRequirementYear("2023");
        demStudent.setGrade(SchoolGradeCodes.GRADE12.getCode());
        school.setSchoolCategoryCode(SchoolCategoryCodes.PUBLIC.getCode());

        Student studentApiStudent = new Student();
        studentApiStudent.setStudentID(UUID.randomUUID().toString());
        studentApiStudent.setPen("123456789");
        studentApiStudent.setLocalID("8887555");
        studentApiStudent.setLegalFirstName("JIM");
        studentApiStudent.setLegalLastName("JACKSON");
        studentApiStudent.setDob("1990-01-01");
        studentApiStudent.setStatusCode(StudentStatusCodes.A.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent);

        var gradStudentRecord = new GradStudentRecord();
        gradStudentRecord.setSchoolOfRecordId("03636018");
        gradStudentRecord.setProgramCompletionDate("2024-01-15");
        gradStudentRecord.setGraduated("true");
        gradStudentRecord.setProgram("2018");
        gradStudentRecord.setStudentID(demStudent.getDemographicStudentID().toString());
        gradStudentRecord.setStudentStatusCode("CUR");

        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(gradStudentRecord);

        StudentRuleData studentRuleDataWarning = createMockStudentRuleData(demStudent, courseStudent, assessmentStudent, school);
        val validationErrorsWarning = rulesProcessor.processRules(studentRuleDataWarning);

        var d12WarningOptional = validationErrorsWarning.stream()
                .filter(e -> e.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL_GRAD.getCode()))
                .findFirst();

        assertThat(d12WarningOptional)
                .as("Expecting D12 WARNING when grad program changed for graduated non-SCCP student")
                .isPresent();

        d12WarningOptional.ifPresent(warning -> {
            assertThat(warning.getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.GRAD_REQUIREMENT_YEAR.getCode());
            assertThat(warning.getValidationIssueSeverityCode()).isEqualTo(StudentValidationIssueSeverityCode.WARNING.getCode());
            assertThat(warning.getValidationIssueDescription()).contains(gradStudentRecord.getProgram());
        });
    }

    @Test
    void testD12BlankGradRequirement_Valid_When_GradProgramChanged_IsGraduated_IsSCCP() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        var assessmentStudent = createMockAssessmentStudent();
        var demStudent = createMockDemographicStudent(savedFileSet);
        var school = createMockSchoolTombstone();

        demStudent.setGradRequirementYear("2023");
        demStudent.setGrade(SchoolGradeCodes.GRADE12.getCode());
        school.setSchoolCategoryCode(SchoolCategoryCodes.PUBLIC.getCode());

        Student studentApiStudent = new Student();
        studentApiStudent.setStudentID(UUID.randomUUID().toString());
        studentApiStudent.setPen("123456789");
        studentApiStudent.setLocalID("8887555");
        studentApiStudent.setLegalFirstName("JIM");
        studentApiStudent.setLegalLastName("JACKSON");
        studentApiStudent.setDob("1990-01-01");
        studentApiStudent.setStatusCode(StudentStatusCodes.A.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent);

        var gradStudentRecord = new GradStudentRecord();
        gradStudentRecord.setSchoolOfRecordId("03636018");
        gradStudentRecord.setProgramCompletionDate("2024-01-15");
        gradStudentRecord.setGraduated("true");
        gradStudentRecord.setProgram("SCCP");
        gradStudentRecord.setStudentID(demStudent.getDemographicStudentID().toString());
        gradStudentRecord.setStudentStatusCode("CUR");

        when(restUtils.getGradStudentRecordByStudentID(any(UUID.class), any(UUID.class))).thenReturn(gradStudentRecord);

        StudentRuleData studentRuleDataValid = createMockStudentRuleData(demStudent, courseStudent, assessmentStudent, school);
        val validationErrorsValid = rulesProcessor.processRules(studentRuleDataValid);

        var d12WarningOptional = validationErrorsValid.stream()
                .filter(e -> e.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL_GRAD.getCode()))
                .findFirst();

        assertThat(d12WarningOptional)
                .as("Expecting NO D12 WARNING when student program in GRAD is SCCP, even if program changed")
                .isNotPresent();
    }

    @Test
    void testD12BlankGradRequirement_Valid_When_GradProgramSame_IsGraduated_NotSCCP() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        var assessmentStudent = createMockAssessmentStudent();
        var demStudent = createMockDemographicStudent(savedFileSet);
        var school = createMockSchoolTombstone();

        demStudent.setGradRequirementYear("2018");
        demStudent.setGrade(SchoolGradeCodes.GRADE12.getCode());
        school.setSchoolCategoryCode(SchoolCategoryCodes.PUBLIC.getCode());

        Student studentApiStudent = new Student();
        studentApiStudent.setStudentID(UUID.randomUUID().toString());
        studentApiStudent.setPen("123456789");
        studentApiStudent.setLocalID("8887555");
        studentApiStudent.setLegalFirstName("JIM");
        studentApiStudent.setLegalLastName("JACKSON");
        studentApiStudent.setDob("1990-01-01");
        studentApiStudent.setStatusCode(StudentStatusCodes.A.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent);

        var gradStudentRecord = new GradStudentRecord();
        gradStudentRecord.setSchoolOfRecordId("03636018");
        gradStudentRecord.setProgramCompletionDate("2024-01-15");
        gradStudentRecord.setGraduated("true");
        gradStudentRecord.setProgram("2018");
        gradStudentRecord.setStudentID(demStudent.getDemographicStudentID().toString());
        gradStudentRecord.setStudentStatusCode("CUR");

        when(restUtils.getGradStudentRecordByStudentID(any(UUID.class), any(UUID.class))).thenReturn(gradStudentRecord);

        StudentRuleData studentRuleDataValid = createMockStudentRuleData(demStudent, courseStudent, assessmentStudent, school);
        val validationErrorsValid = rulesProcessor.processRules(studentRuleDataValid);

        var d12WarningOptional = validationErrorsValid.stream()
                .filter(e -> e.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL_GRAD.getCode()))
                .findFirst();

        assertThat(d12WarningOptional)
                .as("Expecting NO D12 WARNING when submitted program matches GRAD program")
                .isNotPresent();
    }

    @Test
    void testD12BlankGradRequirement_Error_When_BlankYear_NoGradRecord() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        var assessmentStudent = createMockAssessmentStudent();
        var demStudent = createMockDemographicStudent(savedFileSet);
        var school = createMockSchoolTombstone();

        demStudent.setGradRequirementYear("");
        demStudent.setGrade(SchoolGradeCodes.GRADE12.getCode());
        school.setSchoolCategoryCode(SchoolCategoryCodes.PUBLIC.getCode());

        when(restUtils.getGradStudentRecordByStudentID(any(UUID.class), any(UUID.class))).thenReturn(null);

        StudentRuleData studentRuleDataError = createMockStudentRuleData(demStudent, courseStudent, assessmentStudent, school);
        val validationErrorsError = rulesProcessor.processRules(studentRuleDataError);

        var d12ErrorOptional = validationErrorsError.stream()
                .filter(e -> e.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL.getCode()))
                .findFirst();

        assertThat(d12ErrorOptional)
                .as("Expecting D12 ERROR when grad requirement year is blank, even without grad record")
                .isPresent();

        d12ErrorOptional.ifPresent(err -> {
            assertThat(err.getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.GRAD_REQUIREMENT_YEAR.getCode());
            assertThat(err.getValidationIssueSeverityCode()).isEqualTo(StudentValidationIssueSeverityCode.ERROR.getCode());
        });
    }

    @Test
    void testD12BlankGradRequirement_Valid_When_GradProgramChanged_NotGraduated() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        var assessmentStudent = createMockAssessmentStudent();
        var demStudent = createMockDemographicStudent(savedFileSet);
        var school = createMockSchoolTombstone();

        demStudent.setGradRequirementYear("2023");
        demStudent.setGrade(SchoolGradeCodes.GRADE12.getCode());
        school.setSchoolCategoryCode(SchoolCategoryCodes.PUBLIC.getCode());

        Student studentApiStudent = new Student();
        studentApiStudent.setStudentID(UUID.randomUUID().toString());
        studentApiStudent.setPen("123456789");
        studentApiStudent.setLocalID("8887555");
        studentApiStudent.setLegalFirstName("JIM");
        studentApiStudent.setLegalLastName("JACKSON");
        studentApiStudent.setDob("1990-01-01");
        studentApiStudent.setStatusCode(StudentStatusCodes.A.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent);

        var gradStudentRecord = new GradStudentRecord();
        gradStudentRecord.setSchoolOfRecordId("03636018");
        gradStudentRecord.setProgramCompletionDate(null);
        gradStudentRecord.setGraduated("false");
        gradStudentRecord.setProgram("2018");
        gradStudentRecord.setStudentID(demStudent.getDemographicStudentID().toString());
        gradStudentRecord.setStudentStatusCode("CUR");

        when(restUtils.getGradStudentRecordByStudentID(any(UUID.class), any(UUID.class))).thenReturn(gradStudentRecord);

        StudentRuleData studentRuleDataValid = createMockStudentRuleData(demStudent, courseStudent, assessmentStudent, school);
        val validationErrorsValid = rulesProcessor.processRules(studentRuleDataValid);

        var d12WarningOptional = validationErrorsValid.stream()
                .filter(e -> e.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL_GRAD.getCode()))
                .findFirst();

        assertThat(d12WarningOptional)
                .as("Expecting NO D12 WARNING when student has not graduated, even if program changed")
                .isNotPresent();
    }

}
