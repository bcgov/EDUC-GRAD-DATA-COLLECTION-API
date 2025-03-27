package ca.bc.gov.educ.graddatacollection.api.rules.assessment.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
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
 *  | V14 | ERROR    |  Invalid exam school provided.                                        |V03|
 *
 */
@Component
@Slf4j
@Order(140)
public class ExamSchoolRule implements AssessmentValidationBaseRule {

    private final RestUtils restUtils;

    public ExamSchoolRule(RestUtils restUtils) {
        this.restUtils = restUtils;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<AssessmentStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V14: for assessment {} and assessmentStudentID :: {}", studentRuleData.getAssessmentStudentEntity().getAssessmentID() ,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        var shouldExecute = isValidationDependencyResolved("V14", validationErrorsMap);

        log.debug("In shouldExecute of V14: Condition returned - {} for assessmentStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getAssessmentStudentEntity().getAssessmentStudentID());

        return  shouldExecute;
    }

    @Override
    public List<AssessmentStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getAssessmentStudentEntity();
        log.debug("In executeValidation of V14 for assessmentStudentID :: {}", student.getAssessmentStudentID());
        final List<AssessmentStudentValidationIssue> errors = new ArrayList<>();

        if (student.getExamSchoolID() != null && !isSchoolValid(student.getExamSchoolID())){
            log.debug("V14: Invalid assessment center provided. :: {}", student.getAssessmentStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.EXAM_SCHOOL, AssessmentStudentValidationIssueTypeCode.EXAM_SCHOOL_INVALID, AssessmentStudentValidationIssueTypeCode.EXAM_SCHOOL_INVALID.getMessage()));
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
        } else {
            return false;
        }
        return true;
    }

}
