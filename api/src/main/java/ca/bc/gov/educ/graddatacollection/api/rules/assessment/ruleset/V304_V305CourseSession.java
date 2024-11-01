package ca.bc.gov.educ.graddatacollection.api.rules.assessment.ruleset;

import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.AssessmentRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1.AssessmentStudentDetailResponse;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.AssessmentStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *  | ID          | Severity | Rule                                                                                                       | Dependent On |
 *  |-------------|----------|------------------------------------------------------------------------------------------------------------|--------------|
 *  | V304        | ERROR    | The assessment session is a duplicate of an existing assessment session for this student/assessment/level  |--------------|
 *  | V305        | ERROR    | Student has already reached the maximum number of writes for this Assessment specified                     |--------------|
 *
 */
@Component
@Slf4j
@Order(130)
public class V304_V305CourseSession implements AssessmentValidationBaseRule {

    private final AssessmentRulesService assessmentRulesService;

    public V304_V305CourseSession(AssessmentRulesService assessmentRulesService) {
        this.assessmentRulesService = assessmentRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<AssessmentStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V304 & V305: for assessment {} and assessmentStudentID :: {}", studentRuleData.getAssessmentStudentEntity().getAssessmentID() ,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        var shouldExecute = isValidationDependencyResolved("V304", validationErrorsMap)
                && StringUtils.isNotBlank(studentRuleData.getAssessmentStudentEntity().getCourseStatus())
                && !studentRuleData.getAssessmentStudentEntity().getCourseStatus().equalsIgnoreCase("W");

        log.debug("In shouldExecute of V304 & V305: Condition returned - {} for assessmentStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        return  shouldExecute;
    }

    @Override
    public List<AssessmentStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getAssessmentStudentEntity();
        log.debug("In executeValidation of V304 & V305 for assessmentStudentID :: {}", student.getAssessmentStudentID());
        final List<AssessmentStudentValidationIssue> errors = new ArrayList<>();

        var assessmentID = assessmentRulesService.getAssessmentID(student.getCourseYear(), student.getCourseMonth(), student.getCourseCode());
        var studentApiStudent = assessmentRulesService.getStudent(student.getPen());

        AssessmentStudentDetailResponse studAssessmentDetail = null;

        if(studentApiStudent != null) {
            studAssessmentDetail = assessmentRulesService.getAssessmentStudentDetail(UUID.fromString(studentApiStudent.getStudentID()), UUID.fromString(assessmentID));
        }

        if (studAssessmentDetail == null || studAssessmentDetail.isHasPriorRegistration()) {
            log.debug("V304: The assessment session is a duplicate of an existing assessment session for this student/assessment/level :: {}", student.getAssessmentStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, AssessmentStudentValidationFieldCode.COURSE_CODE, AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_DUP));
        }else if (Integer.parseInt(studAssessmentDetail.getNumberOfAttempts()) >= 2) {
            log.debug("V305: Student has already reached the maximum number of writes for this Assessment :: {}", student.getAssessmentStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, AssessmentStudentValidationFieldCode.COURSE_CODE, AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_EXCEED));
        }
        return errors;
    }

}
