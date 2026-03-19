package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.StudentStatusCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GradStudentRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | C41  | ERROR    | An error in the DEM file for this student is preventing the           |    C02       |
 *  |      |          | processing of their course data (D06,D19,D20,D21).                    |              |
 *
 */

@Component
@Slf4j
@Order(25)
public class CourseDemFileError implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public CourseDemFileError(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C41: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());
    
        var shouldExecute = isValidationDependencyResolved("C41", validationErrorsMap);
    
        log.debug("In shouldExecute of C41: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());
    
        return  shouldExecute;
    }
    
    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var courseStudent = studentRuleData.getCourseStudentEntity();
        var demographicStudent = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of C41 for courseStudentID :: {}", courseStudent.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        // D06
        if (StringUtils.isBlank(demographicStudent.getStudentStatus()) || !StudentStatusCodes.getValidStudentStatusCodesExcludingM().contains(demographicStudent.getStudentStatus())) {
            log.debug("StudentStatus-D06 (C41): {} for demographicStudentID :: {}", CourseStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE.getMessage(), demographicStudent.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.STUDENT_STATUS, CourseStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE, CourseStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE.getMessage()));
            return errors;
        }

        // D19
        GradStudentRecord gradStudent = courseRulesService.getGradStudentRecord(studentRuleData, demographicStudent.getPen());
        if (gradStudent != null
                && demographicStudent.getStudentStatus().equalsIgnoreCase(StudentStatusCodes.T.getCode())
                && "CUR".equalsIgnoreCase(gradStudent.getStudentStatusCode())
                && !gradStudent.getSchoolOfRecordId().equalsIgnoreCase(studentRuleData.getSchool().getSchoolId())) {
            log.debug("StudentStatus-D19 (C41): {} for demographicStudentID :: {}", CourseStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE.getMessage(), demographicStudent.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.STUDENT_STATUS, CourseStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE, CourseStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE.getMessage()));
            return errors;
        }

        // D20
        if (gradStudent == null && demographicStudent.getStudentStatus().equalsIgnoreCase(StudentStatusCodes.T.getCode())) {
            log.debug("StudentStatus-D20 (C41): {} for demographicStudentID :: {}", CourseStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE.getMessage(), demographicStudent.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.STUDENT_STATUS, CourseStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE, CourseStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE.getMessage()));
            return errors;
        }

        // D21 partial - only checking if student status in the student api is M
        var student = courseRulesService.getStudentApiStudent(studentRuleData, demographicStudent.getPen());

        String ministryStudentStatus = student.getStatusCode();

        if ("M".equalsIgnoreCase(ministryStudentStatus)) {
            log.debug("StudentStatus-D21 (C41): {} for demographicStudentID :: {}", CourseStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE.getMessage(), demographicStudent.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.STUDENT_STATUS, CourseStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE, CourseStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE.getMessage()));
        }
        return errors;
    }
}

