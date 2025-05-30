package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ReportingPeriodEntity;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CoregCoursesRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GradStudentCourseRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GradStudentRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
            log.debug("No StudentApiStudent found for PEN: {}", pen);
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
            log.debug("No GradStudentRecord found for null studentApiStudent Record");
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
            log.debug("No GradStudentRecord found for student ID: {}", studentUUID);
            studentRuleData.setGradStudentRecord(null);
        }
        studentRuleData.setGradStudentRecordFetched(true);
        return studentRuleData.getGradStudentRecord();
    }

    public CoregCoursesRecord getCoregCoursesRecord(StudentRuleData studentRuleData, String courseCode, String courseLevel) {
        if (StringUtils.isEmpty(courseCode)) {
            log.debug("External ID components are empty. Skipping call out for course with course code: {}, course level: {}, for course student: {}", courseCode, courseLevel, studentRuleData.getCourseStudentEntity().getCourseStudentID());
            return null;
        }

        String externalID = StringUtils.isEmpty(courseLevel) ? courseCode : String.format("%-5s", courseCode) + courseLevel;

        if (studentRuleData.getCoregCoursesRecord() != null) {
            return studentRuleData.getCoregCoursesRecord();
        }

        CoregCoursesRecord coregCourses = getCoRegCourseRecord(externalID, studentRuleData.getCourseStudentEntity().getCourseStudentID());
        studentRuleData.setCoregCoursesRecord(coregCourses);
        return coregCourses;
    }

    public CoregCoursesRecord getCoregRelatedCoursesRecord(StudentRuleData studentRuleData, String relatedCourseCode, String relatedCourseLevel) {
        if (StringUtils.isEmpty(relatedCourseCode)) {
            log.debug("External ID components are empty. Skipping call out for course with related course code: {}, related course level: {}, for course student: {}", relatedCourseCode, relatedCourseLevel, studentRuleData.getCourseStudentEntity().getCourseStudentID());
            return null;
        }

        String externalID = StringUtils.isEmpty(relatedCourseLevel) ? relatedCourseCode : String.format("%-5s", relatedCourseCode) + relatedCourseLevel;

        if (studentRuleData.getCoregRelatedCoursesRecord() != null) {
            return studentRuleData.getCoregRelatedCoursesRecord();
        }
        CoregCoursesRecord coregRelatedCourses = getCoRegCourseRecord(externalID, studentRuleData.getCourseStudentEntity().getCourseStudentID());
        studentRuleData.setCoregRelatedCoursesRecord(coregRelatedCourses);
        return coregRelatedCourses;
    }

    public CoregCoursesRecord getCoRegCourseRecord(String externalID, UUID courseStudentID) {
        try {
            log.debug("Calling out for course with external ID: {} for course student: {}", externalID, courseStudentID);
            return restUtils.getCoursesByExternalID(UUID.randomUUID(), externalID);
        } catch (EntityNotFoundException e) {
            log.debug("No CoRegCoursesRecord found for externalID: {} for course student: {}", externalID, courseStudentID);
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
            log.debug("No gradStudentCourses found for externalID: {}", pen);
            return null;
        }
    }

    public boolean isSummerCollection(IncomingFilesetEntity incomingFilesetEntity) {
        ReportingPeriodEntity reportingPeriod = incomingFilesetEntity.getReportingPeriod();
        LocalDateTime now = LocalDateTime.now();
        return (now.isEqual(reportingPeriod.getSummerStart()) || now.isAfter(reportingPeriod.getSummerStart()))
                && (now.isEqual(reportingPeriod.getSummerEnd()) || now.isBefore(reportingPeriod.getSummerEnd()));
    }
}

