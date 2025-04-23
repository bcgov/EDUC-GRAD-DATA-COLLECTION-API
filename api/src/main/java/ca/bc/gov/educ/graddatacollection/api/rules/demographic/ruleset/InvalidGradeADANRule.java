package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradRequirementYearCodes;
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
 *  | D24 | WARN     | If student is reported on any other graduation program or SCCP their  | D05, D07   |
 *  |      |          |  reported grade should not be AD or AN.	                              |              |
 *
 */

@Component
@Slf4j
@Order(240)
public class InvalidGradeADANRule implements DemographicValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentGrade-D24: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = isValidationDependencyResolved("D24", validationErrorsMap);

        log.debug("In shouldExecute of StudentGrade-D24: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentGrade-D24 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        if (StringUtils.isNotBlank(student.getGradRequirementYear()) && StringUtils.isNotBlank(student.getGrade())
            && GradRequirementYearCodes.getNonAdultGraduationProgramYearCodes().stream().anyMatch(nonAdultGradYear -> nonAdultGradYear.equalsIgnoreCase(student.getGradRequirementYear()))
            && SchoolGradeCodes.getGradAdultGrades().stream().anyMatch(validGrade -> validGrade.equalsIgnoreCase(student.getGrade()))) {
            log.debug("StudentGrade-D24:  Student grade should not be AD or AN for the reported graduation program for demographicStudentID :: {}", student.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.GRADE, DemographicStudentValidationIssueTypeCode.GRADE_OG_INVALID, DemographicStudentValidationIssueTypeCode.GRADE_OG_INVALID.getMessage()));
        }
        return errors;
    }
}
