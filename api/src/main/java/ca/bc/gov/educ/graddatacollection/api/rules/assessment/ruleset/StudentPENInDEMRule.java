package ca.bc.gov.educ.graddatacollection.api.rules.assessment.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.AssessmentRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.AssessmentStudentValidationIssue;
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
 *  | V01 | ERROR    | Must match a PEN in the .DEM file along with Student Surname,         | -            |
 *                      Mincode
 */
@Component
@Slf4j
@Order(10)
public class StudentPENInDEMRule implements AssessmentValidationBaseRule {

    private final AssessmentRulesService assessmentRulesService;

    public StudentPENInDEMRule(AssessmentRulesService assessmentRulesService) {
        this.assessmentRulesService = assessmentRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<AssessmentStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V01: for assessment {} and assessmentStudentID :: {}", studentRuleData.getAssessmentStudentEntity().getAssessmentID() ,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        var shouldExecute = true;

        log.debug("In shouldExecute of V01: Condition returned - {} for assessmentStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        return  shouldExecute;
    }

    @Override
    public List<AssessmentStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getAssessmentStudentEntity();
        log.debug("In executeValidation of V01 for assessmentStudentID :: {}", student.getAssessmentStudentID());
        final List<AssessmentStudentValidationIssue> errors = new ArrayList<>();

        DemographicStudentEntity demographicStudentEntity = assessmentRulesService.getDemographicDataForStudentByPen(student.getIncomingFileset().getIncomingFilesetID(), student.getPen());

        if (demographicStudentEntity == null) {
            log.debug("V01: Error 1: {} for assessmentStudentID :: {}", AssessmentStudentValidationIssueTypeCode.DEM_DATA_MISSING.getMessage(), student.getAssessmentStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.PEN, AssessmentStudentValidationIssueTypeCode.DEM_DATA_MISSING, AssessmentStudentValidationIssueTypeCode.DEM_DATA_MISSING.getMessage()));
        } else {
            if (!StringUtils.equalsIgnoreCase(student.getLastName(), demographicStudentEntity.getLastName())) {
                log.debug("V01: Error 2: {} for assessmentStudentID :: {}", AssessmentStudentValidationIssueTypeCode.DEM_DATA_XAM_DATA_SURNAME_MISMATCH.getMessage(), student.getAssessmentStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.LAST_NAME, AssessmentStudentValidationIssueTypeCode.DEM_DATA_XAM_DATA_SURNAME_MISMATCH, AssessmentStudentValidationIssueTypeCode.DEM_DATA_XAM_DATA_SURNAME_MISMATCH.getMessage()));
            }
            if (!StringUtils.equalsIgnoreCase(student.getLocalID(), demographicStudentEntity.getLocalID())) {
                log.debug("V01: Error 3: {} for assessmentStudentID :: {}", AssessmentStudentValidationIssueTypeCode.DEM_DATA_XAM_DATA_LOCALID_MISMATCH.getMessage(), student.getAssessmentStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.LOCAL_ID, AssessmentStudentValidationIssueTypeCode.DEM_DATA_XAM_DATA_LOCALID_MISMATCH, AssessmentStudentValidationIssueTypeCode.DEM_DATA_XAM_DATA_LOCALID_MISMATCH.getMessage()));
            }
        }
        return errors;
    }

}
