package ca.bc.gov.educ.graddatacollection.api.service.v1;
import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GradStudentRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DemographicRulesService {

    private final RestUtils restUtils;

    public GradStudentRecord getGradStudentRecord(StudentRuleData studentRuleData){
        var demStud = studentRuleData.getDemographicStudentEntity();

        if (demStud == null || demStud.getPen() == null) {
            throw new GradDataCollectionAPIRuntimeException("DemographicRulesService: Demographic Student or PEN is missing.");
        }

        if (studentRuleData.getGradStudentRecord() != null) {
            return studentRuleData.getGradStudentRecord();
        }

        if (studentRuleData.getStudentApiStudent() == null) {
            log.debug("Fetching student data using PEN: {}", demStud.getPen());
            studentRuleData.setStudentApiStudent(restUtils.getStudentByPEN(UUID.randomUUID(), demStud.getPen()));

            if (studentRuleData.getStudentApiStudent() == null || studentRuleData.getStudentApiStudent().getStudentID() == null) {
                throw new GradDataCollectionAPIRuntimeException("DemographicRulesService: Student API data is missing or invalid for PEN: " + demStud.getPen());
            }
        }

        try {
            log.debug("Fetching GradStudentRecord for student ID: {}", studentRuleData.getStudentApiStudent().getStudentID());
            UUID studentUUID = UUID.fromString(studentRuleData.getStudentApiStudent().getStudentID());
            GradStudentRecord gradStudent = restUtils.getGradStudentRecordByStudentID(UUID.randomUUID(), studentUUID);

            studentRuleData.setGradStudentRecord(gradStudent);
            return gradStudent;

        } catch (IllegalArgumentException e) {
            throw new GradDataCollectionAPIRuntimeException("DemographicRulesService: Invalid student ID format: " + studentRuleData.getStudentApiStudent().getStudentID());
        } catch (Exception e) {
            throw new GradDataCollectionAPIRuntimeException("DemographicRulesService: Error fetching GradStudentRecord for student ID: " + studentRuleData.getStudentApiStudent().getStudentID());
        }
    }
}
