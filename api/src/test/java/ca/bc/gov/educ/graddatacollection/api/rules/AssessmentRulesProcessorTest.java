package ca.bc.gov.educ.graddatacollection.api.rules;


import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
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

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
        when(this.restUtils.getSchoolBySchoolID(any())).thenReturn(Optional.of(createMockSchool()));
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(null), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var assessmentStudent2 = createMockAssessmentStudent();
        assessmentStudent2.setTransactionID("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(incomingFileset), createMockCourseStudent(incomingFileset), assessmentStudent2, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode());
        assertThat(validationError2.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_DATA_MISSING.getMessage());
    }

    @Test
    void testV04CourseLevelRule() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setCourseLevel("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_LEVEL.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_LEVEL_NOT_BLANK.getCode());
        assertThat(validationError2.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_LEVEL_NOT_BLANK.getMessage());
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setCourseMonth("12");
        assessmentStudent.setCourseCode("LTE10");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_MONTH.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode());
        assertThat(validationError2.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getMessage());

        assessmentStudent.setCourseMonth("11");
        assessmentStudent.setCourseCode("LTE10");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError3.size()).isZero();

        assessmentStudent.setCourseMonth("11");
        assessmentStudent.setCourseCode("MA10");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_CODE.getCode());
        assertThat(validationError4.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode());
        assertThat(validationError4.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getMessage().formatted(assessmentStudent.getCourseCode(), assessmentStudent.getCourseYear(), assessmentStudent.getCourseMonth()));

        assessmentStudent.setCourseMonth("12");
        assessmentStudent.setCourseCode("MA10");
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError5.size()).isNotZero();
        assertThat(validationError5.stream().anyMatch(err ->
            err.getValidationIssueFieldCode().equals(ValidationFieldCode.COURSE_MONTH.getCode()) &&
            err.getValidationIssueCode().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode()) &&
            err.getValidationIssueDescription().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getMessage())
        )).isTrue();
        assertThat(validationError5.stream().anyMatch(err ->
            err.getValidationIssueFieldCode().equals(ValidationFieldCode.COURSE_CODE.getCode()) &&
            err.getValidationIssueCode().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()) &&
            err.getValidationIssueDescription().equals(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getMessage().formatted(assessmentStudent.getCourseCode(), assessmentStudent.getCourseYear(), assessmentStudent.getCourseMonth()))
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
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setCourseStatus("W");
        AssessmentStudentDetailResponse response = new AssessmentStudentDetailResponse();
        response.setHasPriorRegistration(true);
        response.setAlreadyWrittenAssessment(true);
        when(this.restUtils.getAssessmentStudentDetail(any(),any())).thenReturn(response);

        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_CODE.getCode());
        assertThat(validationError3.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_DUP.getCode());
        assertThat(validationError3.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_DUP.getMessage());

        response.setHasPriorRegistration(false);
        response.setAlreadyWrittenAssessment(true);
        when(this.restUtils.getAssessmentStudentDetail(any(),any())).thenReturn(response);

        assessmentStudent.setCourseStatus("A");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError4.size()).isZero();

        response.setHasPriorRegistration(true);
        response.setAlreadyWrittenAssessment(false);
        when(this.restUtils.getAssessmentStudentDetail(any(),any())).thenReturn(response);

        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError5.size()).isZero();

        response.setHasPriorRegistration(false);
        response.setAlreadyWrittenAssessment(false);
        when(this.restUtils.getAssessmentStudentDetail(any(),any())).thenReturn(response);

        val validationError6 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError6.size()).isZero();

        assessmentStudent.setCourseStatus("A");
        response.setHasPriorRegistration(true);
        response.setAlreadyWrittenAssessment(true);
        when(this.restUtils.getAssessmentStudentDetail(any(),any())).thenReturn(response);

        val validationError7 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError7.size()).isZero();
    }

    @Test
    void testV05InterimSchoolPercentageRule() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setInterimSchoolPercent("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.INTERIM_SCHOOL_PERCENT.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.INTERIM_SCHOOL_PERCENTAGE_NOT_BLANK.getCode());
        assertThat(validationError2.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.INTERIM_SCHOOL_PERCENTAGE_NOT_BLANK.getMessage());
    }

    @Test
    void testV06InterimLetterGradeRule() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setInterimLetterGrade("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.INTERIM_LETTER_GRADE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_NOT_BLANK.getCode());
        assertThat(validationError2.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_NOT_BLANK.getMessage());
    }

    @Test
    void testV07FinalSchoolPercentageRule() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setFinalSchoolPercent("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_SCHOOL_PERCENT.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.FINAL_SCHOOL_PERCENTAGE_NOT_BLANK.getCode());
        assertThat(validationError2.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.FINAL_SCHOOL_PERCENTAGE_NOT_BLANK.getMessage());
    }

    @Test
    void testV08FinalPercentageRule() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setFinalPercent("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_PERCENTAGE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.FINAL_PERCENTAGE_NOT_BLANK.getCode());
        assertThat(validationError2.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.FINAL_PERCENTAGE_NOT_BLANK.getMessage());
    }

    @Test
    void testV09FinalLetterGradeRule() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setFinalLetterGrade("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_LETTER_GRADE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_NOT_BLANK.getCode());
        assertThat(validationError2.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_NOT_BLANK.getMessage());
    }

    @Test
    void testV10ProvincialSpecialCaseRule() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setProvincialSpecialCase("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PROVINCIAL_SPECIAL_CASE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.PROVINCIAL_SPECIAL_CASE_NOT_BLANK.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.PROVINCIAL_SPECIAL_CASE_NOT_BLANK.getCode().formatted(assessmentStudent.getProvincialSpecialCase()));
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setCourseStatus("");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_STATUS.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getCode());
        assertThat(validationError2.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getMessage().formatted(assessmentStudent.getCourseStatus()));

        assessmentStudent.setCourseStatus("B");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_STATUS.getCode());
        assertThat(validationError3.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getCode());
        assertThat(validationError3.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getMessage().formatted(assessmentStudent.getCourseStatus()));

        assessmentStudent.setCourseStatus("A");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError4.size()).isZero();

        assessmentStudent.setCourseStatus("W");
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError5.stream().anyMatch(err ->
            err.getValidationIssueFieldCode().equals(ValidationFieldCode.COURSE_STATUS.getCode()) &&
            err.getValidationIssueCode().equals(AssessmentStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getCode()) &&
            err.getValidationIssueDescription().equals(AssessmentStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getMessage().formatted(assessmentStudent.getCourseStatus()))
        )).isFalse();
    }

    @Test
    void testV11NumberOfCreditsRule() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setNumberOfCredits("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.NUM_CREDITS.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_NOT_BLANK.getCode());
        assertThat(validationError2.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_NOT_BLANK.getMessage());
    }

    @Test
    void testV12CourseTypeRule() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setCourseType("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.CRSE_TYPE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_TYPE_NOT_BLANK.getCode());
        assertThat(validationError2.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_TYPE_NOT_BLANK.getMessage());
    }

    @Test
    void testV13ToWriteFlagRule() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setToWriteFlag("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.TO_WRITE_FLAG.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.TO_WRITE_FLAG_NOT_BLANK.getCode());
        assertThat(validationError2.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.TO_WRITE_FLAG_NOT_BLANK.getMessage());
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setExamSchoolID(UUID.randomUUID());
        when(this.restUtils.getSchoolBySchoolID(any())).thenReturn(Optional.ofNullable(null));
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.EXAM_SCHOOL.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.EXAM_SCHOOL_INVALID.getCode());
        assertThat(validationError2.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.EXAM_SCHOOL_INVALID.getMessage());
    }

    @Test
    void testV15CourseSessionRule() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        assessmentStudentRepository.save(assessmentStudent);

        assessmentStudent.setAssessmentStudentID(null);
        assessmentStudentRepository.save(assessmentStudent);

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_SESSION.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DUPLICATE_XAM_RECORD.getCode());
        assertThat(validationError2.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DUPLICATE_XAM_RECORD.getMessage());
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setCourseCode("LTF12");
        var school = createMockSchool();
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

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode());
        assertThat(validationError2.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getMessage());

        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError3.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode());
        assertThat(validationError3.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getMessage());

        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError4.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode());
        assertThat(validationError4.get(0).getValidationIssueDescription()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getMessage());

        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
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

        var school = createMockSchool();
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
        var school = createMockSchool();
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
        var school = createMockSchool();
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
}
