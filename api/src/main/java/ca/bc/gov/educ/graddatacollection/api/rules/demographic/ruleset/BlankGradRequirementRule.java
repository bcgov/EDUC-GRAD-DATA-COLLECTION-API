package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolCategoryCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolGradeCodes;
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
 *  | D12  | ERROR    | Null program can only be accepted/valid if the student grade is GA    |    D03      |
 *                      or the reporting school is a non-Independent First Nations School
 *                      AND the student program completion date is null in the GRAD API (or
 *                      does not exist in the GRAD API)
 *
 */
@Component
@Slf4j
@Order(120)
public class BlankGradRequirementRule implements DemographicValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentProgram-D12: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute =  isValidationDependencyResolved("D12", validationErrorsMap);

        log.debug("In shouldExecute of StudentProgram-D12: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentProgram-12 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        var gradRecord = studentRuleData.getGradStudentRecord();
        var schoolCategory = studentRuleData.getSchool().getSchoolCategoryCode();

        boolean isNotGradeGA = student.getGrade() != null && !student.getGrade().equalsIgnoreCase(SchoolGradeCodes.GRADUATED_ADULT.getCode());
        boolean isNotIndependentFNS = schoolCategory != null && !schoolCategory.equalsIgnoreCase(SchoolCategoryCodes.INDP_FNS.getCode());
        boolean gradRequirementYearIsBlank = StringUtils.isBlank(student.getGradRequirementYear());
        boolean isGraduated = gradRecord != null && StringUtils.isNotBlank(gradRecord.getProgramCompletionDate());

        if (gradRequirementYearIsBlank && (isNotGradeGA || isNotIndependentFNS) && isGraduated) {
            log.debug("StudentProgram-D12: Null program not valid for demographicStudentID :: {}", student.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.GRAD_REQUIREMENT_YEAR, DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL, DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_GRAD_REQUIREMENT_YEAR_NULL.getMessage()));
        }

        return errors;
    }

}
