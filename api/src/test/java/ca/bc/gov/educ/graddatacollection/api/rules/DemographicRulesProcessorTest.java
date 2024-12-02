package ca.bc.gov.educ.graddatacollection.api.rules;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradRequirementYearCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.StudentStatusCodes;
import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentRulesProcessor;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.service.v1.DemographicRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.*;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.graddatacollection.api.struct.external.scholarships.v1.CitizenshipCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
class DemographicRulesProcessorTest extends BaseGradDataCollectionAPITest {

    @InjectMocks
    private DemographicRulesService demographicRulesService;

    @Autowired
    private DemographicStudentRulesProcessor rulesProcessor;

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
        Student studentApiStudent = new Student();
        studentApiStudent.setStudentID(UUID.randomUUID().toString());
        studentApiStudent.setPen("123456789");
        studentApiStudent.setStatusCode(StudentStatusCodes.A.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent);
        GradStudentRecord gradStudentRecord = new GradStudentRecord();
        gradStudentRecord.setSchoolOfRecord("03636018");
        gradStudentRecord.setStudentStatusCode("CUR");
        gradStudentRecord.setGraduated("false");
        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(gradStudentRecord);
    }

    @Test
    void testV102DemographicStudentPEN() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded()),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent.setPen("");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_PEN.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PEN_BLANK.getCode());

        var demographicStudent2 = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent2.setPen(null);
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_PEN.getCode());
        assertThat(validationError3.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PEN_BLANK.getCode());
    }

    @Test
    void testV107DemographicStudentAddress() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded()),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent.setAddressLine1("");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_ADDRESS.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_ADDRESS_BLANK.getCode());

        var demographicStudent2 = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent2.setAddressLine1(null);
        demographicStudent2.setAddressLine2("not null");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isZero();

        var demographicStudent3 = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent3.setCity("");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent3, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_ADDRESS.getCode());
        assertThat(validationError4.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_CITY_BLANK.getCode());

        var demographicStudent4 = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent4.setPostalCode("123456");
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent4, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError5.size()).isNotZero();
        assertThat(validationError5.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_ADDRESS.getCode());
        assertThat(validationError5.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_POSTAL_CODE_INVALID.getCode());

        var demographicStudent5 = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent5.setProvincialCode("AB");
        val validationError6 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent5, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError6.size()).isNotZero();
        assertThat(validationError6.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_ADDRESS.getCode());
        assertThat(validationError6.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROVINCE_CODE_INVALID.getCode());

        var demographicStudent6 = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent6.setCountryCode("US");
        val validationError7 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent6, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError7.size()).isNotZero();
        assertThat(validationError7.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_ADDRESS.getCode());
        assertThat(validationError7.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_COUNTRY_CODE_INVALID.getCode());
    }

    @Test
    void testV108DemographicStudentAdultBirthdate() {

        var demographicStudent = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent.setGradRequirementYear(GradRequirementYearCodes.YEAR_1950.getCode());
        demographicStudent.setBirthdate("20200101");
        demographicStudent.setGrade("AD");
        demographicStudent.setSchoolCertificateCompletionDate("");
        assertThat(demographicStudent.getGradRequirementYear()).isEqualTo(GradRequirementYearCodes.YEAR_1950.getCode());

        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(null);

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_ADULT.getCode());
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_BIRTHDATE.getCode());
    }

    @Test
    void testV109DemographicStudentCitizenship() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded()),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent.setCitizenship("Z");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_CITIZENSHIP_CODE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_CITIZENSHIP_CODE_INVALID.getCode());
    }

    @Test
    void testV110DemographicValidGradeRule() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded()),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent.setGrade("22");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_GRADE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.GRADE_INVALID.getCode());

        var demographicStudent2 = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent2.setGrade(null);
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_GRADE.getCode());
        assertThat(validationError3.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.GRADE_INVALID.getCode());
    }

    @Test
    void testV111DemographicValidGradeRule() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded()),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent.setGrade("07");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_GRADE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.GRADE_NOT_IN_GRAD.getCode());
    }

    @Test
    void testV112DemographicValidGradeProgramRule() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded()),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent.setGradRequirementYear("1950");
        demographicStudent.setGrade("08");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_GRADE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.GRADE_AG_INVALID.getCode());
    }

    @Test
    void testV113DemographicValidGradeProgramRule() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded()),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent.setGradRequirementYear("SCCP");
        demographicStudent.setGrade("AD");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_GRADE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.GRADE_OG_INVALID.getCode());
    }

    @Test
    void testV114DemographicProgramCodeRule() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded()),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent.setProgramCode1("ZEE");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_PROGRAM_CODE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getCode());

        var demographicStudent2 = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent2.setProgramCode1("ZE");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_PROGRAM_CODE.getCode());
        assertThat(validationError3.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getCode());

        var demographicStudent3 = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent3.setProgramCode1("Z");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent3, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_PROGRAM_CODE.getCode());
        assertThat(validationError4.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getCode());
    }

    @Test
    void testV116DemographicValidStatusRule() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded()),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent.setStudentStatusCode("M");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_STATUS.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_MERGED.getCode());

        var demographicStudent2 = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent.setStudentStatusCode(null);
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isZero();
    }

    @Test
    void testV117DemographicValidStatusRule() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded()),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent.setStudentStatusCode("Z");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_STATUS.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_INVALID.getCode());

        var demographicStudent2 = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent2.setStudentStatusCode(null);
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_STATUS.getCode());
        assertThat(validationError3.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_INVALID.getCode());
    }

    @Test
    void testV118DemographicStudentStatus() {
        StudentRuleData studentRuleData = createMockStudentRuleData(createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded()),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool());
        val validationError1 = rulesProcessor.processRules(studentRuleData);
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent.setStudentStatusCode(StudentStatusCodes.D.getCode());
        StudentRuleData studentRuleData2 = createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool());
        val validationError2 = rulesProcessor.processRules(studentRuleData2);
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_STATUS.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_PEN_MISMATCH.getCode());

        var demographicStudent2 = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent2.setStudentStatusCode(StudentStatusCodes.T.getCode());
        StudentRuleData studentRuleData3 = createMockStudentRuleData(demographicStudent2, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool());
        val validationError3 = rulesProcessor.processRules(studentRuleData3);
        assertThat(validationError3.size()).isZero();
    }

    @Test
    void testV119DemographicStudentStatus() {
        StudentRuleData studentRuleData = createMockStudentRuleData(createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded()),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool());
        val validationError1 = rulesProcessor.processRules(studentRuleData);
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent.setStudentStatusCode("T");
        SchoolTombstone schoolTombstone = createMockSchool();
        schoolTombstone.setMincode("03636012");
        StudentRuleData studentRuleData2 = createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), schoolTombstone);
        val validationError2 = rulesProcessor.processRules(studentRuleData2);
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_STATUS.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_SCHOOL_OF_RECORD_MISMATCH.getCode());
    }

    @Test
    void testV120DemographicStudentStatus() {
        StudentRuleData studentRuleData = createMockStudentRuleData(createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded()),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool());
        val validationError1 = rulesProcessor.processRules(studentRuleData);
        assertThat(validationError1.size()).isZero();

        Student studentApiStudent = new Student();
        studentApiStudent.setStudentID(UUID.randomUUID().toString());
        studentApiStudent.setPen("123456789");
        studentApiStudent.setStatusCode(StudentStatusCodes.D.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent);

        when(restUtils.getGradStudentRecordByStudentID(any(UUID.class), any(UUID.class)))
                .thenThrow(new EntityNotFoundException(GradStudentRecord.class));

        var demographicStudent = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent.setStudentStatusCode("D");
        StudentRuleData studentRuleData2 = createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool());
        val validationError2 = rulesProcessor.processRules(studentRuleData2);
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_STATUS.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_INCORRECT_NEW_STUDENT.getCode());
    }

    @Test
    void testV121DemographicStudentProgramRule() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded()),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent.setGradRequirementYear("1332");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_PROGRAM_CODE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode());

        var demographicStudent2 = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent2.setGradRequirementYear(null);
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_PROGRAM_CODE.getCode());
        assertThat(validationError3.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_INVALID.getCode());
    }

    @Test
    void testV123DemographicStudentProgramRule() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded()),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        GradStudentRecord gradStudentRecord = new GradStudentRecord();
        gradStudentRecord.setSchoolOfRecord("03636011");
        gradStudentRecord.setStudentStatusCode("CUR");
        gradStudentRecord.setGraduated("false");
        gradStudentRecord.setProgram("EXP");
        gradStudentRecord.setProgramCompletionDate("2024-01-01");
        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(gradStudentRecord);

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded()), createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_PROGRAM_CODE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_PROGRAM_CLOSED.getCode());
    }

    @Test
    void testV125DemographicStudentProgramRule() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded()),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        GradStudentRecord gradStudentRecord = new GradStudentRecord();
        gradStudentRecord.setSchoolOfRecord("03636011");
        gradStudentRecord.setStudentStatusCode("CUR");
        gradStudentRecord.setGraduated("true");
        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(gradStudentRecord);

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded()), createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_PROGRAM_CODE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_ALREADY_GRADUATED.getCode());
    }


    @Test
    void testV126DemographicSCCPCompletionDate() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded()),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent.setSchoolCertificateCompletionDate("20041312");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.SCCP_COMPLETION_DATE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getCode());

        var demographicStudent2 = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent2.setSchoolCertificateCompletionDate("20042");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.SCCP_COMPLETION_DATE.getCode());
        assertThat(validationError3.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getCode());

        var demographicStudent3 = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent3.setSchoolCertificateCompletionDate(null);
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent3, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError4.size()).isZero();

        var demographicStudent4 = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent4.setSchoolCertificateCompletionDate("");
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent4, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError5.size()).isZero();
    }

    @Test
    void testV127DemographicSCCPCompletionDate() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded()),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent.setGradRequirementYear(GradRequirementYearCodes.YEAR_2023.getCode());
        assertThat(demographicStudent.getGradRequirementYear()).isEqualTo(GradRequirementYearCodes.YEAR_2023.getCode());

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.SCCP_COMPLETION_DATE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_STUDENT_PROGRAM.getCode());

        var demographicStudent2 = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent2.setSchoolCertificateCompletionDate("20050701");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));

        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.SCCP_COMPLETION_DATE.getCode());
        assertThat(validationError3.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getCode());
    }

    @Test
    void testV128DemographicStudentBirthdate() {
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded()),createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var demographicStudent = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent.setBirthdate("");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_BIRTHDATE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getCode());

        var demographicStudent2 = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent2.setBirthdate("2222");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent2, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_BIRTHDATE.getCode());
        assertThat(validationError3.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getCode());

        var demographicStudent3 = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent3.setBirthdate("1990010");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent3, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_BIRTHDATE.getCode());
        assertThat(validationError4.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getCode());

        var demographicStudent4 = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent4.setBirthdate("199001011");
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent4, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError5.size()).isNotZero();
        assertThat(validationError5.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_BIRTHDATE.getCode());
        assertThat(validationError5.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getCode());

        var demographicStudent5 = createMockDemographicStudent(createMockIncomingFilesetEntityWithAllFilesLoaded());
        demographicStudent5.setBirthdate(null);
        val validationError6 = rulesProcessor.processRules(createMockStudentRuleData(demographicStudent5, createMockCourseStudent(), createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError6.size()).isNotZero();
        assertThat(validationError6.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.STUDENT_BIRTHDATE.getCode());
        assertThat(validationError6.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getCode());
    }
}
