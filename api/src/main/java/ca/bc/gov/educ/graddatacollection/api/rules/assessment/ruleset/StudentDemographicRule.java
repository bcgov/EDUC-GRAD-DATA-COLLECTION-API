package ca.bc.gov.educ.graddatacollection.api.rules.assessment.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.rules.utils.RuleUtil;
import ca.bc.gov.educ.graddatacollection.api.service.v1.AssessmentRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.AssessmentStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V02 | ERROR    |  Student XAM record will not be processed due to an issue with the    | V01         |
 *                       student's demographics
 *
 */
@Component
@Slf4j
@Order(20)
public class StudentDemographicRule implements AssessmentValidationBaseRule {

    private final AssessmentRulesService assessmentRulesService;

    public StudentDemographicRule(AssessmentRulesService assessmentRulesService) {
        this.assessmentRulesService = assessmentRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<AssessmentStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V02: for assessment {} and assessmentStudentID :: {}", studentRuleData.getAssessmentStudentEntity().getAssessmentID() ,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        var shouldExecute = isValidationDependencyResolved("V02", validationErrorsMap);

        log.debug("In shouldExecute of V02: Condition returned - {} for assessmentStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        return  shouldExecute;
    }

    @Override
    public List<AssessmentStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getAssessmentStudentEntity();
        log.debug("In executeValidation of V02 for assessmentStudentID :: {}", student.getAssessmentStudentID());
        final List<AssessmentStudentValidationIssue> errors = new ArrayList<>();

        var studentApiStudent = assessmentRulesService.getStudentApiStudent(studentRuleData, student.getPen());

        var demographicStudentEntity = assessmentRulesService.getDemographicDataForStudent(student.getIncomingFileset().getIncomingFilesetID(), student.getPen(), student.getLastName(), student.getLocalID());

        studentRuleData.setStudentApiStudent(studentApiStudent);
        studentRuleData.setDemographicStudentEntity(demographicStudentEntity);

        if (!RuleUtil.validateStudentRecordExists(studentRuleData.getStudentApiStudent()) ||
            !RuleUtil.validateStudentSurnameMatches(demographicStudentEntity, studentRuleData.getStudentApiStudent()) ||
            !RuleUtil.validateStudentGivenNameMatches(demographicStudentEntity, studentRuleData.getStudentApiStudent()) ||
            !RuleUtil.validateStudentMiddleNameMatches(demographicStudentEntity, studentRuleData.getStudentApiStudent()) ||
            !RuleUtil.validateStudentDOBMatches(demographicStudentEntity, studentRuleData.getStudentApiStudent())){
            log.debug("V02: Error: {} for assessmentStudentID :: {}", AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getMessage(), student.getAssessmentStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.PEN, AssessmentStudentValidationIssueTypeCode.DEM_ISSUE, AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getMessage()));
        }
        return errors;
    }

}
