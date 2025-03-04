package ca.bc.gov.educ.graddatacollection.api.rules;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradRequirementYearCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolCategoryCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.StudentStatusCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.CourseStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentRulesProcessor;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.service.v1.DemographicRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.*;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.graddatacollection.api.struct.external.scholarships.v1.CitizenshipCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private DemographicRulesService demographicRulesService;

    @Autowired
    private DemographicStudentRulesProcessor rulesProcessor;

    @Autowired
    private IncomingFilesetRepository incomingFilesetRepository;

    @Autowired
    private CourseStudentRepository courseStudentRepository;

    @MockBean
    private RestUtils restUtils;

    @BeforeEach
    void setUp() {
        when(restUtils.getScholarshipsCitizenshipCodes()).thenReturn(
                List.of(
                        new CitizenshipCode("C", "Canadian", "Valid Citizenship Code", 1, "2020-01-01", "2099-12-31"),
                        new CitizenshipCode("O", "Other", "Valid Citizenship Code", 2, "2020-01-01", "2099-12-31"),
                        new CitizenshipCode("", "Blank", "Valid for Blank Citizenship", 3, "2020-01-01", "2099-12-31")
                )
        );
        when(restUtils.getGradGrades()).thenReturn(
                List.of(
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
        when(restUtils.getCareerPrograms()).thenReturn(
                List.of(
                        new CareerProgramCode("AA", "Art Careers", "", 1, "20200101", "20990101"),
                        new CareerProgramCode("AB", "Autobody", "", 2, "20200101", "20990101"),
                        new CareerProgramCode("AC", "Agribusiness", "", 3, "20200101", "20990101")
                )
        );
        when(restUtils.getOptionalPrograms()).thenReturn(
                List.of(
                        new OptionalProgramCode(UUID.randomUUID(), "FR", "SCCP French Certificate", "", 1, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new OptionalProgramCode(UUID.randomUUID(), "AD", "Advanced Placement", "", 2, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new OptionalProgramCode(UUID.randomUUID(), "DD", "Dual Dogwood", "", 3, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
                )
        );
        when(restUtils.getProgramRequirementCodes()).thenReturn(
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
        when(restUtils.getGraduationProgramCodes()).thenReturn(
                List.of(
                        new GraduationProgramCode("1950", "Adult Graduation Program", "Description for 1950", 4, Date.valueOf(LocalDate.now()), Date.valueOf("2222-01-01"), "associatedCred"),
                        new GraduationProgramCode("2023", "B.C. Graduation Program", "Description for 2023", 4,Date.valueOf(LocalDate.now()), Date.valueOf("2222-01-01"), "associatedCred"),
                        new GraduationProgramCode("2018-EN", "B.C. Graduation Program 2018", "Description for 2018", 4,Date.valueOf(LocalDate.now()), Date.valueOf("2222-01-01"), "associatedCred"),
                        new GraduationProgramCode("2004-PF", "B.C. Graduation Program 2004", "Description for 2004", 4, Date.valueOf(LocalDate.now()), Date.valueOf("2222-01-01"), "associatedCred"),
                        new GraduationProgramCode("1996-EN", "B.C. Graduation Program 1996", "Description for 1996", 4, Date.valueOf(LocalDate.now()), Date.valueOf("2222-01-01"), "associatedCred"),
                        new GraduationProgramCode("1986-PF", "B.C. Graduation Program 1986", "Description for 1986", 4, Date.valueOf(LocalDate.now()), Date.valueOf("2222-01-01"), "associatedCred"),
                        new GraduationProgramCode("SCCP", "School Completion Certificate Program", "Description for SCCP", 4, Date.valueOf(LocalDate.now()), Date.valueOf("2222-01-01"), "associatedCred"),
                        new GraduationProgramCode("NONPROG", "Expired Program", "Description for Expired", 4, Date.valueOf(LocalDate.now()), Date.valueOf("2222-01-01"), "associatedCred")
                )
        );
        Student studentApiStudent = new Student();
        studentApiStudent.setStudentID(UUID.randomUUID().toString());
        studentApiStudent.setPen("123456789");
        studentApiStudent.setLocalID("8887555");
        studentApiStudent.setLegalFirstName("JIM");
        studentApiStudent.setLegalLastName("JACKSON");
        studentApiStudent.setDob("19900101");
        studentApiStudent.setStatusCode(StudentStatusCodes.A.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent);
        GradStudentRecord gradStudentRecord = new GradStudentRecord();
        gradStudentRecord.setSchoolOfRecordId("03636018");
        gradStudentRecord.setStudentStatusCode("CUR");
        gradStudentRecord.setProgramCompletionDate("2023-06-30 00:00:00.000");
        gradStudentRecord.setGraduated("false");
        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(gradStudentRecord);
    }

    @Test
    void testV101DemographicStudentLocalID() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        StudentRuleData studentRuleData = createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool());
        val validationError1 = rulesProcessor.processRules(studentRuleData);
        assertThat(validationError1.size()).isZero();

        Student studentApiStudent = new Student();
        studentApiStudent.setStudentID(UUID.randomUUID().toString());
        studentApiStudent.setPen("123456789");
        studentApiStudent.setLocalID("8887554");
        studentApiStudent.setLegalLastName("JACKSON");
        studentApiStudent.setLegalFirstName("JIM");
        studentApiStudent.setDob("19900101");
        studentApiStudent.setStatusCode(StudentStatusCodes.A.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent);

        StudentRuleData studentRuleData2 = createMockStudentRuleData(createMockDemographicStudent(savedFileSet), createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool());
        val validationError2 = rulesProcessor.processRules(studentRuleData2);
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.LOCAL_ID.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_LOCAL_ID_MISMATCH.getCode());

        Student studentApiStudent2 = new Student();
        studentApiStudent2.setStudentID(UUID.randomUUID().toString());
        studentApiStudent2.setPen("123456789");
        studentApiStudent2.setLocalID("");
        studentApiStudent2.setLegalLastName("JACKSON");
        studentApiStudent2.setLegalFirstName("JIM");
        studentApiStudent2.setDob("19900101");
        studentApiStudent2.setStatusCode(StudentStatusCodes.A.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent2);

        StudentRuleData studentRuleData3 = createMockStudentRuleData(createMockDemographicStudent(savedFileSet), createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool());
        val validationError3 = rulesProcessor.processRules(studentRuleData3);
        assertThat(validationError3.size()).isZero();
    }

    @Test
    void testV103DemographicStudentPEN() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());
        
        StudentRuleData studentRuleData = createMockStudentRuleData(createMockDemographicStudent(savedFileSet), createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool());
        val validationError1 = rulesProcessor.processRules(studentRuleData);
        assertThat(validationError1.size()).isZero();

        Student studentApiStudent2 = new Student();
        studentApiStudent2.setStudentID(UUID.randomUUID().toString());
        studentApiStudent2.setPen(null);
        studentApiStudent2.setLocalID("8887555");
        studentApiStudent2.setLegalLastName("JACKSON");
        studentApiStudent2.setLegalFirstName("JIM");
        studentApiStudent2.setDob("19900101");
        studentApiStudent2.setStatusCode(StudentStatusCodes.A.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent2);

        StudentRuleData studentRuleData2 = createMockStudentRuleData(createMockDemographicStudent(savedFileSet), createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool());
        val validationError2 = rulesProcessor.processRules(studentRuleData2);
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PEN_NOT_FOUND.getCode());
    }

    @Test
    void testV104DemographicStudentPEN() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        StudentRuleData studentRuleData = createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool());
        val validationError1 = rulesProcessor.processRules(studentRuleData);
        assertThat(validationError1.size()).isZero();

        var incomingFileset2 = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet2 = incomingFilesetRepository.save(incomingFileset2);
        var demStudent2 = createMockDemographicStudent(savedFileSet2);
        var courseStudent2 = createMockCourseStudent(savedFileSet2);
        demStudent2.setIncomingFileset(courseStudent2.getIncomingFileset());
        StudentRuleData studentRuleData2 = createMockStudentRuleData(demStudent2, courseStudent2, createMockAssessmentStudent(), createMockSchool());
        val validationError2 = rulesProcessor.processRules(studentRuleData2);
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PEN_MISMATCH.getCode());
    }

    @Test
    void testV105DemographicStudentName() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        StudentRuleData studentRuleData = createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool());
        val validationError1 = rulesProcessor.processRules(studentRuleData);
        assertThat(validationError1.size()).isZero();

        var demStudent2 = createMockDemographicStudent(savedFileSet);
        demStudent2.setLastName("A");
        StudentRuleData studentRuleData2 = createMockStudentRuleData(demStudent2, courseStudent, createMockAssessmentStudent(), createMockSchool());
        val validationError2 = rulesProcessor.processRules(studentRuleData2);
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.LAST_NAME.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_SURNAME_MISMATCH.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(
    "SURNAME mismatch. School submitted: A and the Ministry PEN system has: JACKSON. If the submitted SURNAME is correct, request a PEN update through EDX Secure Messaging https://educationdataexchange.gov.bc.ca/login.");

        demStudent2.setLastName("");
        StudentRuleData studentRuleData5 = createMockStudentRuleData(demStudent2, courseStudent, createMockAssessmentStudent(), createMockSchool());
        val validationError5 = rulesProcessor.processRules(studentRuleData5);
        assertThat(validationError5.size()).isNotZero();
        assertThat(validationError5.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.LAST_NAME.getCode());
        assertThat(validationError5.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_SURNAME_MISMATCH.getCode());
        assertThat(validationError5.getFirst().getValidationIssueDescription()).isEqualTo(
                "SURNAME mismatch. School submitted a blank surname and the Ministry PEN system has: JACKSON. If the submitted SURNAME is correct, request a PEN update through EDX Secure Messaging https://educationdataexchange.gov.bc.ca/login.");

        demStudent2.setLastName(null);
        StudentRuleData studentRuleDat6 = createMockStudentRuleData(demStudent2, courseStudent, createMockAssessmentStudent(), createMockSchool());
        val validationError6 = rulesProcessor.processRules(studentRuleDat6);
        assertThat(validationError6.size()).isNotZero();
        assertThat(validationError6.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.LAST_NAME.getCode());
        assertThat(validationError6.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_SURNAME_MISMATCH.getCode());
        assertThat(validationError6.getFirst().getValidationIssueDescription()).isEqualTo(
                "SURNAME mismatch. School submitted a blank surname and the Ministry PEN system has: JACKSON. If the submitted SURNAME is correct, request a PEN update through EDX Secure Messaging https://educationdataexchange.gov.bc.ca/login.");

        var demStudent3 = createMockDemographicStudent(savedFileSet);
        demStudent3.setMiddleName("A");
        StudentRuleData studentRuleData3 = createMockStudentRuleData(demStudent3, courseStudent, createMockAssessmentStudent(), createMockSchool());
        val validationError3 = rulesProcessor.processRules(studentRuleData3);
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.MIDDLE_NAME.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_MIDDLE_MISMATCH.getCode());
        assertThat(validationError3.getFirst().getValidationIssueDescription()).isEqualTo(
                "MIDDLE NAME mismatch. School submitted: A but the Ministry PEN system is blank. If the submitted MIDDLE NAME is correct, request a PEN update through EDX Secure Messaging https://educationdataexchange.gov.bc.ca/login.");

        var demStudent4 = createMockDemographicStudent(savedFileSet);
        demStudent4.setFirstName("A");
        StudentRuleData studentRuleData4 = createMockStudentRuleData(demStudent4, courseStudent, createMockAssessmentStudent(), createMockSchool());
        val validationError4 = rulesProcessor.processRules(studentRuleData4);
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FIRST_NAME.getCode());
        assertThat(validationError4.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_GIVEN_MISMATCH.getCode());
        assertThat(validationError4.getFirst().getValidationIssueDescription()).isEqualTo(
    "FIRST NAME mismatch. School submitted: A and the Ministry PEN system has: JIM. If the submitted FIRST NAME is correct, request a PEN update through EDX Secure Messaging https://educationdataexchange.gov.bc.ca/login.");
    }

    @Test
    void testV106DemographicStudentBirthdate() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        StudentRuleData studentRuleData = createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool());
        val validationError1 = rulesProcessor.processRules(studentRuleData);
        assertThat(validationError1.size()).isZero();

        var demStudent2 = createMockDemographicStudent(savedFileSet);
        demStudent2.setBirthdate("12341212");
        StudentRuleData studentRuleData2 = createMockStudentRuleData(demStudent2, courseStudent, createMockAssessmentStudent(), createMockSchool());
        val validationError2 = rulesProcessor.processRules(studentRuleData2);
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.BIRTHDATE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_MISMATCH.getCode());
    }

    @Test
    void testV107DemographicStudentAddress() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());
        
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(savedFileSet), createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setGrade("12");
        demographicStudent.setAddressLine1("");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.ADDRESS1.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_ADDRESS_BLANK.getCode());

        var demographicStudent2 = createMockDemographicStudent(savedFileSet);
        demographicStudent2.setGrade("12");
        demographicStudent2.setAddressLine1(null);
        demographicStudent2.setAddressLine2("not null");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isZero();

        var demographicStudent3 = createMockDemographicStudent(savedFileSet);
        demographicStudent3.setGrade("12");
        demographicStudent3.setCity("");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent3, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.CITY.getCode());
        assertThat(validationError4.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_CITY_BLANK.getCode());

        var demographicStudent4 = createMockDemographicStudent(savedFileSet);
        demographicStudent4.setGrade("12");
        demographicStudent4.setPostalCode("123456");
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent4, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError5.size()).isNotZero();
        assertThat(validationError5.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.POSTAL_CODE.getCode());
        assertThat(validationError5.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_POSTAL_CODE_INVALID.getCode());

        var demographicStudent5 = createMockDemographicStudent(savedFileSet);
        demographicStudent5.setGrade("12");
        demographicStudent5.setProvincialCode("AB");
        val validationError6 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent5, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError6.size()).isNotZero();
        assertThat(validationError6.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PROVINCIAL_CODE.getCode());
        assertThat(validationError6.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROVINCE_CODE_INVALID.getCode());

        var demographicStudent6 = createMockDemographicStudent(savedFileSet);
        demographicStudent6.setGrade("12");
        demographicStudent6.setCountryCode("US");
        val validationError7 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent6, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError7.size()).isNotZero();
        assertThat(validationError7.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COUNTRY_CODE.getCode());
        assertThat(validationError7.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_COUNTRY_CODE_INVALID.getCode());

        var demographicStudent8 = createMockDemographicStudent(savedFileSet);
        demographicStudent8.setCountryCode("US");
        demographicStudent8.setProvincialCode("AB");
        demographicStudent8.setPostalCode("12AA56");
        demographicStudent8.setCity("");
        demographicStudent8.setAddressLine1(null);
        demographicStudent8.setAddressLine2("not null");
        val validationError8 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent8, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError8.size()).isZero();
    }

    @Test
    void testV108DemographicStudentAdultBirthdate() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
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
        studentApiStudent.setDob("20200101");
        studentApiStudent.setStatusCode(StudentStatusCodes.A.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent);

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setGradRequirementYear(GradRequirementYearCodes.YEAR_1950.getCode());
        demographicStudent.setBirthdate("20200101");
        demographicStudent.setGrade("AD");
        demographicStudent.setSchoolCertificateCompletionDate("");
        assertThat(demographicStudent.getGradRequirementYear()).isEqualTo(GradRequirementYearCodes.YEAR_1950.getCode());

        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(null);

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_ADULT.getCode());
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.BIRTHDATE.getCode());
    }

    @Test
    void testV109DemographicStudentCitizenship() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setCitizenship("Z");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.CITIZENSHIP.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_CITIZENSHIP_CODE_INVALID.getCode());

        demographicStudent.setCitizenship(null);
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isZero();

        demographicStudent.setCitizenship("");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError4.size()).isZero();
    }

    @Test
    void testV110DemographicValidGradeRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setGrade("22");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.GRADE_INVALID.getCode());

        var demographicStudent2 = createMockDemographicStudent(savedFileSet);
        demographicStudent2.setGrade(null);
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.GRADE.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.GRADE_INVALID.getCode());
    }

    @Test
    void testV111DemographicValidGradeRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setGrade("07");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.GRADE_NOT_IN_GRAD.getCode());
    }

    @Test
    void testV112DemographicValidGradeProgramRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setGradRequirementYear("1950");
        demographicStudent.setGrade("08");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.GRADE_AG_INVALID.getCode());
    }

    @Test
    void testV113DemographicValidGradeProgramRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());
        
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setGradRequirementYear("SCCP");
        demographicStudent.setGrade("AD");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.GRADE_OG_INVALID.getCode());
    }

    @Test
    void testV114DemographicProgramCodeRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());
        
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setProgramCode1("ZEE");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PROGRAM_CODE_1.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getCode());

        var demographicStudent2 = createMockDemographicStudent(savedFileSet);
        demographicStudent2.setProgramCode1("ZE");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PROGRAM_CODE_1.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getCode());

        var demographicStudent3 = createMockDemographicStudent(savedFileSet);
        demographicStudent3.setProgramCode1("Z");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent3, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PROGRAM_CODE_1.getCode());
        assertThat(validationError4.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getCode());
    }

    @Test
    void testV116DemographicValidStatusRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
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
        studentApiStudent.setDob("19900101");
        studentApiStudent.setStatusCode(StudentStatusCodes.A.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent);

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        studentApiStudent.setStatusCode(StudentStatusCodes.M.getCode());
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_MERGED.getCode());

        var demographicStudent2 = createMockDemographicStudent(savedFileSet);
        demographicStudent.setStudentStatusCode(null);
        studentApiStudent.setStatusCode(StudentStatusCodes.A.getCode());
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isZero();
    }

    @Test
    void testV117DemographicValidStatusRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setStudentStatus("Z");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.STUDENT_STATUS.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_INVALID.getCode());

        var demographicStudent2 = createMockDemographicStudent(savedFileSet);
        demographicStudent2.setStudentStatus(null);
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.STUDENT_STATUS.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_INVALID.getCode());
    }

    @Test
    void testV118DemographicStudentStatus() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());
        var school = createMockSchool();
        school.setSchoolId("03636018");

        StudentRuleData studentRuleData = createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), school);
        val validationError1 = rulesProcessor.processRules(studentRuleData);
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setStudentStatus(StudentStatusCodes.D.getCode());
        StudentRuleData studentRuleData2 = createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), school);
        val validationError2 = rulesProcessor.processRules(studentRuleData2);
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.STUDENT_STATUS.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_PEN_MISMATCH.getCode());

        var demographicStudent2 = createMockDemographicStudent(savedFileSet);
        demographicStudent2.setStudentStatus(StudentStatusCodes.T.getCode());
        StudentRuleData studentRuleData3 = createMockStudentRuleData(demographicStudent2, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), school);
        val validationError3 = rulesProcessor.processRules(studentRuleData3);
        assertThat(validationError3.size()).isZero();
    }

    @Test
    void testV119DemographicStudentStatus() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        StudentRuleData studentRuleData = createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool());
        val validationError1 = rulesProcessor.processRules(studentRuleData);
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setStudentStatus("T");
        SchoolTombstone schoolTombstone = createMockSchool();
        schoolTombstone.setMincode("03636012");
        StudentRuleData studentRuleData2 = createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), schoolTombstone);
        val validationError2 = rulesProcessor.processRules(studentRuleData2);
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.STUDENT_STATUS.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_SCHOOL_OF_RECORD_MISMATCH.getCode());
    }

    @Test
    void testV120DemographicStudentStatus() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        StudentRuleData studentRuleData = createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool());
        val validationError1 = rulesProcessor.processRules(studentRuleData);
        assertThat(validationError1.size()).isZero();

        Student studentApiStudent = new Student();
        studentApiStudent.setStudentID(UUID.randomUUID().toString());
        studentApiStudent.setPen("123456789");
        studentApiStudent.setLocalID("8887555");
        studentApiStudent.setLegalFirstName("JIM");
        studentApiStudent.setLegalLastName("JACKSON");
        studentApiStudent.setDob("19900101");
        studentApiStudent.setStatusCode(StudentStatusCodes.D.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent);

        when(restUtils.getGradStudentRecordByStudentID(any(UUID.class), any(UUID.class)))
                .thenThrow(new EntityNotFoundException(GradStudentRecord.class));

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setStudentStatus("D");
        StudentRuleData studentRuleData2 = createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool());
        val validationError2 = rulesProcessor.processRules(studentRuleData2);
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.STUDENT_STATUS.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_INCORRECT_NEW_STUDENT.getCode());
    }

    @Test
    void testV121DemographicStudentProgramRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setGradRequirementYear("1332");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.GRAD_REQUIREMENT_YEAR.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode());

        var demographicStudent2 = createMockDemographicStudent(savedFileSet);
        demographicStudent2.setGradRequirementYear(null);
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.stream().noneMatch(code -> code.getValidationIssueFieldCode().equalsIgnoreCase(ValidationFieldCode.GRAD_REQUIREMENT_YEAR.getCode()))).isTrue();
        assertThat(validationError3.stream().noneMatch(code -> code.getValidationIssueCode().equalsIgnoreCase(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode()))).isTrue();
    }

    @Test
    void testV122DemographicStudentProgramRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setGradRequirementYear(GradRequirementYearCodes.YEAR_1950.getCode());
        demographicStudent.setGrade("AD");
        var school = createMockSchool();
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
    void testV123DemographicStudentProgramRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        var validationErrors = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationErrors).isEmpty();

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

        GraduationProgramCode expiredProgram = new GraduationProgramCode();
        expiredProgram.setProgramCode("2023");
        expiredProgram.setExpiryDate(new Date(System.currentTimeMillis() - 1000));
        when(restUtils.getGraduationProgramCodes()).thenReturn(List.of(expiredProgram));
        var validationErrors2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent2, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationErrors2).isNotEmpty();
        assertThat(validationErrors2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.GRAD_REQUIREMENT_YEAR.getCode());
        assertThat(validationErrors2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_PROGRAM_CLOSED.getCode());
    }

    @Test
    void testV125DemographicStudentProgramRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        GradStudentRecord gradStudentRecord = new GradStudentRecord();
        gradStudentRecord.setSchoolOfRecordId(UUID.randomUUID().toString());
        gradStudentRecord.setStudentStatusCode("CUR");
        gradStudentRecord.setGraduated("true");
        gradStudentRecord.setProgramCompletionDate("2023-01-01 00:00:00.000");

        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(gradStudentRecord);

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(savedFileSet), createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_ALREADY_GRADUATED.getCode());
    }


    @Test
    void testV126DemographicSCCPCompletionDate() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setSchoolCertificateCompletionDate("20041312");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.SCHOOL_CERTIFICATE_COMPLETION_DATE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getCode());

        var demographicStudent2 = createMockDemographicStudent(savedFileSet);
        demographicStudent2.setSchoolCertificateCompletionDate("20042");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.SCHOOL_CERTIFICATE_COMPLETION_DATE.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getCode());

        var demographicStudent3 = createMockDemographicStudent(savedFileSet);
        demographicStudent3.setSchoolCertificateCompletionDate(null);
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent3, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError4.size()).isZero();

        var demographicStudent4 = createMockDemographicStudent(savedFileSet);
        demographicStudent4.setSchoolCertificateCompletionDate("");
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent4, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError5.size()).isZero();
    }

    @Test
    void testV127DemographicSCCPCompletionDate() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setGradRequirementYear(GradRequirementYearCodes.YEAR_2023.getCode());
        assertThat(demographicStudent.getGradRequirementYear()).isEqualTo(GradRequirementYearCodes.YEAR_2023.getCode());

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isZero();

        var demographicStudent2 = createMockDemographicStudent(savedFileSet);
        demographicStudent2.setSchoolCertificateCompletionDate("20050701");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));

        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.SCHOOL_CERTIFICATE_COMPLETION_DATE.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getCode());
    }

    @Test
    void testV128DemographicStudentBirthdate() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(savedFileSet);
        demographicStudent.setBirthdate("");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.BIRTHDATE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getCode());

        var demographicStudent2 = createMockDemographicStudent(savedFileSet);
        demographicStudent2.setBirthdate("2222");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.BIRTHDATE.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getCode());

        var demographicStudent3 = createMockDemographicStudent(savedFileSet);
        demographicStudent3.setBirthdate("1990010");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent3, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.BIRTHDATE.getCode());
        assertThat(validationError4.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getCode());

        var demographicStudent4 = createMockDemographicStudent(savedFileSet);
        demographicStudent4.setBirthdate("199001011");
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent4, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError5.size()).isNotZero();
        assertThat(validationError5.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.BIRTHDATE.getCode());
        assertThat(validationError5.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getCode());

        var demographicStudent5 = createMockDemographicStudent(savedFileSet);
        demographicStudent5.setBirthdate(null);
        val validationError6 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent5, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError6.size()).isNotZero();
        assertThat(validationError6.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.BIRTHDATE.getCode());
        assertThat(validationError6.getFirst().getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getCode());
    }

    @Test
    void testV130DemographicStudentProgramNull() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudentRepository.save(courseStudent);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setPen(courseStudent.getPen());
        demStudent.setIncomingFileset(courseStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent2 = createMockDemographicStudent(savedFileSet);
        demographicStudent2.setGradRequirementYear(null);
        GradStudentRecord gradStudentRecord = new GradStudentRecord();
        gradStudentRecord.setProgramCompletionDate(null);
        gradStudentRecord.setGraduated("false");
        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(gradStudentRecord);
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3).anyMatch(error -> error.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL.getCode()));

        var demographicStudent3 = createMockDemographicStudent(savedFileSet);
        demographicStudent3.setGradRequirementYear(null);
        demographicStudent3.setGrade("GA");
        demographicStudent3.setSchoolCertificateCompletionDate("");
        var school = createMockSchool();
        school.setSchoolCategoryCode(SchoolCategoryCodes.FED_BAND.getCode());
        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(null);
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent3, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), school));
        assertThat(validationError4.size()).isZero();

        var demographicStudent4 = createMockDemographicStudent(savedFileSet);
        demographicStudent4.setGradRequirementYear(null);
        demographicStudent4.setGrade("08");
        school.setSchoolCategoryCode(SchoolCategoryCodes.FED_BAND.getCode());
        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(null);
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent4, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), school));
        assertThat(validationError5.size()).isNotZero();
        assertThat(validationError5).anyMatch(error -> error.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL.getCode()));

        var demographicStudent5 = createMockDemographicStudent(savedFileSet);
        demographicStudent5.setGradRequirementYear(null);
        demographicStudent5.setGrade("GA");
        school.setSchoolCategoryCode(SchoolCategoryCodes.PUBLIC.getCode());
        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(null);
        val validationError6 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent5, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), school));
        assertThat(validationError6.size()).isNotZero();
        assertThat(validationError6).anyMatch(error -> error.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL.getCode()));

        var demographicStudent6 = createMockDemographicStudent(savedFileSet);
        demographicStudent6.setGradRequirementYear(null);
        demographicStudent6.setGrade("08");
        school.setSchoolCategoryCode(SchoolCategoryCodes.PUBLIC.getCode());
        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(null);
        val validationError7 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent6, createMockCourseStudent(savedFileSet), createMockAssessmentStudent(), school));
        assertThat(validationError7.size()).isNotZero();
        assertThat(validationError7).anyMatch(error -> error.getValidationIssueCode().equals(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL.getCode()));
    }
}
