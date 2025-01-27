package ca.bc.gov.educ.graddatacollection.api.rules.assessment.ruleset;

import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.AssessmentStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V317 | ERROR    |  Invalid exam school provided.                                        |--------------|
 *
 */
@Component
@Slf4j
@Order(240)
public class V317ExamSchool implements AssessmentValidationBaseRule {

    private final RestUtils restUtils;

    public V317ExamSchool(RestUtils restUtils) {
        this.restUtils = restUtils;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<AssessmentStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V317: for assessment {} and assessmentStudentID :: {}", studentRuleData.getAssessmentStudentEntity().getAssessmentID() ,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        var shouldExecute = true;

        log.debug("In shouldExecute of V317: Condition returned - {} for assessmentStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        return  shouldExecute;
    }

    @Override
    public List<AssessmentStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getAssessmentStudentEntity();
        log.debug("In executeValidation of V317 for assessmentStudentID :: {}", student.getAssessmentStudentID());
        final List<AssessmentStudentValidationIssue> errors = new ArrayList<>();

        if (student.getExamSchoolID() == null || !isSchoolValid(student.getExamSchoolID())){
            log.debug("V317: Invalid exam school provided :: {}", student.getAssessmentStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, AssessmentStudentValidationFieldCode.EXAM_SCHOOL, AssessmentStudentValidationIssueTypeCode.EXAM_SCHOOL_INVALID, AssessmentStudentValidationIssueTypeCode.EXAM_SCHOOL_INVALID.getMessage()));
        }
        return errors;
    }

    private boolean isSchoolValid(UUID schoolID){
        var schoolOpt = restUtils.getSchoolBySchoolID(schoolID.toString());
        if(schoolOpt.isPresent()) {
            var school = schoolOpt.get();
            var currentDate = LocalDateTime.now();
            LocalDateTime openDate = null;
            LocalDateTime closeDate = null;
            try {
                openDate = LocalDateTime.parse(school.getOpenedDate());

                if (openDate.isAfter(currentDate)){
                    return false;
                }

                if(school.getClosedDate() != null) {
                    closeDate = LocalDateTime.parse(school.getClosedDate());
                }else{
                    closeDate = LocalDateTime.now().plusDays(5);
                }
            } catch (DateTimeParseException e) {
                return false;
            }

            if (!(openDate.isBefore(currentDate) && closeDate.isAfter(currentDate))) {
                return false;
            }
        }
        return true;
    }

}
