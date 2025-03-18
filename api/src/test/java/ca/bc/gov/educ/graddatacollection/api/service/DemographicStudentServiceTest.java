package ca.bc.gov.educ.graddatacollection.api.service;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.service.v1.DemographicStudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class DemographicStudentServiceTest {

    private DemographicStudentRepository demographicStudentRepository;
    private DemographicStudentService demographicStudentService;

    @BeforeEach
    void setUp() {
        demographicStudentRepository = mock(DemographicStudentRepository.class);
        demographicStudentService = new DemographicStudentService(
                null, // MessagePublisher
                null, // IncomingFilesetRepository
                null, // RestUtils
                demographicStudentRepository,
                null, // DemographicStudentRulesProcessor
                null  // ErrorFilesetStudentService
        );
    }

    @Test
    void getDemStudent_withIncomingFilesetIdAndSchoolId_shouldReturnEntity() {
        String pen = "123456789";
        UUID incomingFilesetId = UUID.randomUUID();
        UUID schoolId = UUID.randomUUID();

        DemographicStudentEntity expected = new DemographicStudentEntity();
        expected.setPen(pen);

        when(demographicStudentRepository.findByIncomingFilesetIDAndSchoolID(
                incomingFilesetId, pen, schoolId, FilesetStatus.COMPLETED.getCode()))
                .thenReturn(Optional.of(expected));

        DemographicStudentEntity result = demographicStudentService.getDemStudent(pen, incomingFilesetId, schoolId);
        assertThat(result).isEqualTo(expected);
        verify(demographicStudentRepository, times(1))
                .findByIncomingFilesetIDAndSchoolID(incomingFilesetId, pen, schoolId, FilesetStatus.COMPLETED.getCode());
    }

    @Test
    void getDemStudent_withoutIncomingFilesetId_butWithSchoolId_shouldReturnEntity() {
        String pen = "123456789";
        UUID incomingFilesetId = null;
        UUID schoolId = UUID.randomUUID();

        DemographicStudentEntity expected = new DemographicStudentEntity();
        expected.setPen(pen);

        when(demographicStudentRepository.findFirstBySchoolIDAndPen(
                schoolId, FilesetStatus.COMPLETED.getCode(), pen))
                .thenReturn(Optional.of(expected));

        DemographicStudentEntity result = demographicStudentService.getDemStudent(pen, incomingFilesetId, schoolId);
        assertThat(result).isEqualTo(expected);
        verify(demographicStudentRepository, times(1))
                .findFirstBySchoolIDAndPen(schoolId, FilesetStatus.COMPLETED.getCode(), pen);
    }

    @Test
    void getDemStudent_withoutSchoolOrDistrict_shouldThrowIllegalArgumentException() {
        String pen = "123456789";
        UUID incomingFilesetId = null;
        UUID schoolId = null;

        assertThatThrownBy(() ->
                demographicStudentService.getDemStudent(pen, incomingFilesetId, schoolId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("schoolID must be provided.");
    }

    @Test
    void getDemStudent_entityNotFound_shouldThrowEntityNotFoundException() {
        String pen = "123456789";
        UUID incomingFilesetId = UUID.randomUUID();
        UUID schoolId = UUID.randomUUID();

        when(demographicStudentRepository.findByIncomingFilesetIDAndSchoolID(
                incomingFilesetId, pen, schoolId, FilesetStatus.COMPLETED.getCode()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                demographicStudentService.getDemStudent(pen, incomingFilesetId, schoolId))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
