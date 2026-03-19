package ca.bc.gov.educ.graddatacollection.api.rules.assessment.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.StudentStatusCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.AssessmentRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GradStudentRecord;
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
 *  | V23  | ERROR    | An error in the DEM file for this student is preventing the           |    V01, V02  |
 *  |      |          | processing of their assessment data (D06,D19,D20,D21).                |              |
 *
 */

@Component
@Slf4j
@Order(25)
public class AssessmentDemFileError implements AssessmentValidationBaseRule {

    private final AssessmentRulesService assessmentRulesService;

    public AssessmentDemFileError(AssessmentRulesService assessmentRulesService) {
        this.assessmentRulesService = assessmentRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<AssessmentStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V23: for assessmentStudentID :: {}", studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        var shouldExecute = isValidationDependencyResolved("V23", validationErrorsMap);

        log.debug("In shouldExecute of V23: Condition returned - {} for assessmentStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        return  shouldExecute;
    }

    @Override
    public List<AssessmentStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var assessmentStudent = studentRuleData.getAssessmentStudentEntity();
        var demographicStudent = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of V23 for assessmentStudentID :: {}", assessmentStudent.getAssessmentStudentID());
        final List<AssessmentStudentValidationIssue> errors = new ArrayList<>();

        // D06
        if (StringUtils.isBlank(demographicStudent.getStudentStatus()) || !StudentStatusCodes.getValidStudentStatusCodesExcludingM().contains(demographicStudent.getStudentStatus())) {
            log.debug("StudentStatus-D06 (V23): {} for demographicStudentID :: {}", AssessmentStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE.getMessage(), demographicStudent.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.STUDENT_STATUS, AssessmentStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE, AssessmentStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE.getMessage()));
            return errors;
        }

        // D19
        GradStudentRecord gradStudent = assessmentRulesService.getGradStudentRecord(studentRuleData, demographicStudent.getPen());
        if (gradStudent != null
                && demographicStudent.getStudentStatus().equalsIgnoreCase(StudentStatusCodes.T.getCode())
                && "CUR".equalsIgnoreCase(gradStudent.getStudentStatusCode())
                && !gradStudent.getSchoolOfRecordId().equalsIgnoreCase(studentRuleData.getSchool().getSchoolId())) {
            log.debug("StudentStatus-D19 (V23): {} for demographicStudentID :: {}", AssessmentStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE.getMessage(), demographicStudent.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.STUDENT_STATUS, AssessmentStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE, AssessmentStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE.getMessage()));
            return errors;
        }

        // D20
        if (gradStudent == null && demographicStudent.getStudentStatus().equalsIgnoreCase(StudentStatusCodes.T.getCode())) {
            log.debug("StudentStatus-D20 (V23): {} for demographicStudentID :: {}", AssessmentStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE.getMessage(), demographicStudent.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.STUDENT_STATUS, AssessmentStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE, AssessmentStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE.getMessage()));
            return errors;
        }

        // D21 partial - only checking if student status in the student api is M
        var student = assessmentRulesService.getStudentApiStudent(studentRuleData, demographicStudent.getPen());

        String ministryStudentStatus = student.getStatusCode();

        if ("M".equalsIgnoreCase(ministryStudentStatus)) {
            log.debug("StudentStatus-D21 (V23): {} for demographicStudentID :: {}", AssessmentStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE.getMessage(), demographicStudent.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.STUDENT_STATUS, AssessmentStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE, AssessmentStudentValidationIssueTypeCode.ERROR_IN_DEM_FILE.getMessage()));
        }
        return errors;
    }
}

