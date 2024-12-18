package ca.bc.gov.educ.graddatacollection.api.rest;

import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.messaging.MessagePublisher;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.EquivalencyChallengeCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GradStudentRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;

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

        String jsonResponse = "{\"studentID\":\"123456789\", \"exception\":\"\", \"program\":\"Program A\",\"programCompletionDate\":\"20230615\",\"schoolOfRecord\":\"School XYZ\",\"studentStatusCode\":\"Active\", \"graduated\":\"true\"}";
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
}
