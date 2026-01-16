package ca.bc.gov.educ.graddatacollection.api.service;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolStudentStatus;
import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.FinalDemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.*;
import ca.bc.gov.educ.graddatacollection.api.service.v1.DemographicStudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DemographicStudentServiceTest extends BaseGradDataCollectionAPITest {

    @Autowired
    private ReportingPeriodRepository reportingPeriodRepository;
    @Autowired
    private FinalIncomingFilesetRepository incomingFilesetRepository;
    @Autowired
    private FinalDemographicStudentRepository demographicStudentRepository;
    @Autowired
    private DemographicStudentService demographicStudentService;

    @Test
    void getDemStudent_withIncomingFilesetIdAndSchoolId_shouldReturnEntity() {
        String pen = "123456789";
        var reportingPeriod = reportingPeriodRepository.save(createMockReportingPeriodEntity());
        var fileset = createMockFinalIncomingFilesetEntityWithAllFilesLoaded(reportingPeriod);
        fileset.setFilesetStatusCode(FilesetStatus.COMPLETED.getCode());
        var incomingFileset = incomingFilesetRepository.save(fileset);

        FinalDemographicStudentEntity expected = new FinalDemographicStudentEntity();
        expected.setPen(pen);
        expected.setIncomingFileset(incomingFileset);
        expected.setStudentStatusCode(SchoolStudentStatus.VERIFIED.getCode());
        expected.setCreateUser("ABC");
        expected.setUpdateUser("ABC");

        demographicStudentRepository.save(expected);

        var result = demographicStudentService.getDemStudent(pen, incomingFileset.getIncomingFilesetID(), incomingFileset.getSchoolID());
        assertThat(result).isEqualTo(expected);
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

        assertThatThrownBy(() ->
                demographicStudentService.getDemStudent(pen, incomingFilesetId, schoolId))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
