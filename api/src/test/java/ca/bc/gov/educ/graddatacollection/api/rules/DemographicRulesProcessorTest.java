package ca.bc.gov.educ.graddatacollection.api.rules;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradRequirementYearCodes;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentRulesProcessor;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.CareerProgramCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GradGrade;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.OptionalProgramCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.scholarships.v1.CitizenshipCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Slf4j
class DemographicRulesProcessorTest extends BaseGradDataCollectionAPITest {

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
                        new GradGrade("08", "Grade 8", "", 1, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), null, "8", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new GradGrade("09", "Grade 9", "", 2, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), null, "9", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new GradGrade("10", "Grade 10", "", 3, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), null, "10", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new GradGrade("11", "Grade 11", "e", 4, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), null, "11", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new GradGrade("12", "Grade 12", "", 5, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), null, "12", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new GradGrade("AD", "Adult", "", 6, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), null, "AD", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new GradGrade("AN", "Adult Non-Graduate", "", 7, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), null, "AN", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new GradGrade("HS", "Home School", "", 8, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), null, "HS", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new GradGrade("SU", "Secondary Ungraded", "", 9, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), null, "SU", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new GradGrade("GA", "Graduated Adult", "", 10, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), null, "GA", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now())
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
                        new OptionalProgramCode(UUID.randomUUID(), "FR", "SCCP French Certificate", "", 1, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), "", "", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new OptionalProgramCode(UUID.randomUUID(), "AD", "Advanced Placement", "", 2, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), "", "", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now()),
                        new OptionalProgramCode(UUID.randomUUID(), "DD", "Dual Dogwood", "", 3, LocalDateTime.parse("2020-01-01T00:00:00"), LocalDateTime.parse("2099-12-31T23:59:59"), "", "", "unitTests", LocalDateTime.now(), "unitTests", LocalDateTime.now())
                )
        );
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
        demographicStudent.setProgramCode1(null);
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
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.get(0).getValidationIssueFieldCode()).isEqualTo(DemographicStudentValidationFieldCode.SCCP_COMPLETION_DATE.getCode());
        assertThat(validationError4.get(0).getValidationIssueCode()).isEqualTo(DemographicStudentValidationIssueTypeCode.SCCP_INVALID_DATE.getCode());
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
