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
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | D16 | ERROR    | Student birthdate must match what is in PEN	                          | D03, D04     |
 *
 */


@Component
@Slf4j
@Order(160)
public class BirthdateMismatchRule implements DemographicValidationBaseRule {

    private final DemographicRulesService demographicRulesService;
    private final ApplicationProperties props;

    public BirthdateMismatchRule(DemographicRulesService demographicRulesService, ApplicationProperties props) {
        this.demographicRulesService = demographicRulesService;
        this.props = props;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentBirthdate-D16: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = isValidationDependencyResolved("D16", validationErrorsMap);

        log.debug("In shouldExecute of StudentBirthdate-D16: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var demStudent = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentBirthdate-D16 for demographicStudentID :: {}", demStudent.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();
        var studentApiStudent = demographicRulesService.getStudentApiStudent(studentRuleData, demStudent.getPen());
        var secureMessageUrl = props.getEdxBaseUrl() + "/inbox";
        if (!RuleUtil.validateStudentDOBMatches(demStudent, studentApiStudent)) {
            log.debug("StudentBirthdate-D16: Student birthdate must match what is in PEN for demographicStudentID :: {}", demStudent.getDemographicStudentID());

            String schoolBirthdate =  StringEscapeUtils.escapeHtml4(demStudent.getBirthdate());
            String ministryBirthdate = studentApiStudent.getDob();
            var message = "BIRTHDATE mismatch. School submitted: " + schoolBirthdate + " and the Ministry PEN system has: " + ministryBirthdate + ". If the submitted BIRTHDATE is correct, request a PEN update through <a href=\""+secureMessageUrl+"\">EDX Secure Messaging </a>";
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.BIRTHDATE, DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_MISMATCH, message));
        }
        return errors;
    }

}
