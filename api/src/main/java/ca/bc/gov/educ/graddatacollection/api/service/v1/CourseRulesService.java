package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.CourseStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.LetterGrade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZonedDateTime;
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

    public boolean containsDemographicDataForStudent(UUID incomingFilesetID, String pen, String surname, String localID) {
        var results = demographicStudentRepository.findAllByIncomingFileset_IncomingFilesetIDAndLastNameEqualsIgnoreCaseAndPenEqualsIgnoreCaseAndLocalIDEqualsIgnoreCase(incomingFilesetID, surname, pen, localID);
        return !results.isEmpty();
    }

    public DemographicStudentEntity getDemographicDataForStudent(UUID incomingFilesetID, String pen, String surname, String localID) {
        var results = demographicStudentRepository.findAllByIncomingFileset_IncomingFilesetIDAndLastNameEqualsIgnoreCaseAndPenEqualsIgnoreCaseAndLocalIDEqualsIgnoreCase(incomingFilesetID, surname, pen, localID);
        if(!results.isEmpty()) {
            return results.getFirst();
        }
        return null;
    }

    public Boolean letterGradeMatch(LetterGrade letterGrade, String finalGrade, LocalDate sessionStartDate) {
        // expiry dates can be null
        LocalDate effectiveDate = ZonedDateTime.parse(letterGrade.getEffectiveDate()).toLocalDate();
        LocalDate expiryDate = letterGrade.getExpiryDate() != null ? ZonedDateTime.parse(letterGrade.getExpiryDate()).toLocalDate() : null;

        boolean isWithinDateRange = sessionStartDate.isAfter(effectiveDate) && (expiryDate == null || sessionStartDate.isBefore(expiryDate));

        return isWithinDateRange && letterGrade.getGrade().equalsIgnoreCase(finalGrade);
    }

    public boolean checkIfStudentHasDuplicateInFileset(String pen, String courseCode, String courseMonth, String courseYear, String courseLevel) {
        return courseStudentRepository.countByPenEqualsAndCourseCodeEqualsAndCourseMonthEqualsAndCourseYearEqualsAndCourseLevelEquals(pen, courseCode, courseMonth, courseYear, courseLevel) > 1;
    }
}
