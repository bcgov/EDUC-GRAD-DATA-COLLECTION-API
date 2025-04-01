package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.rules.utils.RuleUtil;
import ca.bc.gov.educ.graddatacollection.api.service.v1.DemographicRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | D10 | ERROR    | Student name must match what is in PEN	                              | D03          |
 *
 */
@Component
@Slf4j
@Order(100)
public class InvalidStudentNameRule implements DemographicValidationBaseRule {

    private final DemographicRulesService demographicRulesService;
    private final ApplicationProperties props;

    public InvalidStudentNameRule(DemographicRulesService demographicRulesService, ApplicationProperties props) {
        this.demographicRulesService = demographicRulesService;
        this.props = props;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of studentName-D10: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = isValidationDependencyResolved("D10", validationErrorsMap);

        log.debug("In shouldExecute of studentName-D10: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var demStudent = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of studentName-D10 for demographicStudentID :: {}", demStudent.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();
        var secureMessageUrl = props.getEdxBaseUrl() + "/inbox";

        var student = demographicRulesService.getStudentApiStudent(studentRuleData, demStudent.getPen());

        if (!RuleUtil.validateStudentSurnameMatches(demStudent, student)) {
                String schoolSurname =  StringEscapeUtils.escapeHtml4(demStudent.getLastName());
                String ministrySurname = student.getLegalLastName();
                String message = StringUtils.isBlank(schoolSurname)
                        ? "SURNAME mismatch. School submitted a blank surname and the Ministry PEN system has: " + ministrySurname + ". If the submitted SURNAME is correct, request a PEN update through <a href=\""+secureMessageUrl+"\">EDX Secure Messaging </a>"
                        : "SURNAME mismatch. School submitted: " + schoolSurname + " and the Ministry PEN system has: " + ministrySurname + ". If the submitted SURNAME is correct, request a PEN update through <a href=\""+secureMessageUrl+"\">EDX Secure Messaging </a>";
                log.debug("studentSurName-D10: Error: " + message + " for demographicStudentID :: {}", demStudent.getDemographicStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.LAST_NAME, DemographicStudentValidationIssueTypeCode.STUDENT_SURNAME_MISMATCH, message));
            }
            if (!RuleUtil.validateStudentMiddleNameMatches(demStudent, student)) {
                String schoolMiddleName = StringEscapeUtils.escapeHtml4(demStudent.getMiddleName());
                String ministryMiddleNames = student.getLegalMiddleNames();
                String message;
                if (StringUtils.isBlank(schoolMiddleName)) {
                    message = "MIDDLE NAME mismatch. School submitted a blank MIDDLE NAME and the Ministry PEN system has: " + ministryMiddleNames
                            + ". If the submitted MIDDLE NAME is correct, request a PEN update through <a href=\""+secureMessageUrl+"\">EDX Secure Messaging </a>";
                } else if (StringUtils.isBlank(ministryMiddleNames)) {
                    message = "MIDDLE NAME mismatch. School submitted: " + schoolMiddleName + " but the Ministry PEN system is blank. "
                            + "If the submitted MIDDLE NAME is correct, request a PEN update through <a href=\""+secureMessageUrl+"\">EDX Secure Messaging </a>";
                } else {
                    message = "MIDDLE NAME mismatch. School submitted: " + schoolMiddleName + " and the Ministry PEN system has: " + ministryMiddleNames
                            + ". If the submitted MIDDLE NAME is correct, request a PEN update through <a href=\""+secureMessageUrl+"\">EDX Secure Messaging </a>";
                }
                log.debug("studentMiddleNames-D10: Error: " + message + " for demographicStudentID :: {}", demStudent.getDemographicStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.MIDDLE_NAME, DemographicStudentValidationIssueTypeCode.STUDENT_MIDDLE_MISMATCH, message));
            }
            if (!RuleUtil.validateStudentGivenNameMatches(demStudent, student)) {
                String schoolGiven = StringEscapeUtils.escapeHtml4(demStudent.getFirstName());
                String ministryGiven = student.getLegalFirstName();
                String message;
                if (StringUtils.isBlank(schoolGiven)) {
                    message = "FIRST NAME mismatch. School submitted a blank FIRST NAME and the Ministry PEN system has: " + ministryGiven
                            + ". If the submitted FIRST NAME is correct, request a PEN update through <a href=\""+secureMessageUrl+"\">EDX Secure Messaging </a>";
                } else if (StringUtils.isBlank(ministryGiven)) {
                    message = "FIRST NAME mismatch. School submitted: " + schoolGiven + " but the Ministry PEN system is blank. "
                            + "If the submitted FIRST NAME is correct, request a PEN update through <a href=\""+secureMessageUrl+"\">EDX Secure Messaging </a>";
                } else {
                    message = "FIRST NAME mismatch. School submitted: " + schoolGiven + " and the Ministry PEN system has: " + ministryGiven
                            + ". If the submitted FIRST NAME is correct, request a PEN update through <a href=\""+secureMessageUrl+"\">EDX Secure Messaging </a>";
                }
                log.debug("studentGivenName-D10: Error: " + message + " for demographicStudentID :: {}", demStudent.getDemographicStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.FIRST_NAME, DemographicStudentValidationIssueTypeCode.STUDENT_GIVEN_MISMATCH, message));
            }
        return errors;
    }

}
