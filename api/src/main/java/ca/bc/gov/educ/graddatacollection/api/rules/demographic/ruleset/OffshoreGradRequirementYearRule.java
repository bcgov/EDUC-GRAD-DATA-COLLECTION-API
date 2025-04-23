package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradRequirementYearCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolCategoryCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | D13 | ERROR    | If Offshore School, cannot be 1950 or SCCP	                          |  D05         |
 *  |      |          |                                                                       |              |
 */

@Component
@Slf4j
@Order(130)
public class OffshoreGradRequirementYearRule implements DemographicValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentProgram-D13: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = isValidationDependencyResolved("D13", validationErrorsMap);

        log.debug("In shouldExecute of StudentProgram-D13: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentProgram-D13 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        if (StringUtils.isNotBlank(student.getGradRequirementYear()) && studentRuleData.getSchool().getSchoolCategoryCode().equalsIgnoreCase(SchoolCategoryCodes.OFFSHORE.getCode()) &&
            GradRequirementYearCodes.getOffshoreSchoolNotAllowedCodes().stream().anyMatch(code -> code.equalsIgnoreCase(student.getGradRequirementYear()))) {
            log.debug("StudentProgram-D13: Error: 1950 and SCCP are not valid program codes for offshore schools. The student's DEM file will not be processed. demographicStudentID :: {}", student.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.GRAD_REQUIREMENT_YEAR, DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_SCHOOL_CATEGORY_CODE_INVALID, DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_SCHOOL_CATEGORY_CODE_INVALID.getMessage()));
        }
        return errors;
    }
}
