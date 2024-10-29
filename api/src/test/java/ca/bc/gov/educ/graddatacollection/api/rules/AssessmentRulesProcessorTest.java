package ca.bc.gov.educ.graddatacollection.api.rules;


import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentRulesProcessor;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1.Session;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    private AssessmentStudentRulesProcessor rulesProcessor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.demographicStudentRepository.deleteAll();
        this.incomingFilesetRepository.deleteAll();
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var assessmentStudent2 = createMockAssessmentStudent();
        assessmentStudent2.setTransactionID("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(incomingFileset), createMockCourseStudent(), assessmentStudent2, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(AssessmentStudentValidationFieldCode.PEN.getCode());
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setCourseLevel("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(), assessmentStudent, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(AssessmentStudentValidationFieldCode.COURSE_LEVEL.getCode());
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(), assessmentStudent, createMockSchool()));
        assertThat(validationError1.size()).isZero();

        assessmentStudent.setCourseCode("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, createMockCourseStudent(), assessmentStudent, createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.get(0).getValidationIssueFieldCode()).isEqualTo(AssessmentStudentValidationFieldCode.COURSE_CODE.getCode());
        assertThat(validationError2.get(0).getValidationIssueCode()).isEqualTo(AssessmentStudentValidationIssueTypeCode.COURSE_CODE_INVALID.getCode());
    }
}
