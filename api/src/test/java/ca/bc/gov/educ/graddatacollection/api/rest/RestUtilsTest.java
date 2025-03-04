package ca.bc.gov.educ.graddatacollection.api.rest;

import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CoregCoursesRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CourseAllowableCreditRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CourseCharacteristicsRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CourseCodeRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.*;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.FacilityTypeCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolCategoryCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.scholarships.v1.CitizenshipCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static ca.bc.gov.educ.graddatacollection.api.rest.RestUtils.NATS_TIMEOUT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

        GradStudentRecord expectedRecord = new GradStudentRecord(
                "123456789",
                "",
                "Program A",
                "20230615",
                "School XYZ",
                "Active",
                "true"
        );

        String jsonResponse = "{\"studentID\":\"123456789\", \"exception\":\"\", \"program\":\"Program A\",\"programCompletionDate\":\"20230615\",\"schoolOfRecordId\":\"School XYZ\",\"studentStatusCode\":\"Active\", \"graduated\":\"true\"}";
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
        String pen = "131411258";

        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        GradDataCollectionAPIRuntimeException exception = assertThrows(
                GradDataCollectionAPIRuntimeException.class,
                () -> restUtils.getGradStudentCoursesByPEN(correlationID, pen)
        );

        assertEquals(NATS_TIMEOUT + correlationID, exception.getMessage());
    }

    @Test
    void testGetGradStudentCoursesByPEN_WhenExceptionOccurs_ShouldThrowGradDataCollectionAPIRuntimeException() {
        UUID correlationID = UUID.randomUUID();
        String pen = "131411258";
        Exception mockException = new Exception("exception");

        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.failedFuture(mockException));

        assertThrows(
                GradDataCollectionAPIRuntimeException.class,
                () -> restUtils.getGradStudentCoursesByPEN(correlationID, pen)
        );
    }

    @Test
    void testGetGradStudentCoursesByPEN_WhenValidPEN_ShouldReturnGradStudentCourseRecords() {
        UUID correlationID = UUID.randomUUID();
        String pen = "131411258";

        String jsonResponse = """
        [
            {"pen":"131411258","courseCode":"CLE","courseName":"CAREER-LIFE EDUCATION","originalCredits":4,"courseLevel":"","sessionDate":"2021/06","customizedCourseName":"","gradReqMet":null,"completedCoursePercentage":100.0,"completedCourseLetterGrade":"A","interimPercent":100.0,"interimLetterGrade":"","bestSchoolPercent":null,"bestExamPercent":null,"schoolPercent":null,"examPercent":null,"equivOrChallenge":"","fineArtsAppliedSkills":"","metLitNumRequirement":null,"credits":4,"creditsUsedForGrad":null,"relatedCourse":"","relatedCourseName":null,"relatedLevel":"","hasRelatedCourse":"N","genericCourseType":"","language":"","workExpFlag":" ","specialCase":null,"toWriteFlag":null,"provExamCourse":"N","courseDetails":{"courseCode":"CLE","courseLevel":"","courseName":"CAREER-LIFE EDUCATION","language":"","startDate":"2018-06-30","endDate":"1858-11-16","workExpFlag":" ","genericCourseType":"","courseID":"3201860","numCredits":4},"failed":false,"duplicate":false,"notCompleted":false},
            {"pen":"131411258","courseCode":"CLC","courseName":"CAREER-LIFE CONNECTIONS","originalCredits":4,"courseLevel":"","sessionDate":"2023/06","customizedCourseName":"","gradReqMet":null,"completedCoursePercentage":95.0,"completedCourseLetterGrade":"A","interimPercent":95.0,"interimLetterGrade":"","bestSchoolPercent":null,"bestExamPercent":null,"schoolPercent":null,"examPercent":null,"equivOrChallenge":"","fineArtsAppliedSkills":"","metLitNumRequirement":null,"credits":4,"creditsUsedForGrad":null,"relatedCourse":"","relatedCourseName":null,"relatedLevel":"","hasRelatedCourse":"N","genericCourseType":"","language":"","workExpFlag":" ","specialCase":null,"toWriteFlag":null,"provExamCourse":"N","courseDetails":{"courseCode":"CLC","courseLevel":"","courseName":"CAREER-LIFE CONNECTIONS","language":"","startDate":"2018-06-30","endDate":"1858-11-16","workExpFlag":" ","genericCourseType":"","courseID":"3201862","numCredits":4},"failed":false,"duplicate":false,"notCompleted":false}
        ]
        """;

        List<GradStudentCourseRecord> expectedRecords = List.of(
            new GradStudentCourseRecord(
                "131411258",
                "CLE",
                "CAREER-LIFE EDUCATION",
                4,
                "",
                "2021/06",
                "",
                null,
                100.0,
                "A",
                100.0,
                "",
                null,
                null,
                null,
                null,
                "",
                "",
                null,
                4,
                null,
                "",
                null,
                "",
                "N",
                "",
                "",
                " ",
                null,
                null,
                "N",
                false,
                false,
                false,
                new GradCourseRecord(
                    "CLE",
                    "",
                    "CAREER-LIFE EDUCATION",
                    "",
                   "2018-06-30",
                   "1858-11-16",
                    " ",
                    "",
                    "3201860",
                    4
                )
            ),
            new GradStudentCourseRecord(
                "131411258",
                "CLC",
                "CAREER-LIFE CONNECTIONS",
                4,
                "",
                "2023/06",
                "",
                null,
                95.0,
                "A",
                95.0,
                "",
                null,
                null,
                null,
                null,
                "",
                "",
                null,
                4,
                null,
                "",
                null,
                "",
                "N",
                "",
                "",
                " ",
                null,
                null,
                "N",
                false,
                false,
                false,
                new GradCourseRecord(
                    "CLC",
                    "",
                    "CAREER-LIFE CONNECTIONS",
                    "",
                    "2018-06-30",
                    "1858-11-16",
                    " ",
                    "",
                    "3201862",
                    4
                )
            )
        );

        byte[] mockResponseData = jsonResponse.getBytes(StandardCharsets.UTF_8);

        io.nats.client.Message mockMessage = mock(io.nats.client.Message.class);
        when(mockMessage.getData()).thenReturn(mockResponseData);
        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.completedFuture(mockMessage));

        List<GradStudentCourseRecord> actualRecords = restUtils.getGradStudentCoursesByPEN(correlationID, pen);

        assertEquals(expectedRecords, actualRecords);
    }


    @Test
    void testPopulateEquivalencyChallengeCodeMap() {
        List<EquivalencyChallengeCode> mockEquivalencyCodes = List.of(
                new EquivalencyChallengeCode("E", "Equivalency", "Indicates course credit through equivalency.", "1", "1984-01-01 00:00:00.000", null, "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                new EquivalencyChallengeCode("C", "Challenge", "Indicates course credit through challenge process.", "2", "1984-01-01 00:00:00.000", null, "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
        );

        doReturn(mockEquivalencyCodes).when(restUtils).getEquivalencyChallengeCodeList();

        restUtils.populateEquivalencyChallengeCodeMap();

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

        doReturn(mockLetterGrades).when(restUtils).getLetterGradeList();

        restUtils.populateLetterGradeMap();

        assertEquals(2, restUtils.getLetterGradeList().size());
        assertEquals("4", restUtils.getLetterGradeList().getFirst().getGpaMarkValue());
        assertEquals("3", restUtils.getLetterGradeList().getLast().getGpaMarkValue());
    }

    @Test
    void testPopulateScholarshipsCitizenshipCodeMap() {
        List<CitizenshipCode> mockCitizenshipCodes = List.of(
                new CitizenshipCode("C", "Canadian", "Valid Citizenship Code", 1, "2020-01-01", "2099-12-31"),
                new CitizenshipCode("O", "Other", "Valid Citizenship Code", 2, "2020-01-01", "2099-12-31")
        );

        doReturn(mockCitizenshipCodes).when(restUtils).getScholarshipsCitizenshipCodeList();

        restUtils.populateCitizenshipCodesMap();

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

        doReturn(mockGradGrades).when(restUtils).getGradGradeList();

        restUtils.populateGradGradesMap();

        assertEquals(2, restUtils.getGradGradeList().size());
        assertEquals("Grade 8", restUtils.getGradGradeList().getFirst().getLabel());
        assertEquals("Grade 12", restUtils.getGradGradeList().getLast().getLabel());
    }

    @Test
    void testPopulateCareerProgramMap() {
        List<CareerProgramCode> mockCareerPrograms = List.of(
                new CareerProgramCode("AA", "Art Careers", "", 1, "20200101", "20990101"),
                new CareerProgramCode("AC", "Agribusiness", "", 3, "20200101", "20990101")
        );

        doReturn(mockCareerPrograms).when(restUtils).getCareerProgramCodeList();

        restUtils.populateCareerProgramsMap();

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

        restUtils.populateOptionalProgramsMap();

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

        restUtils.populateProgramRequirementCodesMap();

        assertEquals(2, restUtils.getProgramRequirementCodeList().size());
        assertEquals("Adult Graduation Program", restUtils.getProgramRequirementCodeList().getFirst().getLabel());
        assertEquals("B.C. Graduation Program", restUtils.getProgramRequirementCodeList().getLast().getLabel());
    }

    @Test
    void testPopulateGradProgramCodesMap() {
        List<GraduationProgramCode> mockGradPrograms = List.of(
                new GraduationProgramCode("1950", "Adult Graduation Program", "Description for 1950", 4, Date.valueOf(LocalDate.now()), Date.valueOf("2222-01-01"), "associatedCred"),
                new GraduationProgramCode("2023", "B.C. Graduation Program", "Description for 2023", 4, Date.valueOf(LocalDate.now()), Date.valueOf("2222-01-01"), "associatedCred")
        );

        doReturn(mockGradPrograms).when(restUtils).getGraduationProgramCodeList();

        restUtils.populateGradProgramCodesMap();

        assertEquals(2, restUtils.getGraduationProgramCodeList().size());
        assertEquals("Adult Graduation Program", restUtils.getGraduationProgramCodeList().getFirst().getProgramName());
        assertEquals("B.C. Graduation Program", restUtils.getGraduationProgramCodeList().getLast().getProgramName());
    }

    @Test
    void testPopulateFacilityTypeCodesMap() {
        List<FacilityTypeCode> mockFacilityTypes = List.of(
                new FacilityTypeCode("FT1", "Facility One", "Description for Facility One", "LEG1", 1, "2020-01-01", "2030-01-01"),
                new FacilityTypeCode("FT2", "Facility Two", "Description for Facility Two", "LEG2", 2, "2020-01-01", "2030-01-01")
        );

        doReturn(mockFacilityTypes).when(restUtils).getFacilityTypeCodeList();

        restUtils.populateFacilityTypeCodesMap();

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
}
