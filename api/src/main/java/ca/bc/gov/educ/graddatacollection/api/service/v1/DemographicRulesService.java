package ca.bc.gov.educ.graddatacollection.api.service.v1;
import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
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

    public Student getStudentApiStudent(StudentRuleData studentRuleData) {
        if (studentRuleData.getStudentApiStudent() != null) {
            return studentRuleData.getStudentApiStudent();
        }

        log.debug("DemographicRulesService:getStudentApiStudent: Fetching student data using PEN: {}", studentRuleData.getDemographicStudentEntity().getPen());
        Student studentApiStudent = restUtils.getStudentByPEN(UUID.randomUUID(), studentRuleData.getDemographicStudentEntity().getPen());
        studentRuleData.setStudentApiStudent(studentApiStudent);
        return studentApiStudent;
    }


    public GradStudentRecord getGradStudentRecord(StudentRuleData studentRuleData){
        if (studentRuleData.getGradStudentRecord() != null) {
            return studentRuleData.getGradStudentRecord();
        }

        if (studentRuleData.getStudentApiStudent() == null) {
            getStudentApiStudent(studentRuleData);
        }

        log.debug("DemographicRulesService:getGradStudentRecord: Fetching GradStudentRecord for student ID: {}", studentRuleData.getStudentApiStudent().getStudentID());
        UUID studentUUID = UUID.fromString(studentRuleData.getStudentApiStudent().getStudentID());

        try {
            GradStudentRecord gradStudent = restUtils.getGradStudentRecordByStudentID(UUID.randomUUID(), studentUUID);
            studentRuleData.setGradStudentRecord(gradStudent);
            return gradStudent;
        } catch (EntityNotFoundException e) {
            log.warn("No GradStudentRecord found for student ID: {}", studentUUID);
            studentRuleData.setGradStudentRecord(null);
            return null;
        }
    }
}
