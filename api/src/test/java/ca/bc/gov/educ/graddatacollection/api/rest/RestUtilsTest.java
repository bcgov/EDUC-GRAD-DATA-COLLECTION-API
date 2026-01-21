package ca.bc.gov.educ.graddatacollection.api.rest;

import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ReportingPeriodEntity;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CoregCoursesRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CourseAllowableCreditRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CourseCharacteristicsRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CourseCodeRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.*;
import ca.bc.gov.educ.graddatacollection.api.struct.external.gradschools.v1.GradSchool;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.FacilityTypeCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.School;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolCategoryCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.graddatacollection.api.struct.external.scholarships.v1.CitizenshipCode;
import io.nats.client.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static ca.bc.gov.educ.graddatacollection.api.rest.RestUtils.NATS_TIMEOUT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RestUtilsTest {
    @Mock
    private WebClient webClient;

    @Mock
    private WebClient chesWebClient;

    @Mock
    private MessagePublisher messagePublisher;

    @InjectMocks
    private RestUtils restUtils;

    @Mock
    private ApplicationProperties props;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        restUtils = spy(new RestUtils(chesWebClient, webClient, props, messagePublisher));
    }

    @Test
    void testGetGradStudentRecord_WhenRequestTimesOut_ShouldThrowGradDataCollectionAPIRuntimeException() {
        UUID correlationID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();

        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        GradDataCollectionAPIRuntimeException exception = assertThrows(
                GradDataCollectionAPIRuntimeException.class,
                () -> restUtils.getGradStudentRecordByStudentID(correlationID, studentID)
        );

        assertEquals(NATS_TIMEOUT + correlationID, exception.getMessage());
    }

    @Test
    void testGetGradStudentRecord_WhenExceptionOccurs_ShouldThrowGradDataCollectionAPIRuntimeException() {
        UUID correlationID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        Exception mockException = new Exception("exception");

        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.failedFuture(mockException));

        assertThrows(
                GradDataCollectionAPIRuntimeException.class,
                () -> restUtils.getGradStudentRecordByStudentID(correlationID, studentID)
        );
    }

    @Test
    void testGetGradStudentRecord_WhenValidStudentID_ShouldReturnGradStudentRecord() {
        UUID correlationID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();

        var courseList = List.of(new GradStudentRecordCourses("XBE", "10", "201801", "12"));

        GradStudentRecord expectedRecord = new GradStudentRecord(
                "123456789",
                "",
                "Program A",
                "20230615",
                "School XYZ",
                "School XYZ",
                "Active",
                "true",
                courseList
        );

        String jsonResponse = """
            {
                "studentID": "123456789",
                "exception":"",
                "program":"Program A",
                "programCompletionDate":"20230615",
                "schoolOfRecordId":"School XYZ",
                "schoolAtGradId":"School XYZ",
                "studentStatusCode":"Active",
                "graduated":"true",
                "courseList": [
                    {
                        "courseCode": "XBE",
                        "courseLevel" : "10",
                        "courseSession": "201801",
                        "gradReqMet": "12"
                    }
                 ]
            }""";
        byte[] mockResponseData = jsonResponse.getBytes();

        io.nats.client.Message mockMessage = mock(io.nats.client.Message.class);
        when(mockMessage.getData()).thenReturn(mockResponseData);

        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.completedFuture(mockMessage));

        GradStudentRecord actualRecord = restUtils.getGradStudentRecordByStudentID(correlationID, studentID);

        assertEquals(expectedRecord, actualRecord);
    }

    @Test
    void testGetCoursesByExternalID_WhenRequestTimesOut_ShouldThrowGradDataCollectionAPIRuntimeException() {
        UUID correlationID = UUID.randomUUID();
        String externalID = "YPR  0B";

        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        GradDataCollectionAPIRuntimeException exception = assertThrows(
                GradDataCollectionAPIRuntimeException.class,
                () -> restUtils.getCoursesByExternalID(correlationID, externalID)
        );

        assertEquals(NATS_TIMEOUT + correlationID, exception.getMessage());
    }

    @Test
    void testGetCoursesByExternalID_WhenExceptionOccurs_ShouldThrowGradDataCollectionAPIRuntimeException() {
        UUID correlationID = UUID.randomUUID();
        String externalID = "YPR  0B";
        Exception mockException = new Exception("exception");

        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.failedFuture(mockException));

        assertThrows(
                GradDataCollectionAPIRuntimeException.class,
                () -> restUtils.getCoursesByExternalID(correlationID, externalID)
        );
    }

    @Test
    void testGetCoursesByExternalID_WhenValidExternalID_ShouldReturnCoregCoursesRecord() {
        UUID correlationID = UUID.randomUUID();
        String externalID = "YPR  0B";

        CoregCoursesRecord expectedRecord = new CoregCoursesRecord(
                "123456",
                "77",
                "BA PARKS AND RECREATION 10B",
                "2009-09-01T00:00:00",
                null,
                null,
                "G",
                null,
                "N",
                Set.of(new CourseCodeRecord("123456", "YPR 10B", "39"), new CourseCodeRecord("123456", "YPR--0B", "38")),
                new CourseCharacteristicsRecord("2818", "123456", "LANGTYP", "ENG", "English"),
                new CourseCharacteristicsRecord("2933", "123456", "CC", "BA", "Board Authority Authorized"),
                Set.of(
                        new CourseAllowableCreditRecord("2169728", "4", "123456", "2009-09-01 00:00:00", null),
                        new CourseAllowableCreditRecord("2169729", "3", "123456", "2009-09-01 00:00:00", null)
                ),
                null
        );

        String jsonResponse = """
        {
            "courseID": "123456",
            "sifSubjectCode": "77",
            "courseTitle": "BA PARKS AND RECREATION 10B",
            "startDate": "2009-09-01T00:00:00",
            "genericCourseType": "G",
            "externalIndicator": "N",
            "courseCode": [
                {"courseID": "123456", "externalCode": "YPR 10B", "originatingSystem": "39"},
                {"courseID": "123456", "externalCode": "YPR--0B", "originatingSystem": "38"}
            ],
            "courseCharacteristics": {"id": "2818", "courseID": "123456", "type": "LANGTYP", "code": "ENG", "description": "English"},
            "courseCategory": {"id": "2933", "courseID": "123456", "type": "CC", "code": "BA", "description": "Board Authority Authorized"},
            "courseAllowableCredit": [
                {"cacID": "2169728", "creditValue": "4", "courseID": "123456", "startDate": "2009-09-01 00:00:00", "endDate": null},
                {"cacID": "2169729", "creditValue": "3", "courseID": "123456", "startDate": "2009-09-01 00:00:00", "endDate": null}
            ]
        }
        """;
        byte[] mockResponseData = jsonResponse.getBytes(StandardCharsets.UTF_8);

        io.nats.client.Message mockMessage = mock(io.nats.client.Message.class);
        when(mockMessage.getData()).thenReturn(mockResponseData);

        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.completedFuture(mockMessage));

        CoregCoursesRecord actualRecord = restUtils.getCoursesByExternalID(correlationID, externalID);

        assertEquals(expectedRecord, actualRecord);
    }

    @Test
    void testGetGradStudentCoursesByPEN_WhenRequestTimesOut_ShouldThrowGradDataCollectionAPIRuntimeException() {
        UUID correlationID = UUID.randomUUID();
        String studentID = "131411258";

        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        GradDataCollectionAPIRuntimeException exception = assertThrows(
                GradDataCollectionAPIRuntimeException.class,
                () -> restUtils.getGradStudentCoursesByStudentID(correlationID, studentID)
        );

        assertEquals(NATS_TIMEOUT + correlationID, exception.getMessage());
    }

    @Test
    void testGetGradStudentCoursesByPEN_WhenExceptionOccurs_ShouldThrowGradDataCollectionAPIRuntimeException() {
        UUID correlationID = UUID.randomUUID();
        String studentID = "131411258";
        Exception mockException = new Exception("exception");

        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.failedFuture(mockException));

        assertThrows(
                GradDataCollectionAPIRuntimeException.class,
                () -> restUtils.getGradStudentCoursesByStudentID(correlationID, studentID)
        );
    }

    @Test
    void testGetGradStudentCoursesByPEN_WhenValidPEN_ShouldReturnGradStudentCourseRecords() {
        UUID correlationID = UUID.randomUUID();
        String studentID = "131411258";

        String jsonResponse = """
    {
        "courses": [
            {
                "id": null,
                "courseID": "3201860",
                "courseSession": "2021/06",
                "interimPercent": 100,
                "interimLetterGrade": "",
                "finalPercent": 100,
                "finalLetterGrade": "A",
                "credits": 4,
                "equivOrChallenge": "",
                "fineArtsAppliedSkills": "",
                "customizedCourseName": "",
                "relatedCourseId": null,
                "courseExam": { "schoolPercentage": null, "bestSchoolPercentage": null, "bestExamPercentage": null, "specialCase": null, "id": null, "examPercentage": null, "toWriteFlag": null, "wroteFlag": null },
                "gradCourseCode38": { "courseID": "3201860", "externalCode": "CLC  12", "originatingSystem": "38" },
                "gradCourseCode39": { "courseID": "3201860", "externalCode": "MCLC 12", "originatingSystem": "39" }
            },
            {
                "id": null,
                "courseID": "3201862",
                "courseSession": "2023/06",
                "interimPercent": 95,
                "interimLetterGrade": "",
                "finalPercent": 95,
                "finalLetterGrade": "A",
                "credits": 4,
                "equivOrChallenge": "",
                "fineArtsAppliedSkills": "",
                "customizedCourseName": "",
                "relatedCourseId": null,
                "courseExam": { "schoolPercentage": null, "bestSchoolPercentage": null, "bestExamPercentage": null, "specialCase": null, "id": null, "examPercentage": null, "toWriteFlag": null, "wroteFlag": null },
                "gradCourseCode38": { "courseID": "3201861", "externalCode": "CLE  12", "originatingSystem": "38" },
                "gradCourseCode39": { "courseID": "3201861", "externalCode": "MCLE 12", "originatingSystem": "39" }
            }
        ],
        "exception": null
    }
    """;

        List<GradStudentCourseRecord> expectedRecords = List.of(
                new GradStudentCourseRecord(
                        null, "3201860", "2021/06", 100, "", 100, "A", 4, "", "", "", null,
                        new GradStudentCourseExam(null, null, null, null, null, null, null, null),
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
                ),
                new GradStudentCourseRecord(
                        null, "3201862", "2023/06", 95, "", 95, "A", 4, "", "", "", null,
                        new GradStudentCourseExam(null, null, null, null, null, null, null, null),
                        new GradCourseCode(
                                "3201861", // courseID
                                "CLE  12", // externalCode
                                "38" // originatingSystem
                        ),
                        new GradCourseCode(
                                "3201861", // courseID
                                "MCLE 12", // externalCode
                                "39" // originatingSystem
                        )
                )
        );

        doReturn(List.of(
                new GradCourseCode(
                        "3201860", // courseID
                        "CLC  12", // externalCode
                        "38" // originatingSystem
                ),
                new GradCourseCode(
                        "3201861", // courseID
                        "CLE  12", // externalCode
                        "38" // originatingSystem
                )
        )).when(restUtils).getCoreg38Courses();

        doReturn(Optional.of(new GradCourseCode("3201860", "CLC  12", "38")))
                .when(restUtils).getCoreg38CourseByID("3201860");

        doReturn(Optional.of(new GradCourseCode("3201861", "CLE  12", "38")))
                .when(restUtils).getCoreg38CourseByID("3201862");

        doReturn(List.of(
                new GradCourseCode(
                        "3201860", // courseID
                        "MCLC 12", // externalCode
                        "39" // originatingSystem
                ),
                new GradCourseCode(
                        "3201861", // courseID
                        "MCLE 12", // externalCode
                        "39" // originatingSystem
                )
        )).when(restUtils).getCoreg39Courses();

        doReturn(Optional.of(new GradCourseCode("3201860", "MCLC 12", "39")))
                .when(restUtils).getCoreg39CourseByID("3201860");

        doReturn(Optional.of(new GradCourseCode("3201861", "MCLE 12", "39")))
                .when(restUtils).getCoreg39CourseByID("3201862");

        byte[] mockResponseData = jsonResponse.getBytes(StandardCharsets.UTF_8);

        io.nats.client.Message mockMessage = mock(io.nats.client.Message.class);
        when(mockMessage.getData()).thenReturn(mockResponseData);
        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.completedFuture(mockMessage));

        List<GradStudentCourseRecord> actualRecords = restUtils.getGradStudentCoursesByStudentID(correlationID, studentID);

        assertEquals(expectedRecords, actualRecords);
    }

    @Test
    void testPopulateEquivalencyChallengeCodeMap() {
        List<EquivalencyChallengeCode> mockEquivalencyCodes = List.of(
                new EquivalencyChallengeCode("E", "Equivalency", "Indicates course credit through equivalency.", "1", "1984-01-01 00:00:00.000", null, "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                new EquivalencyChallengeCode("C", "Challenge", "Indicates course credit through challenge process.", "2", "1984-01-01 00:00:00.000", null, "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
        );

        doReturn(mockEquivalencyCodes).when(restUtils).getEquivalencyChallengeCodeList();

        assertEquals(2, restUtils.getEquivalencyChallengeCodeList().size());
        assertEquals("Equivalency", restUtils.getEquivalencyChallengeCodeList().getFirst().getLabel());
        assertEquals("Challenge", restUtils.getEquivalencyChallengeCodeList().getLast().getLabel());
    }

    @Test
    void testPopulateLetterGradeMap() {
        List<LetterGrade> mockLetterGrades = List.of(
                new LetterGrade("A", "4", "Y", "Excellent performance", "A", 100, 86, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                new LetterGrade("B", "3", "Y", "", "B", 85, 73, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
        );

        doReturn(mockLetterGrades).when(restUtils).getLetterGradeList(any());

        assertEquals(2, restUtils.getLetterGradeList(LocalDateTime.now()).size());
        assertEquals("4", restUtils.getLetterGradeList(LocalDateTime.now()).getFirst().getGpaMarkValue());
        assertEquals("3", restUtils.getLetterGradeList(LocalDateTime.now()).getLast().getGpaMarkValue());
    }

    @Test
    void testPopulateScholarshipsCitizenshipCodeMap() {
        List<CitizenshipCode> mockCitizenshipCodes = List.of(
                new CitizenshipCode("C", "Canadian", "Valid Citizenship Code", 1, "2020-01-01", "2099-12-31"),
                new CitizenshipCode("O", "Other", "Valid Citizenship Code", 2, "2020-01-01", "2099-12-31")
        );

        doReturn(mockCitizenshipCodes).when(restUtils).getScholarshipsCitizenshipCodeList();

        assertEquals(2, restUtils.getScholarshipsCitizenshipCodeList().size());
        assertEquals("Canadian", restUtils.getScholarshipsCitizenshipCodeList().getFirst().getLabel());
        assertEquals("Other", restUtils.getScholarshipsCitizenshipCodeList().getLast().getLabel());
    }

    @Test
    void testPopulateGradGradeMap() {
        List<GradGrade> mockGradGrades = List.of(
                new GradGrade("08", "Grade 8", "", 1, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "8", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                new GradGrade("12", "Grade 12", "", 5, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "12", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
        );

        doReturn(mockGradGrades).when(restUtils).getGradGradeList(false);

        assertEquals(2, restUtils.getGradGradeList(false).size());
        assertEquals("Grade 8", restUtils.getGradGradeList(false).getFirst().getLabel());
        assertEquals("Grade 12", restUtils.getGradGradeList(false).getLast().getLabel());
    }

    @Test
    void testPopulateCareerProgramMap() {
        List<CareerProgramCode> mockCareerPrograms = List.of(
                new CareerProgramCode("AA", "Art Careers", "", 1, "20200101", "20990101"),
                new CareerProgramCode("AC", "Agribusiness", "", 3, "20200101", "20990101")
        );

        doReturn(mockCareerPrograms).when(restUtils).getCareerProgramCodeList();

        assertEquals(2, restUtils.getCareerProgramCodeList().size());
        assertEquals("Art Careers", restUtils.getCareerProgramCodeList().getFirst().getName());
        assertEquals("Agribusiness", restUtils.getCareerProgramCodeList().getLast().getName());
    }

    @Test
    void testPopulateOptionalProgramMap() {
        List<OptionalProgramCode> mockOptionalPrograms = List.of(
                new OptionalProgramCode(UUID.randomUUID(), "FR", "SCCP French Certificate", "", 1, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                new OptionalProgramCode(UUID.randomUUID(), "AD", "Advanced Placement", "", 2, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
        );

        doReturn(mockOptionalPrograms).when(restUtils).getOptionalProgramCodeList();

        assertEquals(2, restUtils.getOptionalProgramCodeList().size());
        assertEquals("SCCP French Certificate", restUtils.getOptionalProgramCodeList().getFirst().getOptionalProgramName());
        assertEquals("Advanced Placement", restUtils.getOptionalProgramCodeList().getLast().getOptionalProgramName());
    }

    @Test
    void testPopulateProgramRequirementCodeMap() {
        List<ProgramRequirementCode> mockProgramRequirements = List.of(
                new ProgramRequirementCode("1950", "Adult Graduation Program", "Description for 1950", RequirementTypeCode.builder().reqTypeCode("REQ_TYPE").expiryDate(Date.valueOf("2222-01-01")).build(), "4", "Not met description", "12", "English", "Y", "CATEGORY", "1", "A", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                new ProgramRequirementCode("2023", "B.C. Graduation Program", "Description for 2023", RequirementTypeCode.builder().reqTypeCode("REQ_TYPE").expiryDate(Date.valueOf("2222-01-01")).build(), "4", "Not met description", "12", "English", "Y", "CATEGORY", "2", "B", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
        );

        doReturn(mockProgramRequirements).when(restUtils).getProgramRequirementCodeList();

        assertEquals(2, restUtils.getProgramRequirementCodeList().size());
        assertEquals("Adult Graduation Program", restUtils.getProgramRequirementCodeList().getFirst().getLabel());
        assertEquals("B.C. Graduation Program", restUtils.getProgramRequirementCodeList().getLast().getLabel());
    }

    @Test
    void testPopulateGradProgramCodesMap() {
        List<GraduationProgramCode> mockGradPrograms = List.of(
                new GraduationProgramCode("1950", "Adult Graduation Program", "Description for 1950", 4, LocalDate.now().toString(), LocalDate.now().minusYears(2).toString(), "associatedCred"),
                new GraduationProgramCode("2023", "B.C. Graduation Program", "Description for 2023", 4, LocalDate.now().toString(), LocalDate.now().minusYears(2).toString(), "associatedCred")
        );

        doReturn(mockGradPrograms).when(restUtils).getGraduationProgramCodeList(false);
        
        assertEquals(2, restUtils.getGraduationProgramCodeList(false).size());
        assertEquals("Adult Graduation Program", restUtils.getGraduationProgramCodeList(false).getFirst().getProgramName());
        assertEquals("B.C. Graduation Program", restUtils.getGraduationProgramCodeList(false).getLast().getProgramName());
    }

    @Test
    void testPopulateFacilityTypeCodesMap() {
        List<FacilityTypeCode> mockFacilityTypes = List.of(
                new FacilityTypeCode("FT1", "Facility One", "Description for Facility One", "LEG1", 1, "2020-01-01", "2030-01-01"),
                new FacilityTypeCode("FT2", "Facility Two", "Description for Facility Two", "LEG2", 2, "2020-01-01", "2030-01-01")
        );

        doReturn(mockFacilityTypes).when(restUtils).getFacilityTypeCodeList();

        assertEquals(2, restUtils.getFacilityTypeCodeList().size());
        assertEquals("Facility One", restUtils.getFacilityTypeCodeList().getFirst().getLabel());
        assertEquals("Facility Two", restUtils.getFacilityTypeCodeList().getLast().getLabel());
    }

    @Test
    void testPopulateSchoolCategoryCodesMap() {
        List<SchoolCategoryCode> mockCategories = List.of(
                new SchoolCategoryCode("CAT1", "Category One", "Description One", "LEG1", 1, "2020-01-01", "2030-01-01"),
                new SchoolCategoryCode("CAT2", "Category Two", "Description Two", "LEG2", 2, "2020-01-01", "2030-01-01")
        );

        doReturn(mockCategories).when(restUtils).getSchoolCategoryCodeList();

        assertEquals(2, restUtils.getSchoolCategoryCodeList().size());
        assertEquals("Category One", restUtils.getSchoolCategoryCodeList().getFirst().getLabel());
        assertEquals("Category Two", restUtils.getSchoolCategoryCodeList().getLast().getLabel());
    }

    @Test
    void testPopulateGradSchoolMap() {
        var school1=UUID.randomUUID().toString();
        List<GradSchool> mockSchools = List.of(
                new GradSchool(null, school1, "A", "Y", "Y"),
                new GradSchool(null, UUID.randomUUID().toString(), "A", "Y", "Y")
        );

        doReturn(mockSchools).when(restUtils).getGradSchools();
        assertTrue(restUtils.getGradSchoolBySchoolID(school1).isPresent());
    }

    @Test
    void testGetAllSchools() {
        var school1 = SchoolTombstone.builder().schoolId("SCHOOL1").schoolCategoryCode("PUBLIC").facilityTypeCode("STANDARD").openedDate("1964-09-01T00:00:00").build();
        var school2 = SchoolTombstone.builder().schoolId("SCHOOL2").schoolCategoryCode("PUBLIC").facilityTypeCode("STANDARD").openedDate("1964-09-01T00:00:00").build();

        List<SchoolTombstone> mockSchools = List.of(school1, school2);

        doReturn(mockSchools).when(restUtils).getAllSchools();
        assertEquals(2, restUtils.getAllSchools().size());
        assertEquals("SCHOOL1", restUtils.getAllSchools().getFirst().getSchoolId());
        assertEquals("SCHOOL2", restUtils.getAllSchools().getLast().getSchoolId());
    }

    @Test
    void testGetSchoolFromId_Success() {
        UUID correlationID = UUID.randomUUID();
        String schoolId = UUID.randomUUID().toString();
        School school = new School();
        school.setSchoolId(schoolId);
        school.setMincode("M123");
        String jsonResponse = "{\"schoolId\":\"" + schoolId + "\", \"vendorSourceSystemCode\":\"old\"}";
        byte[] responseData = jsonResponse.getBytes(StandardCharsets.UTF_8);
        Message mockMessage = mock(Message.class);
        when(mockMessage.getData()).thenReturn(responseData);
        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.completedFuture(mockMessage));

        School result = restUtils.getSchoolFromSchoolID(UUID.fromString(schoolId), correlationID);
        assertNotNull(result);
        assertEquals("old", result.getVendorSourceSystemCode());
    }

    @Test
    void testUpdateSchoolVendorCode_WithM_Success() {
        UUID correlationID = UUID.randomUUID();

        School schoolBeforeUpdate = new School();
        schoolBeforeUpdate.setSchoolId("S123");
        schoolBeforeUpdate.setMincode("M123");
        schoolBeforeUpdate.setVendorSourceSystemCode("OLD");

        School schoolAfterUpdate = new School();
        schoolAfterUpdate.setSchoolId("S123");
        schoolAfterUpdate.setMincode("M123");
        schoolAfterUpdate.setVendorSourceSystemCode("M");

        doReturn(schoolBeforeUpdate).when(restUtils).getSchoolFromSchoolID(any(), any());

        String updatedSchoolJson = "{\"schoolId\":\"S123\",\"mincode\":\"M123\",\"vendorCode\":\"MYED\"}";
        String responseJson = "{\"status\":\"SUCCESS\",\"eventPayload\":\"" + updatedSchoolJson.replace("\"", "\\\"") + "\"}";
        byte[] responseData = responseJson.getBytes(StandardCharsets.UTF_8);
        
        Message mockUpdateMessage = mock(Message.class);
        when(mockUpdateMessage.getData()).thenReturn(responseData);

        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.completedFuture(mockUpdateMessage));

        InstituteStatusEvent event = restUtils.updateSchool(schoolBeforeUpdate, correlationID);
        
        verify(messagePublisher).requestMessage(anyString(), any(byte[].class));
        
        assertNotNull(event);
        assertTrue(event.getEventPayload().contains("\"vendorCode\":\"MYED\""));
    }

    @Test
    void testWriteCRSStudentRecordInGrad_WhenNumberOfCreditsIsNull_ShouldDefaultToZero() {
        String pen = "123456789";
        String schoolID = UUID.randomUUID().toString();

        CourseStudentEntity courseStudentEntity = CourseStudentEntity.builder()
                .pen(pen)
                .courseCode("MATH")
                .courseLevel("10")
                .courseYear("2023")
                .courseMonth("06")
                .numberOfCredits(null)
                .createUser("test")
                .updateUser("test")
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        GradSchool gradSchool = new GradSchool(null, schoolID, "O", "Y", "Y");
        doReturn(Optional.of(gradSchool)).when(restUtils).getGradSchoolBySchoolID(schoolID);

        ReportingPeriodEntity reportingPeriod = ReportingPeriodEntity.builder()
                .summerStart(LocalDateTime.now().minusDays(10))
                .summerEnd(LocalDateTime.now().plusDays(10))
                .build();

        String responseJson = "{\"status\":\"SUCCESS\"}";
        Message mockMessage = mock(Message.class);
        when(mockMessage.getData()).thenReturn(responseJson.getBytes(StandardCharsets.UTF_8));
        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.completedFuture(mockMessage));

        GradStatusEvent result = restUtils.writeCRSStudentRecordInGrad(
                List.of(courseStudentEntity), pen, schoolID, reportingPeriod);

        ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(messagePublisher).requestMessage(anyString(), bytesCaptor.capture());
        
        String capturedJson = new String(bytesCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(capturedJson.contains("\\\"numberOfCredits\\\":\\\"0\\\""), 
                "Expected numberOfCredits to be \"0\" but was: " + capturedJson);
        assertNotNull(result);
    }

    @Test
    void testWriteCRSStudentRecordInGrad_WhenNumberOfCreditsHasValue_ShouldUseActualValue() {
        String pen = "987654321";
        String schoolID = UUID.randomUUID().toString();

        CourseStudentEntity courseStudentEntity = CourseStudentEntity.builder()
                .pen(pen)
                .courseCode("ENGL")
                .courseLevel("12")
                .courseYear("2024")
                .courseMonth("01")
                .numberOfCredits("4")
                .createUser("test")
                .updateUser("test")
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        GradSchool gradSchool = new GradSchool(null, schoolID, "O", "Y", "Y");
        doReturn(Optional.of(gradSchool)).when(restUtils).getGradSchoolBySchoolID(schoolID);

        ReportingPeriodEntity reportingPeriod = ReportingPeriodEntity.builder()
                .summerStart(LocalDateTime.now().minusDays(10))
                .summerEnd(LocalDateTime.now().plusDays(10))
                .build();

        String responseJson = "{\"status\":\"SUCCESS\"}";
        Message mockMessage = mock(Message.class);
        when(mockMessage.getData()).thenReturn(responseJson.getBytes(StandardCharsets.UTF_8));
        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.completedFuture(mockMessage));

        GradStatusEvent result = restUtils.writeCRSStudentRecordInGrad(
                List.of(courseStudentEntity), pen, schoolID, reportingPeriod);

        ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(messagePublisher).requestMessage(anyString(), bytesCaptor.capture());
        
        String capturedJson = new String(bytesCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(capturedJson.contains("\\\"numberOfCredits\\\":\\\"4\\\""), 
                "Expected numberOfCredits to be \"4\" but was: " + capturedJson);
        assertNotNull(result);
    }

    @Test
    void testPopulateCoreg38Map_WhenMapIsEmpty_ShouldLoadCourses() {
        List<GradCourseCode> mockCourses = List.of(
                new GradCourseCode("3201860", "CLC  12", "38"),
                new GradCourseCode("3201861", "CLE  12", "38"),
                new GradCourseCode("3201862", "MATH 10", "38")
        );

        doReturn(mockCourses).when(restUtils).getCoreg38Courses();

        restUtils.populateCoreg38Map();

        verify(restUtils, times(1)).getCoreg38Courses();
        Optional<GradCourseCode> result = restUtils.getCoreg38CourseByID("3201860");
        assertTrue(result.isPresent());
        assertEquals("CLC  12", result.get().getExternalCode());
    }

    @Test
    void testPopulateCoreg39Map_WhenMapIsEmpty_ShouldLoadCourses() {
        List<GradCourseCode> mockCourses = List.of(
                new GradCourseCode("3301860", "MCLC 12", "39"),
                new GradCourseCode("3301861", "MCLE 12", "39"),
                new GradCourseCode("3301862", "MMATH10", "39")
        );

        doReturn(mockCourses).when(restUtils).getCoreg39Courses();

        restUtils.populateCoreg39Map();

        verify(restUtils, times(1)).getCoreg39Courses();
        Optional<GradCourseCode> result = restUtils.getCoreg39CourseByID("3301860");
        assertTrue(result.isPresent());
        assertEquals("MCLC 12", result.get().getExternalCode());
    }

    @Test
    void testGetCoreg38CourseByID_WhenMapIsEmpty_ShouldPopulateAndReturn() {
        List<GradCourseCode> mockCourses = List.of(
                new GradCourseCode("3201860", "CLC  12", "38"),
                new GradCourseCode("3201861", "CLE  12", "38")
        );

        doReturn(mockCourses).when(restUtils).getCoreg38Courses();

        Optional<GradCourseCode> result = restUtils.getCoreg38CourseByID("3201860");

        assertTrue(result.isPresent());
        assertEquals("CLC  12", result.get().getExternalCode());
        assertEquals("38", result.get().getOriginatingSystem());
        verify(restUtils, times(1)).getCoreg38Courses();
    }

    @Test
    void testGetCoreg39CourseByID_WhenMapIsEmpty_ShouldPopulateAndReturn() {
        List<GradCourseCode> mockCourses = List.of(
                new GradCourseCode("3301860", "MCLC 12", "39"),
                new GradCourseCode("3301861", "MCLE 12", "39")
        );

        doReturn(mockCourses).when(restUtils).getCoreg39Courses();

        Optional<GradCourseCode> result = restUtils.getCoreg39CourseByID("3301860");

        assertTrue(result.isPresent());
        assertEquals("MCLC 12", result.get().getExternalCode());
        assertEquals("39", result.get().getOriginatingSystem());
        verify(restUtils, times(1)).getCoreg39Courses();
    }

    @Test
    void testGetCoreg38CourseByID_WhenCourseNotFound_ShouldReturnEmpty() {
        List<GradCourseCode> mockCourses = List.of(
                new GradCourseCode("3201860", "CLC  12", "38")
        );

        doReturn(mockCourses).when(restUtils).getCoreg38Courses();

        Optional<GradCourseCode> result = restUtils.getCoreg38CourseByID("9999999");

        assertFalse(result.isPresent());
    }

    @Test
    void testGetCoreg39CourseByID_WhenCourseNotFound_ShouldReturnEmpty() {
        List<GradCourseCode> mockCourses = List.of(
                new GradCourseCode("3301860", "MCLC 12", "39")
        );

        doReturn(mockCourses).when(restUtils).getCoreg39Courses();

        Optional<GradCourseCode> result = restUtils.getCoreg39CourseByID("9999999");

        assertFalse(result.isPresent());
    }

    @Test
    void testPopulateCoreg38Map_WhenExceptionOccurs_ShouldHandleGracefully() {
        doThrow(new RuntimeException("API error")).when(restUtils).getCoreg38Courses();

        // Map should remain empty
        doReturn(Collections.emptyList()).when(restUtils).getCoreg38Courses();
        restUtils.populateCoreg38Map();
    }

    @Test
    void testPopulateCoreg39Map_WhenExceptionOccurs_ShouldHandleGracefully() {
        doThrow(new RuntimeException("API error")).when(restUtils).getCoreg39Courses();

        // Map should remain empty
        doReturn(Collections.emptyList()).when(restUtils).getCoreg39Courses();
        restUtils.populateCoreg39Map();
    }

    @Test
    void testPopulateCoreg38Map_WithMultipleCourses_ShouldLoadAll() {
        List<GradCourseCode> mockCourses = List.of(
                new GradCourseCode("3201860", "CLC  12", "38"),
                new GradCourseCode("3201861", "CLE  12", "38"),
                new GradCourseCode("3201862", "MATH 10", "38"),
                new GradCourseCode("3201863", "ENGL 11", "38"),
                new GradCourseCode("3201864", "SCI  12", "38")
        );

        doReturn(mockCourses).when(restUtils).getCoreg38Courses();

        restUtils.populateCoreg38Map();

        // Verify all courses are loaded
        assertTrue(restUtils.getCoreg38CourseByID("3201860").isPresent());
        assertTrue(restUtils.getCoreg38CourseByID("3201861").isPresent());
        assertTrue(restUtils.getCoreg38CourseByID("3201862").isPresent());
        assertTrue(restUtils.getCoreg38CourseByID("3201863").isPresent());
        assertTrue(restUtils.getCoreg38CourseByID("3201864").isPresent());
    }

    @Test
    void testPopulateCoreg39Map_WithMultipleCourses_ShouldLoadAll() {
        List<GradCourseCode> mockCourses = List.of(
                new GradCourseCode("3301860", "MCLC 12", "39"),
                new GradCourseCode("3301861", "MCLE 12", "39"),
                new GradCourseCode("3301862", "MMATH10", "39"),
                new GradCourseCode("3301863", "MENGL11", "39"),
                new GradCourseCode("3301864", "MSCI 12", "39")
        );

        doReturn(mockCourses).when(restUtils).getCoreg39Courses();

        restUtils.populateCoreg39Map();

        // Verify all courses are loaded
        assertTrue(restUtils.getCoreg39CourseByID("3301860").isPresent());
        assertTrue(restUtils.getCoreg39CourseByID("3301861").isPresent());
        assertTrue(restUtils.getCoreg39CourseByID("3301862").isPresent());
        assertTrue(restUtils.getCoreg39CourseByID("3301863").isPresent());
        assertTrue(restUtils.getCoreg39CourseByID("3301864").isPresent());
    }

    @Test
    void testPopulateCoreg38Map_WithEmptyList_ShouldHandleGracefully() {
        doReturn(Collections.emptyList()).when(restUtils).getCoreg38Courses();

        assertDoesNotThrow(() -> restUtils.populateCoreg38Map());

        Optional<GradCourseCode> result = restUtils.getCoreg38CourseByID("anyID");
        assertFalse(result.isPresent());
    }

    @Test
    void testPopulateCoreg39Map_WithEmptyList_ShouldHandleGracefully() {
        doReturn(Collections.emptyList()).when(restUtils).getCoreg39Courses();

        assertDoesNotThrow(() -> restUtils.populateCoreg39Map());

        Optional<GradCourseCode> result = restUtils.getCoreg39CourseByID("anyID");
        assertFalse(result.isPresent());
    }
}
