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
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                                                           | Dependent On |
 *  |------|----------|----------------------------------------------------------------------------------------------------------------|--------------|
 *  | D21  | ERROR    |  If the student is submitted with a status of A or T and the student’s status in the Student API is “Merged”   |  D03, D06    |
 *  |      |          |  If the student is submitted with a status of A or T and the student’s status in the Student API is “Deceased” |              |
 *  |      |          |  If the student is submitted with a status of D and the student’s status in the Student API must be “Deceased” |              |
 *
 */

@Component
@Slf4j
@Order(210)
public class StudentStatusMismatchRule implements DemographicValidationBaseRule {

    private final DemographicRulesService demographicRulesService;
    private final ApplicationProperties props;
    public StudentStatusMismatchRule(DemographicRulesService demographicRulesService, ApplicationProperties props) {
        this.demographicRulesService = demographicRulesService;
        this.props = props;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentStatus-D21: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute =  isValidationDependencyResolved("D21", validationErrorsMap);

        log.debug("In shouldExecute of StudentStatus-D21: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var demStudent = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentStatus-D21 for demographicStudentID :: {}", demStudent.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();
        var secureMessageUrl = props.getEdxBaseUrl() + "/inbox";
        var student = demographicRulesService.getStudentApiStudent(studentRuleData, demStudent.getPen());

        String demStudentStatus = demStudent.getStudentStatus();
        String ministryStudentStatus = student.getStatusCode();

        if (("A".equalsIgnoreCase(demStudentStatus) || "T".equalsIgnoreCase(demStudentStatus))
                && "M".equalsIgnoreCase(ministryStudentStatus)) {
            String message = "Student PEN has been merged with a pre-existing PEN. Ensure the valid, pre-existing PEN appears in system data file extracts. If needed, request PEN support through <a href=\"" + secureMessageUrl + "\">EDX Secure Messaging </a>";
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.PEN, DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_MERGED, message));
        }
        else if (("A".equalsIgnoreCase(demStudentStatus) || "T".equalsIgnoreCase(demStudentStatus))
                && "D".equalsIgnoreCase(ministryStudentStatus)) {
            String message = "STUDENT STATUS mismatch. School submitted: " + StringEscapeUtils.escapeHtml4(demStudentStatus) + " and the Ministry PEN system has: " + ministryStudentStatus + ". If the submitted STUDENT STATUS is correct, request a PEN update through <a href=\""+secureMessageUrl+"\">EDX Secure Messaging </a>";
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.STUDENT_STATUS, DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_PEN_MISMATCH, message));
        }
        else if ("D".equalsIgnoreCase(demStudentStatus)
                && !"D".equalsIgnoreCase(ministryStudentStatus)) {
            String message = "STUDENT STATUS mismatch. School submitted: " + StringEscapeUtils.escapeHtml4(demStudentStatus) + " and the Ministry PEN system has: " + ministryStudentStatus + ". If the submitted STUDENT STATUS is correct, request a PEN update through <a href=\""+secureMessageUrl+"\">EDX Secure Messaging </a>";
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.STUDENT_STATUS, DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_PEN_MISMATCH, message));
        }

        return errors;
    }
}
