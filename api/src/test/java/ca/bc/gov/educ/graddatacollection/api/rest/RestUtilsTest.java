package ca.bc.gov.educ.graddatacollection.api.rest;

import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.*;
import ca.bc.gov.educ.graddatacollection.api.struct.external.scholarships.v1.CitizenshipCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;

import java.sql.Date;
import java.util.List;
import java.time.LocalDateTime;
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
    void testPopulateEquivalencyChallengeCodeMap() {
        List<EquivalencyChallengeCode> mockEquivalencyCodes = List.of(
                new EquivalencyChallengeCode("E", "Equivalency", "Indicates course credit through equivalency.", "1", "1984-01-01 00:00:00.000", null, "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                new EquivalencyChallengeCode("C", "Challenge", "Indicates course credit through challenge process.", "2", "1984-01-01 00:00:00.000", null, "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
        );

        doReturn(mockEquivalencyCodes).when(restUtils).getEquivalencyChallengeCodes();

        restUtils.populateEquivalencyChallengeCodeMap();

        assertEquals(2, restUtils.getEquivalencyChallengeCodes().size());
        assertEquals("Equivalency", restUtils.getEquivalencyChallengeCodes().getFirst().getLabel());
        assertEquals("Challenge", restUtils.getEquivalencyChallengeCodes().getLast().getLabel());
    }

    @Test
    void testPopulateLetterGradeMap() {
        List<LetterGrade> mockLetterGrades = List.of(
                new LetterGrade("A", "4", "Y", "Excellent performance", "A", 100, 86, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                new LetterGrade("B", "3", "Y", "", "B", 85, 73, null, "1940-01-01T08:00:00.000+00:00", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
        );

        doReturn(mockLetterGrades).when(restUtils).getLetterGrades();

        restUtils.populateLetterGradeMap();

        assertEquals(2, restUtils.getLetterGrades().size());
        assertEquals("4", restUtils.getLetterGrades().getFirst().getGpaMarkValue());
        assertEquals("3", restUtils.getLetterGrades().getLast().getGpaMarkValue());
    }

    @Test
    void testPopulateScholarshipsCitizenshipCodeMap() {
        List<CitizenshipCode> mockCitizenshipCodes = List.of(
                new CitizenshipCode("C", "Canadian", "Valid Citizenship Code", 1, "2020-01-01", "2099-12-31"),
                new CitizenshipCode("O", "Other", "Valid Citizenship Code", 2, "2020-01-01", "2099-12-31")
        );

        doReturn(mockCitizenshipCodes).when(restUtils).getScholarshipsCitizenshipCodes();

        restUtils.populateCitizenshipCodesMap();

        assertEquals(2, restUtils.getScholarshipsCitizenshipCodes().size());
        assertEquals("Canadian", restUtils.getScholarshipsCitizenshipCodes().getFirst().getLabel());
        assertEquals("Other", restUtils.getScholarshipsCitizenshipCodes().getLast().getLabel());
    }

    @Test
    void testPopulateGradGradeMap() {
        List<GradGrade> mockGradGrades = List.of(
                new GradGrade("08", "Grade 8", "", 1, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "8", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                new GradGrade("12", "Grade 12", "", 5, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "12", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
        );

        doReturn(mockGradGrades).when(restUtils).getGradGrades();

        restUtils.populateGradGradesMap();

        assertEquals(2, restUtils.getGradGrades().size());
        assertEquals("Grade 8", restUtils.getGradGrades().getFirst().getLabel());
        assertEquals("Grade 12", restUtils.getGradGrades().getLast().getLabel());
    }

    @Test
    void testPopulateCareerProgramMap() {
        List<CareerProgramCode> mockCareerPrograms = List.of(
                new CareerProgramCode("AA", "Art Careers", "", 1, "20200101", "20990101"),
                new CareerProgramCode("AC", "Agribusiness", "", 3, "20200101", "20990101")
        );

        doReturn(mockCareerPrograms).when(restUtils).getCareerPrograms();

        restUtils.populateCareerProgramsMap();

        assertEquals(2, restUtils.getCareerPrograms().size());
        assertEquals("Art Careers", restUtils.getCareerPrograms().getFirst().getName());
        assertEquals("Agribusiness", restUtils.getCareerPrograms().getLast().getName());
    }

    @Test
    void testPopulateOptionalProgramMap() {
        List<OptionalProgramCode> mockOptionalPrograms = List.of(
                new OptionalProgramCode(UUID.randomUUID(), "FR", "SCCP French Certificate", "", 1, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                new OptionalProgramCode(UUID.randomUUID(), "AD", "Advanced Placement", "", 2, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
        );

        doReturn(mockOptionalPrograms).when(restUtils).getOptionalPrograms();

        restUtils.populateOptionalProgramsMap();

        assertEquals(2, restUtils.getOptionalPrograms().size());
        assertEquals("SCCP French Certificate", restUtils.getOptionalPrograms().getFirst().getOptionalProgramName());
        assertEquals("Advanced Placement", restUtils.getOptionalPrograms().getLast().getOptionalProgramName());
    }

    @Test
    void testPopulateProgramRequirementCodeMap() {
        List<ProgramRequirementCode> mockProgramRequirements = List.of(
                new ProgramRequirementCode("1950", "Adult Graduation Program", "Description for 1950", RequirementTypeCode.builder().reqTypeCode("REQ_TYPE").expiryDate(Date.valueOf("2222-01-01")).build(), "4", "Not met description", "12", "English", "Y", "CATEGORY", "1", "A", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                new ProgramRequirementCode("2023", "B.C. Graduation Program", "Description for 2023", RequirementTypeCode.builder().reqTypeCode("REQ_TYPE").expiryDate(Date.valueOf("2222-01-01")).build(), "4", "Not met description", "12", "English", "Y", "CATEGORY", "2", "B", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
        );

        doReturn(mockProgramRequirements).when(restUtils).getProgramRequirementCodes();

        restUtils.populateProgramRequirementCodesMap();

        assertEquals(2, restUtils.getProgramRequirementCodes().size());
        assertEquals("Adult Graduation Program", restUtils.getProgramRequirementCodes().getFirst().getLabel());
        assertEquals("B.C. Graduation Program", restUtils.getProgramRequirementCodes().getLast().getLabel());
    }
}
