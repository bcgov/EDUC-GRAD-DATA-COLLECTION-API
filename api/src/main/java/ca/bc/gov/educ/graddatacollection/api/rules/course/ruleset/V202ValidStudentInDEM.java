package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.rules.utils.RuleUtil;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V202 | ERROR    |  Student CRS record will not be processed due to an issue with the    |-----V201-----|
 *                       student's demographics
 *
 */
@Component
@Slf4j
@Order(20)
public class V202ValidStudentInDEM implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public V202ValidStudentInDEM(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V202: for course {} and courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID() ,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V202", validationErrorsMap);

        log.debug("In shouldExecute of V202: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of V202 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        var studentApiStudent = courseRulesService.getStudentApiStudent(studentRuleData, student.getPen());

        var demographicStudentEntity = courseRulesService.getDemographicDataForStudent(student.getIncomingFileset().getIncomingFilesetID(), student.getPen(), student.getLastName(), student.getLocalID());

        studentRuleData.setStudentApiStudent(studentApiStudent);
        studentRuleData.setDemographicStudentEntity(demographicStudentEntity);

        if (!RuleUtil.validateStudentRecordExists(studentRuleData.getStudentApiStudent())){
            log.debug("V202: Student CRS record will not be processed due to an issue with the student's demographics PEN :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.PEN, CourseStudentValidationIssueTypeCode.DEM_ISSUE, CourseStudentValidationIssueTypeCode.DEM_ISSUE.getMessage()));
        } else {
            if (!RuleUtil.validateStudentSurnameMatches(demographicStudentEntity, studentRuleData.getStudentApiStudent())) {
                log.debug("V202: Student CRS record will not be processed due to an issue with the student's demographics LAST NAME :: {}", student.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.LAST_NAME, CourseStudentValidationIssueTypeCode.DEM_ISSUE, CourseStudentValidationIssueTypeCode.DEM_ISSUE.getMessage()));
            }
            if (!RuleUtil.validateStudentGivenNameMatches(demographicStudentEntity, studentRuleData.getStudentApiStudent())) {
                log.debug("V202: Student CRS record will not be processed due to an issue with the student's demographics MIDDLE NAME :: {}", student.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.FIRST_NAME, CourseStudentValidationIssueTypeCode.DEM_ISSUE, CourseStudentValidationIssueTypeCode.DEM_ISSUE.getMessage()));
            }
            if (!RuleUtil.validateStudentMiddleNameMatches(demographicStudentEntity, studentRuleData.getStudentApiStudent())) {
                log.debug("V202: Student CRS record will not be processed due to an issue with the student's demographics FIRST NAME :: {}", student.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.MIDDLE_NAME, CourseStudentValidationIssueTypeCode.DEM_ISSUE, CourseStudentValidationIssueTypeCode.DEM_ISSUE.getMessage()));
            }
            if (!RuleUtil.validateStudentDOBMatches(demographicStudentEntity, studentRuleData.getStudentApiStudent())) {
                log.debug("V202: Student CRS record will not be processed due to an issue with the student's demographics DOB :: {}", student.getCourseStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.BIRTHDATE, CourseStudentValidationIssueTypeCode.DEM_ISSUE, CourseStudentValidationIssueTypeCode.DEM_ISSUE.getMessage()));
            }
        }
        return errors;
    }

}
