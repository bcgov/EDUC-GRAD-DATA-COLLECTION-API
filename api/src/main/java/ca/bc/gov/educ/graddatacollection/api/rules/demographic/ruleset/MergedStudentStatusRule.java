package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.StudentStatusCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
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
 *  | ID   | Severity | Rule                                                              | Dependent On |
 *  |------|----------|------------------------------------------------------------------ |--------------|
 *  | D22 | ERROR    |  Reported students cannot have a status of “M” (merged) in         |   D03, D06 |
 *  |      |          | the PEN system.                    	                              |              |
 *
 */

@Component
@Slf4j
@Order(220)
public class MergedStudentStatusRule implements DemographicValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentStatus-D22: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute =  isValidationDependencyResolved("D22", validationErrorsMap);

        log.debug("In shouldExecute of StudentStatus-D22: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getStudentApiStudent();
        log.debug("In executeValidation of StudentStatus-D22 for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        if (StudentStatusCodes.getStudentStatusCodeM().equalsIgnoreCase(student.getStatusCode())) {
            log.debug("StudentStatus-D22:Student PEN has been merged with a pre-existing PEN for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.PEN, DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_MERGED, DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_MERGED.getMessage()));
        }
        return errors;
    }
}
