package ca.bc.gov.educ.graddatacollection.api.service;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ErrorFilesetStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.service.v1.ErrorFilesetStudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ErrorFilesetStudentServiceTest extends BaseGradDataCollectionAPITest {
    @MockBean
    protected RestUtils restUtils;
    @Autowired
    DemographicStudentRepository demographicStudentRepository;
    @Autowired
    IncomingFilesetRepository incomingFilesetRepository;
    @Autowired
    ErrorFilesetStudentRepository errorFilesetStudentRepository;
    @Autowired
    ErrorFilesetStudentService errorFilesetStudentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.demographicStudentRepository.deleteAll();
        this.incomingFilesetRepository.deleteAll();
        this.errorFilesetStudentRepository.deleteAll();
    }

    @Test
    void testFlagErrorOnStudents() {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var mockFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        mockFileset.setSchoolID(UUID.fromString(school.getSchoolId()));
        var fileset = incomingFilesetRepository.save(mockFileset);
        try {
            errorFilesetStudentService.flagErrorOnStudent(fileset.getIncomingFilesetID(), "123456789", false, null, null, null, null);
        } catch(Exception e) {
            fail("Should not have thrown any exception");
        }
    }

    @Test
    void testFlagErrorOnStudents_ShouldBeOk() {
        var school = this.createMockSchool();
        school.setMincode("07965039");
        when(this.restUtils.getSchoolBySchoolID(anyString())).thenReturn(Optional.of(school));
        var mockFileset = createMockIncomingFilesetEntityWithAllFilesLoaded();
        mockFileset.setSchoolID(UUID.fromString(school.getSchoolId()));
        var fileset = incomingFilesetRepository.save(mockFileset);

        var errorStudent = createMockErrorFilesetStudentEntity(fileset);
        errorStudent.setPen("123456789");
        errorFilesetStudentRepository.save(errorStudent);

        try {
            errorFilesetStudentService.flagErrorOnStudent(fileset.getIncomingFilesetID(), "123456789", false, null, null, null, null);
        } catch(Exception e) {
            fail("Should not have thrown any exception");
        }
    }

}
