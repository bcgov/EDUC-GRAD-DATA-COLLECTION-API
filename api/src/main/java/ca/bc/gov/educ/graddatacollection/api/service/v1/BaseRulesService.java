package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CoregCoursesRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GradStudentCourseRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GradStudentRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class BaseRulesService {

    protected final RestUtils restUtils;

    public Student getStudentApiStudent(StudentRuleData studentRuleData, String pen) {
        if (Boolean.TRUE.equals(studentRuleData.getStudentApiStudentFetched())) {
            return studentRuleData.getStudentApiStudent();
        }

        log.debug("BaseRulesService:getStudentApiStudent: Fetching student data using PEN: {}", pen);
        try {
            Student studentApiStudent = restUtils.getStudentByPEN(UUID.randomUUID(), pen);
            studentRuleData.setStudentApiStudent(studentApiStudent);
        } catch (EntityNotFoundException e) {
            log.warn("No StudentApiStudent found for PEN: {}", pen);
            studentRuleData.setStudentApiStudent(null);
        }
        studentRuleData.setStudentApiStudentFetched(true);
        return studentRuleData.getStudentApiStudent();
    }

    public GradStudentRecord getGradStudentRecord(StudentRuleData studentRuleData, String pen){
        if (Boolean.TRUE.equals(studentRuleData.getGradStudentRecordFetched())) {
            return studentRuleData.getGradStudentRecord();
        }

        if (studentRuleData.getStudentApiStudent() == null) {
            getStudentApiStudent(studentRuleData, pen);
        }

        if (studentRuleData.getStudentApiStudent() == null) {
            log.warn("No GradStudentRecord found for null studentApiStudent Record");
            studentRuleData.setGradStudentRecord(null);
            studentRuleData.setGradStudentRecordFetched(true);
            return null;
        }

        log.debug("BaseRulesService:getGradStudentRecord: Fetching GradStudentRecord for student ID: {}", studentRuleData.getStudentApiStudent().getStudentID());
        UUID studentUUID = UUID.fromString(studentRuleData.getStudentApiStudent().getStudentID());

        try {
            GradStudentRecord gradStudent = restUtils.getGradStudentRecordByStudentID(UUID.randomUUID(), studentUUID);
            studentRuleData.setGradStudentRecord(gradStudent);
        } catch (EntityNotFoundException e) {
            log.warn("No GradStudentRecord found for student ID: {}", studentUUID);
            studentRuleData.setGradStudentRecord(null);
        }
        studentRuleData.setGradStudentRecordFetched(true);
        return studentRuleData.getGradStudentRecord();
    }

    public CoregCoursesRecord getCoregCoursesRecord(StudentRuleData studentRuleData, String externalID) {
        if (studentRuleData.getCoregCoursesRecordMap() != null && studentRuleData.getCoregCoursesRecordMap().containsKey(externalID)) {
            return studentRuleData.getCoregCoursesRecordMap().get(externalID);
        }

        try {
            log.debug("Calling out for course with external ID: {} for course student: {}", externalID, studentRuleData.getCourseStudentEntity().getCourseStudentID());
            CoregCoursesRecord coregCourses = restUtils.getCoursesByExternalID(UUID.randomUUID(), externalID);

            if (studentRuleData.getCoregCoursesRecordMap() == null) {
                studentRuleData.setCoregCoursesRecordMap(new HashMap<>());
            }
            studentRuleData.getCoregCoursesRecordMap().put(externalID, coregCourses);

            return coregCourses;
        } catch (EntityNotFoundException e) {
            log.warn("No CoregCoursesRecord found for externalID: {}", externalID);
            return null;
        }
    }

    public List<GradStudentCourseRecord> getStudentCourseRecord(StudentRuleData studentRuleData, String pen) {
        if (studentRuleData.getGradStudentCourseRecordList() != null) {
            return studentRuleData.getGradStudentCourseRecordList();
        }

        try {
            List<GradStudentCourseRecord> gradStudentCourses = restUtils.getGradStudentCoursesByPEN(UUID.randomUUID(), pen);

            studentRuleData.setGradStudentCourseRecordList(gradStudentCourses);

            return gradStudentCourses;
        } catch (EntityNotFoundException e) {
            log.warn("No gradStudentCourses found for externalID: {}", pen);
            return null;
        }
    }
}

