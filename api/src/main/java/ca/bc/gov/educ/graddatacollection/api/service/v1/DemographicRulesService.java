package ca.bc.gov.educ.graddatacollection.api.service.v1;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.CourseStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class DemographicRulesService extends BaseRulesService {

    private final CourseStudentRepository courseStudentRepository;

    public DemographicRulesService(RestUtils restUtils, CourseStudentRepository courseStudentRepository) {
        super(restUtils);
        this.courseStudentRepository = courseStudentRepository;
    }

    public boolean containsCoursePenForStudent(UUID incomingFilesetID, String pen) {
        var results = courseStudentRepository.findAllByIncomingFileset_IncomingFilesetIDAndPenEqualsIgnoreCase(incomingFilesetID, pen);
        return !results.isEmpty();
    }
}
