package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseRulesService {

    private final DemographicStudentRepository demographicStudentRepository;

    private final RestUtils restUtils;

    public boolean containsDemographicDataForStudent(UUID incomingFilesetID, String pen, String surname, String localID) {
        var results = demographicStudentRepository.findAllByIncomingFileset_IncomingFilesetIDAndLastNameEqualsIgnoreCaseAndPenEqualsIgnoreCaseAndLocalIDEqualsIgnoreCase(incomingFilesetID, surname, pen, localID);
        return !results.isEmpty();
    }

    public Student getStudent(String pen){
        return restUtils.getStudentByPEN(UUID.randomUUID(), pen);
    }

    public DemographicStudentEntity getDemographicDataForStudent(UUID incomingFilesetID, String pen, String surname, String localID) {
        var results = demographicStudentRepository.findAllByIncomingFileset_IncomingFilesetIDAndLastNameEqualsIgnoreCaseAndPenEqualsIgnoreCaseAndLocalIDEqualsIgnoreCase(incomingFilesetID, surname, pen, localID);
        if(!results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }
}
