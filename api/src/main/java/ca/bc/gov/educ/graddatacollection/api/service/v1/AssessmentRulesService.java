package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.AssessmentSessionMonths;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.NumeracyAssessmentCodes;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.AssessmentStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1.AssessmentStudentDetailResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    public boolean sessionMonthIsValid(String month) {
        return AssessmentSessionMonths.findByValue(month).isPresent();
    }

    public boolean sessionIsInPast(String year, String month) {
        String dateString = "%s-%s-01".formatted(year, month);
        LocalDate sessionDate = LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE);
        LocalDate currentDate = LocalDate.now().withDayOfMonth(1);
        return sessionDate.isBefore(currentDate);
    }

    public boolean courseIsValidForSession(String year, String month, String courseCode) {
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
        var stud = restUtils.getAssessmentStudentDetail(studentID, assessmentID);
        log.debug("AssessmentRulesService:getAssessmentStudentDetail: Fetched student data using student ID: {} :: assessmentDetail: {}", studentID, stud);
        return stud;
    }

    public boolean checkIfStudentHasDuplicatesInFileset(UUID incomingFilesetID, String pen, String courseCode, String courseMonth, String courseYear){
        return assessmentStudentRepository.countByIncomingFileset_IncomingFilesetIDAndPenEqualsAndCourseCodeEqualsAndCourseMonthEqualsAndCourseYearEquals(incomingFilesetID, pen, courseCode, courseMonth, courseYear) > 1;
    }

    public boolean checkIfStudentHasDuplicatesInFilesetWithNumeracyCheck(UUID incomingFilesetID, String pen, String courseCode, String courseMonth, String courseYear) {
        final List<String> numeracyCodes = NumeracyAssessmentCodes.getAllCodes();
        boolean isNumeracy = numeracyCodes.stream().anyMatch(code -> code.equalsIgnoreCase(courseCode));
        if (isNumeracy) {
            long count = assessmentStudentRepository.countNumeracyDuplicates(incomingFilesetID, pen, numeracyCodes, courseMonth, courseYear);
            return count > 1;
        } else {
            return checkIfStudentHasDuplicatesInFileset(incomingFilesetID, pen, courseCode, courseMonth, courseYear);
        }
    }
}
