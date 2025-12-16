package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.StudentStatusCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
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
*  | ID   | Severity | Rule                                                                                                   | Dependent On |
*  |------|----------|--------------------------------------------------------------------------------------------------------|--------------|
*  | D19  | ERROR    | 1. If the incoming status is "T", the student’s status in GRAD must be "CUR" or "TER".                 | D03, D06     |
*  |      |          | 2. If the incoming status is "T" and the student’s status in GRAD is "CUR",                            |              |
*  |      |          |    the reporting school must be the student’s school of record in GRAD.                                |              |
*/

@Component
@Slf4j
@Order(190)
public class CurrentStudentReportedWithIncorrectStatusRule implements DemographicValidationBaseRule {

    private final DemographicRulesService demographicRulesService;

    public CurrentStudentReportedWithIncorrectStatusRule(DemographicRulesService demographicRulesService) {
        this.demographicRulesService = demographicRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentStatus-D19: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute =  isValidationDependencyResolved("D19", validationErrorsMap);

        log.debug("In shouldExecute of StudentStatus-D19: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentStatus-D19 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        var gradStudent = demographicRulesService.getGradStudentRecord(studentRuleData, student.getPen());
        if (gradStudent != null
                && student.getStudentStatus().equalsIgnoreCase(StudentStatusCodes.T.getCode())
                && "CUR".equalsIgnoreCase(gradStudent.getStudentStatusCode())
                && !gradStudent.getSchoolOfRecordId().equalsIgnoreCase(studentRuleData.getSchool().getSchoolId())) {
            log.debug("StudentStatus-D19: {} for demographicStudentID :: {}", DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_SCHOOL_OF_RECORD_MISMATCH.getMessage(), student.getDemographicStudentID());
            errors.add(createValidationIssue(
                    StudentValidationIssueSeverityCode.ERROR,
                    ValidationFieldCode.STUDENT_STATUS,
                    DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_SCHOOL_OF_RECORD_MISMATCH,
                    DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_SCHOOL_OF_RECORD_MISMATCH.getMessage()
            ));
        }
        return errors;
    }
}
