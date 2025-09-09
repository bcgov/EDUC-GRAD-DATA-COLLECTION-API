package ca.bc.gov.educ.graddatacollection.api.rules.assessment.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.AssessmentRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1.AssessmentStudentDetailResponse;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.AssessmentStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.NumeracyAssessmentCodes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *  | ID   | Severity | Rule                                                                                                                              | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------------------------------------------------------------------|--------------|
 *  | V22  | ERROR    | Error if the COURSE_STATUS is not "W" and any of the following are true:                                                          | V03          |
 *  |      |          |  - The incoming registration is for NME or NME10 and a registration exists for the student in the same session for NMF or NMF10.  |              |
 *  |      |          |  - The incoming registration is for NMF or NMF10 and a registration exists for the student in the same session for NME or NME10.  |              |
 */
@Component
@Slf4j
@Order(220)
public class CourseCodeNumeracyRule implements AssessmentValidationBaseRule {

    private final AssessmentRulesService assessmentRulesService;

    public CourseCodeNumeracyRule(AssessmentRulesService assessmentRulesService) {
        this.assessmentRulesService = assessmentRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<AssessmentStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V22: for assessment {} and assessmentStudentID :: {}", studentRuleData.getAssessmentStudentEntity().getAssessmentID() ,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        var shouldExecute = isValidationDependencyResolved("V22", validationErrorsMap);

        log.debug("In shouldExecute of V22: Condition returned - {} for assessmentStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        return  shouldExecute;
    }

    @Override
    public List<AssessmentStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getAssessmentStudentEntity();
        log.debug("In executeValidation of V22 for assessmentStudentID :: {}", student.getAssessmentStudentID());
        final List<AssessmentStudentValidationIssue> errors = new ArrayList<>();

        var studentApiStudent = assessmentRulesService.getStudentApiStudent(studentRuleData, student.getPen());
        var assessmentID = assessmentRulesService.getAssessmentID(student.getCourseYear(), student.getCourseMonth(), student.getCourseCode());
        log.info("V22: Found assesssment ID is :: {} for assessmentStudentID :: {}", assessmentID, student.getAssessmentStudentID());

        AssessmentStudentDetailResponse studAssessmentDetail = studentRuleData.getAssessmentStudentDetail();

        if (studentApiStudent != null && studAssessmentDetail == null) {
            studAssessmentDetail = assessmentRulesService.getAssessmentStudentDetail(UUID.fromString(studentApiStudent.getStudentID()), UUID.fromString(assessmentID));
            studentRuleData.setAssessmentStudentDetail(studAssessmentDetail);
        }

        if (studAssessmentDetail != null && studAssessmentDetail.isHasPriorRegistration() && !studentRuleData.getAssessmentStudentEntity().getCourseStatus().equalsIgnoreCase("W")) {
            String incomingCode = student.getCourseCode();
            String existingCode = studAssessmentDetail.getAlreadyRegisteredAssessmentTypeCode();
            if (isNumeracyConflict(incomingCode, existingCode)) {
                String errorMessage = AssessmentStudentValidationIssueTypeCode.NUMERACY_DUPLICATE.getMessage().formatted(StringEscapeUtils.escapeHtml4(existingCode));
                log.debug("V22: Error: {} for assessmentStudentID :: {}", errorMessage, student.getAssessmentStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_CODE, AssessmentStudentValidationIssueTypeCode.NUMERACY_DUPLICATE, errorMessage));
            }
        }
        return errors;
    }

    private boolean isNumeracyConflict(String incomingCode, String existingCode) {
        if (incomingCode == null || existingCode == null) {
            return false;
        }
        String incoming = incomingCode.trim().toUpperCase();
        String existing = existingCode.trim().toUpperCase();

        boolean inIsNME = incoming.equalsIgnoreCase(NumeracyAssessmentCodes.NME.getCode()) || incoming.equalsIgnoreCase(NumeracyAssessmentCodes.NME10.getCode());
        boolean inIsNMF = incoming.equalsIgnoreCase(NumeracyAssessmentCodes.NMF.getCode()) || incoming.equalsIgnoreCase(NumeracyAssessmentCodes.NMF10.getCode());
        boolean exIsNME = existing.equalsIgnoreCase(NumeracyAssessmentCodes.NME.getCode()) || existing.equalsIgnoreCase(NumeracyAssessmentCodes.NME10.getCode());
        boolean exIsNMF = existing.equalsIgnoreCase(NumeracyAssessmentCodes.NMF.getCode()) || existing.equalsIgnoreCase(NumeracyAssessmentCodes.NMF10.getCode());

        return (inIsNME && exIsNMF) || (inIsNMF && exIsNME);
    }
}
