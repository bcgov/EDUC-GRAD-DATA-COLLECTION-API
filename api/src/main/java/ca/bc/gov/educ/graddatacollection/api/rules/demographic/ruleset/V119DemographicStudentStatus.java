package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.StudentStatusCodes;
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
 *  | V119 | ERROR    |  If student status = "T" and the mincode provided in the data file    | V117         |
 *  |      |          |  does not match the current School of Record in GRAD and the student  |              |
 *  |      |          |  status on the students' GRAD data is CUR (current)                   |              |
 */

@Component
@Slf4j
@Order(1900)
public class V119DemographicStudentStatus implements DemographicValidationBaseRule {

    private final DemographicRulesService demographicRulesService;

    public V119DemographicStudentStatus(DemographicRulesService demographicRulesService) {
        this.demographicRulesService = demographicRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentStatus-V119: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute =  isValidationDependencyResolved("V19", validationErrorsMap);

        log.debug("In shouldExecute of StudentStatus-V119: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentStatus-V119 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        var gradStudent = demographicRulesService.getGradStudentRecord(studentRuleData);
        if (student.getStudentStatusCode().equalsIgnoreCase(StudentStatusCodes.T.getCode()) &&
            gradStudent.getStudentStatusCode().equalsIgnoreCase("CUR") &&
            !gradStudent.getSchoolOfRecord().equalsIgnoreCase(studentRuleData.getSchool().getMincode())
            ) {
            log.debug("StudentStatus-V119:Student This school is not the School of Record showing in GRAD; the student record will not be updated. demographicStudentID :: {}", student.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, DemographicStudentValidationFieldCode.STUDENT_STATUS, DemographicStudentValidationIssueTypeCode.STUDENT_STATUS_SCHOOL_OF_RECORD_MISMATCH));
        }

        return errors;
    }
}
