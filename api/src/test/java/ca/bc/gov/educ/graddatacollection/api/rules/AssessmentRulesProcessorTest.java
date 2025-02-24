package ca.bc.gov.educ.graddatacollection.api.rules;


import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolReportingRequirementCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.AssessmentStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
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

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.demographicStudentRepository.deleteAll();
        this.incomingFilesetRepository.deleteAll();
        this.assessmentStudentRepository.deleteAll();

        Student stud = new Student();
        stud.setStudentID(UUID.randomUUID().toString());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud);

        AssessmentStudentDetailResponse response = new AssessmentStudentDetailResponse();
        response.setHasPriorRegistration(false);
        response.setNumberOfAttempts("1");
        when(this.restUtils.getAssessmentStudentDetail(any(),any())).thenReturn(response);
        when(this.restUtils.getSchoolBySchoolID(any())).thenReturn(Optional.of(createMockSchool()));
    }

    @Test
    void testV301StudentPENRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Student stud = new Student();
        stud.setStudentID(UUID.randomUUID().toString());
        stud.setDob(demStudent.getBirthdate());
        stud.setLegalLastName(demStudent.getLastName());
        stud.setLegalFirstName(demStudent.getFirstName());
        stud.setLegalMiddleNames(demStudent.getMiddleName());
        stud.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud);

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
    }

    @Test
    void testV302CourseLevelRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Student stud = new Student();
        stud.setStudentID(UUID.randomUUID().toString());
        stud.setDob(demStudent.getBirthdate());
        stud.setLegalLastName(demStudent.getLastName());
        stud.setLegalFirstName(demStudent.getFirstName());
        stud.setLegalMiddleNames(demStudent.getMiddleName());
        stud.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud);

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
    }

    @Test
    void testV303CourseCodeRule() {
        Session sess = createMockSession();
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(anyString(), anyString())).thenReturn(Optional.of(sess));
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());
        assessmentStudent.setCourseCode("LTE10");

        Student stud = new Student();
        stud.setStudentID(UUID.randomUUID().toString());
        stud.setDob(demStudent.getBirthdate());
        stud.setLegalLastName(demStudent.getLastName());
        stud.setLegalFirstName(demStudent.getFirstName());
        stud.setLegalMiddleNames(demStudent.getMiddleName());
        stud.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud);

        Session session = new Session();
        Assessment assessment = new Assessment();
        assessment.setAssessmentID(UUID.randomUUID().toString());
        session.setAssessments(Arrays.asList(assessment));
        assessment.setAssessmentTypeCode(assessmentStudent.getCourseCode());
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(any(),any())).thenReturn(Optional.of(session));

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setCourseCode("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_CODE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_CODE_INVALID.getCode());
    }

    @Test
    void testV304CourseSessionRule() {
        Session sess = createMockSession();
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(anyString(), anyString())).thenReturn(Optional.of(sess));
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());
        assessmentStudent.setCourseCode("LTE10");

        Student stud = new Student();
        stud.setStudentID(UUID.randomUUID().toString());
        stud.setDob(demStudent.getBirthdate());
        stud.setLegalLastName(demStudent.getLastName());
        stud.setLegalFirstName(demStudent.getFirstName());
        stud.setLegalMiddleNames(demStudent.getMiddleName());
        stud.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud);

        Session session = new Session();
        Assessment assessment = new Assessment();
        assessment.setAssessmentID(UUID.randomUUID().toString());
        session.setAssessments(Arrays.asList(assessment));
        assessment.setAssessmentTypeCode(assessmentStudent.getCourseCode());
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(any(),any())).thenReturn(Optional.of(session));

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        AssessmentStudentDetailResponse response = new AssessmentStudentDetailResponse();
        response.setHasPriorRegistration(true);
        response.setNumberOfAttempts("1");
        when(this.restUtils.getAssessmentStudentDetail(any(),any())).thenReturn(response);

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_CODE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_DUP.getCode());

        response.setHasPriorRegistration(false);
        response.setNumberOfAttempts("3");
        when(this.restUtils.getAssessmentStudentDetail(any(),any())).thenReturn(response);

        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_CODE.getCode());
        assertThat(validationError3.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_EXCEED.getCode());

        response.setHasPriorRegistration(false);
        response.setNumberOfAttempts("1");
        response.setAlreadyWrittenAssessment(true);
        when(this.restUtils.getAssessmentStudentDetail(any(),any())).thenReturn(response);

        assessmentStudent.setCourseStatus("W");

        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_STATUS.getCode());
        assertThat(validationError4.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_ALREADY_WRITTEN.getCode());
    }

    @Test
    void testV306InterimSchoolPercentageRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Student stud = new Student();
        stud.setStudentID(UUID.randomUUID().toString());
        stud.setDob(demStudent.getBirthdate());
        stud.setLegalLastName(demStudent.getLastName());
        stud.setLegalFirstName(demStudent.getFirstName());
        stud.setLegalMiddleNames(demStudent.getMiddleName());
        stud.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud);

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
    }

    @Test
    void testV307InterimLetterGradeRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Student stud = new Student();
        stud.setStudentID(UUID.randomUUID().toString());
        stud.setDob(demStudent.getBirthdate());
        stud.setLegalLastName(demStudent.getLastName());
        stud.setLegalFirstName(demStudent.getFirstName());
        stud.setLegalMiddleNames(demStudent.getMiddleName());
        stud.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud);

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
    }

    @Test
    void testV308FinalSchoolPercentageRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Student stud = new Student();
        stud.setStudentID(UUID.randomUUID().toString());
        stud.setDob(demStudent.getBirthdate());
        stud.setLegalLastName(demStudent.getLastName());
        stud.setLegalFirstName(demStudent.getFirstName());
        stud.setLegalMiddleNames(demStudent.getMiddleName());
        stud.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud);

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
    }

    @Test
    void testV309FinalPercentageRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Student stud = new Student();
        stud.setStudentID(UUID.randomUUID().toString());
        stud.setDob(demStudent.getBirthdate());
        stud.setLegalLastName(demStudent.getLastName());
        stud.setLegalFirstName(demStudent.getFirstName());
        stud.setLegalMiddleNames(demStudent.getMiddleName());
        stud.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud);

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
    }

    @Test
    void testV310FinalLetterGradeRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Student stud = new Student();
        stud.setStudentID(UUID.randomUUID().toString());
        stud.setDob(demStudent.getBirthdate());
        stud.setLegalLastName(demStudent.getLastName());
        stud.setLegalFirstName(demStudent.getFirstName());
        stud.setLegalMiddleNames(demStudent.getMiddleName());
        stud.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud);

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
    }

    @Test
    void testV311ProvincialSpecialCaseRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Student stud = new Student();
        stud.setStudentID(UUID.randomUUID().toString());
        stud.setDob(demStudent.getBirthdate());
        stud.setLegalLastName(demStudent.getLastName());
        stud.setLegalFirstName(demStudent.getFirstName());
        stud.setLegalMiddleNames(demStudent.getMiddleName());
        stud.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud);

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
    }

    @Test
    void testV312CourseStatusRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Student stud = new Student();
        stud.setStudentID(UUID.randomUUID().toString());
        stud.setDob(demStudent.getBirthdate());
        stud.setLegalLastName(demStudent.getLastName());
        stud.setLegalFirstName(demStudent.getFirstName());
        stud.setLegalMiddleNames(demStudent.getMiddleName());
        stud.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud);

        Session session = new Session();
        Assessment assessment = new Assessment();
        assessment.setAssessmentID(UUID.randomUUID().toString());
        session.setAssessments(Arrays.asList(assessment));
        assessment.setAssessmentTypeCode(assessmentStudent.getCourseCode());
        when(this.restUtils.getAssessmentSessionByCourseMonthAndYear(any(),any())).thenReturn(Optional.of(session));

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setCourseStatus("B");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_STATUS.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getCode());
    }

    @Test
    void testV314NumberOfCreditsRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Student stud = new Student();
        stud.setStudentID(UUID.randomUUID().toString());
        stud.setDob(demStudent.getBirthdate());
        stud.setLegalLastName(demStudent.getLastName());
        stud.setLegalFirstName(demStudent.getFirstName());
        stud.setLegalMiddleNames(demStudent.getMiddleName());
        stud.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud);

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
    }

    @Test
    void testV315CourseTypeRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Student stud = new Student();
        stud.setStudentID(UUID.randomUUID().toString());
        stud.setDob(demStudent.getBirthdate());
        stud.setLegalLastName(demStudent.getLastName());
        stud.setLegalFirstName(demStudent.getFirstName());
        stud.setLegalMiddleNames(demStudent.getMiddleName());
        stud.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud);

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
    }

    @Test
    void testV316ToWriteFlagRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Student stud = new Student();
        stud.setStudentID(UUID.randomUUID().toString());
        stud.setDob(demStudent.getBirthdate());
        stud.setLegalLastName(demStudent.getLastName());
        stud.setLegalFirstName(demStudent.getFirstName());
        stud.setLegalMiddleNames(demStudent.getMiddleName());
        stud.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud);

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
    }

    @Test
    void testV317ExamSchoolRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Student stud = new Student();
        stud.setStudentID(UUID.randomUUID().toString());
        stud.setDob(demStudent.getBirthdate());
        stud.setLegalLastName(demStudent.getLastName());
        stud.setLegalFirstName(demStudent.getFirstName());
        stud.setLegalMiddleNames(demStudent.getMiddleName());
        stud.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud);

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
    }

    @Test
    void testV318CourseSessionRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Student stud = new Student();
        stud.setStudentID(UUID.randomUUID().toString());
        stud.setDob(demStudent.getBirthdate());
        stud.setLegalLastName(demStudent.getLastName());
        stud.setLegalFirstName(demStudent.getFirstName());
        stud.setLegalMiddleNames(demStudent.getMiddleName());
        stud.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud);

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
    }

    @Test
    void testV319CourseCodeRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var assessmentStudent = createMockAssessmentStudent();
        assessmentStudent.setPen(demStudent.getPen());
        assessmentStudent.setLocalID(demStudent.getLocalID());
        assessmentStudent.setLastName(demStudent.getLastName());
        assessmentStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Student stud = new Student();
        stud.setStudentID(UUID.randomUUID().toString());
        stud.setDob(demStudent.getBirthdate());
        stud.setLegalLastName(demStudent.getLastName());
        stud.setLegalFirstName(demStudent.getFirstName());
        stud.setLegalMiddleNames(demStudent.getMiddleName());
        stud.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud);

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
    }


    @Test
    void testV320ValidStudentInDEMRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
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

        Student stud = new Student();
        stud.setStudentID(UUID.randomUUID().toString());
        stud.setDob(demStudent.getBirthdate());
        stud.setLegalLastName(demStudent.getLastName());
        stud.setLegalFirstName(demStudent.getFirstName());
        stud.setLegalMiddleNames(demStudent.getMiddleName());
        stud.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud);

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        Student stud2 = new Student();
        stud2.setStudentID(UUID.randomUUID().toString());
        stud2.setDob(demStudent.getBirthdate());
        stud2.setLegalLastName(demStudent.getLastName());
        stud2.setLegalFirstName(demStudent.getFirstName());
        stud2.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud2);

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode());

        Student stud3 = new Student();
        stud3.setStudentID(UUID.randomUUID().toString());
        stud3.setDob(demStudent.getBirthdate());
        stud3.setLegalLastName(demStudent.getLastName());
        stud3.setLegalMiddleNames(demStudent.getMiddleName());
        stud3.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud3);

        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError3.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode());

        Student stud4 = new Student();
        stud4.setStudentID(UUID.randomUUID().toString());
        stud4.setDob(demStudent.getBirthdate());
        stud4.setLegalFirstName(demStudent.getFirstName());
        stud4.setLegalMiddleNames(demStudent.getMiddleName());
        stud4.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud4);

        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError4.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode());

        Student stud5 = new Student();
        stud5.setStudentID(UUID.randomUUID().toString());
        stud5.setLegalLastName(demStudent.getLastName());
        stud5.setLegalFirstName(demStudent.getFirstName());
        stud5.setLegalMiddleNames(demStudent.getMiddleName());
        stud5.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud5);

        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(savedFileSet), assessmentStudent, createMockSchool()));
        assertThat(validationError5.size()).isNotZero();
        assertThat(validationError5.get(0).getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError5.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode());
    }
}
