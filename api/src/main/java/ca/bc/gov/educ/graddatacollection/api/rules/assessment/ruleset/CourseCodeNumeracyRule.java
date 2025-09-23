package ca.bc.gov.educ.graddatacollection.api.rules.assessment.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.NumeracyAssessmentCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.AssessmentRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1.AssessmentStudentDetailResponse;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.AssessmentStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *  | ID   | Severity | Rule                                                                                                                        | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------------------------------------------------------------|--------------|
 *  | V22  | ERROR    | Assessment session and code cannot be a duplicate numeracy registration within the “assessment register” (assessment        | V03          |
 *  |      |          | student table in the assessment api) for the student. Numeracy assessments, NME10, NMF10, NME, and NMF, are all             |              |
 *  |      |          | considered the same assessment code.                                                                                        |              |
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
        
        String oppositeCode = getOppositeNumeracyCode(student.getCourseCode());
        if (oppositeCode == null) {
            log.debug("V22: Course code {} is not a numeracy code, skipping validation for assessmentStudentID :: {}", student.getCourseCode(), student.getAssessmentStudentID());
            return errors;
        }
        
        var assessmentID = assessmentRulesService.getAssessmentID(student.getCourseYear(), student.getCourseMonth(), oppositeCode);
        log.debug("V22: Found assessment ID is :: {} for opposite code {} and assessmentStudentID :: {}", assessmentID, oppositeCode, student.getAssessmentStudentID());


        AssessmentStudentDetailResponse studAssessmentDetail = null;

        if (studentApiStudent != null) {
            studAssessmentDetail = assessmentRulesService.getAssessmentStudentDetail(UUID.fromString(studentApiStudent.getStudentID()), UUID.fromString(assessmentID));
        }

        if (studAssessmentDetail != null && studAssessmentDetail.isHasPriorRegistration() && !studentRuleData.getAssessmentStudentEntity().getCourseStatus().equalsIgnoreCase("W")) {
            String incomingCode = student.getCourseCode();
            log.debug("V22: Found conflict - incoming code {} has existing opposite registration {} for assessmentStudentID :: {}", incomingCode, oppositeCode, student.getAssessmentStudentID());

            String errorMessage = AssessmentStudentValidationIssueTypeCode.NUMERACY_DUPLICATE.getMessage().formatted(StringEscapeUtils.escapeHtml4(oppositeCode));
            log.debug("V22: Error: {} for assessmentStudentID :: {}", errorMessage, student.getAssessmentStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_CODE, AssessmentStudentValidationIssueTypeCode.NUMERACY_DUPLICATE, errorMessage));
        }
        return errors;
    }

    /**
     * Get the opposite numeracy code for conflict checking.
     * NMF -> NME, NME -> NMF, NMF10 -> NME10, NME10 -> NMF10
     */
    private String getOppositeNumeracyCode(String courseCode) {
        if (courseCode == null) {
            return null;
        }
        
        String code = courseCode.trim().toUpperCase();
        
        if (code.equalsIgnoreCase(NumeracyAssessmentCodes.NMF.getCode())) {
            return NumeracyAssessmentCodes.NME.getCode();
        } else if (code.equalsIgnoreCase(NumeracyAssessmentCodes.NME.getCode())) {
            return NumeracyAssessmentCodes.NMF.getCode();
        } else if (code.equalsIgnoreCase(NumeracyAssessmentCodes.NMF10.getCode())) {
            return NumeracyAssessmentCodes.NME10.getCode();
        } else if (code.equalsIgnoreCase(NumeracyAssessmentCodes.NME10.getCode())) {
            return NumeracyAssessmentCodes.NMF10.getCode();
        }
        
        return null; // Not a numeracy code
    }
}
