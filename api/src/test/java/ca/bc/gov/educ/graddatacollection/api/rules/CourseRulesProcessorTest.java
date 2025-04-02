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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @Autowired
    private CourseStudentRepository courseStudentRepository;

    @Autowired
    private ReportingPeriodRepository reportingPeriodRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.demographicStudentRepository.deleteAll();
        this.incomingFilesetRepository.deleteAll();

        when(restUtils.getLetterGradeList(true)).thenReturn(
                List.of(
                        new LetterGrade("A", "4", "Y", "The student demonstrates excellent or outstanding performance in relation to expected learning outcomes for the course or subject and grade.", "A", 100, 86, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("B", "3", "Y", "", "B", 85, 73, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("C+", "2.5", "Y", "", "C+", 72, 67, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("F", "0", "N", "", "F", 49, 0, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("IE", "0", "N", "", "Insufficient Evidence", 0, 0, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("RM", "0", "Y", "", "Requirement Met", 0, 0, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new LetterGrade("W", "0", "N", "", "Withdraw", 0, 0, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
                )
        );
        when(restUtils.getEquivalencyChallengeCodeList()).thenReturn(
                List.of(
                        new EquivalencyChallengeCode("E", "Equivalency", "Indicates that the course credit was earned through an equivalency review.", "1", "1984-01-01 00:00:00.000", null, "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new EquivalencyChallengeCode("C", "Challenge", "Indicates that the course credit was earned through the challenge process.", "2", "1984-01-01 00:00:00.000", null, "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
                )
        );
        when(restUtils.getGradStudentCoursesByPEN(any(), any())).thenReturn(
                List.of(
                        new GradStudentCourseRecord(
                                "131411258", "CLE", "CAREER-LIFE EDUCATION", 4, "", "2021/06", "", null, 100.0, "A", 100.0, "", null, null, null, null, "", "", null, 4, null, "", null, "", "N", "", "", " ", null, null, "N", false, false, false,
                                new GradCourseRecord(
                                        "CLE", "", "CAREER-LIFE EDUCATION", "", "2018-06-30", "1858-11-16", " ", "", "3201860", 4
                                )
                        ),
                        new GradStudentCourseRecord(
                                "131411258", "CLC", "CAREER-LIFE CONNECTIONS", 4, "", "2023/06", "", null, 95.0, "A", 95.0, "", null, null, null, null, "", "", null, 4, null, "", null, "", "N", "", "", " ", null, null, "N", false, false, false,
                                new GradCourseRecord(
                                        "CLC", "", "CAREER-LIFE CONNECTIONS", "", "2018-06-30", "1858-11-16", " ", "", "3201862", 4
                                )
                        )
                )
        );
        CoregCoursesRecord coursesRecord = new CoregCoursesRecord();
        coursesRecord.setStartDate(LocalDateTime.of(1983, 2, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        coursesRecord.setCompletionEndDate(LocalDateTime.of(9999, 5, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
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
    void testV201StudentPENRule() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var incomingFileset2 = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet2 = incomingFilesetRepository.save(incomingFileset2);
        var courseStudent2 = createMockCourseStudent(savedFileSet2);
        courseStudent2.setTransactionID("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(incomingFileset2), courseStudent2, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode());
    }

    @Test
    void testV202ValidStudentInDEMRule() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        Student stud2 = new Student();
        stud2.setStudentID(UUID.randomUUID().toString());
        stud2.setDob("1990-01-01");
        stud2.setLegalLastName(demStudent.getLastName());
        stud2.setLegalFirstName("ABC");
        stud2.setPen(demStudent.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud2);
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(incomingFileset), courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FIRST_NAME.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode());
    }

    @Test
    void testV203CourseStatusRule() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseStatus("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_STATUS.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getCode());
    }

    @Test
    void testV204CourseStatusRule() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseStatus("W");
        courseStudent.setCourseCode("CLE");
        courseStudent.setCourseLevel("LEVEL");
        courseStudent.setCourseMonth("06");
        courseStudent.setCourseYear("2023");

        when(restUtils.getGradStudentCoursesByPEN(any(), any())).thenReturn(
            List.of(
                new GradStudentCourseRecord(
                    "131411258", "CLE", "CAREER-LIFE EDUCATION", 4, "LEVEL", "2023/06", "", null, 100.0, "A", 100.0, "", null, null, null, null, "", "", null, 4, null, "", null, "", "N", "", "", " ", null, null, "N", false, false, false,
                    new GradCourseRecord(
                        "CLE", "", "CAREER-LIFE EDUCATION", "", "2018-06-30", "1858-11-16", " ", "", "3201860", 4
                    )
                ),
                new GradStudentCourseRecord(
                    "131411258", "CLC", "CAREER-LIFE CONNECTIONS", 4, "", "2023/06", "", null, 95.0, "A", 95.0, "", null, null, null, null, "", "", null, 4, null, "", null, "", "N", "", "", " ", null, null, "N", false, false, false,
                    new GradCourseRecord(
                        "CLC", "", "CAREER-LIFE CONNECTIONS", "", "2018-06-30", "1858-11-16", " ", "", "3201862", 4
                    )
                )
            )
        );

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(incomingFileset), courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_STATUS.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_RECORD_EXISTS.getCode());
    }

    @Test
    void testV205CourseStatusRule() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseStatus("W");

        GradStudentRecord gradStudentRecord = new GradStudentRecord();
        gradStudentRecord.setSchoolOfRecordId("03636018");
        gradStudentRecord.setStudentStatusCode("CUR");
        gradStudentRecord.setProgramCompletionDate("2023-06-30 00:00:00.000");
        gradStudentRecord.setGraduated("true");
        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(gradStudentRecord);

        courseStudent.setCourseCode("CLE");

        when(restUtils.getGradStudentCoursesByPEN(any(), any())).thenReturn(
                List.of(
                        new GradStudentCourseRecord(
                                "131411258", "CLE", "CAREER-LIFE EDUCATION", 4, "LEVEL", "2023/06", "", "MET", 100.0, "A", 100.0, "", null, null, null, null, "", "", null, 4, null, "", null, "", "N", "", "", " ", null, null, "N", false, false, false,
                                new GradCourseRecord(
                                        "CLE", "", "CAREER-LIFE EDUCATION", "", "2018-06-30", "1858-11-16", " ", "", "3201860", 4
                                )
                        ),
                        new GradStudentCourseRecord(
                                "131411258", "CLC", "CAREER-LIFE CONNECTIONS", 4, "", "2023/06", "", null, 95.0, "A", 95.0, "", null, null, null, null, "", "", null, 4, null, "", null, "", "N", "", "", " ", null, null, "N", false, false, false,
                                new GradCourseRecord(
                                        "CLC", "", "CAREER-LIFE CONNECTIONS", "", "2018-06-30", "1858-11-16", " ", "", "3201862", 4
                                )
                        )
                )
        );

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(incomingFileset), courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_STATUS.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_USED_FOR_GRADUATION.getCode());
    }

    @Test
    void testV206CourseCodeDefaultPasses() {
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

        StudentRuleData ruleData = createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool());
        var issues = rulesProcessor.processRules(ruleData);
        assertThat(issues).isEmpty();
    }

    @Test
    void testV206CourseCodeWithOnlyMyEdBC() {
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

        CoregCoursesRecord myEdBCOnlyRecord = new CoregCoursesRecord();
        myEdBCOnlyRecord.setStartDate(LocalDateTime.of(1983, 2, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        myEdBCOnlyRecord.setCompletionEndDate(LocalDateTime.of(9999, 5, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

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

        StudentRuleData ruleData = createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool());
        var issues = rulesProcessor.processRules(ruleData);

        assertThat(issues).isNotEmpty();
        assertThat(issues.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_CODE.getCode());
        assertThat(issues.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_CODE_COREG_MYEDBC_INVALID.getCode());
    }

    @Test
    void testV206CourseCodeWithRecordButNoCodes() {
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
        emptyCodesRecord.setStartDate(LocalDateTime.of(1983, 2, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        emptyCodesRecord.setCompletionEndDate(LocalDateTime.of(9999, 5, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
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

        StudentRuleData ruleData = createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool());
        var issues = rulesProcessor.processRules(ruleData);

        assertThat(issues).isNotEmpty();
        assertThat(issues.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_CODE.getCode());
        assertThat(issues.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_CODE_COREG_TRAX_INVALID.getCode());
    }

    @Test
    void testV207CourseCodeRule() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseCode("QCLE");
        courseStudent.setCourseLevel("LEVEL");
        courseStudent.setCourseMonth("06");
        courseStudent.setCourseYear("2023");

        when(restUtils.getGradStudentCoursesByPEN(any(), any())).thenReturn(
                List.of(
                        new GradStudentCourseRecord(
                                "131411258", "CLE", "CAREER-LIFE EDUCATION", 4, "LEVEL", "2023/06", "", null, 100.0, "A", 100.0, "", null, null, null, null, "", "", null, 4, null, "", null, "", "N", "", "", " ", null, null, "N", false, false, false,
                                new GradCourseRecord(
                                        "CLE", "", "CAREER-LIFE EDUCATION", "", "2018-06-30", "1858-11-16", " ", "", "3201860", 4
                                )
                        ),
                        new GradStudentCourseRecord(
                                "131411258", "QCLC", "CAREER-LIFE CONNECTIONS", 4, "LEVEL", "2023/06", "", null, 95.0, "A", 95.0, "", null, null, null, null, "", "", null, 4, null, "", null, "", "N", "", "", " ", null, null, "N", false, false, false,
                                new GradCourseRecord(
                                        "QCLC", "", "CAREER-LIFE CONNECTIONS", "", "2018-06-30", "1858-11-16", " ", "", "3201862", 4
                                )
                        )
                )
        );

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(incomingFileset), courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_CODE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.Q_CODE_INVALID.getCode());

        courseStudent.setCourseCode("QCLC");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(incomingFileset), courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isZero();
    }


    @Test
    void testV208CourseSession() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudentRepository.save(courseStudent);
        courseStudent.setCourseStudentID(null);
        courseStudentRepository.save(courseStudent);

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.PEN.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_DUPLICATE.getCode());
    }

    @Test
    void testV209CourseMonth() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseMonth("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_MONTH.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getCode());

        courseStudent.setCourseMonth("");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_MONTH.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getCode());

        courseStudent.setCourseMonth(null);
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_MONTH.getCode());
        assertThat(validationError4.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getCode());
    }

    @Test
    void testV210CourseSession() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseYear("1983");
        courseStudent.setCourseMonth("01");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_MONTH.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_START_DATE_INVALID.getCode());
    }

    @Test
    void testV211CourseSession() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseYear("9999");
        courseStudent.setCourseMonth("07");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_MONTH.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_COMPLETION_END_DATE_INVALID.getCode());

        courseStudent.setCourseYear(LocalDate.now().minusYears(1).toString());
        courseStudent.setCourseMonth("10");
        CoregCoursesRecord coursesRecord = new CoregCoursesRecord();
        coursesRecord.setStartDate(LocalDateTime.of(1983, 2, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
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
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.stream().noneMatch(code -> code.getValidationIssueFieldCode().equalsIgnoreCase(CourseStudentValidationIssueTypeCode.COURSE_SESSION_COMPLETION_END_DATE_INVALID.getCode()))).isTrue();
    }

    @Test
    void testV212CourseSession() {
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
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        // Case 2: Course session too old
        courseStudent.setCourseYear("1983");
        courseStudent.setCourseMonth("12");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_MONTH.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode());

        // Case 3: Course session too far in the future
        courseStudent.setCourseYear(String.valueOf(nextSchoolYearEnd.getYear() + 1));
        courseStudent.setCourseMonth("02");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_MONTH.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode());

        // Case 4: Invalid course year and month - skips
        courseStudent.setCourseYear(null);
        courseStudent.setCourseMonth("01");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError4.stream().noneMatch(code -> code.getValidationIssueFieldCode().equalsIgnoreCase(CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()))).isTrue();

        // Case 5: Boundary case - earliest valid date
        courseStudent.setCourseYear("1984");
        courseStudent.setCourseMonth("01");
        courseStudent.setFinalPercentage("");
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError5.size()).isZero();

        // Case 6: Boundary case - last month of next school year
        courseStudent.setCourseYear(String.valueOf(nextSchoolYearEnd.getYear()));
        courseStudent.setCourseMonth("09");
        courseStudent.setFinalLetterGrade("");
        val validationError6 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError6.size()).isZero();

        // Case 7: Boundary case - just before the earliest valid date
        courseStudent.setCourseYear("1983");
        courseStudent.setCourseMonth("12");
        val validationError7 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError7.size()).isNotZero();
        assertThat(validationError7.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_MONTH.getCode());
        assertThat(validationError7.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode());

        // Case 8: Boundary case - just after the next school year ends
        courseStudent.setCourseYear(String.valueOf(nextSchoolYearEnd.getYear()));
        courseStudent.setCourseMonth("10");
        val validationError8 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError8.size()).isNotZero();
        assertThat(validationError8.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_MONTH.getCode());
        assertThat(validationError8.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode());
    }

    @Test
    void testV213CourseCode() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseCode("CLE");
        courseStudent.setCourseLevel("LEVEL");
        courseStudent.setCourseMonth("06");
        courseStudent.setCourseYear("2023");

        when(restUtils.getGradStudentCoursesByPEN(any(), any())).thenReturn(
                List.of(
                        new GradStudentCourseRecord(
                                "131411258", "CLE", "CAREER-LIFE EDUCATION", 4, "LEVEL", "2023/06", "", null, 100.0, "A", 100.0, "", null, null, null, Double.MAX_VALUE, "", "", null, 4, null, "", null, "", "N", "", "", " ", null, null, "N", false, false, false,
                                new GradCourseRecord(
                                        "CLE", "", "CAREER-LIFE EDUCATION", "", "2018-06-30", "1858-11-16", " ", "", "3201860", 4
                                )
                        ),
                        new GradStudentCourseRecord(
                                "131411258", "CLC", "CAREER-LIFE CONNECTIONS", 4, "", "2023/06", "", null, 95.0, "A", 95.0, "", null, null, null, null, "", "", null, 4, null, "", null, "", "N", "", "", " ", null, null, "N", false, false, false,
                                new GradCourseRecord(
                                        "CLC", "", "CAREER-LIFE CONNECTIONS", "", "2018-06-30", "1858-11-16", " ", "", "3201862", 4
                                )
                        )
                )
        );

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(createMockDemographicStudent(incomingFileset), courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_CODE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.EXAMINABLE_COURSES_DISCONTINUED.getCode());
    }

    @Test
    void testV214InterimPercent() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setInterimPercentage("-1");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.INTERIM_PERCENTAGE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.INTERIM_PCT_INVALID.getCode());

        courseStudent.setInterimPercentage("101");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.INTERIM_PERCENTAGE.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.INTERIM_PCT_INVALID.getCode());
    }

    @Test
    void testV215InterimLetterGrade() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setInterimGrade("ABCD");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.INTERIM_GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_INVALID.getCode());
    }

    @Test
    void testV216InterimGradePercent() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setInterimPercentage("100");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.INTERIM_PERCENTAGE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_PERCENTAGE_MISMATCH.getCode());
    }

    @Test
    void testV217FinalLetterGradeAndPercentNotBlank() {
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
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setFinalLetterGrade("A");
        courseStudent.setFinalPercentage("90");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_PERCENTAGE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_OR_PERCENT_NOT_BLANK.getCode());
    }

    @Test
    void testV218FinalPercent() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setFinalPercentage("-1");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_PERCENTAGE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_PCT_INVALID.getCode());

        courseStudent.setFinalPercentage("101");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_PERCENTAGE.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_PCT_INVALID.getCode());
    }

    @Test
    void testV219FinalPercent() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setFinalPercentage("94");
        courseStudent.setCourseYear("1990");
        courseStudent.setCourseMonth("02");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_PERCENTAGE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_PCT_NOT_BLANK.getCode());

        courseStudent.setCourseYear(null);
        courseStudent.setCourseMonth("01");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.stream().noneMatch(code -> code.getValidationIssueFieldCode().equalsIgnoreCase(CourseStudentValidationIssueTypeCode.FINAL_PCT_NOT_BLANK.getCode()))).isTrue();

        courseStudent.setFinalPercentage("94");
        courseStudent.setCourseYear("ABCD");
        courseStudent.setCourseMonth("12");
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError4.size()).isNotZero();
        Assertions.assertTrue(validationError4.stream().anyMatch(validationError -> validationError.getValidationIssueFieldCode().equalsIgnoreCase(ValidationFieldCode.FINAL_PERCENTAGE.getCode())));
        Assertions.assertTrue(validationError4.stream().anyMatch(validationError -> validationError.getValidationIssueCode().equalsIgnoreCase(CourseStudentValidationIssueTypeCode.FINAL_PCT_NOT_BLANK.getCode())));
    }

    @Test
    void testV220FinalLetterGrade() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setFinalLetterGrade("ABCD");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_LETTER_GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_INVALID.getCode());
    }

    @Test
    void testV221FinalLetterGradePercent() {
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


        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setFinalPercentage("22");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_PERCENTAGE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_PERCENTAGE_MISMATCH.getCode());
    }

    @Test
    void testV222FinalLetterGradeRM() {
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
        courseStudent.setFinalPercentage("0");
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseCode("ABC");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_LETTER_GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_RM.getCode());
    }

    @Test
    void testV223FinalLetterGradeNotRM() {
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
        courseStudent.setFinalPercentage("0");
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setFinalLetterGrade("A");
        courseStudent.setFinalPercentage("90");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_LETTER_GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_NOT_RM.getCode());
    }

    @Test
    void testV224FinalLetterGradeAndPercentNotBlank() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setFinalLetterGrade("");
        courseStudent.setFinalPercentage("");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_PERCENTAGE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_OR_PERCENT_BLANK.getCode());
    }

    @Test
    void testV225FinalLetterGradeIE() {
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
        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setFinalLetterGrade("IE");
        courseStudent.setFinalPercentage("0");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_LETTER_GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_IE.getCode());
    }

    @Test
    void testV226NumberOfCredits() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setNumberOfCredits("4");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.NUMBER_OF_CREDITS.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.NUMBER_OF_CREDITS_INVALID.getCode());
    }

    @Test
    void testV227EquivalencyChallengeCode() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseType("A");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_TYPE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.EQUIVALENCY_CHALLENGE_CODE_INVALID.getCode());
    }

    @Test
    void testV228CourseGraduationRequirement() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        var incomingFileset2 = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet2 = incomingFilesetRepository.save(incomingFileset2);
        var demStudent2 = createMockDemographicStudent(savedFileSet2);
        demStudent2.setGradRequirementYear(GradRequirementYearCodes.YEAR_1986.getCode());
        demographicStudentRepository.save(demStudent2);
        courseStudent.setPen(demStudent2.getPen());
        courseStudent.setLocalID(demStudent2.getLocalID());
        courseStudent.setLastName(demStudent2.getLastName());
        courseStudent.setIncomingFileset(demStudent2.getIncomingFileset());
        courseStudent.setCourseGraduationRequirement("NOTBLANK");

        Student stud2 = new Student();
        stud2.setStudentID(UUID.randomUUID().toString());
        stud2.setDob("1990-01-01");
        stud2.setLegalLastName(demStudent2.getLastName());
        stud2.setLegalFirstName(demStudent2.getFirstName());
        stud2.setPen(demStudent2.getPen());
        when(this.restUtils.getStudentByPEN(any(),any())).thenReturn(stud2);

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent2, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_GRADUATION_REQUIREMENT.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.GRADUATION_REQUIREMENT_INVALID.getCode());
    }

    @Test
    void testV229CourseGraduationRequirement() {
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var incomingFileset = createMockIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        var savedFileSet = incomingFilesetRepository.save(incomingFileset);
        var demStudent = createMockDemographicStudent(savedFileSet);
        demStudent.setGradRequirementYear("1996");
        demographicStudentRepository.save(demStudent);
        var courseStudent = createMockCourseStudent(savedFileSet);
        courseStudent.setPen(demStudent.getPen());
        courseStudent.setLocalID(demStudent.getLocalID());
        courseStudent.setLastName(demStudent.getLastName());
        courseStudent.setIncomingFileset(demStudent.getIncomingFileset());

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseGraduationRequirement("B");

        CoregCoursesRecord coursesRecord = new CoregCoursesRecord();
        coursesRecord.setStartDate(LocalDateTime.of(1983, 2, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        coursesRecord.setCompletionEndDate(LocalDateTime.of(9999, 5, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
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
        courseCategory.setType("ZZ");
        courseCategory.setCode("ZZ");
        courseCategory.setCode("ZZ");
        courseCategory.setDescription("");
        coursesRecord.setCourseCategory(courseCategory);
        when(restUtils.getCoursesByExternalID(any(), any())).thenReturn(coursesRecord);

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_GRADUATION_REQUIREMENT.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.GRAD_REQT_FINE_ARTS_APPLIED_SKILLS_1996_GRAD_PROG_INVALID.getCode());
    }

    @Test
    void testV230CourseGraduationRequirement() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseGraduationRequirement("B");

        CoregCoursesRecord coursesRecord = new CoregCoursesRecord();
        coursesRecord.setStartDate(LocalDateTime.of(1983, 2, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        coursesRecord.setCompletionEndDate(LocalDateTime.of(9999, 5, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
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
        courseCategory.setType("ZZ");
        courseCategory.setCode("ZZ");
        courseCategory.setDescription("");
        coursesRecord.setCourseCategory(courseCategory);
        when(restUtils.getCoursesByExternalID(any(), any())).thenReturn(coursesRecord);

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_GRADUATION_REQUIREMENT.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.GRAD_REQT_FINE_ARTS_APPLIED_SKILLS_2004_2018_2023_GRAD_PROG_INVALID.getCode());
    }

    @Test
    void testV231CourseGraduationRequirement(){
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseGraduationRequirement("Z");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_GRADUATION_REQUIREMENT.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.INVALID_FINE_ARTS_APPLIED_SKILLS_CODE.getCode());
    }

    @Test
    void testV232CourseGraduationRequirementNumberOfCredits() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
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
        coursesRecord.setStartDate(LocalDateTime.of(1983, 2, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        coursesRecord.setCompletionEndDate(LocalDateTime.of(9999, 5, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
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

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent2, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_GRADUATION_REQUIREMENT.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.GRADUATION_REQUIREMENT_NUMBER_CREDITS_INVALID.getCode());
    }

    @Test
    void testV233relatedCourseRelatedLevel() {
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
        coursesRecord.setStartDate(LocalDateTime.of(1983, 2, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        coursesRecord.setCompletionEndDate(LocalDateTime.of(9999, 5, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        CoregCoursesRecord coursesRecord2 = new CoregCoursesRecord();
        coursesRecord2.setStartDate(LocalDateTime.of(1983, 2, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        coursesRecord2.setCompletionEndDate(LocalDateTime.of(9999, 5, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
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

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.RELATED_COURSE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_NOT_INDEPENDENT_DIRECTED_STUDIES.getCode());
        assertThat(validationError2.getLast().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.RELATED_LEVEL.getCode());
        assertThat(validationError2.getLast().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_NOT_INDEPENDENT_DIRECTED_STUDIES.getCode());
    }

    @Test
    void testV234relatedCourseRelatedLevel() {
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
        courseStudent.setRelatedCourse("WRO");
        courseStudent.setRelatedLevel("NG");


        CoregCoursesRecord coursesRecord = new CoregCoursesRecord();
        coursesRecord.setStartDate(LocalDateTime.of(1983, 2, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        coursesRecord.setCompletionEndDate(LocalDateTime.of(9999, 5, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        CoregCoursesRecord coursesRecord2 = new CoregCoursesRecord();
        coursesRecord2.setStartDate(LocalDateTime.of(1983, 2, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        coursesRecord2.setCompletionEndDate(LocalDateTime.of(9999, 5, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
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
        coursesRecord.setCourseAllowableCredit(courseAllowableCredits2);
        CourseCharacteristicsRecord courseCategory2 = new CourseCharacteristicsRecord();
        courseCategory2.setId("2932");
        courseCategory2.setType("ZZ");
        courseCategory2.setCode("ZZ");
        courseCategory2.setDescription("");
        coursesRecord2.setCourseCategory(courseCategory2);
        coursesRecord2.setProgramGuideTitle("INDEPENDENT DIRECTED STUDIES");
        when(restUtils.getCoursesByExternalID(any(), any())).thenAnswer(invocation -> {
            String externalId = invocation.getArgument(1);
            System.out.println("Called with externalId: [" + externalId + "]");
            if ("IDS  12G".equals(externalId)) {
                return coursesRecord;
            } else if ("WRO  NG".equals(externalId)) {
                return null;
            }
            return null;
        });

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.RELATED_COURSE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.RELATED_COURSE_RELATED_LEVEL_INVALID.getCode());
        assertThat(validationError2.getLast().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.RELATED_LEVEL.getCode());
        assertThat(validationError2.getLast().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.RELATED_COURSE_RELATED_LEVEL_INVALID.getCode());
    }

    @Test
    void testV235relatedCourseRelatedLevel() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        CoregCoursesRecord coursesRecord = new CoregCoursesRecord();
        coursesRecord.setStartDate(LocalDateTime.of(1983, 2, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        coursesRecord.setCompletionEndDate(LocalDateTime.of(9999, 5, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
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

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.RELATED_COURSE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.RELATED_COURSE_RELATED_LEVEL_MISSING_FOR_INDY.getCode());
        assertThat(validationError2.getLast().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.RELATED_LEVEL.getCode());
        assertThat(validationError2.getLast().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.RELATED_COURSE_RELATED_LEVEL_MISSING_FOR_INDY.getCode());
    }

    @Test
    void testV236CourseDescription() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        CoregCoursesRecord coursesRecord = new CoregCoursesRecord();
        coursesRecord.setStartDate(LocalDateTime.of(1983, 2, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        coursesRecord.setCompletionEndDate(LocalDateTime.of(9999, 5, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
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

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_DESCRIPTION.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_DESCRIPTION_INVALID.getCode());
    }

    @Test
    void testV237CourseYear() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        courseStudent.setCourseYear("123");
        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_YEAR.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_YEAR_INVALID.getCode());

        courseStudent.setCourseYear("");
        val validationError3 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError3.size()).isNotZero();
        assertThat(validationError3.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_YEAR.getCode());
        assertThat(validationError3.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_YEAR_INVALID.getCode());

        courseStudent.setCourseYear(null);
        val validationError4 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError4.size()).isNotZero();
        assertThat(validationError4.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_YEAR.getCode());
        assertThat(validationError4.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_YEAR_INVALID.getCode());

        courseStudent.setCourseYear("12345");
        val validationError5 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError5.size()).isNotZero();
        assertThat(validationError5.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_YEAR.getCode());
        assertThat(validationError5.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_YEAR_INVALID.getCode());
    }

    @Test
    void testV238FinalLetterGrade() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        when(restUtils.getGradStudentCoursesByPEN(any(), any())).thenReturn(
                List.of(
                        new GradStudentCourseRecord(
                                "131411258", "CLE", "CAREER-LIFE EDUCATION", 4, "LEVEL", "2023/06", "", "15", 100.0, "A", 100.0, "", null, null, null, null, "", "", null, 4, null, "", null, "", "N", "", "", " ", null, null, "N", false, false, false,
                                new GradCourseRecord(
                                        "CLE", "", "CAREER-LIFE EDUCATION", "", "2018-06-30", "1858-11-16", " ", "", "3201860", 4
                                )
                        ),
                        new GradStudentCourseRecord(
                                "131411258", "CLC", "CAREER-LIFE CONNECTIONS", 4, "", "2023/06", "", null, 95.0, "A", 95.0, "", null, null, null, null, "", "", null, 4, null, "", null, "", "N", "", "", " ", null, null, "N", false, false, false,
                                new GradCourseRecord(
                                        "CLC", "", "CAREER-LIFE CONNECTIONS", "", "2018-06-30", "1858-11-16", " ", "", "3201862", 4
                                )
                        )
                )
        );
        GradStudentRecord gradStudentRecord = new GradStudentRecord();
        gradStudentRecord.setSchoolOfRecordId(UUID.randomUUID().toString());
        gradStudentRecord.setStudentStatusCode("CUR");
        gradStudentRecord.setGraduated("true");
        when(restUtils.getGradStudentRecordByStudentID(any(), any())).thenReturn(gradStudentRecord);

        courseStudent.setCourseLevel("LEVEL");
        courseStudent.setCourseCode("CLE");
        courseStudent.setCourseYear("2023");
        courseStudent.setCourseMonth("06");
        courseStudent.setFinalLetterGrade("W");
        courseStudent.setFinalPercentage("0");

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_LETTER_GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_USED_FOR_GRADUATION.getCode());
    }

    @Test
    void testV239CourseStatusSession() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        when(restUtils.getGradStudentCoursesByPEN(any(), any())).thenReturn(
                List.of(
                        new GradStudentCourseRecord(
                                "131411258", "CLE", "CAREER-LIFE EDUCATION", 4, "LEVEL", "2023/06", "", "15", 100.0, "A", 100.0, "", null, null, null, null, "", "", null, 4, null, "", null, "", "N", "", "", " ", null, null, "N", false, false, false,
                                new GradCourseRecord(
                                        "CLE", "", "CAREER-LIFE EDUCATION", "", "2018-06-30", "1858-11-16", " ", "", "3201860", 4
                                )
                        ),
                        new GradStudentCourseRecord(
                                "131411258", "CLC", "CAREER-LIFE CONNECTIONS", 4, "", "2023/06", "", null, 95.0, "A", 95.0, "", null, null, null, null, "", "", null, 4, null, "", null, "", "N", "", "", " ", null, null, "N", false, false, false,
                                new GradCourseRecord(
                                        "CLC", "", "CAREER-LIFE CONNECTIONS", "", "2018-06-30", "1858-11-16", " ", "", "3201862", 4
                                )
                        )
                )
        );

        courseStudent.setCourseLevel("LEVEL");
        courseStudent.setCourseCode("CLE");
        courseStudent.setCourseYear("2022");
        courseStudent.setCourseMonth("06");
        courseStudent.setCourseStatus("W");

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.COURSE_STATUS.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.COURSE_WRONG_SESSION.getCode());
    }

    @Test
    void testV240FinalLetterSession() {
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

        val validationError1 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError1.size()).isZero();

        when(restUtils.getGradStudentCoursesByPEN(any(), any())).thenReturn(
                List.of(
                        new GradStudentCourseRecord(
                                "131411258", "CLE", "CAREER-LIFE EDUCATION", 4, "LEVEL", "2023/06", "", "15", 100.0, "A", 100.0, "", null, null, null, null, "", "", null, 4, null, "", null, "", "N", "", "", " ", null, null, "N", false, false, false,
                                new GradCourseRecord(
                                        "CLE", "", "CAREER-LIFE EDUCATION", "", "2018-06-30", "1858-11-16", " ", "", "3201860", 4
                                )
                        ),
                        new GradStudentCourseRecord(
                                "131411258", "CLC", "CAREER-LIFE CONNECTIONS", 4, "", "2023/06", "", null, 95.0, "A", 95.0, "", null, null, null, null, "", "", null, 4, null, "", null, "", "N", "", "", " ", null, null, "N", false, false, false,
                                new GradCourseRecord(
                                        "CLC", "", "CAREER-LIFE CONNECTIONS", "", "2018-06-30", "1858-11-16", " ", "", "3201862", 4
                                )
                        )
                )
        );

        courseStudent.setCourseLevel("LEVEL");
        courseStudent.setCourseCode("CLE");
        courseStudent.setCourseYear("2022");
        courseStudent.setCourseMonth("06");
        courseStudent.setFinalLetterGrade("W");
        courseStudent.setFinalPercentage("0");

        val validationError2 = rulesProcessor.processRules(createMockStudentRuleData(demStudent, courseStudent, createMockAssessmentStudent(), createMockSchool()));
        assertThat(validationError2.size()).isNotZero();
        assertThat(validationError2.getFirst().getValidationIssueFieldCode()).isEqualTo(ValidationFieldCode.FINAL_LETTER_GRADE.getCode());
        assertThat(validationError2.getFirst().getValidationIssueCode()).isEqualTo(CourseStudentValidationIssueTypeCode.FINAL_LETTER_WRONG_SESSION.getCode());
    }
}
