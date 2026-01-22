package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseRulesService;
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
 *  | C01  | ERROR    | Must match a PEN in the .DEM file along with Student Surname,         | -            |
 *                      Mincode
 */
@Component
@Slf4j
@Order(10)
public class CourseStudentPENInDEMRule implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public CourseStudentPENInDEMRule(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C01: for assessment {} and courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID() ,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = true;

        log.debug("In shouldExecute of C01: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of C01 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        DemographicStudentEntity demographicStudentEntity = courseRulesService.getDemographicDataForStudent(student.getIncomingFileset().getIncomingFilesetID(), student.getPen(), student.getLastName(), student.getLocalID());

        if (demographicStudentEntity == null) {
            log.debug("V201: Error 1: {} for courseStudentID :: {}", CourseStudentValidationIssueTypeCode.DEM_DATA_MISSING.getMessage(), student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.PEN, CourseStudentValidationIssueTypeCode.DEM_DATA_MISSING, CourseStudentValidationIssueTypeCode.DEM_DATA_MISSING.getMessage()));
        } else if (!StringUtils.equalsIgnoreCase(student.getLastName(), demographicStudentEntity.getLastName())) {
            log.debug("V201: Error 2: {} for courseStudentID :: {}", CourseStudentValidationIssueTypeCode.DEM_DATA_CRS_DATA_MISMATCH.getMessage(), student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.LAST_NAME, CourseStudentValidationIssueTypeCode.DEM_DATA_CRS_DATA_MISMATCH, CourseStudentValidationIssueTypeCode.DEM_DATA_CRS_DATA_MISMATCH.getMessage()));
        }
        return errors;
    }

}
