package ca.bc.gov.educ.graddatacollection.api.service;

import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ErrorFilesetStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.service.v1.ErrorFilesetStudentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ErrorFilesetStudentServiceTest {

    @Mock
    private ErrorFilesetStudentRepository errorFilesetStudentRepository;

    @InjectMocks
    private ErrorFilesetStudentService errorFilesetStudentService;

    @Test
    void flagErrorOnStudent_withDemStudent_shouldCallInsertIgnoreConflictWithCorrectArgs() {
        var incomingFilesetID = UUID.randomUUID();
        var pen = "123456789";
        var createDate = LocalDateTime.now();
        var updateDate = LocalDateTime.now();

        DemographicStudentEntity demStudent = new DemographicStudentEntity();
        demStudent.setLocalID("LOCAL1");
        demStudent.setLastName("SMITH");
        demStudent.setFirstName("JANE");
        demStudent.setBirthdate("20000101");

        errorFilesetStudentService.flagErrorOnStudent(incomingFilesetID, pen, demStudent, "TEST_USER", createDate, "TEST_USER", updateDate);

        verify(errorFilesetStudentRepository).insertIgnoreConflict(
                any(UUID.class),
                eq(incomingFilesetID),
                eq(pen),
                eq("LOCAL1"),
                eq("SMITH"),
                eq("JANE"),
                eq("20000101"),
                eq("TEST_USER"),
                eq(createDate),
                eq("TEST_USER"),
                eq(updateDate)
        );
    }

    @Test
    void flagErrorOnStudent_withNullDemStudent_shouldCallInsertIgnoreConflictWithNullDemographicFields() {
        var incomingFilesetID = UUID.randomUUID();
        var pen = "987654321";
        var createDate = LocalDateTime.now();
        var updateDate = LocalDateTime.now();

        errorFilesetStudentService.flagErrorOnStudent(incomingFilesetID, pen, null, "TEST_USER", createDate, "TEST_USER", updateDate);

        var idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(errorFilesetStudentRepository).insertIgnoreConflict(
                idCaptor.capture(),
                eq(incomingFilesetID),
                eq(pen),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq("TEST_USER"),
                eq(createDate),
                eq("TEST_USER"),
                eq(updateDate)
        );
        // Each call generates a fresh UUID
        assertThat(idCaptor.getValue()).isNotNull();
    }
}

