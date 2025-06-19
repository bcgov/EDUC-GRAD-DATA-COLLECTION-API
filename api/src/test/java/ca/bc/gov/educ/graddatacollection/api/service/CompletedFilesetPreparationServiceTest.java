package ca.bc.gov.educ.graddatacollection.api.service;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CompletedFilesetPreparationService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFilesetSagaData;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

public class CompletedFilesetPreparationServiceTest extends BaseGradDataCollectionAPITest {
    @Mock
    IncomingFilesetRepository incomingFilesetRepository;

    @InjectMocks
    CompletedFilesetPreparationService service;

    UUID filesetId = UUID.randomUUID();

    @Test
    void testPrepareFilesetData_happyPath() {
        // given
        IncomingFilesetEntity entity = new IncomingFilesetEntity();
        entity.setIncomingFilesetID(filesetId);
        DemographicStudentEntity demoStudent = createDemoStudentEntity();
        entity.setDemographicStudentEntities(Set.of(demoStudent));
        when(incomingFilesetRepository.findById(filesetId)).thenReturn(Optional.of(entity));

        // when
        var result = service.prepareFilesetData(List.of(filesetId));

        // then
        assertThat(result).hasSize(1);
        IncomingFilesetSagaData datum = result.get(0);
        assertThat(datum.getIncomingFileset().getIncomingFilesetID()).isEqualTo(filesetId.toString());
        assertThat(datum.getDemographicStudent().getDemographicStudentID()).isEqualTo(demoStudent.getDemographicStudentID().toString());
    }

    @Test
    void testPrepareFilesetData_missingFilesetThrows() {
        when(incomingFilesetRepository.findById(filesetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.prepareFilesetData(List.of(filesetId)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("IncomingFilesetEntity");
    }

    @Test
    void testPrepareFilesetData_noDemographicStudentThrows() {
        IncomingFilesetEntity entity = new IncomingFilesetEntity();
        entity.setIncomingFilesetID(filesetId);
        entity.setDemographicStudentEntities(Set.of()); // empty
        when(incomingFilesetRepository.findById(filesetId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.prepareFilesetData(List.of(filesetId)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("DemographicStudent");
    }

    private DemographicStudentEntity createDemoStudentEntity() {
        DemographicStudentEntity d = new DemographicStudentEntity();
        d.setDemographicStudentID(UUID.randomUUID());
        return d;
    }
}
