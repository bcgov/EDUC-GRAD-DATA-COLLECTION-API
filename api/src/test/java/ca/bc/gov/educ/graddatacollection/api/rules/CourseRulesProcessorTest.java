package ca.bc.gov.educ.graddatacollection.api.rules;


import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentRulesProcessor;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.EquivalencyChallengeCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.LetterGrade;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
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
        when(restUtils.getLetterGrades()).thenReturn(
                List.of(
                        new LetterGrade("A", "4", "Y", "The student demonstrates excellent or outstanding performance in relation to expected learning outcomes for the course or subject and grade.", "A", 100, 86, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("B", "3", "Y", "", "B", 85, 73, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("C+", "2.5", "Y", "", "C+", 72, 67, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("F", "0", "N", "", "F", 49, 0, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("IE", "0", "N", "", "Insufficient Evidence", 0, 0, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("RM", "0", "Y", "", "Requirement Met", 0, 0, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
                )
        );
        when(restUtils.getEquivalencyChallengeCodes()).thenReturn(
                List.of(
                        new EquivalencyChallengeCode("E", "Equivalency", "Indicates that the course credit was earned through an equivalency review.", "1", "1984-01-01 00:00:00.000", null, "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new EquivalencyChallengeCode("C", "Challenge", "Indicates that the course credit was earned through the challenge process.", "2", "1984-01-01 00:00:00.000", null, "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
                )
        );
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
    void testV209CourseMonth() {
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

        courseStudent.setCourseMonth("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.COURSE_MONTH.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getCode());

        courseStudent.setCourseMonth("");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.COURSE_MONTH.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getCode());

        courseStudent.setCourseMonth(null);
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.COURSE_MONTH.getCode());
        assertThat(validationError4.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getCode());
    }


    @Test
    void testV212CourseSession() {
        // Dynamically determine the current date and school year for testing - can't mock LocalDate.now()
        LocalDate today = LocalDate.now();
        int currentYear = (today.getMonthValue() >= 10) ? today.getYear() : today.getYear() - 1;
        YearMonth currentSchoolYearStart = YearMonth.of(currentYear, 10);
        YearMonth nextSchoolYearEnd = YearMonth.of(currentYear + 1, 9);

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
        when(this.restUtils.getStudentByPEN(any(), any())).thenReturn(stud1);

        // Case 1: Valid course session (within the current school year)
        courseStudent.setCourseYear(String.valueOf(currentSchoolYearStart.getYear()));
        courseStudent.setCourseMonth("10");
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        // Case 2: Course session too old
        courseStudent.setCourseYear("1983");
        courseStudent.setCourseMonth("12");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.COURSE_SESSION.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode());

        // Case 3: Course session too far in the future
        courseStudent.setCourseYear(String.valueOf(nextSchoolYearEnd.getYear() + 1));
        courseStudent.setCourseMonth("02");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.COURSE_SESSION.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode());

        // Case 4: Invalid course year and month - skips
        courseStudent.setCourseYear(null);
        courseStudent.setCourseMonth("01");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError4.size()).isZero();

        // Case 5: Boundary case - earliest valid date
        courseStudent.setCourseYear("1984");
        courseStudent.setCourseMonth("01");
        courseStudent.setFinalPercentage("");
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError5.size()).isZero();

        // Case 6: Boundary case - last month of next school year
        courseStudent.setCourseYear(String.valueOf(nextSchoolYearEnd.getYear()));
        courseStudent.setCourseMonth("09");
        courseStudent.setFinalGrade("");
        val validationError6 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError6.size()).isZero();

        // Case 7: Boundary case - just before the earliest valid date
        courseStudent.setCourseYear("1983");
        courseStudent.setCourseMonth("12");
        val validationError7 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError7.size()).isNotZero();
        assertThat(validationError7.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.COURSE_SESSION.getCode());
        assertThat(validationError7.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode());

        // Case 8: Boundary case - just after the next school year ends
        courseStudent.setCourseYear(String.valueOf(nextSchoolYearEnd.getYear()));
        courseStudent.setCourseMonth("10");
        val validationError8 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError8.size()).isNotZero();
        assertThat(validationError8.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.COURSE_SESSION.getCode());
        assertThat(validationError8.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode());
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
    void testV215InterimLetterGrade() {
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

        courseStudent.setInterimGrade("ABCD");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.INTERIM_LETTER_GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_INVALID.getCode());
    }

    @Test
    void testV216InterimGradePercent() {
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

        courseStudent.setInterimPercentage("100");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.INTERIM_LETTER_GRADE_PERCENTAGE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_PERCENTAGE_MISMATCH.getCode());
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
        courseStudent.setCourseMonth("01");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isZero();

        courseStudent.setFinalPercentage("94");
        courseStudent.setCourseYear("ABCD");
        courseStudent.setCourseMonth("12");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError4.size()).isNotZero();
        Assertions.assertTrue(validationError4.stream().anyMatch(validationError -> validationError.getValidationIssueFieldCode().equalsIgnoreCase(CourseStudentValidationFieldCode.FINAL_PCT.getCode())));
        Assertions.assertTrue(validationError4.stream().anyMatch(validationError -> validationError.getValidationIssueCode().equalsIgnoreCase(CourseStudentValidationIssueTypeCode.FINAL_PCT_NOT_BLANK.getCode())));
    }

    @Test
    void testV219FinalLetterGrade() {
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

        courseStudent.setFinalGrade("ABCD");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.FINAL_LETTER_GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_INVALID.getCode());
    }

    @Test
    void testV2220FinalLetterGradePercent() {
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

        courseStudent.setFinalPercentage("22");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.FINAL_LETTER_GRADE_PERCENTAGE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_PERCENTAGE_MISMATCH.getCode());
    }

    @Test
    void testV221FinalLetterGradeRM() {
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

        courseStudent.setFinalGrade("RM");
        courseStudent.setCourseCode("GT");
        courseStudent.setFinalPercentage("0");
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseCode("ABC");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.FINAL_LETTER_GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_RM.getCode());
    }

    @Test
    void testV222FinalLetterGradeNotRM() {
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

        courseStudent.setFinalGrade("RM");
        courseStudent.setCourseCode("GT");
        courseStudent.setFinalPercentage("0");
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setFinalGrade("A");
        courseStudent.setFinalPercentage("90");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.FINAL_LETTER_GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_NOT_RM.getCode());
    }

    @Test
    void testV223FinalLetterGradeAndPercentNotBlank() {
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

        YearMonth validFutureCourseSession = YearMonth.now().plusMonths(6);
        courseStudent.setCourseYear(String.valueOf(validFutureCourseSession.getYear()));
        courseStudent.setCourseMonth(String.format("%02d", validFutureCourseSession.getMonthValue()));

        courseStudent.setFinalGrade("");
        courseStudent.setFinalPercentage("");
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setFinalGrade("A");
        courseStudent.setFinalPercentage("90");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.FINAL_LETTER_GRADE_PERCENTAGE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_OR_PERCENT_NOT_BLANK.getCode());
    }

    @Test
    void testV224FinalLetterGradeAndPercentNotBlank() {
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

        courseStudent.setFinalGrade("");
        courseStudent.setFinalPercentage("");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.FINAL_LETTER_GRADE_PERCENTAGE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_OR_PERCENT_BLANK.getCode());
    }

    @Test
    void testV225FinalLetterGradeIE() {
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

        courseStudent.setCourseYear("2022");
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setFinalGrade("IE");
        courseStudent.setFinalPercentage("0");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.FINAL_LETTER_GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_IE.getCode());
    }

    @Test
    void testV227EquivalencyChallengeCode() {
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

        courseStudent.setCourseType("A");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.EQUIVALENCY_CHALLENGE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.EQUIVALENCY_CHALLENGE_CODE_INVALID.getCode());
    }

    @Test
    void testV228GraduationRequirement() {
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

        courseStudent.setCourseGraduationRequirement("1986");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(CourseStudentValidationFieldCode.GRADUATION_REQUIREMENT.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.GRADUATION_REQUIREMENT_INVALID.getCode());
    }
}
