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
 *  | v105 | ERROR    | Student name must match what is in PEN	                              | -            |
 *
 */
@Component
@Slf4j
@Order(300)
public class V105DemographicStudentName implements DemographicValidationBaseRule {

    private final DemographicRulesService demographicRulesService;
    private static final String MINISTRY_PEN_PARTIAL = " and the Ministry PEN system has: ";

    public V105DemographicStudentName(DemographicRulesService demographicRulesService) {
        this.demographicRulesService = demographicRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of studentName-v105: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = true;

        log.debug("In shouldExecute of studentName-v105: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var demStudent = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of studentName-v105 for demographicStudentID :: {}", demStudent.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        var student = demographicRulesService.getStudentApiStudent(studentRuleData, demStudent.getPen());

        if (RuleUtil.validateStudentRecordExists(student)) {
            if (!RuleUtil.validateStudentSurnameMatches(demStudent, student)) {
                log.debug("studentName-v105:Error: The submitted SURNAME does not match the ministry database. for demographicStudentID :: {}", demStudent.getDemographicStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.LAST_NAME, DemographicStudentValidationIssueTypeCode.STUDENT_SURNAME_MISMATCH,
        "SURNAME mismatch. School submitted: " + demStudent.getLastName() + MINISTRY_PEN_PARTIAL + student.getLegalLastName() + ". If the submitted SURNAME is correct, request a PEN update through EDX Secure Messaging https://educationdataexchange.gov.bc.ca/login."));
            }
            if (!RuleUtil.validateStudentMiddleNameMatches(demStudent, student)) {
                log.debug("studentName-v105: Error: The submitted MIDDLE NAME does not match the ministry database. for demographicStudentID :: {}", demStudent.getDemographicStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.MIDDLE_NAME, DemographicStudentValidationIssueTypeCode.STUDENT_MIDDLE_MISMATCH,
        "MIDDLE NAME mismatch. School submitted: " + demStudent.getMiddleName() + MINISTRY_PEN_PARTIAL + student.getLegalMiddleNames() + ". If the submitted MIDDLE NAME is correct, request a PEN update through EDX Secure Messaging https://educationdataexchange.gov.bc.ca/login."));
            }
            if (!RuleUtil.validateStudentGivenNameMatches(demStudent, student)) {
                log.debug("studentName-v105:Error: The submitted FIRST NAME does not match the ministry database. for demographicStudentID :: {}", demStudent.getDemographicStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.FIRST_NAME, DemographicStudentValidationIssueTypeCode.STUDENT_GIVEN_MISMATCH,
        "FIRST NAME mismatch. School submitted: " + demStudent.getFirstName() + MINISTRY_PEN_PARTIAL + student.getLegalFirstName() + ". If the submitted FIRST NAME is correct, request a PEN update through EDX Secure Messaging https://educationdataexchange.gov.bc.ca/login."));
            }
        }
        return errors;
    }

}
