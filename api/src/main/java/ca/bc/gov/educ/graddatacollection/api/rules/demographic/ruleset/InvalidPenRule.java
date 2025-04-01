package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.DemographicRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode.STUDENT_PEN_NOT_FOUND;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | D03 | ERROR    | PEN must exist in the PEN system.	                                  | -            |
 *
 */
@Component
@Slf4j
@Order(30)
public class InvalidPenRule implements DemographicValidationBaseRule {

    private final DemographicRulesService demographicRulesService;

    public InvalidPenRule(DemographicRulesService demographicRulesService) {
        this.demographicRulesService = demographicRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentPEN-D03: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = true;

        log.debug("In shouldExecute of StudentPEN-D03: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var demStudent = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentPEN-D03 for demographicStudentID :: {}", demStudent.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();
        var student = demographicRulesService.getStudentApiStudent(studentRuleData, demStudent.getPen());

        if (student == null || StringUtils.isBlank(student.getPen())) {
            log.debug("StudentPEN-D03: Error: Invalid PEN. Student not found on PEN database so the record for this student cannot be updated for demographicStudentID :: {}", demStudent.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.PEN, STUDENT_PEN_NOT_FOUND, STUDENT_PEN_NOT_FOUND.getMessage()));
        }
        return errors;
    }

}
