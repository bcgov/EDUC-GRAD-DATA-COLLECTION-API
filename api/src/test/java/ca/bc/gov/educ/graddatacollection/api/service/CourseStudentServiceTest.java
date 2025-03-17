package ca.bc.gov.educ.graddatacollection.api.service;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.CourseStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseStudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CourseStudentServiceTest {

    private CourseStudentRepository courseStudentRepository;
    private CourseStudentService courseStudentService;

    @BeforeEach
    void setUp() {
        courseStudentRepository = mock(CourseStudentRepository.class);
        courseStudentService = new CourseStudentService(
                null, // MessagePublisher
                null, // IncomingFilesetRepository
                courseStudentRepository,
                null, // RestUtils
                null, // CourseStudentRulesProcessor
                null  // ErrorFilesetStudentService
        );
    }

    @Test
    void getCrsStudents_withSchoolId_shouldReturnList() {
        String pen = "123456789";
        UUID incomingFilesetId = UUID.randomUUID();
        UUID schoolId = UUID.randomUUID();
        UUID districtId = null;

        List<CourseStudentEntity> expectedList = List.of(new CourseStudentEntity(), new CourseStudentEntity());

        when(courseStudentRepository.findByIncomingFilesetIDAndSchoolID(
                eq(incomingFilesetId), eq(pen), eq(schoolId), eq(FilesetStatus.COMPLETED.getCode())
        )).thenReturn(expectedList);

        List<CourseStudentEntity> result = courseStudentService.getCrsStudents(pen, incomingFilesetId, schoolId, districtId);
        assertThat(result).isEqualTo(expectedList);
        verify(courseStudentRepository, times(1))
                .findByIncomingFilesetIDAndSchoolID(incomingFilesetId, pen, schoolId, FilesetStatus.COMPLETED.getCode());
    }

    @Test
    void getCrsStudents_withDistrictId_shouldReturnList() {
        String pen = "123456789";
        UUID incomingFilesetId = UUID.randomUUID();
        UUID schoolId = null;
        UUID districtId = UUID.randomUUID();

        List<CourseStudentEntity> expectedList = List.of(new CourseStudentEntity());

        when(courseStudentRepository.findByIncomingFilesetIDAndDistrictID(
                eq(incomingFilesetId), eq(pen), eq(districtId), eq(FilesetStatus.COMPLETED.getCode())
        )).thenReturn(expectedList);

        List<CourseStudentEntity> result = courseStudentService.getCrsStudents(pen, incomingFilesetId, schoolId, districtId);
        assertThat(result).isEqualTo(expectedList);
        verify(courseStudentRepository, times(1))
                .findByIncomingFilesetIDAndDistrictID(incomingFilesetId, pen, districtId, FilesetStatus.COMPLETED.getCode());
    }

    @Test
    void getCrsStudents_withoutSchoolOrDistrict_shouldThrowIllegalArgumentException() {
        String pen = "123456789";
        UUID incomingFilesetId = UUID.randomUUID();
        UUID schoolId = null;
        UUID districtId = null;

        assertThatThrownBy(() ->
                courseStudentService.getCrsStudents(pen, incomingFilesetId, schoolId, districtId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Either schoolID or districtID must be provided.");
    }
}
