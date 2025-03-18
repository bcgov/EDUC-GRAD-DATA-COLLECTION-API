package ca.bc.gov.educ.graddatacollection.api.service;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.AssessmentStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.service.v1.AssessmentStudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AssessmentStudentServiceTest {

    private AssessmentStudentRepository assessmentStudentRepository;
    private AssessmentStudentService assessmentStudentService;

    @BeforeEach
    void setUp() {
        assessmentStudentRepository = mock(AssessmentStudentRepository.class);
        assessmentStudentService = new AssessmentStudentService(
                null, // MessagePublisher
                null, // IncomingFilesetRepository
                null, // RestUtils
                assessmentStudentRepository,
                null, // AssessmentStudentRulesProcessor
                null  // ErrorFilesetStudentService
        );
    }

    @Test
    void getXamStudents_withSchoolId_shouldReturnList() {
        String pen = "123456789";
        UUID incomingFilesetId = UUID.randomUUID();
        UUID schoolId = UUID.randomUUID();

        List<AssessmentStudentEntity> expectedList = List.of(new AssessmentStudentEntity(), new AssessmentStudentEntity());

        when(assessmentStudentRepository.findByIncomingFilesetIDAndSchoolID(
                incomingFilesetId, pen, schoolId, FilesetStatus.COMPLETED.getCode()))
                .thenReturn(expectedList);

        List<AssessmentStudentEntity> result = assessmentStudentService.getXamStudents(pen, incomingFilesetId, schoolId);
        assertThat(result).isEqualTo(expectedList);
        verify(assessmentStudentRepository, times(1))
                .findByIncomingFilesetIDAndSchoolID(incomingFilesetId, pen, schoolId, FilesetStatus.COMPLETED.getCode());
    }

    @Test
    void getXamStudents_withoutSchoolOrDistrict_shouldThrowIllegalArgumentException() {
        String pen = "123456789";
        UUID incomingFilesetId = UUID.randomUUID();
        UUID schoolId = null;

        assertThatThrownBy(() ->
                assessmentStudentService.getXamStudents(pen, incomingFilesetId, schoolId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("schoolID must be provided.");
    }
}
