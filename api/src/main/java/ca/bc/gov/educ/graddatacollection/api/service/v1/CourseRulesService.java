package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolStudentStatus;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.CourseStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CourseRulesService extends BaseRulesService {
    
    private final DemographicStudentRepository demographicStudentRepository;
    private final CourseStudentRepository courseStudentRepository;

    public CourseRulesService(DemographicStudentRepository demographicStudentRepository, RestUtils restUtils, CourseStudentRepository courseStudentRepository) {
        super(restUtils);
        this.demographicStudentRepository = demographicStudentRepository;
        this.courseStudentRepository = courseStudentRepository;
    }

    public DemographicStudentEntity getDemographicDataForStudent(UUID incomingFilesetID, String pen, String surname, String localID) {
        var results = demographicStudentRepository.findAllByIncomingFileset_IncomingFilesetIDAndLastNameEqualsIgnoreCaseAndPenEqualsIgnoreCaseAndLocalIDEqualsIgnoreCase(incomingFilesetID, surname, pen, localID);
        if(!results.isEmpty()) {
            return results.getFirst();
        }
        return null;
    }

    public DemographicStudentEntity getDemographicDataForStudentByPen(UUID incomingFilesetID, String pen) {
        var results = demographicStudentRepository.findAllByIncomingFileset_IncomingFilesetIDAndPenEqualsIgnoreCase(incomingFilesetID, pen);
        if(!results.isEmpty()) {
            return results.getFirst();
        }
        return null;
    }

    public boolean checkIfStudentHasDuplicateInFileset(UUID incomingFilesetID, String pen, String courseCode, String courseMonth, String courseYear, String courseLevel) {
        return courseStudentRepository.countByIncomingFileset_IncomingFilesetIDAndPenEqualsAndCourseCodeEqualsAndCourseMonthEqualsAndCourseYearEqualsAndCourseLevelEquals(incomingFilesetID, pen, courseCode, courseMonth, courseYear, courseLevel) > 1;
    }
    
    public List<CourseStudentEntity> findByIncomingFilesetIDAndPenAndStudentStatusCode(final UUID incomingFilesetID, final String pen) {
        return this.courseStudentRepository.findAllByIncomingFileset_IncomingFilesetIDAndPenAndStudentStatusCode(incomingFilesetID, pen, String.valueOf(SchoolStudentStatus.UPDATE_CRS));
    }
}
