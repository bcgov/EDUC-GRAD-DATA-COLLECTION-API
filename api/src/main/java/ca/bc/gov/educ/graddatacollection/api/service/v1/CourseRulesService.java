package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
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

    public CourseRulesService(DemographicStudentRepository demographicStudentRepository, RestUtils restUtils) {
        super(restUtils);
        this.demographicStudentRepository = demographicStudentRepository;
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

    public Boolean letterGradeMatch(LetterGrade letterGrade, String finalGrade) {
        // expiry dates can be null
        LocalDate effectiveDate = ZonedDateTime.parse(letterGrade.getEffectiveDate()).toLocalDate();
        LocalDate expiryDate = letterGrade.getExpiryDate() != null ? ZonedDateTime.parse(letterGrade.getExpiryDate()).toLocalDate() : null;
        LocalDate currentDate = LocalDate.now();

        boolean isWithinDateRange = currentDate.isAfter(effectiveDate) && (expiryDate == null || currentDate.isBefore(expiryDate));

        return isWithinDateRange && letterGrade.getGrade().equalsIgnoreCase(finalGrade);
    }
}
