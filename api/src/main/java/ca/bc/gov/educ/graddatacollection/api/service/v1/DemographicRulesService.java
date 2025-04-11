package ca.bc.gov.educ.graddatacollection.api.service.v1;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.CourseStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class DemographicRulesService extends BaseRulesService {

    private final CourseStudentRepository courseStudentRepository;
    private final DemographicStudentRepository demographicStudentRepository;

    public DemographicRulesService(RestUtils restUtils, CourseStudentRepository courseStudentRepository, DemographicStudentRepository demographicStudentRepository) {
        super(restUtils);
        this.courseStudentRepository = courseStudentRepository;
        this.demographicStudentRepository = demographicStudentRepository;
    }

    public boolean containsCoursePenForStudent(UUID incomingFilesetID, String pen) {
        var results = courseStudentRepository.findAllByIncomingFileset_IncomingFilesetIDAndPenEqualsIgnoreCase(incomingFilesetID, pen);
        return !results.isEmpty();
    }

    public DemographicStudentEntity getDemographicDataForStudent(UUID incomingFilesetID, String pen, String surname, String localID) {
        var results = demographicStudentRepository.findAllByIncomingFileset_IncomingFilesetIDAndLastNameEqualsIgnoreCaseAndPenEqualsIgnoreCaseAndLocalIDEqualsIgnoreCase(incomingFilesetID, surname, pen, localID);
        if(!results.isEmpty()) {
            return results.getFirst();
        }
        return null;
    }
}
