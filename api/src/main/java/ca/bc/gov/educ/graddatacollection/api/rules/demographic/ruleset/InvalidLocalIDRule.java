package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
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

/**
 *  | ID   | Severity | Rule                                                               | Dependent On |
 *  |------|----------|-------------------------------------------------------------------|--------------|
 *  | D09 | WARNING  | If Student Local ID is not blank, should match what's in PEN	      | D03          |
 *
 */
@Component
@Slf4j
@Order(90)
public class InvalidLocalIDRule implements DemographicValidationBaseRule {

    private final DemographicRulesService demographicRulesService;
    private final ApplicationProperties props;

    public InvalidLocalIDRule(DemographicRulesService demographicRulesService, ApplicationProperties props) {
        this.demographicRulesService = demographicRulesService;
        this.props = props;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of studentLocalD-D09: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = isValidationDependencyResolved("D09", validationErrorsMap);

        log.debug("In shouldExecute of studentLocalD-D09: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var demStudent = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of studentLocalD-D09 for demographicStudentID :: {}", demStudent.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        boolean isSummer = demographicRulesService.isSummerCollection(demStudent.getIncomingFileset());
        var secureMessageUrl = props.getEdxBaseUrl() + "/inbox";
        var student = demographicRulesService.getStudentApiStudent(studentRuleData, demStudent.getPen());

        if (!isSummer &&
            StringUtils.isNotBlank(student.getLocalID()) &&
            !student.getLocalID().equalsIgnoreCase(demStudent.getLocalID())) {
            log.debug("studentLocalD-D09: Warning: The submitted STUDENT LOCAL ID does not match the ministry database. If the submitted STUDENT LOCAL ID is correct, submit PEN update request through Secure Messaging Inbox in EDX. for demographicStudentID :: {}", demStudent.getDemographicStudentID());
            String message = "The submitted STUDENT LOCAL ID does not match the Ministry PEN system. If the submitted data is correct, request a PEN update through <a href=\""+secureMessageUrl+"\">EDX Secure Messaging </a>";
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, ValidationFieldCode.LOCAL_ID, DemographicStudentValidationIssueTypeCode.STUDENT_LOCAL_ID_MISMATCH, message));
        }
        return errors;
    }

}
