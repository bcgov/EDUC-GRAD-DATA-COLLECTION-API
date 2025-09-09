package ca.bc.gov.educ.graddatacollection.api.rules;


import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.NumeracyAssessmentCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolReportingRequirementCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.StudentStatusCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.AssessmentStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ReportingPeriodRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentRulesProcessor;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.ruleset.CourseCodeNumeracyRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.AssessmentRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1.Assessment;
import ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1.AssessmentStudentDetailResponse;
import ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1.Session;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
class AssessmentRulesProcessorTest extends BaseGradDataCollectionAPITest {

    @Autowired
    private RestUtils restUtils;

    @Autowired
    private IncomingFilesetRepository incomingFilesetRepository;

    @Autowired
    private DemographicStudentRepository demographicStudentRepository;

    @Autowired
    private AssessmentStudentRepository assessmentStudentRepository;

    @Autowired
    private AssessmentStudentRulesProcessor rulesProcessor;

    @Autowired
    private ReportingPeriodRepository reportingPeriodRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.demographicStudentRepository.deleteAll();
        this.incomingFilesetRepository.deleteAll();
        this.assessmentStudentRepository.deleteAll();
        this.reportingPeriodRepository.deleteAll();

        Student studentApiStudent = new Student();
        studentApiStudent.setStudentID(UUID.randomUUID().toString());
        studentApiStudent.setPen("123456789");
        studentApiStudent.setLocalID("8887555");
        studentApiStudent.setLegalFirstName("JIM");
        studentApiStudent.setLegalLastName("JACKSON");
        studentApiStudent.setDob("1990-01-01");
        studentApiStudent.setStatusCode(StudentStatusCodes.A.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent);

        AssessmentStudentDetailResponse response = new AssessmentStudentDetailResponse();
        response.setHasPriorRegistration(false);
        response.setNumberOfAttempts("1");
        when(this.restUtils.getAssessmentStudentDetail(any(),any())).thenReturn(response);
        when(this.restUtils.getSchoolBySchoolID(any())).thenReturn(Optional.of(createMockSchoolTombstone()));
    }

    @Test
    void testV01StudentPENRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Session session = new Session();
        Assessment assessment = new Assessment();
        assessment.setAssessmentID(UUID.randomUUID().toString());
        session.setAssessments(Arrays.asList(assessment));
        assessment.setAssessmentTypeCode(assessmentStudent.getCourseCode());
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(any(),any())).thenReturn(Optional.of(session));

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(null), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        var assessmentStudent2 = createMockAssessmentStudent();
        assessmentStudent2.setTransactionID("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(incomingFileset), createMockCourseStudent(incomingFileset), assessmentStudent2, createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode());
        assertThat(validationError2.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_DATA_MISSING.getMessage());
    }

    @Test
    void testV03CourseCodeRule() {
        Session sess = createMockSession();
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(anyString(), anyString())).thenReturn(Optional.of(sess));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());
        assessmentStudent.setCourseCode("LTE10");

        Session session = new Session();
        Assessment assessment = new Assessment();
        assessment.setAssessmentID(UUID.randomUUID().toString());
        assessment.setAssessmentTypeCode("LTE10");
        session.setAssessments(Arrays.asList(assessment));
        assessment.setAssessmentTypeCode(assessmentStudent.getCourseCode());
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(any(),any())).thenReturn(Optional.of(session));

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setCourseMonth("12");
        assessmentStudent.setCourseCode("LTE10");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError2.stream().anyMatch(err ->
            err.getValidationIssueFieldCode().equals(ValidationFieldCode.COURSE_MONTH.getCode()) &&
            err.getValidationIssueCode().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode()) &&
            err.getValidationIssueDescription().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getMessage())
        )).isTrue();

        assessmentStudent.setCourseMonth("11");
        assessmentStudent.setCourseCode("LTE10");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError3.size()).isZero();

        assessmentStudent.setCourseMonth("11");
        assessmentStudent.setCourseCode("MA10");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError4.stream().anyMatch(err ->
            err.getValidationIssueFieldCode().equals(ValidationFieldCode.COURSE_CODE.getCode()) &&
            err.getValidationIssueCode().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()) &&
            err.getValidationIssueDescription().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getMessage().formatted(assessmentStudent.getCourseCode(), assessmentStudent.getCourseYear(), assessmentStudent.getCourseMonth()))
        )).isTrue();

        assessmentStudent.setCourseMonth("12");
        assessmentStudent.setCourseCode("MA10");
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError5.stream().anyMatch(err ->
            err.getValidationIssueFieldCode().equals(ValidationFieldCode.COURSE_MONTH.getCode()) &&
            err.getValidationIssueCode().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode()) &&
            err.getValidationIssueDescription().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getMessage())
        )).isTrue();

        LocalDate current = LocalDate.now();

        assessmentStudent.setCourseYear(current.plusYears(1).format(DateTimeFormatter.ofPattern("yyyy")));
        assessmentStudent.setCourseMonth(current.plusYears(1).format(DateTimeFormatter.ofPattern("MM")));
        assessmentStudent.setCourseCode("MA10");
        val validationError9 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError9.stream().noneMatch(err ->
            err.getValidationIssueFieldCode().equals(ValidationFieldCode.COURSE_MONTH.getCode()) &&
            err.getValidationIssueCode().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_IN_THE_PAST.getCode()) &&
            err.getValidationIssueDescription().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_IN_THE_PAST.getMessage())
        )).isTrue();
        assertThat(validationError9.stream().noneMatch(err ->
            err.getValidationIssueFieldCode().equals(ValidationFieldCode.COURSE_YEAR.getCode()) &&
            err.getValidationIssueCode().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_IN_THE_PAST.getCode()) &&
            err.getValidationIssueDescription().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_IN_THE_PAST.getMessage())
        )).isTrue();

        assessmentStudent.setCourseYear(current.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy")));
        assessmentStudent.setCourseMonth(current.plusMonths(1).format(DateTimeFormatter.ofPattern("MM")));
        assessmentStudent.setCourseCode("MA10");
        val validationError10 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError10.stream().noneMatch(err ->
            err.getValidationIssueFieldCode().equals(ValidationFieldCode.COURSE_MONTH.getCode()) &&
            err.getValidationIssueCode().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_IN_THE_PAST.getCode()) &&
            err.getValidationIssueDescription().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_IN_THE_PAST.getMessage())
        )).isTrue();
        assertThat(validationError10.stream().noneMatch(err ->
            err.getValidationIssueFieldCode().equals(ValidationFieldCode.COURSE_YEAR.getCode()) &&
            err.getValidationIssueCode().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_IN_THE_PAST.getCode()) &&
            err.getValidationIssueDescription().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_IN_THE_PAST.getMessage())
        )).isTrue();

        assessmentStudent.setCourseYear(current.plusMonths(1).plusYears(1).format(DateTimeFormatter.ofPattern("yyyy")));
        assessmentStudent.setCourseMonth(current.plusMonths(1).plusYears(1).format(DateTimeFormatter.ofPattern("MM")));
        assessmentStudent.setCourseCode("MA10");
        val validationError11 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError11.stream().noneMatch(err ->
            err.getValidationIssueFieldCode().equals(ValidationFieldCode.COURSE_MONTH.getCode()) &&
            err.getValidationIssueCode().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_IN_THE_PAST.getCode()) &&
            err.getValidationIssueDescription().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_IN_THE_PAST.getMessage())
        )).isTrue();
        assertThat(validationError11.stream().noneMatch(err ->
            err.getValidationIssueFieldCode().equals(ValidationFieldCode.COURSE_YEAR.getCode()) &&
            err.getValidationIssueCode().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_IN_THE_PAST.getCode()) &&
            err.getValidationIssueDescription().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_IN_THE_PAST.getMessage())
        )).isTrue();
    }

    @Test
    void testV19ProficiencyScoreRule() {
        Session sess = createMockSession();
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(anyString(), anyString())).thenReturn(Optional.of(sess));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());
        assessmentStudent.setCourseCode("LTE10");

        Session session = new Session();
        Assessment assessment = new Assessment();
        assessment.setAssessmentID(UUID.randomUUID().toString());
        session.setAssessments(Arrays.asList(assessment));
        assessment.setAssessmentTypeCode(assessmentStudent.getCourseCode());
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(any(),any())).thenReturn(Optional.of(session));

        assessmentStudent.setCourseStatus("A");
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setCourseStatus("W");
        AssessmentStudentDetailResponse response = new AssessmentStudentDetailResponse();
        response.setHasPriorRegistration(true);
        response.setAlreadyWrittenAssessment(true);
        when(this.restUtils.getAssessmentStudentDetail(any(),any())).thenReturn(response);

        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_CODE.getCode());
        assertThat(validationError3.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_DUP.getCode());
        assertThat(validationError3.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_DUP.getMessage());

        response.setHasPriorRegistration(false);
        response.setAlreadyWrittenAssessment(true);
        when(this.restUtils.getAssessmentStudentDetail(any(),any())).thenReturn(response);

        assessmentStudent.setCourseStatus("A");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError4.size()).isZero();

        response.setHasPriorRegistration(true);
        response.setAlreadyWrittenAssessment(false);
        when(this.restUtils.getAssessmentStudentDetail(any(),any())).thenReturn(response);

        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError5.size()).isZero();

        response.setHasPriorRegistration(false);
        response.setAlreadyWrittenAssessment(false);
        when(this.restUtils.getAssessmentStudentDetail(any(),any())).thenReturn(response);

        val validationError6 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError6.size()).isZero();

        assessmentStudent.setCourseStatus("A");
        response.setHasPriorRegistration(true);
        response.setAlreadyWrittenAssessment(true);
        when(this.restUtils.getAssessmentStudentDetail(any(),any())).thenReturn(response);

        val validationError7 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError7.size()).isZero();
    }

    @Test
    void testV18CourseStatusRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Session session = new Session();
        Assessment assessment = new Assessment();
        assessment.setAssessmentID(UUID.randomUUID().toString());
        session.setAssessments(Arrays.asList(assessment));
        assessment.setAssessmentTypeCode(assessmentStudent.getCourseCode());
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(any(),any())).thenReturn(Optional.of(session));

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setCourseStatus("");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_STATUS.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getCode());
        assertThat(validationError2.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getMessage().formatted(assessmentStudent.getCourseStatus()));

        assessmentStudent.setCourseStatus("B");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_STATUS.getCode());
        assertThat(validationError3.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getCode());
        assertThat(validationError3.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getMessage().formatted(assessmentStudent.getCourseStatus()));

        assessmentStudent.setCourseStatus("A");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError4.size()).isZero();

        assessmentStudent.setCourseStatus("W");
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError5.stream().anyMatch(err ->
            err.getValidationIssueFieldCode().equals(ValidationFieldCode.COURSE_STATUS.getCode()) &&
            err.getValidationIssueCode().equals(AssessmentStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getCode()) &&
            err.getValidationIssueDescription().equals(AssessmentStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getMessage().formatted(assessmentStudent.getCourseStatus()))
        )).isFalse();
    }


    @Test
    void testV14ExamSchoolRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Session session = new Session();
        Assessment assessment = new Assessment();
        assessment.setAssessmentID(UUID.randomUUID().toString());
        session.setAssessments(Arrays.asList(assessment));
        assessment.setAssessmentTypeCode(assessmentStudent.getCourseCode());
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(any(),any())).thenReturn(Optional.of(session));

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setExamSchoolID(UUID.randomUUID());
        when(this.restUtils.getSchoolBySchoolID(any())).thenReturn(Optional.ofNullable(null));
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.EXAM_SCHOOL.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.EXAM_SCHOOL_INVALID.getCode());
        assertThat(validationError2.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.EXAM_SCHOOL_INVALID.getMessage());
    }

    @Test
    void testV15CourseSessionRule() {
        String futureSessionYear = LocalDate.now().plusYears(1).format(DateTimeFormatter.ofPattern("yyyy"));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Session session = new Session();
        Assessment assessment = new Assessment();
        assessment.setAssessmentID(UUID.randomUUID().toString());
        session.setAssessments(List.of(assessment));
        assessment.setAssessmentTypeCode(assessmentStudent.getCourseCode());
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(any(),any())).thenReturn(Optional.of(session));

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        assessmentStudentRepository.save(assessmentStudent);

        assessmentStudent.setAssessmentStudentID(null);
        assessmentStudentRepository.save(assessmentStudent);

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_SESSION.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DUPLICATE_XAM_RECORD.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DUPLICATE_XAM_RECORD.getMessage());

        Session numeracySession = new Session();
        var numeracyAssessments = NumeracyAssessmentCodes.getAllCodes().stream().map(code -> {
            Assessment a = new Assessment();
            a.setAssessmentID(UUID.randomUUID().toString());
            a.setAssessmentTypeCode(code);
            return a;
        }).toList();
        numeracySession.setAssessments(numeracyAssessments);
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(any(), any())).thenReturn(Optional.of(numeracySession));

        var numeracyStudent1 = createMockAssessmentStudent();
        numeracyStudent1.setPen(demStudent.getPen());
        numeracyStudent1.setLocalID(demStudent.getLocalID());
        numeracyStudent1.setLastName(demStudent.getLastName());
        numeracyStudent1.setIncomingFileset(demStudent.getIncomingFileset());
        numeracyStudent1.setCourseCode(NumeracyAssessmentCodes.NME10.getCode());
        numeracyStudent1.setCourseMonth("06");
        numeracyStudent1.setCourseYear(futureSessionYear);
        assessmentStudentRepository.save(numeracyStudent1);

        var numeracyStudent2 = createMockAssessmentStudent();
        numeracyStudent2.setPen(demStudent.getPen());
        numeracyStudent2.setLocalID(demStudent.getLocalID());
        numeracyStudent2.setLastName(demStudent.getLastName());
        numeracyStudent2.setIncomingFileset(demStudent.getIncomingFileset());
        numeracyStudent2.setCourseCode(NumeracyAssessmentCodes.NMF.getCode());
        numeracyStudent2.setCourseMonth("06");
        numeracyStudent2.setCourseYear(futureSessionYear);
        numeracyStudent2.setAssessmentStudentID(null);
        assessmentStudentRepository.save(numeracyStudent2);

        numeracyStudent2.setAssessmentStudentID(null);
        val numeracyValidationError = rulesProcessor.processRules(
            createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), numeracyStudent2, createMockSchoolTombstone())
        );
        assertThat(numeracyValidationError.size()).isNotZero();
        assertThat(numeracyValidationError.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_SESSION.getCode());
        assertThat(numeracyValidationError.getFirst().getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DUPLICATE_XAM_RECORD.getCode());
        assertThat(numeracyValidationError.getFirst().getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DUPLICATE_XAM_RECORD.getMessage());

        var nonNumeracyStudent = createMockAssessmentStudent();
        nonNumeracyStudent.setPen(demStudent.getPen());
        nonNumeracyStudent.setLocalID(demStudent.getLocalID());
        nonNumeracyStudent.setLastName(demStudent.getLastName());
        nonNumeracyStudent.setIncomingFileset(demStudent.getIncomingFileset());
        nonNumeracyStudent.setCourseCode("LTE10");
        nonNumeracyStudent.setCourseMonth("06");
        nonNumeracyStudent.setCourseYear(futureSessionYear);
        nonNumeracyStudent.setAssessmentStudentID(null);
        assessmentStudentRepository.save(nonNumeracyStudent);

        val nonNumeracyValidationError = rulesProcessor.processRules(
            createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), nonNumeracyStudent, createMockSchoolTombstone())
        );
        assertThat(nonNumeracyValidationError.stream().anyMatch(err ->
            err.getValidationIssueFieldCode().equals(ValidationFieldCode.COURSE_SESSION.getCode()) &&
            err.getValidationIssueCode().equals(AssessmentStudentValidationIssueTypeCode.DUPLICATE_XAM_RECORD.getCode())
        )).isFalse();
    }

    @Test
    void testV16CourseCodeCSFRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Session session = new Session();
        Assessment assessment = new Assessment();
        assessment.setAssessmentID(UUID.randomUUID().toString());
        session.setAssessments(Arrays.asList(assessment));
        assessment.setAssessmentTypeCode(assessmentStudent.getCourseCode());
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(any(),any())).thenReturn(Optional.of(session));

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setCourseCode("LTF12");
        var school = createMockSchoolTombstone();
        school.setSchoolReportingRequirementCode(SchoolReportingRequirementCodes.CSF.getCode());

        Session session2 = new Session();
        Assessment assessment2 = new Assessment();
        assessment2.setAssessmentID(UUID.randomUUID().toString());
        session2.setAssessments(Arrays.asList(assessment2));
        assessment2.setAssessmentTypeCode("LTF12");
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(any(),any())).thenReturn(Optional.of(session2));

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, school));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_CODE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_CODE_CSF.getCode());
        assertThat(validationError2.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_CODE_CSF.getMessage());
    }


    @Test
    void testV02ValidStudentInDEMRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setMiddleName("ABC");
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Session session = new Session();
        Assessment assessment = new Assessment();
        assessment.setAssessmentID(UUID.randomUUID().toString());
        session.setAssessments(Arrays.asList(assessment));
        assessment.setAssessmentTypeCode(assessmentStudent.getCourseCode());
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(any(),any())).thenReturn(Optional.of(session));

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode());
        assertThat(validationError2.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getMessage());

        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError3.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode());
        assertThat(validationError3.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getMessage());

        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError4.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode());
        assertThat(validationError4.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getMessage());

        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchoolTombstone()));
        assertThat(validationError5.size()).isNotZero();
        assertThat(validationError5.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError5.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode());
        assertThat(validationError5.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getMessage());
    }

    @Test
    void testV17CourseCodeNonCSFRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());
        assessmentStudent.setCourseCode("LTP12");

        Session session = new Session();
        Assessment assessment = new Assessment();
        assessment.setAssessmentID(UUID.randomUUID().toString());
        session.setAssessments(Arrays.asList(assessment));
        assessment.setAssessmentTypeCode(assessmentStudent.getCourseCode());
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(any(),any())).thenReturn(Optional.of(session));

        var school = createMockSchoolTombstone();
        school.setSchoolReportingRequirementCode(SchoolReportingRequirementCodes.CSF.getCode());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, school));
        assertThat(validationError1.size()).isZero();

        Session session2 = new Session();
        Assessment assessment2 = new Assessment();
        assessment2.setAssessmentID(UUID.randomUUID().toString());
        session2.setAssessments(Arrays.asList(assessment2));
        assessment2.setAssessmentTypeCode(assessmentStudent.getCourseCode());
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(any(),any())).thenReturn(Optional.of(session2));

        school.setSchoolReportingRequirementCode(SchoolReportingRequirementCodes.REGULAR.getCode());

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, school));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_CODE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_CODE_NON_CSF.getCode());
    }

    @Test
    void testV20CourseCodeAttemptsRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Session session = new Session();
        Assessment assessment = new Assessment();
        assessment.setAssessmentID(UUID.randomUUID().toString());
        session.setAssessments(Arrays.asList(assessment));
        assessment.setAssessmentTypeCode(assessmentStudent.getCourseCode());
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(any(),any())).thenReturn(Optional.of(session));

        assessmentStudent.setCourseCode("LTF12");
        var school = createMockSchoolTombstone();
        school.setSchoolReportingRequirementCode(SchoolReportingRequirementCodes.REGULAR.getCode());

        Session session2 = new Session();
        Assessment assessment2 = new Assessment();
        assessment2.setAssessmentID(UUID.randomUUID().toString());
        session2.setAssessments(Arrays.asList(assessment2));
        assessment2.setAssessmentTypeCode("LTF12");
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(any(),any())).thenReturn(Optional.of(session2));

        AssessmentStudentDetailResponse response = new AssessmentStudentDetailResponse();
        response.setHasPriorRegistration(false);
        response.setAlreadyWrittenAssessment(false);
        response.setNumberOfAttempts("3");
        when(this.restUtils.getAssessmentStudentDetail(any(),any())).thenReturn(response);
        var ruleData = createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, school);
        ruleData.setAssessmentStudentDetail(response);
        val validationError2 = rulesProcessor.processRules(ruleData);
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_CODE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_CODE_ATTEMPTS.getCode());
        assertThat(validationError2.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_CODE_ATTEMPTS.getMessage());
    }

    @Test
    void testV21CourseCodeAttemptsRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());
        assessmentStudent.setCourseStatus("W");

        Session session = new Session();
        Assessment assessment = new Assessment();
        assessment.setAssessmentID(UUID.randomUUID().toString());
        session.setAssessments(Arrays.asList(assessment));
        assessment.setAssessmentTypeCode(assessmentStudent.getCourseCode());
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(any(),any())).thenReturn(Optional.of(session));

        assessmentStudent.setCourseCode("LTF12");
        var school = createMockSchoolTombstone();
        school.setSchoolReportingRequirementCode(SchoolReportingRequirementCodes.REGULAR.getCode());

        Session session2 = new Session();
        Assessment assessment2 = new Assessment();
        assessment2.setAssessmentID(UUID.randomUUID().toString());
        session2.setAssessments(Arrays.asList(assessment2));
        assessment2.setAssessmentTypeCode("LTF12");
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(any(),any())).thenReturn(Optional.of(session2));

        AssessmentStudentDetailResponse response = new AssessmentStudentDetailResponse();
        response.setHasPriorRegistration(false);
        response.setAlreadyWrittenAssessment(false);
        response.setNumberOfAttempts("0");
        when(this.restUtils.getAssessmentStudentDetail(any(),any())).thenReturn(response);
        var ruleData = createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, school);
        ruleData.setAssessmentStudentDetail(response);
        val validationError2 = rulesProcessor.processRules(ruleData);
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_STATUS.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_STATUS_W_INVALID.getCode());
    }

    @Test
    void testV22CourseCodeNumeracyRule() {
        String futureSessionYear = LocalDate.now().plusYears(1).format(DateTimeFormatter.ofPattern("yyyy"));
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);

        Session numeracySession = new Session();
        var numeracyAssessments = NumeracyAssessmentCodes.getAllCodes().stream().map(code -> {
            Assessment a = new Assessment();
            a.setAssessmentID(UUID.randomUUID().toString());
            a.setAssessmentTypeCode(code);
            return a;
        }).toList();
        numeracySession.setAssessments(numeracyAssessments);
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(any(), any())).thenReturn(Optional.of(numeracySession));

        var numeracyStudent1 = createMockAssessmentStudent();
        numeracyStudent1.setPen(demStudent.getPen());
        numeracyStudent1.setLocalID(demStudent.getLocalID());
        numeracyStudent1.setLastName(demStudent.getLastName());
        numeracyStudent1.setIncomingFileset(demStudent.getIncomingFileset());
        numeracyStudent1.setCourseCode(NumeracyAssessmentCodes.NME10.getCode());
        numeracyStudent1.setCourseMonth("06");
        numeracyStudent1.setCourseYear(futureSessionYear);
        assessmentStudentRepository.save(numeracyStudent1);

        var numeracyStudent2 = createMockAssessmentStudent();
        numeracyStudent2.setPen(demStudent.getPen());
        numeracyStudent2.setLocalID(demStudent.getLocalID());
        numeracyStudent2.setLastName(demStudent.getLastName());
        numeracyStudent2.setIncomingFileset(demStudent.getIncomingFileset());
        numeracyStudent2.setCourseCode(NumeracyAssessmentCodes.NMF.getCode());
        numeracyStudent2.setCourseMonth("06");
        numeracyStudent2.setCourseYear(futureSessionYear);
        numeracyStudent2.setAssessmentStudentID(null);
        numeracyStudent2.setCourseStatus("A");

        AssessmentStudentDetailResponse response = new AssessmentStudentDetailResponse();
        response.setHasPriorRegistration(true);
        response.setAlreadyRegisteredAssessmentTypeCode(NumeracyAssessmentCodes.NME10.getCode());
        when(this.restUtils.getAssessmentStudentDetail(any(), any())).thenReturn(response);

        val ruleData = createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), numeracyStudent2, createMockSchoolTombstone());
        ruleData.setAssessmentStudentDetail(response);

        val numeracyValidationError = rulesProcessor.processRules(ruleData);
        assertThat(numeracyValidationError.size()).isNotZero();
        assertThat(numeracyValidationError.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_CODE.getCode());
        assertThat(numeracyValidationError.getFirst().getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.NUMERACY_DUPLICATE.getCode());
        assertThat(numeracyValidationError.getFirst().getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.NUMERACY_DUPLICATE.getMessage().formatted(NumeracyAssessmentCodes.NME10.getCode()));

        response.setHasPriorRegistration(false);
        ruleData.setAssessmentStudentDetail(response);
        val numeracyValidationError2 = rulesProcessor.processRules(ruleData);
        assertThat(numeracyValidationError2.stream().anyMatch(err ->
            err.getValidationIssueFieldCode().equals(ValidationFieldCode.COURSE_CODE.getCode()) &&
            err.getValidationIssueCode().equals(AssessmentStudentValidationIssueTypeCode.NUMERACY_DUPLICATE.getCode())
        )).isFalse();
    }

    @Test
    void testIsNumeracyConflictVariousCases() throws Exception {
        AssessmentRulesService svc = mock(AssessmentRulesService.class);
        CourseCodeNumeracyRule rule = new CourseCodeNumeracyRule(svc);

        var method = CourseCodeNumeracyRule.class.getDeclaredMethod("isNumeracyConflict", String.class, String.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(rule, NumeracyAssessmentCodes.NME.getCode(), NumeracyAssessmentCodes.NMF.getCode()));
        assertTrue((Boolean) method.invoke(rule, NumeracyAssessmentCodes.NME10.getCode(), NumeracyAssessmentCodes.NMF.getCode()));
        assertTrue((Boolean) method.invoke(rule, NumeracyAssessmentCodes.NME.getCode(), NumeracyAssessmentCodes.NMF10.getCode()));
        assertTrue((Boolean) method.invoke(rule, NumeracyAssessmentCodes.NME10.getCode(), NumeracyAssessmentCodes.NMF10.getCode()));

        assertTrue((Boolean) method.invoke(rule, NumeracyAssessmentCodes.NMF.getCode(), NumeracyAssessmentCodes.NME.getCode()));
        assertTrue((Boolean) method.invoke(rule, NumeracyAssessmentCodes.NMF10.getCode(), NumeracyAssessmentCodes.NME.getCode()));

        assertTrue((Boolean) method.invoke(rule, " nMe10 ", " nmF "));
        assertTrue((Boolean) method.invoke(rule, "nme", "NMF10"));

        assertFalse((Boolean) method.invoke(rule, "MA10", "NMF"));
        assertFalse((Boolean) method.invoke(rule, NumeracyAssessmentCodes.NME.getCode(), "MA10"));
        assertFalse((Boolean) method.invoke(rule, "LTE10", "MA10"));

        assertFalse((Boolean) method.invoke(rule, null, NumeracyAssessmentCodes.NMF.getCode()));
        assertFalse((Boolean) method.invoke(rule, NumeracyAssessmentCodes.NME.getCode(), null));
        assertFalse((Boolean) method.invoke(rule, null, null));
    }
}
