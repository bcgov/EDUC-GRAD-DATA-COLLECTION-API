package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.StudentStatusCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.DemographicRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GradStudentRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | D20 | ERROR    |  If the student is new to GRAD, that reported status should not        | D03, D06      |
 *  |      |          |  be “T” or “D”.                                                       |              |
 */

@Component
@Slf4j
@Order(200)
public class NewStudentReportedWithIncorrectStatusRule implements DemographicValidationBaseRule {

    private final DemographicRulesService demographicRulesService;

    public NewStudentReportedWithIncorrectStatusRule(DemographicRulesService demographicRulesService) {
        this.demographicRulesService = demographicRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentStatus-D20: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute =  isValidationDependencyResolved("D20", validationErrorsMap);

        log.debug("In shouldExecute of StudentStatus-D20: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentStatus-D20 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        GradStudentRecord gradStudentRecord = demographicRulesService.getGradStudentRecord(studentRuleData, student.getPen());
        if (gradStudentRecord == null &&
            (student.getStudentStatus().equalsIgnoreCase(StudentStatusCodes.T.getCode())
            || student.getStudentStatus().equalsIgnoreCase(StudentStatusCodes.D.getCode()))) {
            log.debug("StudentStatus-D20:Student No GRAD student record found and dem record contains student status of T or D for demographicStudentID: {}", student.getDemographicStudentID());
            var message = "Student is being submitted with status of " + StringEscapeUtils.escapeHtml4(student.getStudentStatus());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, ValidationFieldCode.STUDENT_STATUS, DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_INCORRECT_NEW_STUDENT, message));
        }
        return errors;
    }
}
