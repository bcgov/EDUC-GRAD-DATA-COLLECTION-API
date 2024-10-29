package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V111 | WARN     | Must be a valid grade that is currently effective in GRAD             | -            |
 *
 */

@Component
@Slf4j
@Order(1100)
public class V111DemographicStudentGrade implements DemographicValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentGrade-V111: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = true;

        log.debug("In shouldExecute of StudentGrade-V111: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentGrade-V111 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        // TODO check valid grades that are currently effective in GRAD against this students
        // we should just check the valid grades in grad once, we then know what to check against for every student

        if (false) {
            log.debug("StudentGrade-V111: Must be a valid grade that is currently effective in GRAD for demographicStudentID :: {}", student.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, DemographicStudentValidationFieldCode.STUDENT_GRADE, DemographicStudentValidationIssueTypeCode.GRADE_NOT_IN_GRAD));
        }
        return errors;
    }
}
