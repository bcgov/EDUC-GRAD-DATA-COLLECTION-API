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
 *  | D26 | WARN     | If student is reported on the Adult graduation program (1950)         | D07, D25   |
 *  |      |          | their reported grade must be AD or AN.                                |              |
 */

@Component
@Slf4j
@Order(260)
public class AdultGradeRule implements DemographicValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentGrade-D26: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = isValidationDependencyResolved("D26", validationErrorsMap);

        log.debug("In shouldExecute of StudentGrade-D26: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentGrade-D26 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        if (StringUtils.isNotBlank(student.getGrade()) && StringUtils.isNotBlank(student.getGradRequirementYear())
            && GradRequirementYearCodes.getAdultGraduationProgramYearCodes().stream().anyMatch(adultGradYear -> adultGradYear.equalsIgnoreCase(student.getGradRequirementYear()))
            && SchoolGradeCodes.getGradAdultGrades().stream().noneMatch(validGrade -> validGrade.equalsIgnoreCase(student.getGrade()))) {
            log.debug("StudentGrade-D26: Student reported on the Adult Graduation program (1950) must be grade AD or AN for demographicStudentID :: {}", student.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.GRADE, DemographicStudentValidationIssueTypeCode.GRADE_AG_INVALID, DemographicStudentValidationIssueTypeCode.GRADE_AG_INVALID.getMessage()));
        }
        return errors;
    }
}
