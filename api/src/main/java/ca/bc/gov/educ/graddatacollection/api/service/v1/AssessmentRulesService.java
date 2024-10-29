package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentRulesService {

    private final RestUtils restUtils;
    private final DemographicStudentRepository demographicStudentRepository;

    public boolean containsDemographicDataForStudent(UUID incomingFilesetID, String pen, String surname, String localID) {
        var results = demographicStudentRepository.findAllByIncomingFileset_IncomingFilesetIDAndLastNameEqualsIgnoreCaseAndPenEqualsIgnoreCaseAndLocalIDEqualsIgnoreCase(incomingFilesetID, surname, pen, localID);
        return !results.isEmpty();
    }

    public boolean courseIsValidForSession(String year, String month, String courseCode){
        if(StringUtils.isEmpty(year) || StringUtils.isEmpty(month) || StringUtils.isEmpty(courseCode)){
            return false;
        }
        var session = restUtils.getAssessmentSessionByCourseMonthAndYear(month, year);

        if(session.isPresent()){
            return session.get().getAssessments().stream().anyMatch(assessment -> assessment.getAssessmentTypeCode().equalsIgnoreCase(courseCode));
        }
        return false;
    }
}
