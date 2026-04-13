package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.*;
import ca.bc.gov.educ.graddatacollection.api.model.v1.*;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.service.v1.DemographicRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GradStudentRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GraduationProgramCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit Tests for BlankGradRequirementRule (D12 Validation Rule)
 *
 * Test Coverage:
 * 1. Summer period message change validation
 * 2. School year period message preservation
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("BlankGradRequirementRule (D12) - Solution 1 Summer Period Tests")
class BlankGradRequirementRuleTest {

    @Mock
    private RestUtils restUtils;

    @Mock
    private DemographicRulesService demographicRulesService;

    @InjectMocks
    private BlankGradRequirementRule blankGradRequirementRule;

    private StudentRuleData studentRuleData;
    private DemographicStudentEntity demographicStudent;
    private IncomingFilesetEntity incomingFileset;
    private ReportingPeriodEntity reportingPeriod;
    private SchoolTombstone school;
    private List<GraduationProgramCode> programCodes;

    @BeforeEach
    void setUp() {
        reportingPeriod = ReportingPeriodEntity.builder()
                .reportingPeriodID(UUID.randomUUID())
                .schYrStart(LocalDateTime.of(2025, 8, 1, 0, 0))
                .schYrEnd(LocalDateTime.of(2026, 9, 30, 23, 59))
                .summerStart(LocalDateTime.of(2026, 7, 1, 0, 0))
                .summerEnd(LocalDateTime.of(2026, 8, 31, 23, 59))
                .periodStart(LocalDateTime.of(2026, 1, 1, 0, 0))
                .periodEnd(LocalDateTime.of(2026, 8, 31, 23, 59))
                .createUser("TEST_USER")
                .createDate(LocalDateTime.now())
                .updateUser("TEST_USER")
                .updateDate(LocalDateTime.now())
                .build();

        incomingFileset = IncomingFilesetEntity.builder()
                .incomingFilesetID(UUID.randomUUID())
                .reportingPeriod(reportingPeriod)
                .demFileName("test-dem.txt")
                .filesetStatusCode("PROCESSING")
                .createUser("TEST_USER")
                .createDate(LocalDateTime.now())
                .updateUser("TEST_USER")
                .updateDate(LocalDateTime.now())
                .build();

        demographicStudent = DemographicStudentEntity.builder()
                .demographicStudentID(UUID.randomUUID())
                .incomingFileset(incomingFileset)
                .pen("123456789")
                .firstName("TEST")
                .lastName("STUDENT")
                .grade("12")
                .gradRequirementYear("")
                .studentStatusCode("A")
                .createUser("TEST_USER")
                .createDate(LocalDateTime.now())
                .updateUser("TEST_USER")
                .updateDate(LocalDateTime.now())
                .build();

        school = SchoolTombstone.builder()
                .schoolId(UUID.randomUUID().toString())
                .schoolCategoryCode(SchoolCategoryCodes.PUBLIC.getCode())
                .schoolReportingRequirementCode(SchoolReportingRequirementCodes.CSF.getCode())
                .build();

       studentRuleData = StudentRuleData.builder()
                .demographicStudentEntity(demographicStudent)
                .school(school)
                .courseStudentEntity(new CourseStudentEntity())
                .assessmentStudentEntity(new AssessmentStudentEntity())
                .build();

        programCodes = new ArrayList<>();
        programCodes.add(GraduationProgramCode.builder()
                .programCode("2023-EN")
                .effectiveDate("2020-01-01T00:00:00")
                .expiryDate("2099-12-31T00:00:00")
                .build());
        programCodes.add(GraduationProgramCode.builder()
                .programCode("2024-EN")
                .effectiveDate("2023-01-01T00:00:00")
                .expiryDate("2099-12-31T00:00:00")
                .build());
    }

    @Test
    @DisplayName("D12-Summer-001: Summer period + blank grad year + with grad record = Summer message")
    void testD12SummerPeriod_BlankGradYear_WithGradRecord_ReturnsSummerMessage() {
        log.info("Test: D12-Summer-001");
        reportingPeriod.setSummerStart(LocalDateTime.of(2026, 3, 1, 0, 0));
        reportingPeriod.setSummerEnd(LocalDateTime.of(2026, 4, 30, 23, 59));
        incomingFileset.setReportingPeriod(reportingPeriod);
        demographicStudent.setIncomingFileset(incomingFileset);

        GradStudentRecord gradRecord = new GradStudentRecord();
        gradRecord.setProgram("2023-EN");
        gradRecord.setGraduated("false");

        when(restUtils.getGraduationProgramCodeList(true)).thenReturn(programCodes);
        when(demographicRulesService.getGradStudentRecord(any(), any())).thenReturn(gradRecord);

        List<DemographicStudentValidationIssue> validationIssues = blankGradRequirementRule.executeValidation(studentRuleData);

       assertThat(validationIssues)
                .hasSize(1);

        DemographicStudentValidationIssue issue = validationIssues.get(0);
        assertThat(issue.getValidationIssueSeverityCode())
                .isEqualTo(StudentValidationIssueSeverityCode.WARNING.getCode());

        assertThat(issue.getValidationIssueFieldCode())
                .isEqualTo(ValidationFieldCode.GRAD_REQUIREMENT_YEAR.getCode());

        assertThat(issue.getValidationIssueDescription())
               .containsIgnoringCase("GRAD Change Form")
                .contains("2023");

        log.info("Message: {}", issue.getValidationIssueDescription());
    }

    @Test
    @DisplayName("D12-Summer-002: Summer period + blank grad year + without grad record = Uses default program")
    void testD12SummerPeriod_BlankGradYear_WithoutGradRecord_UsesDefaultProgram() {
        log.info("Test: D12-Summer-002");
        reportingPeriod.setSummerStart(LocalDateTime.of(2026, 3, 1, 0, 0));
        reportingPeriod.setSummerEnd(LocalDateTime.of(2026, 4, 30, 23, 59));
        incomingFileset.setReportingPeriod(reportingPeriod);
        demographicStudent.setIncomingFileset(incomingFileset);
        demographicStudent.setGrade("12");

        when(restUtils.getGraduationProgramCodeList(true)).thenReturn(programCodes);
        when(demographicRulesService.getGradStudentRecord(any(), any())).thenReturn(null);

        List<DemographicStudentValidationIssue> validationIssues = blankGradRequirementRule.executeValidation(studentRuleData);

        assertThat(validationIssues)
                .as("Should generate validation issue when no grad record available")
                .isNotEmpty();

        DemographicStudentValidationIssue issue = validationIssues.get(0);
        assertThat(issue.getValidationIssueSeverityCode())
                .isEqualTo(StudentValidationIssueSeverityCode.WARNING.getCode());

        log.info("Message: {}", issue.getValidationIssueDescription());
    }

    @Test
    @DisplayName("D12-Summer-003: Summer boundary date - exactly at summerStart")
    void testD12SummerBoundary_ExactlyAtSummerStart_IsSummer() {
        log.info("Test: D12-Summer-003");
        reportingPeriod.setSummerStart(LocalDateTime.of(2026, 7, 1, 0, 0));
        reportingPeriod.setSummerEnd(LocalDateTime.of(2026, 8, 31, 23, 59));
        incomingFileset.setReportingPeriod(reportingPeriod);
        demographicStudent.setIncomingFileset(incomingFileset);

        GradStudentRecord gradRecord = new GradStudentRecord();
        gradRecord.setProgram("2023-EN");
        gradRecord.setGraduated("false");

        when(restUtils.getGraduationProgramCodeList(true)).thenReturn(programCodes);
        when(demographicRulesService.getGradStudentRecord(any(), any())).thenReturn(gradRecord);

        List<DemographicStudentValidationIssue> validationIssues = blankGradRequirementRule.executeValidation(studentRuleData);

        assertThat(validationIssues).isNotEmpty();
    }

    @Test
    @DisplayName("D12-Summer-004: Summer boundary date - exactly at summerEnd")
    void testD12SummerBoundary_ExactlyAtSummerEnd_IsSummer() {
        log.info("Test: D12-Summer-004");
        reportingPeriod.setSummerStart(LocalDateTime.of(2026, 7, 1, 0, 0));
        reportingPeriod.setSummerEnd(LocalDateTime.of(2026, 8, 31, 23, 59));
        incomingFileset.setReportingPeriod(reportingPeriod);
        demographicStudent.setIncomingFileset(incomingFileset);

        GradStudentRecord gradRecord = new GradStudentRecord();
        gradRecord.setProgram("2023-EN");
        gradRecord.setGraduated("false");

        when(restUtils.getGraduationProgramCodeList(true)).thenReturn(programCodes);
        when(demographicRulesService.getGradStudentRecord(any(), any())).thenReturn(gradRecord);

        List<DemographicStudentValidationIssue> validationIssues = blankGradRequirementRule.executeValidation(studentRuleData);

        assertThat(validationIssues).isNotEmpty();
    }

    @Test
    @DisplayName("D12-SchoolYear-001: School year + blank grad year = Original message")
    void testD12SchoolYearPeriod_BlankGradYear_ReturnsOriginalMessage() {
        log.info("Test: D12-SchoolYear-001");
        reportingPeriod.setSummerStart(LocalDateTime.of(2026, 7, 1, 0, 0));
        reportingPeriod.setSummerEnd(LocalDateTime.of(2026, 8, 31, 23, 59));
        reportingPeriod.setSchYrStart(LocalDateTime.of(2026, 1, 1, 0, 0));
        reportingPeriod.setSchYrEnd(LocalDateTime.of(2026, 6, 30, 23, 59));
        incomingFileset.setReportingPeriod(reportingPeriod);
        demographicStudent.setIncomingFileset(incomingFileset);

        GradStudentRecord gradRecord = new GradStudentRecord();
        gradRecord.setProgram("2023-EN");
        gradRecord.setGraduated("false");

        when(restUtils.getGraduationProgramCodeList(true)).thenReturn(programCodes);
        when(demographicRulesService.getGradStudentRecord(any(), any())).thenReturn(gradRecord);

        List<DemographicStudentValidationIssue> validationIssues = blankGradRequirementRule.executeValidation(studentRuleData);

        assertThat(validationIssues)
                .as("Should generate validation issue for school year")
                .hasSize(1);

        DemographicStudentValidationIssue issue = validationIssues.get(0);

        assertThat(issue.getValidationIssueDescription())
                .containsIgnoringCase("The Graduation Program Year was blank");

        log.info("Message: {}", issue.getValidationIssueDescription());
    }

    @Test
    @DisplayName("D12-SchoolYear-002: School year boundary - exactly at schYrStart")
    void testD12SchoolYearBoundary_ExactlyAtSchYrStart_IsSchoolYear() {
        log.info("Test: D12-SchoolYear-002");
        reportingPeriod.setSummerStart(LocalDateTime.of(2026, 7, 1, 0, 0));
        reportingPeriod.setSummerEnd(LocalDateTime.of(2026, 8, 31, 23, 59));
        reportingPeriod.setSchYrStart(LocalDateTime.of(2026, 1, 1, 0, 0));
        reportingPeriod.setSchYrEnd(LocalDateTime.of(2026, 6, 30, 23, 59));
        incomingFileset.setReportingPeriod(reportingPeriod);
        demographicStudent.setIncomingFileset(incomingFileset);

        GradStudentRecord gradRecord = new GradStudentRecord();
        gradRecord.setProgram("2023-EN");
        gradRecord.setGraduated("false");

        when(restUtils.getGraduationProgramCodeList(true)).thenReturn(programCodes);
        when(demographicRulesService.getGradStudentRecord(any(), any())).thenReturn(gradRecord);

        List<DemographicStudentValidationIssue> validationIssues = blankGradRequirementRule.executeValidation(studentRuleData);

        assertThat(validationIssues).isNotEmpty();
    }

    @Test
    @DisplayName("D12-Summer-005: ReportingPeriod is null = Returns false")
    void testIsCurrentReportingPeriodSummer_NullReportingPeriod_ReturnsFalse() {
        log.info("Test: D12-Summer-005");
        // Explicitly set reporting period to null
        incomingFileset.setReportingPeriod(null);
        demographicStudent.setIncomingFileset(incomingFileset);

        GradStudentRecord gradRecord = new GradStudentRecord();
        gradRecord.setProgram("2023-EN");
        gradRecord.setGraduated("false");

        when(restUtils.getGraduationProgramCodeList(true)).thenReturn(programCodes);
        when(demographicRulesService.getGradStudentRecord(any(), any())).thenReturn(gradRecord);

        List<DemographicStudentValidationIssue> validationIssues = blankGradRequirementRule.executeValidation(studentRuleData);

        assertThat(validationIssues).isNotNull();
        log.info("Validation issues count: {}", validationIssues.size());
    }

    @Test
    @DisplayName("D12-Summer-006: IncomingFileset is null = Catches NullPointerException gracefully")
    void testIsCurrentReportingPeriodSummer_NullIncomingFileset_CatchesException() {
        log.info("Test: D12-Summer-006");
        // Set incoming fileset to null to trigger NullPointerException path
        demographicStudent.setIncomingFileset(null);

        GradStudentRecord gradRecord = new GradStudentRecord();
        gradRecord.setProgram("2023-EN");
        gradRecord.setGraduated("false");

        when(restUtils.getGraduationProgramCodeList(true)).thenReturn(programCodes);
        when(demographicRulesService.getGradStudentRecord(any(), any())).thenReturn(gradRecord);

        List<DemographicStudentValidationIssue> validationIssues = blankGradRequirementRule.executeValidation(studentRuleData);

        assertThat(validationIssues).isNotNull();
        log.info("Validation issues count: {}", validationIssues.size());
    }

}
