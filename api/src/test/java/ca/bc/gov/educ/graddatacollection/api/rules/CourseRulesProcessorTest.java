package ca.bc.gov.educ.graddatacollection.api.rules;


import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradRequirementYearCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.StudentStatusCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.CourseStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ReportingPeriodRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentRulesProcessor;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CoregCoursesRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CourseAllowableCreditRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CourseCharacteristicsRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CourseCodeRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.*;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Autowired
    private CourseStudentRepository courseStudentRepository;

    @Autowired
    private ReportingPeriodRepository reportingPeriodRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.demographicStudentRepository.deleteAll();
        this.courseStudentRepository.deleteAll();
        this.incomingFilesetRepository.deleteAll();
        this.reportingPeriodRepository.deleteAll();

        when(restUtils.getLetterGradeList(any())).thenReturn(
                List.of(
                        new LetterGrade("A", "4", "Y", "The student demonstrates excellent or outstanding performance in relation to expected learning outcomes for the course or subject and grade.", "A", 100, 86, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("B", "3", "Y", "", "B", 85, 73, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("C+", "2.5", "Y", "", "C+", 72, 67, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("F", "0", "N", "", "F", 49, 0, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("IE", "0", "N", "", "Insufficient Evidence", null, null, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("RM", "0", "Y", "", "Requirement Met", null, null, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("W", "0", "N", "", "Withdraw", null, null, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
                )
        );
        when(restUtils.getEquivalencyChallengeCodeList()).thenReturn(
                List.of(
                        new EquivalencyChallengeCode("E", "Equivalency", "Indicates that the course credit was earned through an equivalency review.", "1", "1984-01-01 00:00:00.000", null, "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new EquivalencyChallengeCode("C", "Challenge", "Indicates that the course credit was earned through the challenge process.", "2", "1984-01-01 00:00:00.000", null, "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
                )
        );
        when(restUtils.getGradStudentCoursesByStudentID(any(), any())).thenReturn(
                List.of(
                        new GradStudentCourseRecord(
                                null, // id
                                "3201860", // courseID
                                "2021/06", // courseSession
                                100, // interimPercent
                                "", // interimLetterGrade
                                100, // finalPercent
                                "A", // finalLetterGrade
                                4, // credits
                                "", // equivOrChallenge
                                "", // fineArtsAppliedSkills
                                "", // customizedCourseName
                                null, // relatedCourseId
                                new GradStudentCourseExam( // courseExam
                                        null, null, null, null, null, null, null, null
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "CLE  12", // externalCode
                                        "38" // originatingSystem
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "MCLE 12", // externalCode
                                        "39" // originatingSystem
                                )
                        ),
                        new GradStudentCourseRecord(
                                null, // id
                                "3201862", // courseID
                                "2023/06", // courseSession
                                95, // interimPercent
                                "", // interimLetterGrade
                                95, // finalPercent
                                "A", // finalLetterGrade
                                4, // credits
                                "", // equivOrChallenge
                                "", // fineArtsAppliedSkills
                                "", // customizedCourseName
                                null, // relatedCourseId
                                new GradStudentCourseExam( // courseExam
                                        null, null, null, null, null, null, null, null
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "CLC  12", // externalCode
                                        "38" // originatingSystem
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "MCLC 12", // externalCode
                                        "39" // originatingSystem
                                )
                        )
                )
        );
        when(restUtils.getCoreg38CourseByID(any())).thenReturn(
                Optional.of(new GradCourseCode(
                        "3201860", // courseID
                        "CLC  12", // externalCode
                        "38" // originatingSystem
                ))
        );
        when(restUtils.getCoreg39CourseByID(any())).thenReturn(
                Optional.of(new GradCourseCode(
                        "3201860", // courseID
                        "MCLC 12", // externalCode
                        "39" // originatingSystem
                ))
        );
        when(restUtils.getExaminableCourseByExternalID(any())).thenReturn(
                List.of()
        );
        CoregCoursesRecord coursesRecord = new CoregCoursesRecord();
        coursesRecord.setStartDate(LocalDateTime.of(1983, 2, 1,0,0,0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        coursesRecord.setCompletionEndDate(LocalDate.of(9999, 5, 1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        Set<CourseCodeRecord> courseCodes = new HashSet<>();
        CourseCodeRecord traxCode = new CourseCodeRecord();
        traxCode.setCourseID("856787");
        traxCode.setExternalCode("PH   11");
        traxCode.setOriginatingSystem("39"); // TRAX
        courseCodes.add(traxCode);
        CourseCodeRecord myEdBCCode = new CourseCodeRecord();
        myEdBCCode.setCourseID("856787");
        myEdBCCode.setExternalCode("MPH--11");
        myEdBCCode.setOriginatingSystem("38"); // MyEdBC
        courseCodes.add(myEdBCCode);
        coursesRecord.setCourseCode(courseCodes);
        Set<CourseAllowableCreditRecord> courseAllowableCredits = new HashSet<>();
        CourseAllowableCreditRecord courseAllowableCreditRecord = new CourseAllowableCreditRecord();
        courseAllowableCreditRecord.setCourseID("856787");
        courseAllowableCreditRecord.setCreditValue("3");
        courseAllowableCreditRecord.setCacID("2145166");
        courseAllowableCreditRecord.setStartDate("1970-01-01 00:00:00");
        courseAllowableCreditRecord.setEndDate(null);
        courseAllowableCredits.add(courseAllowableCreditRecord);
        coursesRecord.setCourseAllowableCredit(courseAllowableCredits);
        CourseCharacteristicsRecord courseCategory = new CourseCharacteristicsRecord();
        courseCategory.setId("2932");
        courseCategory.setType("CC");
        courseCategory.setCode("BA");
        courseCategory.setDescription("");
        coursesRecord.setCourseCategory(courseCategory);
        coursesRecord.setGenericCourseType("G");
        when(restUtils.getCoursesByExternalID(any(), any())).thenReturn(coursesRecord);

        Student studentApiStudent = new Student();
        studentApiStudent.setStudentID(UUID.randomUUID().toString());
        studentApiStudent.setPen("123456789");
        studentApiStudent.setLocalID("8887555");
        studentApiStudent.setLegalFirstName("JIM");
        studentApiStudent.setLegalLastName("JACKSON");
        studentApiStudent.setDob("1990-01-01");
        studentApiStudent.setStatusCode(StudentStatusCodes.A.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent);
    }

    @Test
    void testC01StudentPENRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        var incomingFileset2 = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet2 = incomingFilesetRepository.save(incomingFileset2);
        var courseStudent2 = createMockCourseStudent(savedFileSet2);
        courseStudent2.setTransactionID("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(incomingFileset2), courseStudent2, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.DEM_DATA_MISSING.getMessage());
    }

    @Test
    void testC02ValidStudentInDEMRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        Student stud2 = new Student();
        stud2.setStudentID(UUID.randomUUID().toString());
        stud2.setDob("1990-01-01");
        stud2.setLegalLastName(demStudent.getLastName());
        stud2.setLegalFirstName("ABC");
        stud2.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud2);
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(incomingFileset), courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FIRST_NAME.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.DEM_ISSUE.getMessage());
    }

    @Test
    void testC04CourseStatusRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        courseStudent.setCourseStatus("A");
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.stream().anyMatch(err ->
            err.getValidationIssueFieldCode().equals(ValidationFieldCode.COURSE_STATUS.getCode()) &&
            err.getValidationIssueCode().equals(CourseStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getCode()) &&
            err.getValidationIssueDescription().equals(CourseStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getMessage().formatted(courseStudent.getCourseStatus()))
        )).isFalse();

        courseStudent.setCourseStatus("W");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.stream().anyMatch(err ->
            err.getValidationIssueFieldCode().equals(ValidationFieldCode.COURSE_STATUS.getCode()) &&
            err.getValidationIssueCode().equals(CourseStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getCode()) &&
            err.getValidationIssueDescription().equals(CourseStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getMessage().formatted(courseStudent.getCourseStatus()))
        )).isFalse();

        courseStudent.setCourseStatus("123");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_STATUS.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getCode());
        assertThat(validationError3.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getMessage().formatted(courseStudent.getCourseStatus()));

        courseStudent.setCourseStatus(null);
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_STATUS.getCode());
        assertThat(validationError4.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getCode());
        assertThat(validationError4.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getMessage().formatted(courseStudent.getCourseStatus()));
    }

    @Test
    void testC11CourseStatusRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        CoregCoursesRecord traxAndMyEdBdRecord = new CoregCoursesRecord();
        traxAndMyEdBdRecord.setStartDate(LocalDateTime.of(1983, 2, 1,0,0,0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        traxAndMyEdBdRecord.setCompletionEndDate(LocalDate.of(9999, 5, 1).format(DateTimeFormatter.ISO_LOCAL_DATE));

        Set<CourseCodeRecord> courseCodes = new HashSet<>();
        CourseCodeRecord myEdBCCode = new CourseCodeRecord();
        myEdBCCode.setExternalCode("FCLE  12");
        myEdBCCode.setOriginatingSystem("38");
        courseCodes.add(myEdBCCode);

        CourseCodeRecord traxCode = new CourseCodeRecord();
        traxCode.setExternalCode("CLE  12");
        myEdBCCode.setOriginatingSystem("39");
        courseCodes.add(traxCode);
        traxAndMyEdBdRecord.setCourseCode(courseCodes);

        Set<CourseAllowableCreditRecord> courseAllowableCredits = new HashSet<>();
        CourseAllowableCreditRecord courseAllowableCreditRecord = new CourseAllowableCreditRecord();
        courseAllowableCreditRecord.setCourseID("856787");
        courseAllowableCreditRecord.setCreditValue("3");
        courseAllowableCreditRecord.setCacID("2145166");
        courseAllowableCreditRecord.setStartDate("1970-01-01 00:00:00");
        courseAllowableCreditRecord.setEndDate(null);
        courseAllowableCredits.add(courseAllowableCreditRecord);
        traxAndMyEdBdRecord.setCourseAllowableCredit(courseAllowableCredits);

        when(restUtils.getCoursesByExternalID(any(), any())).thenReturn(traxAndMyEdBdRecord);

        courseStudent.setCourseStatus("W");
        courseStudent.setCourseCode("CLE");
        courseStudent.setCourseLevel("12");
        courseStudent.setCourseMonth("06");
        courseStudent.setCourseYear("2023");

        when(restUtils.getGradStudentCoursesByStudentID(any(), any())).thenReturn(
                List.of(
                        new GradStudentCourseRecord(
                                null, // id
                                "3201860", // courseID
                                "202306", // courseSession
                                100, // interimPercent
                                "", // interimLetterGrade
                                100, // finalPercent
                                "A", // finalLetterGrade
                                4, // credits
                                "", // equivOrChallenge
                                "", // fineArtsAppliedSkills
                                "", // customizedCourseName
                                null, // relatedCourseId
                                new GradStudentCourseExam( // courseExam
                                        null, null, null, null, null, 99, null, null
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "CLE  12", // externalCode
                                        "38" // originatingSystem
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "MCLE 12", // externalCode
                                        "39" // originatingSystem
                                )
                        ),
                        new GradStudentCourseRecord(
                                null, // id
                                "3201862", // courseID
                                "2023/06", // courseSession
                                95, // interimPercent
                                "", // interimLetterGrade
                                95, // finalPercent
                                "A", // finalLetterGrade
                                4, // credits
                                "", // equivOrChallenge
                                "", // fineArtsAppliedSkills
                                "", // customizedCourseName
                                null, // relatedCourseId
                                new GradStudentCourseExam( // courseExam
                                        null, null, null, null, null, 99, null, null
                                ),
                                new GradCourseCode(
                                        "3201861", // courseID
                                        "CLC  12", // externalCode
                                        "38" // originatingSystem
                                ),
                                new GradCourseCode(
                                        "3201861", // courseID
                                        "MCLC 12", // externalCode
                                        "39" // originatingSystem
                                )
                        )
                )
        );
        when(restUtils.getCoreg38CourseByID(any())).thenReturn(
                Optional.of(new GradCourseCode(
                        "3201860", // courseID
                        "MCLE  12", // externalCode
                        "38" // originatingSystem
                ))
        );
        when(restUtils.getCoreg39CourseByID(any())).thenReturn(
                Optional.of(new GradCourseCode(
                        "3201860", // courseID
                        "CLE  12", // externalCode
                        "39" // originatingSystem
                ))
        );

        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(
                new GradStudentRecord(UUID.randomUUID().toString(), null, "2018", null, null, null, null, "true")
        );

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(incomingFileset), courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_STATUS.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_RECORD_EXISTS.getCode());
    }

    @Test
    void testC12CourseStatusRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());
        courseStudent.setCourseCode("CLE");
        courseStudent.setCourseLevel("12");
        courseStudent.setCourseMonth("06");
        courseStudent.setCourseYear("2023");

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseStatus("W");

        GradStudentRecord gradStudentRecord = new GradStudentRecord();
        gradStudentRecord.setSchoolOfRecordId("03636018");
        gradStudentRecord.setStudentStatusCode("CUR");
        gradStudentRecord.setProgramCompletionDate("2023-06-30T00:00:00+01:00");
        gradStudentRecord.setGraduated("true");
        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(gradStudentRecord);

        when(restUtils.getGradStudentCoursesByStudentID(any(), any())).thenReturn(
                List.of(
                        new GradStudentCourseRecord(
                                null, // id
                                "3201860", // courseID
                                "202106", // courseSession
                                100, // interimPercent
                                "", // interimLetterGrade
                                100, // finalPercent
                                "A", // finalLetterGrade
                                4, // credits
                                "", // equivOrChallenge
                                "", // fineArtsAppliedSkills
                                "", // customizedCourseName
                                null, // relatedCourseId
                                new GradStudentCourseExam( // courseExam
                                        null, null, null, null, null, null, null, null
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "CLE  12", // externalCode
                                        "38" // originatingSystem
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "MCLE 12", // externalCode
                                        "39" // originatingSystem
                                )
                        ),
                        new GradStudentCourseRecord(
                                null, // id
                                "3201862", // courseID
                                "202306", // courseSession
                                95, // interimPercent
                                "", // interimLetterGrade
                                95, // finalPercent
                                "A", // finalLetterGrade
                                4, // credits
                                "", // equivOrChallenge
                                "", // fineArtsAppliedSkills
                                "", // customizedCourseName
                                null, // relatedCourseId
                                new GradStudentCourseExam( // courseExam
                                        null, null, null, null, null, null, null, null
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "CLC  12", // externalCode
                                        "38" // originatingSystem
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "MCLC 12", // externalCode
                                        "39" // originatingSystem
                                )
                        )
                )
        );

        when(restUtils.getCoreg38CourseByID(any())).thenReturn(
                Optional.of(new GradCourseCode(
                        "3201860", // courseID
                        "MCLE 12", // externalCode
                        "38" // originatingSystem
                ))
        );
        when(restUtils.getCoreg39CourseByID(any())).thenReturn(
                Optional.of(new GradCourseCode(
                        "3201860", // courseID
                        "CLE  12", // externalCode
                        "39" // originatingSystem
                ))
        );

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(incomingFileset), courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_STATUS.getCode());
        assertTrue(validationError2.stream().anyMatch(e -> e.getValidationIssueCode().equalsIgnoreCase(CourseStudentValidationIssueTypeCode.COURSE_USED_FOR_GRADUATION.getCode())));
    }

    @Test
    void testCourseCodeDefaultPasses() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileset = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileset);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileset);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        StudentRuleData ruleData = createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone());
        var issues = rulesProcessor.processRules(ruleData);
        assertThat(issues).isEmpty();
    }

    @Test
    void testC03CourseCodeMatchesTraxAndBCeID() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileset = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileset);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileset);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());
        courseStudent.setCourseCode("MPH--");
        courseStudent.setCourseLevel("11");

        CoregCoursesRecord traxAndMyEdBdRecord = new CoregCoursesRecord();
        traxAndMyEdBdRecord.setStartDate(LocalDateTime.of(1983, 2, 1,0,0,0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        traxAndMyEdBdRecord.setCompletionEndDate(LocalDate.of(9999, 5, 1).format(DateTimeFormatter.ISO_LOCAL_DATE));

        Set<CourseCodeRecord> courseCodes = new HashSet<>();
        CourseCodeRecord myEdBCCode = new CourseCodeRecord();
        myEdBCCode.setExternalCode("MPH--11");
        myEdBCCode.setOriginatingSystem("38");
        courseCodes.add(myEdBCCode);

        CourseCodeRecord traxCode = new CourseCodeRecord();
        traxCode.setExternalCode("MPH--11");
        myEdBCCode.setOriginatingSystem("39");
        courseCodes.add(traxCode);
        traxAndMyEdBdRecord.setCourseCode(courseCodes);

        Set<CourseAllowableCreditRecord> courseAllowableCredits = new HashSet<>();
        CourseAllowableCreditRecord courseAllowableCreditRecord = new CourseAllowableCreditRecord();
        courseAllowableCreditRecord.setCourseID("856787");
        courseAllowableCreditRecord.setCreditValue("3");
        courseAllowableCreditRecord.setCacID("2145166");
        courseAllowableCreditRecord.setStartDate("1970-01-01 00:00:00");
        courseAllowableCreditRecord.setEndDate(null);
        courseAllowableCredits.add(courseAllowableCreditRecord);
        traxAndMyEdBdRecord.setCourseAllowableCredit(courseAllowableCredits);

        when(restUtils.getCoursesByExternalID(any(), any())).thenReturn(traxAndMyEdBdRecord);

        StudentRuleData ruleData = createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone());
        var issues = rulesProcessor.processRules(ruleData);

        assertThat(issues).isEmpty();
        assertThat(issues.stream().noneMatch(issue ->
                issue.getValidationIssueFieldCode().equals(ValidationFieldCode.COURSE_CODE.getCode()) &&
                issue.getValidationIssueCode().equals(CourseStudentValidationIssueTypeCode.COURSE_CODE_COREG_MYEDBC_INVALID.getCode())
        )).isTrue();
    }

    @Test
    void testC03CourseCodeWithOnlyMyEdBC() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileset = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileset);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileset);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());
        courseStudent.setCourseCode("MPH--");
        courseStudent.setCourseLevel("11");

        CoregCoursesRecord myEdBCOnlyRecord = new CoregCoursesRecord();
        myEdBCOnlyRecord.setStartDate(LocalDateTime.of(1983, 2, 1,0,0,0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        myEdBCOnlyRecord.setCompletionEndDate(LocalDate.of(9999, 5, 1).format(DateTimeFormatter.ISO_LOCAL_DATE));

        Set<CourseCodeRecord> courseCodes = new HashSet<>();
        CourseCodeRecord myEdBCCode = new CourseCodeRecord();
        myEdBCCode.setExternalCode("MPH--11");
        myEdBCCode.setOriginatingSystem("38"); // MyEdBC only
        courseCodes.add(myEdBCCode);
        myEdBCOnlyRecord.setCourseCode(courseCodes);

        Set<CourseAllowableCreditRecord> courseAllowableCredits = new HashSet<>();
        CourseAllowableCreditRecord courseAllowableCreditRecord = new CourseAllowableCreditRecord();
        courseAllowableCreditRecord.setCourseID("856787");
        courseAllowableCreditRecord.setCreditValue("3");
        courseAllowableCreditRecord.setCacID("2145166");
        courseAllowableCreditRecord.setStartDate("1970-01-01 00:00:00");
        courseAllowableCreditRecord.setEndDate(null);
        courseAllowableCredits.add(courseAllowableCreditRecord);
        myEdBCOnlyRecord.setCourseAllowableCredit(courseAllowableCredits);

        when(restUtils.getCoursesByExternalID(any(), any())).thenReturn(myEdBCOnlyRecord);

        StudentRuleData ruleData = createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone());
        var issues = rulesProcessor.processRules(ruleData);

        assertThat(issues).isNotEmpty();
        assertThat(issues.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_CODE.getCode());
        assertThat(issues.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_CODE_COREG_MYEDBC_INVALID.getCode());
    }

    @Test
    void testC03CourseCodeWithOnlyMyEdBCNoMatch() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileset = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileset);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileset);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());
        courseStudent.setCourseCode("MPH");
        courseStudent.setCourseLevel("11");

        CoregCoursesRecord myEdBCOnlyRecord = new CoregCoursesRecord();
        myEdBCOnlyRecord.setStartDate(LocalDateTime.of(1983, 2, 1,0,0,0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        myEdBCOnlyRecord.setCompletionEndDate(LocalDate.of(9999, 5, 1).format(DateTimeFormatter.ISO_LOCAL_DATE));

        Set<CourseCodeRecord> courseCodes = new HashSet<>();
        CourseCodeRecord myEdBCCode = new CourseCodeRecord();
        myEdBCCode.setExternalCode("MPH--11");
        myEdBCCode.setOriginatingSystem("38"); // MyEdBC only
        courseCodes.add(myEdBCCode);
        myEdBCOnlyRecord.setCourseCode(courseCodes);

        Set<CourseAllowableCreditRecord> courseAllowableCredits = new HashSet<>();
        CourseAllowableCreditRecord courseAllowableCreditRecord = new CourseAllowableCreditRecord();
        courseAllowableCreditRecord.setCourseID("856787");
        courseAllowableCreditRecord.setCreditValue("3");
        courseAllowableCreditRecord.setCacID("2145166");
        courseAllowableCreditRecord.setStartDate("1970-01-01 00:00:00");
        courseAllowableCreditRecord.setEndDate(null);
        courseAllowableCredits.add(courseAllowableCreditRecord);
        myEdBCOnlyRecord.setCourseAllowableCredit(courseAllowableCredits);

        when(restUtils.getCoursesByExternalID(any(), any())).thenReturn(myEdBCOnlyRecord);

        StudentRuleData ruleData = createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone());
        var issues = rulesProcessor.processRules(ruleData);

        assertThat(issues).isEmpty();
    }

    @Test
    void testC03CourseCodeWithRecordButNoCodes() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileset = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileset);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileset);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        CoregCoursesRecord emptyCodesRecord = new CoregCoursesRecord();
        emptyCodesRecord.setStartDate(LocalDateTime.of(1983, 2, 1,0,0,0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        emptyCodesRecord.setCompletionEndDate(LocalDate.of(9999, 5, 1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        emptyCodesRecord.setCourseCode(new HashSet<>());  // No course code records

        Set<CourseAllowableCreditRecord> courseAllowableCredits = new HashSet<>();
        CourseAllowableCreditRecord courseAllowableCreditRecord = new CourseAllowableCreditRecord();
        courseAllowableCreditRecord.setCourseID("856787");
        courseAllowableCreditRecord.setCreditValue("3");
        courseAllowableCreditRecord.setCacID("2145166");
        courseAllowableCreditRecord.setStartDate("1970-01-01 00:00:00");
        courseAllowableCreditRecord.setEndDate(null);
        courseAllowableCredits.add(courseAllowableCreditRecord);
        emptyCodesRecord.setCourseAllowableCredit(courseAllowableCredits);

        when(restUtils.getCoursesByExternalID(any(), any())).thenReturn(emptyCodesRecord);

        StudentRuleData ruleData = createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone());
        var issues = rulesProcessor.processRules(ruleData);

        assertThat(issues).isNotEmpty();
        assertThat(issues.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_CODE.getCode());
        assertThat(issues.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_CODE_COREG_TRAX_INVALID.getCode());
    }

    @Test
    void testC05CourseCodeRule() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseCode("QCLE");
        courseStudent.setCourseLevel("12");
        courseStudent.setCourseMonth("06");
        courseStudent.setCourseYear("2023");

        when(restUtils.getGradStudentCoursesByStudentID(any(), any())).thenReturn(
                List.of(
                        new GradStudentCourseRecord(
                                null, // id
                                "3201860", // courseID
                                "202306", // courseSession
                                100, // interimPercent
                                "", // interimLetterGrade
                                100, // finalPercent
                                "A", // finalLetterGrade
                                4, // credits
                                "", // equivOrChallenge
                                "", // fineArtsAppliedSkills
                                "", // customizedCourseName
                                null, // relatedCourseId
                                new GradStudentCourseExam( // courseExam
                                        null, null, null, null, null, null, null, null
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "QCLC 12", // externalCode
                                        "38" // originatingSystem
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "QCLC 12", // externalCode
                                        "39" // originatingSystem
                                )
                        ),
                        new GradStudentCourseRecord(
                                null, // id
                                "3201862", // courseID
                                "2023/06", // courseSession
                                95, // interimPercent
                                "", // interimLetterGrade
                                95, // finalPercent
                                "A", // finalLetterGrade
                                4, // credits
                                "", // equivOrChallenge
                                "", // fineArtsAppliedSkills
                                "", // customizedCourseName
                                null, // relatedCourseId
                                new GradStudentCourseExam( // courseExam
                                        null, null, null, null, null, null, null, null
                                ),
                                new GradCourseCode(
                                        "3201861", // courseID
                                        "QCLE 12", // externalCode
                                        "38" // originatingSystem
                                ),
                                new GradCourseCode(
                                        "3201861", // courseID
                                        "QCLE 12", // externalCode
                                        "39" // originatingSystem
                                )
                        )
                )
        );

        when(restUtils.getCoreg38CourseByID(any())).thenReturn(
                Optional.of(new GradCourseCode(
                        "3201860", // courseID
                        "QCLC 12", // externalCode
                        "38" // originatingSystem
                ))
        );
        when(restUtils.getCoreg39CourseByID(any())).thenReturn(
                Optional.of(new GradCourseCode(
                        "3201860", // courseID
                        "QCLC 12", // externalCode
                        "39" // originatingSystem
                ))
        );

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(incomingFileset), courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_CODE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.Q_CODE_INVALID.getCode());

        courseStudent.setCourseCode("QCLC");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(incomingFileset), courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError3.size()).isZero();
    }


    @Test
    void testC06CourseSession() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        courseStudentRepository.save(courseStudent);
        courseStudent.setCourseStudentID(null);
        courseStudentRepository.save(courseStudent);

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_DUPLICATE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_DUPLICATE.getMessage());
    }

    @Test
    void testC07CourseMonth() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseMonth("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_MONTH.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getMessage().formatted(courseStudent.getCourseMonth()));

        courseStudent.setCourseMonth("");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_MONTH.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getCode());
        assertThat(validationError3.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getMessage().formatted(courseStudent.getCourseMonth()));

        courseStudent.setCourseMonth(null);
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_MONTH.getCode());
        assertThat(validationError4.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getCode());
        assertThat(validationError4.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getMessage().formatted(courseStudent.getCourseMonth()));
    }

    @Test
    void testC13CourseSession() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseYear("1983");
        courseStudent.setCourseMonth("01");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.stream().anyMatch(err ->
            err.getValidationIssueFieldCode().equals(ValidationFieldCode.COURSE_MONTH.getCode()) &&
            err.getValidationIssueCode().equals(CourseStudentValidationIssueTypeCode.COURSE_SESSION_START_DATE_INVALID.getCode()) &&
            err.getValidationIssueDescription().equals(CourseStudentValidationIssueTypeCode.COURSE_SESSION_START_DATE_INVALID.getMessage())
        )).isTrue();
        assertThat(validationError2.stream().anyMatch(err ->
            err.getValidationIssueFieldCode().equals(ValidationFieldCode.COURSE_YEAR.getCode()) &&
            err.getValidationIssueCode().equals(CourseStudentValidationIssueTypeCode.COURSE_SESSION_START_DATE_INVALID.getCode()) &&
            err.getValidationIssueDescription().equals(CourseStudentValidationIssueTypeCode.COURSE_SESSION_START_DATE_INVALID.getMessage())
        )).isTrue();
    }

    @Test
    void testC14CourseSession() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseYear("9999");
        courseStudent.setCourseMonth("07");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_MONTH.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_COMPLETION_END_DATE_INVALID.getCode());

        courseStudent.setCourseYear(LocalDate.now().minusYears(1).toString());
        courseStudent.setCourseMonth("10");
        CoregCoursesRecord coursesRecord = new CoregCoursesRecord();
        coursesRecord.setStartDate(LocalDateTime.of(1983, 2, 1,0,0,0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        coursesRecord.setCompletionEndDate(null);
        coursesRecord.setCourseCode(new HashSet<>());
        Set<CourseAllowableCreditRecord> courseAllowableCredits = new HashSet<>();
        CourseAllowableCreditRecord courseAllowableCreditRecord = new CourseAllowableCreditRecord();
        courseAllowableCreditRecord.setCourseID("856787");
        courseAllowableCreditRecord.setCreditValue("3");
        courseAllowableCreditRecord.setCacID("2145166");
        courseAllowableCreditRecord.setStartDate("1970-01-01 00:00:00");
        courseAllowableCreditRecord.setEndDate(null);
        courseAllowableCredits.add(courseAllowableCreditRecord);
        coursesRecord.setCourseAllowableCredit(courseAllowableCredits);
        when(restUtils.getCoursesByExternalID(any(), any())).thenReturn(coursesRecord);
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError3.stream().noneMatch(code -> code.getValidationIssueFieldCode().equalsIgnoreCase(CourseStudentValidationIssueTypeCode.COURSE_SESSION_COMPLETION_END_DATE_INVALID.getCode()))).isTrue();
    }

    @Test
    void testC16CourseSession() {
        // Dynamically determine the current date and school year for testing - can't mock LocalDate.now()
        LocalDate today = LocalDate.now();
        int currentYear = (today.getMonthValue() >= 10) ? today.getYear() : today.getYear() - 1;
        YearMonth currentSchoolYearStart = YearMonth.of(currentYear, 10);
        YearMonth nextSchoolYearEnd = YearMonth.of(currentYear + 1, 9);

        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        // Case 1: Valid course session (within the current school year)
        courseStudent.setCourseYear(String.valueOf(currentSchoolYearStart.getYear()));
        courseStudent.setCourseMonth("10");
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        // Case 2: Course session too old
        courseStudent.setCourseYear("1983");
        courseStudent.setCourseMonth("12");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_MONTH.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getMessage());

        // Case 3: Course session too far in the future
        courseStudent.setCourseYear(String.valueOf(nextSchoolYearEnd.getYear() + 1));
        courseStudent.setCourseMonth("02");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_MONTH.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode());
        assertThat(validationError3.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getMessage());

        // Case 4: Invalid course year and month - skips
        courseStudent.setCourseYear(null);
        courseStudent.setCourseMonth("01");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError4.stream().noneMatch(code -> code.getValidationIssueFieldCode().equalsIgnoreCase(CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()))).isTrue();

        // Case 5: Boundary case - earliest valid date
        courseStudent.setCourseYear("1984");
        courseStudent.setCourseMonth("01");
        courseStudent.setFinalPercentage("");
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError5.size()).isZero();

        // Case 6: Boundary case - last month of next school year
        courseStudent.setCourseYear(String.valueOf(nextSchoolYearEnd.getYear()));
        courseStudent.setCourseMonth("09");
        courseStudent.setFinalLetterGrade(null);
        System.out.println("courseStudent");
        System.out.println(courseStudent);
        val validationError6 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        System.out.println("problem righ here");
        System.out.println(validationError6);
        assertThat(validationError6.size()).isZero();

        // Case 7: Boundary case - just before the earliest valid date
        courseStudent.setCourseYear("1983");
        courseStudent.setCourseMonth("12");
        val validationError7 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError7.size()).isNotZero();
        assertThat(validationError7.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_MONTH.getCode());
        assertThat(validationError7.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode());
        assertThat(validationError7.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getMessage());

        // Case 8: Boundary case - just after the next school year ends
        courseStudent.setCourseYear(String.valueOf(nextSchoolYearEnd.getYear()));
        courseStudent.setCourseMonth("10");
        val validationError8 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError8.size()).isNotZero();
        assertThat(validationError8.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_MONTH.getCode());
        assertThat(validationError8.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode());
        assertThat(validationError8.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getMessage());
    }

    @Test
    void testPastExaminableCourseRule_whenDataMatches_thenSucceeds_c15() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setCourseCode("CLE");
        courseStudent.setCourseLevel("12");
        courseStudent.setCourseMonth("06");
        courseStudent.setCourseYear("2023");
        courseStudent.setFinalPercentage("95");
        courseStudent.setFinalLetterGrade("A");

        when(restUtils.getCoreg38CourseByID(any())).thenReturn(
                Optional.of(new GradCourseCode(
                        "3201860", // courseID
                        "CLE  12", // externalCode
                        "38" // originatingSystem
                ))
        );
        when(restUtils.getExaminableCourseByExternalID(any())).thenReturn(
                List.of(new GradExaminableCourse(UUID.randomUUID(), "2018", "CLE", "12", "Creative Writing 12",
                        50, 50, null, null, "2020-01", "2024-12"),
                        new GradExaminableCourse(UUID.randomUUID(), "2019", "CLE", "12", "Creative Writing 12",
                                50, 50, null, null, "2020-01", "2024-12"),
                        new GradExaminableCourse(UUID.randomUUID(), "2018", "CLC", "12", "Creative Writing 12",
                                50, 50, null, null, "2020-01", "2024-12"),
                        new GradExaminableCourse(UUID.randomUUID(), "2018", "CLE", "11", "Creative Writing 12",
                                50, 50, null, null, "2020-01", "2024-12"))
        );
        when(restUtils.getGradStudentCoursesByStudentID(any(), any())).thenReturn(
                List.of(new GradStudentCourseRecord("12345", "3201860", "2023/06", 100, "", 95, "A", 4, "", "", "", null, null, new GradCourseCode("3201860", "CLE  12", "38"), null))
        );

        var validationErrors = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));

        assertThat(validationErrors).isEmpty();
    }

    @Test
    void testPastExaminableCourseRule_whenNotExaminable_thenSucceeds_c15() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());

        when(restUtils.getExaminableCourseByExternalID(any())).thenReturn(List.of());

        var validationErrors = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));

        assertThat(validationErrors).isEmpty();
    }

    @Test
    void testPastExaminableCourseRule_whenPercentageMismatches_thenFails_c15() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setCourseCode("CLE");
        courseStudent.setCourseLevel("12");
        courseStudent.setCourseMonth("06");
        courseStudent.setCourseYear("2023");
        courseStudent.setFinalPercentage("90");

        when(restUtils.getExaminableCourseByExternalID(any())).thenReturn(
                List.of(new GradExaminableCourse(UUID.randomUUID(), "2018", "CLE", "12", "Creative Writing 12",
                        50, 50, null, null, "2020-01", "2024-12"))
        );
        when(restUtils.getGradStudentCoursesByStudentID(any(), any())).thenReturn(
                List.of(new GradStudentCourseRecord(null, "3201860", "2023/06", 100, "", 85, "A", 4, "", "", "", null, null, new GradCourseCode("3201860", "CLE  12", "38"), null))
        );

        var validationErrors = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));

        assertThat(validationErrors).hasSize(1);
        assertThat(validationErrors.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.EXAMINABLE_COURSES_DISCONTINUED.getCode());
    }

    @Test
    void testPastExaminableCourseRule_whenGradeMismatches_thenFails_c15() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setCourseCode("CLE");
        courseStudent.setCourseLevel("12");
        courseStudent.setCourseMonth("06");
        courseStudent.setCourseYear("2023");
        courseStudent.setFinalLetterGrade("B");
        courseStudent.setFinalPercentage("80");

        when(restUtils.getExaminableCourseByExternalID(any())).thenReturn(
                List.of(new GradExaminableCourse(UUID.randomUUID(), "2023", "CLE", "12", "Creative Writing 12",
                        50, 50, null, null, "2020-01", "2024-12"))
        );
        when(restUtils.getGradStudentCoursesByStudentID(any(), any())).thenReturn(
                List.of(new GradStudentCourseRecord(null, "3201860", "2023/06", 100, "", 95, "A", 4, "", "", "", null, null, new GradCourseCode("3201860", "CLE  12", "38"), null))
        );

        var validationErrors = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));

        assertThat(validationErrors).hasSize(1);
        assertThat(validationErrors.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.EXAMINABLE_COURSES_DISCONTINUED.getCode());
    }

    @Test
    void testPastExaminableCourseRule_whenCourseNotFound_thenFails_c15() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setCourseCode("CLE");
        courseStudent.setCourseLevel("12");
        courseStudent.setCourseMonth("06");
        courseStudent.setCourseYear("2023");

        when(restUtils.getExaminableCourseByExternalID(any())).thenReturn(
                List.of(new GradExaminableCourse(UUID.randomUUID(), "2018", "CLE", "12", "Creative Writing 12",
                        50, 50, null, null, "2020-01", "2024-12"))
        );
        when(restUtils.getGradStudentCoursesByStudentID(any(), any())).thenReturn(Collections.emptyList());

        var validationErrors = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));

        assertThat(validationErrors).hasSize(1);
        assertThat(validationErrors.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.EXAMINABLE_COURSES_DISCONTINUED.getCode());
    }

    @Test
    void testPastExaminableCourseRule_whenCourseNotFound_gradStudent_thenFails_c15() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setCourseCode("CLE");
        courseStudent.setCourseLevel("12");
        courseStudent.setCourseMonth("06");
        courseStudent.setCourseYear("2023");

        var studentID = UUID.randomUUID().toString();
        Student studentApiStudent = new Student();
        studentApiStudent.setStudentID(studentID);
        studentApiStudent.setPen("123456789");
        studentApiStudent.setLocalID("8887555");
        studentApiStudent.setLegalFirstName("JIM");
        studentApiStudent.setLegalLastName("JACKSON");
        studentApiStudent.setDob("1990-01-01");
        studentApiStudent.setStatusCode(StudentStatusCodes.A.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent);

        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(
                new GradStudentRecord(studentID, null, "2018", null, null, null, null, null)
        );

        when(restUtils.getExaminableCourseByExternalID(any())).thenReturn(
                List.of(new GradExaminableCourse(UUID.randomUUID(), "2018", "CLE", "12", "Creative Writing 12",
                        50, 50, null, null, "2020-01", "2024-12"))
        );
        when(restUtils.getGradStudentCoursesByStudentID(any(), any())).thenReturn(Collections.emptyList());

        var validationErrors = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));

        assertThat(validationErrors).hasSize(1);
        assertThat(validationErrors.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.EXAMINABLE_COURSES_DISCONTINUED.getCode());
    }

    @Test
    void testPastExaminableCourseRule_whenInvalidPercentageFormat_thenFails_c15() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setCourseCode("CLE");
        courseStudent.setCourseLevel("12");
        courseStudent.setCourseMonth("06");
        courseStudent.setCourseYear("2023");
        courseStudent.setFinalPercentage("ninety");

        when(restUtils.getExaminableCourseByExternalID(any())).thenReturn(
                List.of(new GradExaminableCourse(UUID.randomUUID(), "2018", "CLE", "12", "Creative Writing 12",
                        50, 50, null, null, "2020-01", "2024-12"))
        );
        when(restUtils.getGradStudentCoursesByStudentID(any(), any())).thenReturn(
                List.of(new GradStudentCourseRecord(null, "3201860", "2023/06", 100, "", 90, "A", 4, "", "", "", null, null, new GradCourseCode("3201860", "CLE  12", "38"), null))
        );

        var validationErrors = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));

        assertThat(validationErrors.stream().anyMatch(e -> e.getValidationIssueCode().equals(CourseStudentValidationIssueTypeCode.EXAMINABLE_COURSES_DISCONTINUED.getCode()))).isTrue();
    }

    @Test
    void testPastExaminableCourseRule_whenOneValueIsNull_thenFails_c15() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setCourseCode("CLE");
        courseStudent.setCourseLevel("12");
        courseStudent.setCourseMonth("06");
        courseStudent.setCourseYear("2023");

        when(restUtils.getExaminableCourseByExternalID(any())).thenReturn(
                List.of(new GradExaminableCourse(UUID.randomUUID(), "2018", "CLE", "12", "Creative Writing 12",
                        50, 50, null, null, "2020-01", "2024-12"))
        );
        var studentRuleData = createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone());

        courseStudent.setFinalPercentage("90");
        courseStudent.setFinalLetterGrade("A");
        when(restUtils.getGradStudentCoursesByStudentID(any(), any())).thenReturn(
                List.of(new GradStudentCourseRecord(null, "3201860", "2023/06", 100, "", null, "A", 4, "", "", "", null, null, new GradCourseCode("3201860", "CLE  12", "38"), null))
        );
        var validationErrors_percentNull = rulesProcessor.processRules(studentRuleData);
        assertThat(validationErrors_percentNull).hasSize(1);
        assertThat(validationErrors_percentNull.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.EXAMINABLE_COURSES_DISCONTINUED.getCode());

        courseStudent.setFinalPercentage("90");
        courseStudent.setFinalLetterGrade("A");
        when(restUtils.getGradStudentCoursesByStudentID(any(), any())).thenReturn(
                List.of(new GradStudentCourseRecord(null, "3201860", "2023/06", 100, "", 90, null, 4, "", "", "", null, null, new GradCourseCode("3201860", "CLE  12", "38"), null))
        );

        var studentRuleData2 = createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone());
        var validationErrors_gradeNull = rulesProcessor.processRules(studentRuleData2);
        assertThat(validationErrors_gradeNull).hasSize(1);
        assertThat(validationErrors_gradeNull.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.EXAMINABLE_COURSES_DISCONTINUED.getCode());
    }

    @Test
    void testC22InterimPercent() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setInterimPercentage("-1");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.INTERIM_PERCENTAGE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.INTERIM_PCT_INVALID.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.INTERIM_PCT_INVALID.getMessage());

        courseStudent.setInterimPercentage("101");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.INTERIM_PERCENTAGE.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.INTERIM_PCT_INVALID.getCode());
        assertThat(validationError3.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.INTERIM_PCT_INVALID.getMessage());

        courseStudent.setInterimPercentage("not_a_number");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.INTERIM_PERCENTAGE.getCode());
        assertThat(validationError4.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.INTERIM_PCT_INVALID.getCode());
        assertThat(validationError4.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.INTERIM_PCT_INVALID.getMessage());
    }

    @Test
    void testC23InterimLetterGrade() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setInterimLetterGrade("ABCD");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.INTERIM_GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_INVALID.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_INVALID.getMessage().formatted(courseStudent.getInterimLetterGrade()));
    }

    @Test
    void testC29InterimGradePercent() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        // Test: interim percent provided but no interim letter grade
        courseStudent.setInterimLetterGrade("");
        courseStudent.setInterimPercentage("100");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isZero();

        // Test: interim letter grade with percent range, interim percent missing
        courseStudent.setInterimLetterGrade("A");
        courseStudent.setInterimPercentage("");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError4.stream().anyMatch(err ->
            err.getValidationIssueFieldCode().equals(ValidationFieldCode.INTERIM_PERCENTAGE.getCode()) &&
            err.getValidationIssueCode().equals(CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_PERCENT_REQUIRED.getCode()) &&
            err.getValidationIssueDescription().equals(CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_PERCENT_REQUIRED.getMessage())
        )).isTrue();

        // Test: interim letter grade with percent range, interim percent out of range
        courseStudent.setInterimLetterGrade("A");
        courseStudent.setInterimPercentage("85");
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError5.stream().anyMatch(err ->
            err.getValidationIssueFieldCode().equals(ValidationFieldCode.INTERIM_PERCENTAGE.getCode()) &&
            err.getValidationIssueCode().equals(CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_PERCENT_OUT_OF_RANGE.getCode()) &&
            err.getValidationIssueDescription().equals(CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_PERCENT_OUT_OF_RANGE.getMessage())
        )).isTrue();

        // Test: interim letter grade with percent range, interim percent out of range
        courseStudent.setInterimLetterGrade("F");
        courseStudent.setInterimPercentage("0");
        val validationError6 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError6.size()).isZero();
    }

    @Test
    void testC24FinalLetterGradeAndPercentNotBlank() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        YearMonth validFutureCourseSession = YearMonth.now().plusMonths(6);
        courseStudent.setCourseYear(String.valueOf(validFutureCourseSession.getYear()));
        courseStudent.setCourseMonth(String.format("%02d", validFutureCourseSession.getMonthValue()));

        courseStudent.setFinalLetterGrade("");
        courseStudent.setFinalPercentage("");
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isNotZero();
        var issueCode = validationError1.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.FINAL_PERCENTAGE.getCode()));
        var errorCode = validationError1.stream().anyMatch(val -> val.getValidationIssueCode().equals(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_OR_PERCENT_NOT_BLANK.getCode()));
        var errorMessage = validationError1.stream().anyMatch(val -> val.getValidationIssueDescription().equals(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_OR_PERCENT_NOT_BLANK.getMessage()));
        assertThat(issueCode).isFalse();
        assertThat(errorCode).isFalse();
        assertThat(errorMessage).isFalse();

        courseStudent.setFinalLetterGrade("A");
        courseStudent.setFinalPercentage("90");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();

        var issueCode2 = validationError2.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.FINAL_PERCENTAGE.getCode()));
        var errorCode2 = validationError2.stream().anyMatch(val -> val.getValidationIssueCode().equals(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_OR_PERCENT_NOT_BLANK.getCode()));
        var errorMessage2 = validationError2.stream().anyMatch(val -> val.getValidationIssueDescription().equals(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_OR_PERCENT_NOT_BLANK.getMessage()));
        assertThat(issueCode2).isFalse();
        assertThat(errorCode2).isFalse();
        assertThat(errorMessage2).isFalse();
    }

    @Test
    void testC30FinalPercent() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setFinalPercentage("-1");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_PERCENTAGE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_PCT_INVALID.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_PCT_INVALID.getMessage());

        courseStudent.setFinalPercentage("101");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_PERCENTAGE.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_PCT_INVALID.getCode());
        assertThat(validationError3.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_PCT_INVALID.getMessage());

        courseStudent.setFinalPercentage("not_a_number");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_PERCENTAGE.getCode());
        assertThat(validationError4.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_PCT_INVALID.getCode());
        assertThat(validationError4.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_PCT_INVALID.getMessage());
    }

    @Test
    void testC31FinalPercent() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setFinalPercentage("94");
        courseStudent.setCourseYear("1990");
        courseStudent.setCourseMonth("02");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_PERCENTAGE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_PCT_NOT_BLANK.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_PCT_NOT_BLANK.getMessage());

        courseStudent.setCourseMonth("01");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError3.stream().noneMatch(code -> code.getValidationIssueFieldCode().equalsIgnoreCase(CourseStudentValidationIssueTypeCode.FINAL_PCT_NOT_BLANK.getCode()))).isTrue();
    }

    @Test
    void testC32FinalLetterGrade() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setFinalLetterGrade("ABCD");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_LETTER_GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_INVALID.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_INVALID.getMessage().formatted(courseStudent.getFinalLetterGrade()));
    }

    @Test
    void testC37FinalLetterGradePercent() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        // Test: final letter grade with percent range, final percent missing
        courseStudent.setFinalLetterGrade("A");
        courseStudent.setFinalPercentage("");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError4.stream().anyMatch(err ->
                err.getValidationIssueFieldCode().equals(ValidationFieldCode.FINAL_PERCENTAGE.getCode()) &&
                        err.getValidationIssueCode().equals(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_PERCENT_REQUIRED.getCode()) &&
                        err.getValidationIssueDescription().equals(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_PERCENT_REQUIRED.getMessage())
        )).isTrue();

        // Test: final letter grade with percent range, final percent out of range
        courseStudent.setFinalLetterGrade("A");
        courseStudent.setFinalPercentage("85");
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError5.stream().anyMatch(err ->
                err.getValidationIssueFieldCode().equals(ValidationFieldCode.FINAL_PERCENTAGE.getCode()) &&
                        err.getValidationIssueCode().equals(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_PERCENT_OUT_OF_RANGE.getCode()) &&
                        err.getValidationIssueDescription().equals(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_PERCENT_OUT_OF_RANGE.getMessage())
        )).isTrue();

        courseStudent.setFinalLetterGrade("F");
        courseStudent.setFinalPercentage("0");
        val validationError6 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError6.size()).isZero();
    }

    @Test
    void testC38FinalLetterGradeRM() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        courseStudent.setFinalLetterGrade("RM");
        courseStudent.setCourseCode("GT");
        courseStudent.setFinalPercentage(null);
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseCode("ABC");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_LETTER_GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_RM.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_RM.getMessage());
    }

    @Test
    void testC39FinalLetterGradeNotRM() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        courseStudent.setFinalLetterGrade("RM");
        courseStudent.setCourseCode("GT");
        courseStudent.setFinalPercentage(null);
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setFinalLetterGrade("A");
        courseStudent.setFinalPercentage("90");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_LETTER_GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_NOT_RM.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_NOT_RM.getMessage());
    }

    @Test
    void testC25FinalLetterGradeAndPercentNotBlank() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setFinalLetterGrade("");
        courseStudent.setFinalPercentage("");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_PERCENTAGE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_OR_PERCENT_BLANK.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_OR_PERCENT_BLANK.getMessage());
    }

    @Test
    void testC40FinalLetterGradeIE() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        courseStudent.setCourseYear("2022");
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setFinalLetterGrade("IE");
        courseStudent.setFinalPercentage(null);
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_LETTER_GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_IE.getCode());
    }

    @Test
    void testC18NumberOfCredits() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setNumberOfCredits("4");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.NUMBER_OF_CREDITS.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID.getMessage());

        courseStudent.setNumberOfCredits("0");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.NUMBER_OF_CREDITS.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID.getCode());
        assertThat(validationError3.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID.getMessage());

        courseStudent.setNumberOfCredits(null);
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.NUMBER_OF_CREDITS.getCode());
        assertThat(validationError4.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID.getCode());
        assertThat(validationError4.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID.getMessage());

        CoregCoursesRecord coursesRecord = new CoregCoursesRecord();
        coursesRecord.setStartDate(LocalDateTime.of(1983, 2, 1,0,0,0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        coursesRecord.setCompletionEndDate(LocalDate.of(9999, 5, 1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        Set<CourseCodeRecord> courseCodes = new HashSet<>();
        CourseCodeRecord traxCode = new CourseCodeRecord();
        traxCode.setCourseID("856787");
        traxCode.setExternalCode("PH   11");
        traxCode.setOriginatingSystem("39"); // TRAX
        courseCodes.add(traxCode);
        CourseCodeRecord myEdBCCode = new CourseCodeRecord();
        myEdBCCode.setCourseID("856787");
        myEdBCCode.setExternalCode("MPH--11");
        myEdBCCode.setOriginatingSystem("38"); // MyEdBC
        courseCodes.add(myEdBCCode);
        coursesRecord.setCourseCode(courseCodes);
        Set<CourseAllowableCreditRecord> courseAllowableCredits = new HashSet<>();
        CourseAllowableCreditRecord courseAllowableCreditRecord = new CourseAllowableCreditRecord();
        courseAllowableCreditRecord.setCourseID("856787");
        courseAllowableCreditRecord.setCreditValue("3");
        courseAllowableCreditRecord.setCacID("2145166");
        courseAllowableCreditRecord.setStartDate("1970-01-01 00:00:00");
        courseAllowableCreditRecord.setEndDate(null);
        courseAllowableCredits.add(courseAllowableCreditRecord);
        coursesRecord.setCourseAllowableCredit(courseAllowableCredits);
        CourseCharacteristicsRecord courseCategory = new CourseCharacteristicsRecord();
        courseCategory.setId("2932");
        courseCategory.setType("LD");
        courseCategory.setCode("BA");
        courseCategory.setDescription("");
        coursesRecord.setCourseCategory(courseCategory);
        coursesRecord.setGenericCourseType("G");
        when(restUtils.getCoursesByExternalID(any(), any())).thenReturn(coursesRecord);

        courseStudent.setNumberOfCredits(null);
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError5.size()).isNotZero();
        assertThat(validationError5.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.NUMBER_OF_CREDITS.getCode());
        assertThat(validationError5.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID.getCode());
        assertThat(validationError5.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID.getMessage());

        courseStudent.setNumberOfCredits(null);
        courseStudent.setFinalLetterGrade("F");
        val validationError6 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError6.size()).isNotZero();

    }

    @Test
    void testC09EquivalencyChallengeCode() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseType("A");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_TYPE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.EQUIVALENCY_CHALLENGE_CODE_INVALID.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.EQUIVALENCY_CHALLENGE_CODE_INVALID.getMessage().formatted(courseStudent.getCourseType()));
    }

    @Test
    void testC10CourseGraduationRequirement() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseGraduationRequirement("Z");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_GRADUATION_REQUIREMENT.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.INVALID_FINE_ARTS_APPLIED_SKILLS_CODE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.INVALID_FINE_ARTS_APPLIED_SKILLS_CODE.getMessage().formatted(courseStudent.getCourseGraduationRequirement()));
    }

    @Test
    void testC33CourseGraduationRequirementNumberOfCredits() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        var incomingFileset2 = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet2 = incomingFilesetRepository.save(incomingFileset2);
        var demStudent2 = createMockDemographicStudent(savedFileSet2);
        demStudent2.setGradRequirementYear(GradRequirementYearCodes.YEAR_1996.getCode());
        demographicStudentRepository.save(demStudent2);
        courseStudent.setPen(demStudent2.getPen());
        courseStudent.setLocalID(demStudent2.getLocalID());
        courseStudent.setLastName(demStudent2.getLastName());
        courseStudent.setIncomingFileset(demStudent2.getIncomingFileset());
        courseStudent.setCourseGraduationRequirement("B");
        courseStudent.setNumberOfCredits("3");

        CoregCoursesRecord coursesRecord = new CoregCoursesRecord();
        coursesRecord.setStartDate(LocalDateTime.of(1983, 2, 1,0,0,0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        coursesRecord.setCompletionEndDate(LocalDate.of(9999, 5, 1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        Set<CourseCodeRecord> courseCodes = new HashSet<>();
        CourseCodeRecord traxCode = new CourseCodeRecord();
        traxCode.setCourseID("856787");
        traxCode.setExternalCode("PH   11");
        traxCode.setOriginatingSystem("39"); // TRAX
        courseCodes.add(traxCode);
        CourseCodeRecord myEdBCCode = new CourseCodeRecord();
        myEdBCCode.setCourseID("856787");
        myEdBCCode.setExternalCode("MPH--11");
        myEdBCCode.setOriginatingSystem("38"); // MyEdBC
        courseCodes.add(myEdBCCode);
        coursesRecord.setCourseCode(courseCodes);
        Set<CourseAllowableCreditRecord> courseAllowableCredits = new HashSet<>();
        CourseAllowableCreditRecord courseAllowableCreditRecord = new CourseAllowableCreditRecord();
        courseAllowableCreditRecord.setCourseID("856787");
        courseAllowableCreditRecord.setCreditValue("3");
        courseAllowableCreditRecord.setCacID("2145166");
        courseAllowableCreditRecord.setStartDate("1970-01-01 00:00:00");
        courseAllowableCreditRecord.setEndDate(null);
        courseAllowableCredits.add(courseAllowableCreditRecord);
        coursesRecord.setCourseAllowableCredit(courseAllowableCredits);
        CourseCharacteristicsRecord courseCategory = new CourseCharacteristicsRecord();
        courseCategory.setId("2932");
        courseCategory.setType("CC");
        courseCategory.setCode("BA");
        courseCategory.setDescription("");
        coursesRecord.setCourseCategory(courseCategory);
        when(restUtils.getCoursesByExternalID(any(), any())).thenReturn(coursesRecord);

        demStudent2.setGradRequirementYear("1996");

        Student stud2 = new Student();
        stud2.setStudentID(UUID.randomUUID().toString());
        stud2.setDob("1990-01-01");
        stud2.setLegalLastName(demStudent2.getLastName());
        stud2.setLegalFirstName(demStudent2.getFirstName());
        stud2.setPen(demStudent2.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud2);

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent2, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();

        var issueCode = validationError2.stream().anyMatch(val -> val.getValidationIssueFieldCode().equals(ValidationFieldCode.COURSE_GRADUATION_REQUIREMENT.getCode()));
        var errorCode = validationError2.stream().anyMatch(val -> val.getValidationIssueCode().equals(CourseStudentValidationIssueTypeCode.GRADUATION_REQUIREMENT_NUMBER_CREDITS_INVALID.getCode()));
        var errorMessage = validationError2.stream().anyMatch(val -> val.getValidationIssueDescription().equals(CourseStudentValidationIssueTypeCode.GRADUATION_REQUIREMENT_NUMBER_CREDITS_INVALID.getMessage()));
        assertThat(issueCode).isTrue();
        assertThat(errorCode).isTrue();
        assertThat(errorMessage).isTrue();
   }

    @Test
    void testC19relatedCourseRelatedLevel() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());
        courseStudent.setCourseCode("IDS");
        courseStudent.setCourseLevel("12G");
        courseStudent.setRelatedCourse("IDS");
        courseStudent.setRelatedLevel("12G");


        CoregCoursesRecord coursesRecord = new CoregCoursesRecord();
        coursesRecord.setStartDate(LocalDateTime.of(1983, 2, 1,0,0,0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        coursesRecord.setCompletionEndDate(LocalDate.of(9999, 5, 1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        Set<CourseCodeRecord> courseCodes = new HashSet<>();
        CourseCodeRecord traxCode = new CourseCodeRecord();
        traxCode.setCourseID("856787");
        traxCode.setExternalCode("PH   11");
        traxCode.setOriginatingSystem("39"); // TRAX
        courseCodes.add(traxCode);
        CourseCodeRecord myEdBCCode = new CourseCodeRecord();
        myEdBCCode.setCourseID("856787");
        myEdBCCode.setExternalCode("MPH--11");
        myEdBCCode.setOriginatingSystem("38"); // MyEdBC
        courseCodes.add(myEdBCCode);
        coursesRecord.setCourseCode(courseCodes);
        Set<CourseAllowableCreditRecord> courseAllowableCredits = new HashSet<>();
        CourseAllowableCreditRecord courseAllowableCreditRecord = new CourseAllowableCreditRecord();
        courseAllowableCreditRecord.setCourseID("856787");
        courseAllowableCreditRecord.setCreditValue("3");
        courseAllowableCreditRecord.setCacID("2145166");
        courseAllowableCreditRecord.setStartDate("1970-01-01 00:00:00");
        courseAllowableCreditRecord.setEndDate(null);
        courseAllowableCredits.add(courseAllowableCreditRecord);
        coursesRecord.setCourseAllowableCredit(courseAllowableCredits);
        CourseCharacteristicsRecord courseCategory = new CourseCharacteristicsRecord();
        courseCategory.setId("2932");
        courseCategory.setType("CC");
        courseCategory.setCode("BA");
        courseCategory.setDescription("");
        coursesRecord.setCourseCategory(courseCategory);
        coursesRecord.setGenericCourseType("G");
        coursesRecord.setProgramGuideTitle("INDEPENDENT DIRECTED STUDIES");
        when(restUtils.getCoursesByExternalID(any(), any())).thenReturn(coursesRecord);

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        CoregCoursesRecord coursesRecord2 = new CoregCoursesRecord();
        coursesRecord2.setStartDate(LocalDateTime.of(1983, 2, 1,0,0,0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        coursesRecord2.setCompletionEndDate(LocalDate.of(9999, 5, 1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        Set<CourseCodeRecord> courseCodes2 = new HashSet<>();
        CourseCodeRecord traxCode2 = new CourseCodeRecord();
        traxCode2.setCourseID("856787");
        traxCode2.setExternalCode("PH   11");
        traxCode2.setOriginatingSystem("39"); // TRAX
        courseCodes2.add(traxCode2);
        CourseCodeRecord myEdBCCode2 = new CourseCodeRecord();
        myEdBCCode2.setCourseID("856787");
        myEdBCCode2.setExternalCode("MPH--11");
        myEdBCCode2.setOriginatingSystem("38"); // MyEdBC
        courseCodes2.add(myEdBCCode2);
        coursesRecord2.setCourseCode(courseCodes2);
        Set<CourseAllowableCreditRecord> courseAllowableCredits2 = new HashSet<>();
        CourseAllowableCreditRecord courseAllowableCreditRecord2 = new CourseAllowableCreditRecord();
        courseAllowableCreditRecord2.setCourseID("856787");
        courseAllowableCreditRecord2.setCreditValue("3");
        courseAllowableCreditRecord2.setCacID("2145166");
        courseAllowableCreditRecord2.setStartDate("1970-01-01 00:00:00");
        courseAllowableCreditRecord2.setEndDate(null);
        courseAllowableCredits2.add(courseAllowableCreditRecord2);
        coursesRecord2.setCourseAllowableCredit(courseAllowableCredits2);
        CourseCharacteristicsRecord courseCategory2 = new CourseCharacteristicsRecord();
        courseCategory2.setId("2932");
        courseCategory2.setType("CC");
        courseCategory2.setCode("BA");
        courseCategory2.setDescription("");
        coursesRecord2.setCourseCategory(courseCategory2);
        coursesRecord2.setGenericCourseType("G");
        when(restUtils.getCoursesByExternalID(any(), any())).thenReturn(coursesRecord2);

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.RELATED_COURSE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_NOT_INDEPENDENT_DIRECTED_STUDIES.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_NOT_INDEPENDENT_DIRECTED_STUDIES.getMessage());
        assertThat(validationError2.getLast().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.RELATED_LEVEL.getCode());
        assertThat(validationError2.getLast().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_NOT_INDEPENDENT_DIRECTED_STUDIES.getCode());
        assertThat(validationError2.getLast().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_NOT_INDEPENDENT_DIRECTED_STUDIES.getMessage());
    }

//    @Test
//    void testC28relatedCourseRelatedLevel() {
//        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
//        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
//        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
//        var demStudent = createMockDemographicStudent(savedFileSet);
//        demographicStudentRepository.save(demStudent);
//        var courseStudent = createMockCourseStudent(savedFileSet);
//        courseStudent.setPen(demStudent.getPen());
//        courseStudent.setLocalID(demStudent.getLocalID());
//        courseStudent.setLastName(demStudent.getLastName());
//        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());
//        courseStudent.setCourseCode("IDS");
//        courseStudent.setCourseLevel("12G");
//        courseStudent.setRelatedCourse("WRO");
//        courseStudent.setRelatedLevel("NG");
//
//
//        CoregCoursesRecord coursesRecord = new CoregCoursesRecord();
//        coursesRecord.setStartDate(LocalDateTime.of(1983, 2, 1,0,0,0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
//        coursesRecord.setCompletionEndDate(LocalDate.of(9999, 5, 1).format(DateTimeFormatter.ISO_LOCAL_DATE));
//        Set<CourseCodeRecord> courseCodes = new HashSet<>();
//        CourseCodeRecord traxCode = new CourseCodeRecord();
//        traxCode.setCourseID("856787");
//        traxCode.setExternalCode("PH   11");
//        traxCode.setOriginatingSystem("39"); // TRAX
//        courseCodes.add(traxCode);
//        CourseCodeRecord myEdBCCode = new CourseCodeRecord();
//        myEdBCCode.setCourseID("856787");
//        myEdBCCode.setExternalCode("MPH--11");
//        myEdBCCode.setOriginatingSystem("38"); // MyEdBC
//        courseCodes.add(myEdBCCode);
//        coursesRecord.setCourseCode(courseCodes);
//        Set<CourseAllowableCreditRecord> courseAllowableCredits = new HashSet<>();
//        CourseAllowableCreditRecord courseAllowableCreditRecord = new CourseAllowableCreditRecord();
//        courseAllowableCreditRecord.setCourseID("856787");
//        courseAllowableCreditRecord.setCreditValue("3");
//        courseAllowableCreditRecord.setCacID("2145166");
//        courseAllowableCreditRecord.setStartDate("1970-01-01 00:00:00");
//        courseAllowableCreditRecord.setEndDate(null);
//        courseAllowableCredits.add(courseAllowableCreditRecord);
//        coursesRecord.setCourseAllowableCredit(courseAllowableCredits);
//        CourseCharacteristicsRecord courseCategory = new CourseCharacteristicsRecord();
//        courseCategory.setId("2932");
//        courseCategory.setType("CC");
//        courseCategory.setCode("BA");
//        courseCategory.setDescription("");
//        coursesRecord.setCourseCategory(courseCategory);
//        coursesRecord.setGenericCourseType("G");
//        coursesRecord.setProgramGuideTitle("INDEPENDENT DIRECTED STUDIES");
//        when(restUtils.getCoursesByExternalID(any(), any())).thenReturn(coursesRecord);
//
//        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
//        assertThat(validationError1.size()).isZero();
//
//        CoregCoursesRecord coursesRecord2 = new CoregCoursesRecord();
//        coursesRecord.setStartDate(LocalDateTime.of(1983, 2, 1,0,0,0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
//        coursesRecord.setCompletionEndDate(LocalDate.of(9999, 5, 1).format(DateTimeFormatter.ISO_LOCAL_DATE));
//        Set<CourseCodeRecord> courseCodes2 = new HashSet<>();
//        CourseCodeRecord traxCode2 = new CourseCodeRecord();
//        traxCode2.setCourseID("856787");
//        traxCode2.setExternalCode("PH   11");
//        traxCode2.setOriginatingSystem("39"); // TRAX
//        courseCodes2.add(traxCode2);
//        CourseCodeRecord myEdBCCode2 = new CourseCodeRecord();
//        myEdBCCode2.setCourseID("856787");
//        myEdBCCode2.setExternalCode("MPH--11");
//        myEdBCCode2.setOriginatingSystem("38"); // MyEdBC
//        courseCodes2.add(myEdBCCode2);
//        coursesRecord2.setCourseCode(courseCodes2);
//        Set<CourseAllowableCreditRecord> courseAllowableCredits2 = new HashSet<>();
//        CourseAllowableCreditRecord courseAllowableCreditRecord2 = new CourseAllowableCreditRecord();
//        courseAllowableCreditRecord2.setCourseID("856787");
//        courseAllowableCreditRecord2.setCreditValue("3");
//        courseAllowableCreditRecord2.setCacID("2145166");
//        courseAllowableCreditRecord2.setStartDate("1970-01-01 00:00:00");
//        courseAllowableCreditRecord2.setEndDate(null);
//        courseAllowableCredits2.add(courseAllowableCreditRecord2);
//        coursesRecord.setCourseAllowableCredit(courseAllowableCredits2);
//        CourseCharacteristicsRecord courseCategory2 = new CourseCharacteristicsRecord();
//        courseCategory2.setId("2932");
//        courseCategory2.setType("ZZ");
//        courseCategory2.setCode("ZZ");
//        courseCategory2.setDescription("");
//        coursesRecord2.setCourseCategory(courseCategory2);
//        coursesRecord2.setProgramGuideTitle("INDEPENDENT DIRECTED STUDIES");
//        when(restUtils.getCoursesByExternalID(any(), any())).thenAnswer(invocation -> {
//            String externalId = invocation.getArgument(1);
//            System.out.println("Called with externalId: [" + externalId + "]");
//            if ("IDS  12G".equals(externalId)) {
//                return coursesRecord;
//            } else if ("WRO  NG".equals(externalId)) {
//                return null;
//            }
//            return null;
//        });
//
//        // todo c28 can never be hit currently
//
//        courseStudent.setCourseCode("WRO");
//        courseStudent.setCourseLevel("NG");
//
//        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
//        assertThat(validationError2.size()).isNotZero();
//        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.RELATED_COURSE.getCode());
//        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.RELATED_COURSE_RELATED_LEVEL_INVALID.getCode())
//        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.RELATED_COURSE_RELATED_LEVEL_INVALID.getMessage().formatted(courseStudent.getRelatedCourse()));
//        assertThat(validationError2.getLast().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.RELATED_LEVEL.getCode());
//        assertThat(validationError2.getLast().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.RELATED_COURSE_RELATED_LEVEL_INVALID.getCode());
//        assertThat(validationError2.getLast().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.RELATED_COURSE_RELATED_LEVEL_INVALID.getMessage().formatted(courseStudent.getRelatedCourse()));
//    }

    @Test
    void testC20relatedCourseRelatedLevel() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());
        courseStudent.setCourseCode("IDS");
        courseStudent.setCourseLevel("12G");
        courseStudent.setRelatedCourse(null);
        courseStudent.setRelatedLevel(null);

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        CoregCoursesRecord coursesRecord = new CoregCoursesRecord();
        coursesRecord.setStartDate(LocalDateTime.of(1983, 2, 1,0,0,0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        coursesRecord.setCompletionEndDate(LocalDate.of(9999, 5, 1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        Set<CourseCodeRecord> courseCodes = new HashSet<>();
        CourseCodeRecord traxCode = new CourseCodeRecord();
        traxCode.setCourseID("856787");
        traxCode.setExternalCode("PH   11");
        traxCode.setOriginatingSystem("39"); // TRAX
        courseCodes.add(traxCode);
        CourseCodeRecord myEdBCCode = new CourseCodeRecord();
        myEdBCCode.setCourseID("856787");
        myEdBCCode.setExternalCode("MPH--11");
        myEdBCCode.setOriginatingSystem("38"); // MyEdBC
        courseCodes.add(myEdBCCode);
        coursesRecord.setCourseCode(courseCodes);
        Set<CourseAllowableCreditRecord> courseAllowableCredits = new HashSet<>();
        CourseAllowableCreditRecord courseAllowableCreditRecord = new CourseAllowableCreditRecord();
        courseAllowableCreditRecord.setCourseID("856787");
        courseAllowableCreditRecord.setCreditValue("3");
        courseAllowableCreditRecord.setCacID("2145166");
        courseAllowableCreditRecord.setStartDate("1970-01-01 00:00:00");
        courseAllowableCreditRecord.setEndDate(null);
        courseAllowableCredits.add(courseAllowableCreditRecord);
        coursesRecord.setCourseAllowableCredit(courseAllowableCredits);
        CourseCharacteristicsRecord courseCategory = new CourseCharacteristicsRecord();
        courseCategory.setId("2932");
        courseCategory.setType("CC");
        courseCategory.setCode("BA");
        courseCategory.setDescription("");
        coursesRecord.setCourseCategory(courseCategory);
        coursesRecord.setGenericCourseType("G");
        coursesRecord.setProgramGuideTitle("INDEPENDENT DIRECTED STUDIES");
        when(restUtils.getCoursesByExternalID(any(), any())).thenReturn(coursesRecord);

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.RELATED_COURSE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.RELATED_COURSE_RELATED_LEVEL_MISSING_FOR_INDY.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.RELATED_COURSE_RELATED_LEVEL_MISSING_FOR_INDY.getMessage());
        assertThat(validationError2.getLast().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.RELATED_LEVEL.getCode());
        assertThat(validationError2.getLast().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.RELATED_COURSE_RELATED_LEVEL_MISSING_FOR_INDY.getCode());
        assertThat(validationError2.getLast().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.RELATED_COURSE_RELATED_LEVEL_MISSING_FOR_INDY.getMessage());
    }

    @Test
    void testC08CourseYear() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseYear("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_YEAR.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_YEAR_INVALID.getCode());
        assertThat(validationError2.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_YEAR_INVALID.getMessage());

        courseStudent.setCourseYear("");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_YEAR.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_YEAR_INVALID.getCode());
        assertThat(validationError3.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_YEAR_INVALID.getMessage());

        courseStudent.setCourseYear(null);
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_YEAR.getCode());
        assertThat(validationError4.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_YEAR_INVALID.getCode());
        assertThat(validationError4.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_YEAR_INVALID.getMessage());

        courseStudent.setCourseYear("12345");
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError5.size()).isNotZero();
        assertThat(validationError5.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_YEAR.getCode());
        assertThat(validationError5.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_YEAR_INVALID.getCode());
        assertThat(validationError5.getFirst().getValidationIssueDescription()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_YEAR_INVALID.getMessage());
    }

    @Test
    void testC12FinalLetterGrade() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        when(restUtils.getGradStudentCoursesByStudentID(any(), any())).thenReturn(
                List.of(
                        new GradStudentCourseRecord(
                                null, // id
                                "3201860", // courseID
                                "202306", // courseSession
                                100, // interimPercent
                                "", // interimLetterGrade
                                100, // finalPercent
                                "A", // finalLetterGrade
                                4, // credits
                                "", // equivOrChallenge
                                "", // fineArtsAppliedSkills
                                "", // customizedCourseName
                                null, // relatedCourseId
                                new GradStudentCourseExam( // courseExam
                                        null, null, null, null, null, null, null, null
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "CLE  12", // externalCode
                                        "38" // originatingSystem
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "MCLE 12", // externalCode
                                        "39" // originatingSystem
                                )
                        ),
                        new GradStudentCourseRecord(
                                null, // id
                                "3201862", // courseID
                                "2023/06", // courseSession
                                95, // interimPercent
                                "", // interimLetterGrade
                                95, // finalPercent
                                "A", // finalLetterGrade
                                4, // credits
                                "", // equivOrChallenge
                                "", // fineArtsAppliedSkills
                                "", // customizedCourseName
                                null, // relatedCourseId
                                new GradStudentCourseExam( // courseExam
                                        null, null, null, null, null, null, null, null
                                ),
                                new GradCourseCode(
                                        "3201861", // courseID
                                        "CLC  12", // externalCode
                                        "38" // originatingSystem
                                ),
                                new GradCourseCode(
                                        "3201861", // courseID
                                        "MCLC 12", // externalCode
                                        "39" // originatingSystem
                                )
                        )
                )
        );
        when(restUtils.getCoreg38CourseByID(any())).thenReturn(
                Optional.of(new GradCourseCode(
                        "3201860", // courseID
                        "MCLE 12", // externalCode
                        "38" // originatingSystem
                ))
        );
        when(restUtils.getCoreg39CourseByID(any())).thenReturn(
                Optional.of(new GradCourseCode(
                        "3201860", // courseID
                        "CLE  12", // externalCode
                        "39" // originatingSystem
                ))
        );
        GradStudentRecord gradStudentRecord = new GradStudentRecord();
        gradStudentRecord.setSchoolOfRecordId(UUID.randomUUID().toString());
        gradStudentRecord.setStudentStatusCode("CUR");
        gradStudentRecord.setGraduated("true");
        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(gradStudentRecord);

        courseStudent.setCourseLevel("12");
        courseStudent.setCourseCode("CLE");
        courseStudent.setCourseYear("2023");
        courseStudent.setCourseMonth("06");
        courseStudent.setFinalLetterGrade("W");
        courseStudent.setFinalPercentage("0");

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_LETTER_GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_USED_FOR_GRADUATION.getCode());
    }

    @Test
    void testC34CourseStatusSession() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        when(restUtils.getGradStudentCoursesByStudentID(any(), any())).thenReturn(
                List.of(
                        new GradStudentCourseRecord(
                                null, // id
                                "3201860", // courseID
                                "2021/06", // courseSession
                                100, // interimPercent
                                "", // interimLetterGrade
                                100, // finalPercent
                                "A", // finalLetterGrade
                                4, // credits
                                "", // equivOrChallenge
                                "", // fineArtsAppliedSkills
                                "", // customizedCourseName
                                null, // relatedCourseId
                                new GradStudentCourseExam( // courseExam
                                        null, null, null, null, null, null, null, null
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "CLE  12", // externalCode
                                        "38" // originatingSystem
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "MCLE 12", // externalCode
                                        "39" // originatingSystem
                                )
                        ),
                        new GradStudentCourseRecord(
                                null, // id
                                "3201862", // courseID
                                "2023/06", // courseSession
                                95, // interimPercent
                                "", // interimLetterGrade
                                95, // finalPercent
                                "A", // finalLetterGrade
                                4, // credits
                                "", // equivOrChallenge
                                "", // fineArtsAppliedSkills
                                "", // customizedCourseName
                                null, // relatedCourseId
                                new GradStudentCourseExam( // courseExam
                                        null, null, null, null, null, null, null, null
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "CLC  12", // externalCode
                                        "38" // originatingSystem
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "MCLC 12", // externalCode
                                        "39" // originatingSystem
                                )
                        )
                )
        );

        courseStudent.setCourseLevel("12");
        courseStudent.setCourseCode("CLE");
        courseStudent.setCourseYear("2022");
        courseStudent.setCourseMonth("06");
        courseStudent.setCourseStatus("W");

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_STATUS.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_WRONG_SESSION.getCode());
    }

    @Test
    void testC36FinalLetterSession() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError1.size()).isZero();

        when(restUtils.getGradStudentCoursesByStudentID(any(), any())).thenReturn(
                List.of(
                        new GradStudentCourseRecord(
                                null, // id
                                "3201860", // courseID
                                "2021/06", // courseSession
                                100, // interimPercent
                                "", // interimLetterGrade
                                100, // finalPercent
                                "A", // finalLetterGrade
                                4, // credits
                                "", // equivOrChallenge
                                "", // fineArtsAppliedSkills
                                "", // customizedCourseName
                                null, // relatedCourseId
                                new GradStudentCourseExam( // courseExam
                                        null, null, null, null, null, 99, null, null
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "CLE  12", // externalCode
                                        "38" // originatingSystem
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "MCLE 12", // externalCode
                                        "39" // originatingSystem
                                )
                        ),
                        new GradStudentCourseRecord(
                                null, // id
                                "3201862", // courseID
                                "2023/06", // courseSession
                                95, // interimPercent
                                "", // interimLetterGrade
                                95, // finalPercent
                                "A", // finalLetterGrade
                                4, // credits
                                "", // equivOrChallenge
                                "", // fineArtsAppliedSkills
                                "", // customizedCourseName
                                null, // relatedCourseId
                                new GradStudentCourseExam( // courseExam
                                        null, null, null, null, null, 99, null, null
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "CLC  12", // externalCode
                                        "38" // originatingSystem
                                ),
                                new GradCourseCode(
                                        "3201860", // courseID
                                        "MCLC 12", // externalCode
                                        "39" // originatingSystem
                                )
                        )
                )
        );

        when(restUtils.getCoreg38CourseByID(any())).thenReturn(
                Optional.of(new GradCourseCode(
                        "3201860", // courseID
                        "CLE  12", // externalCode
                        "38" // originatingSystem
                ))
        );
        when(restUtils.getCoreg39CourseByID(any())).thenReturn(
                Optional.of(new GradCourseCode(
                        "3201860", // courseID
                        "MCLC 12", // externalCode
                        "39" // originatingSystem
                ))
        );

        courseStudent.setCourseLevel("12");
        courseStudent.setCourseCode("CLE");
        courseStudent.setCourseYear("2021");
        courseStudent.setCourseMonth("06");
        courseStudent.setFinalLetterGrade("W");
        courseStudent.setFinalPercentage("0");

        when(restUtils.getExaminableCourseByExternalID(any())).thenReturn(List.of());
        when(restUtils.getGradStudentCoursesByStudentID(any(), any())).thenReturn(List.of());

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchoolTombstone()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_LETTER_GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_WRONG_SESSION.getCode());
    }
}

