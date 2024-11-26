package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicValidationBaseRule;
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
 *  | V118 | ERROR    |  Student Status must match PEN student status                         |  v117        |
 *  |      |          |                                     	                              |              |
 *
 */

@Component
@Slf4j
@Order(1800)
public class V118DemographicStudentStatus implements DemographicValidationBaseRule {

    private final DemographicRulesService demographicRulesService;

    public V118DemographicStudentStatus(DemographicRulesService demographicRulesService) {
        this.demographicRulesService = demographicRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentStatus-V118: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute =  isValidationDependencyResolved("V18", validationErrorsMap);

        log.debug("In shouldExecute of StudentStatus-V118: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var demStudent = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentStatus-V118 for demographicStudentID :: {}", demStudent.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();
        try {
            var student = demographicRulesService.getStudentApiStudent(demStudent.getPen());
            if (!(demStudent.getStudentStatusCode().equalsIgnoreCase(student.getStatusCode()) ||
                    ("A".equalsIgnoreCase(student.getStatusCode()) && ("T".equalsIgnoreCase(demStudent.getStudentStatusCode())))
            )) {
                log.debug("StudentStatus-V118: Student Status must match PEN.  demographicStudentID :: {}", demStudent.getDemographicStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, DemographicStudentValidationFieldCode.STUDENT_STATUS, DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_PEN_MISMATCH));
            }

        } catch (GradDataCollectionAPIRuntimeException e) {
            log.debug("StudentStatus-V118:Student No student api student record found for demographicStudentID: {}", demStudent.getDemographicStudentID());
            //todo should not finding a student api student fail the test?
            //errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, DemographicStudentValidationFieldCode.STUDENT_STATUS, DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_PEN_MISMATCH));
        }
        return errors;
    }
}
