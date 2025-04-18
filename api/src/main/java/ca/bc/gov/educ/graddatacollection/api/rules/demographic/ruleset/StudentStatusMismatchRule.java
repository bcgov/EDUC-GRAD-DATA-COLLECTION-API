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
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | D21 | ERROR    |  Student Status must match PEN student status                         |  D03, D06    |
 *  |      |          |                                     	                              |              |
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
        if (!(demStudent.getStudentStatus().equalsIgnoreCase(student.getStatusCode()) ||
             ("A".equalsIgnoreCase(student.getStatusCode()) && "T".equalsIgnoreCase(demStudent.getStudentStatus())))
        ) {
            log.debug("StudentStatus-D21: Student Status must match PEN.  demographicStudentID :: {}", demStudent.getDemographicStudentID());
            var demStudentStatus = demStudent.getStudentStatus();
            var ministryStudentStatus = student.getStatusCode();
            String message = "STUDENT STATUS mismatch. School submitted: " + StringEscapeUtils.escapeHtml4(demStudentStatus) + " and the Ministry PEN system has: " + ministryStudentStatus + ". If the submitted STUDENT STATUS is correct, request a PEN update through <a href=\""+secureMessageUrl+"\">EDX Secure Messaging </a>";
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.STUDENT_STATUS, DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_PEN_MISMATCH, message));
        }

        return errors;
    }
}
