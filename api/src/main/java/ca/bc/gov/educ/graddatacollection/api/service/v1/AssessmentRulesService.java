package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.AssessmentStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1.AssessmentStudentDetailResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class AssessmentRulesService extends BaseRulesService {

    private final DemographicStudentRepository demographicStudentRepository;
    private final AssessmentStudentRepository assessmentStudentRepository;

    public AssessmentRulesService(RestUtils restUtils, DemographicStudentRepository demographicStudentRepository, AssessmentStudentRepository assessmentStudentRepository) {
        super(restUtils);
        this.demographicStudentRepository = demographicStudentRepository;
        this.assessmentStudentRepository = assessmentStudentRepository;
    }

    public boolean containsDemographicDataForStudent(UUID incomingFilesetID, String pen, String surname, String localID) {
        var results = demographicStudentRepository.findAllByIncomingFileset_IncomingFilesetIDAndLastNameEqualsIgnoreCaseAndPenEqualsIgnoreCaseAndLocalIDEqualsIgnoreCase(incomingFilesetID, surname, pen, localID);
        return !results.isEmpty();
    }

    public DemographicStudentEntity getDemographicDataForStudent(UUID incomingFilesetID, String pen, String surname, String localID) {
        var results = demographicStudentRepository.findAllByIncomingFileset_IncomingFilesetIDAndLastNameEqualsIgnoreCaseAndPenEqualsIgnoreCaseAndLocalIDEqualsIgnoreCase(incomingFilesetID, surname, pen, localID);
        if(!results.isEmpty()) {
            return results.get(0);
        }
        return null;
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

    public String getAssessmentID(String year, String month, String courseCode){
        var session = restUtils.getAssessmentSessionByCourseMonthAndYear(month, year);

        if(session.isPresent()){
            var returnAssessment = session.get().getAssessments().stream().filter(assessment -> assessment.getAssessmentTypeCode().equalsIgnoreCase(courseCode)).findFirst().orElse(null);
            return returnAssessment != null ? returnAssessment.getAssessmentID() : null;
        }
        return null;
    }

    public AssessmentStudentDetailResponse getAssessmentStudentDetail(UUID studentID, UUID assessmentID){
        return restUtils.getAssessmentStudentDetail(studentID, assessmentID);
    }

    public boolean checkIfStudentHasDuplicatesInFileset(String pen, String courseCode, String courseMonth, String courseYear){
        return assessmentStudentRepository.countByPenEqualsAndCourseCodeEqualsAndCourseMonthEqualsAndCourseYearEquals(pen, courseCode, courseMonth, courseYear) > 1;
    }
}
