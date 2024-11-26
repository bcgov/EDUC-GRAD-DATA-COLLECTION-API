package ca.bc.gov.educ.graddatacollection.api.service.v1;
import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GradStudentRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
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

    public Student getStudentApiStudent(String pen) {
        if (pen == null || pen.isBlank()) {
            throw new GradDataCollectionAPIRuntimeException("DemographicRulesService:getStudentApiStudent: PEN is missing or invalid for dem student record.");
        }

        log.debug("DemographicRulesService:getStudentApiStudent: Fetching student data using PEN: {}", pen);
        Student studentApiStudent = restUtils.getStudentByPEN(UUID.randomUUID(), pen);

        if (studentApiStudent == null || studentApiStudent.getStudentID() == null) {
            throw new GradDataCollectionAPIRuntimeException("DemographicRulesService:getStudentApiStudent: Student API data is missing or invalid for PEN: " + pen);
        }

        return studentApiStudent;
    }


    public GradStudentRecord getGradStudentRecord(StudentRuleData studentRuleData){
        var demStud = studentRuleData.getDemographicStudentEntity();

        if (demStud == null || demStud.getPen() == null) {
            throw new GradDataCollectionAPIRuntimeException("DemographicRulesService:getGradStudentRecord: Demographic Student or PEN is missing.");
        }

        if (studentRuleData.getGradStudentRecord() != null) {
            return studentRuleData.getGradStudentRecord();
        }

        if (studentRuleData.getStudentApiStudent() == null) {
            studentRuleData.setStudentApiStudent(getStudentApiStudent(demStud.getPen()));
        }

        try {
            log.debug("DemographicRulesService:getGradStudentRecord: Fetching GradStudentRecord for student ID: {}", studentRuleData.getStudentApiStudent().getStudentID());
            UUID studentUUID = UUID.fromString(studentRuleData.getStudentApiStudent().getStudentID());
            GradStudentRecord gradStudent = restUtils.getGradStudentRecordByStudentID(UUID.randomUUID(), studentUUID);

            studentRuleData.setGradStudentRecord(gradStudent);
            return gradStudent;

        } catch (Exception e) {
            throw new GradDataCollectionAPIRuntimeException("DemographicRulesService:getGradStudentRecord: Error fetching GradStudentRecord for student ID: " + studentRuleData.getStudentApiStudent().getStudentID());
        }
    }
}
