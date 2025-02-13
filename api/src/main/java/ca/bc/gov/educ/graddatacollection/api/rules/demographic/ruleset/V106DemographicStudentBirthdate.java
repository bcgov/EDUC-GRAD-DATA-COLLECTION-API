package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.rules.utils.RuleUtil;
import ca.bc.gov.educ.graddatacollection.api.service.v1.DemographicRulesService;
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
 *  | V106 | ERROR    | Student birthdate must match what is in PEN	                          | V128         |
 *
 */


@Component
@Slf4j
@Order(600)
public class V106DemographicStudentBirthdate implements DemographicValidationBaseRule {

    private final DemographicRulesService demographicRulesService;

    public V106DemographicStudentBirthdate(DemographicRulesService demographicRulesService) {
        this.demographicRulesService = demographicRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentBirthdate-V106: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = isValidationDependencyResolved("V106", validationErrorsMap);

        log.debug("In shouldExecute of StudentBirthdate-V106: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var demStudent = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentBirthdate-V106 for demographicStudentID :: {}", demStudent.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();
        var studentApiStudent = demographicRulesService.getStudentApiStudent(studentRuleData, demStudent.getPen());

        if (RuleUtil.validateStudentRecordExists(studentApiStudent) &&
            !RuleUtil.validateStudentDOBMatches(demStudent, studentApiStudent)) {
            log.debug("StudentBirthdate-V106: Student birthdate must match what is in PEN for demographicStudentID :: {}", demStudent.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.BIRTHDATE, DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_MISMATCH, DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_MISMATCH.getMessage()));
        }
        return errors;
    }

}
