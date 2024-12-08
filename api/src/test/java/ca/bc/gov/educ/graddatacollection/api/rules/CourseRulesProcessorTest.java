package ca.bc.gov.educ.graddatacollection.api.rules;


import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentRulesProcessor;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
class CourseRulesProcessorTest extends BaseGradDataCollectionAPITest {

    @Autowired
    private CourseStudentRulesProcessor rulesProcessor;

    @Autowired
    private RestUtils restUtils;

    @Autowired
    private IncomingFilesetRepository incomingFilesetRepository;

    @Autowired
    private DemographicStudentRepository demographicStudentRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testV201StudentPENRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Student stud1 = new Student();
        stud1.setStudentID(UUID.randomUUID().toString());
        stud1.setDob(demStudent.getBirthdate());
        stud1.setLegalLastName(demStudent.getLastName());
        stud1.setLegalFirstName(demStudent.getFirstName());
        stud1.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud1);

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var incomingFileset2 = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet2 = incomingFilesetRepository.save(incomingFileset2);
        var courseStudent2 = createMockCourseStudent(savedFileSet2);
        courseStudent2.setTransactionID("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(incomingFileset2), courseStudent2, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.PEN.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode());
    }

    @Test
    void testV202ValidStudentInDEMRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Student stud1 = new Student();
        stud1.setStudentID(UUID.randomUUID().toString());
        stud1.setDob(demStudent.getBirthdate());
        stud1.setLegalLastName(demStudent.getLastName());
        stud1.setLegalFirstName(demStudent.getFirstName());
        stud1.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud1);

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        Student stud2 = new Student();
        stud2.setStudentID(UUID.randomUUID().toString());
        stud2.setDob(demStudent.getBirthdate());
        stud2.setLegalLastName(demStudent.getLastName());
        stud2.setLegalFirstName("ABC");
        stud2.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud2);
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(incomingFileset), courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.PEN.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode());
    }

    @Test
    void testV203CourseStatusRule() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Student stud1 = new Student();
        stud1.setStudentID(UUID.randomUUID().toString());
        stud1.setDob(demStudent.getBirthdate());
        stud1.setLegalLastName(demStudent.getLastName());
        stud1.setLegalFirstName(demStudent.getFirstName());
        stud1.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud1);

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseStatus("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.COURSE_STATUS.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getCode());
    }

    @Test
    void testV214InterimPercent() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Student stud1 = new Student();
        stud1.setStudentID(UUID.randomUUID().toString());
        stud1.setDob(demStudent.getBirthdate());
        stud1.setLegalLastName(demStudent.getLastName());
        stud1.setLegalFirstName(demStudent.getFirstName());
        stud1.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud1);

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setInterimPercentage("-1");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.INTERIM_PCT.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.INTERIM_PCT_INVALID.getCode());

        courseStudent.setInterimPercentage("101");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.INTERIM_PCT.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.INTERIM_PCT_INVALID.getCode());
    }

    @Test
    void testV217FinalPercent() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Student stud1 = new Student();
        stud1.setStudentID(UUID.randomUUID().toString());
        stud1.setDob(demStudent.getBirthdate());
        stud1.setLegalLastName(demStudent.getLastName());
        stud1.setLegalFirstName(demStudent.getFirstName());
        stud1.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud1);

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setFinalPercentage("-1");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.FINAL_PCT.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_PCT_INVALID.getCode());

        courseStudent.setFinalPercentage("101");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.FINAL_PCT.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_PCT_INVALID.getCode());
    }

    @Test
    void testV218FinalPercent() {
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        Student stud1 = new Student();
        stud1.setStudentID(UUID.randomUUID().toString());
        stud1.setDob(demStudent.getBirthdate());
        stud1.setLegalLastName(demStudent.getLastName());
        stud1.setLegalFirstName(demStudent.getFirstName());
        stud1.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud1);

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setFinalPercentage("94");
        courseStudent.setCourseYear("1990");
        courseStudent.setCourseMonth("02");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.FINAL_PCT.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_PCT_NOT_BLANK.getCode());

        courseStudent.setCourseYear(null);
        courseStudent.setCourseMonth(null);
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));

        courseStudent.setFinalPercentage("94");
        courseStudent.setCourseYear("ABCD");
        courseStudent.setCourseMonth("12");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.FINAL_PCT.getCode());
        assertThat(validationError4.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_PCT_NOT_BLANK.getCode());
        assertThat(validationError3.size()).isZero();
    }
}
