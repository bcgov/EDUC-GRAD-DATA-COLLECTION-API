package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradRequirementYearCodes;
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

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | D23 | ERROR    | If the student is submitted on the Adult program, they must be at     | D04, D05     |
 *  |      |          | least 18 years old at the time of data submission, whether or not    |              |
 *  |      |          | they are already in GRAD                                              |              |
 *
 */
@Component
@Slf4j
@Order(230)
public class AdultGraduationRule implements DemographicValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentAdultBirthdate-D23: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = isValidationDependencyResolved("D23", validationErrorsMap);

        log.debug("In shouldExecute of StudentAdultBirthdate-D23: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentAdultBirthdate-D23 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        if (StringUtils.isNotBlank(student.getGradRequirementYear()) &&
            GradRequirementYearCodes.getAdultGraduationProgramYearCodes().stream().anyMatch(code -> code.equalsIgnoreCase(student.getGradRequirementYear())) &&
            Period.between(LocalDate.parse(student.getBirthdate(), DateTimeFormatter.ofPattern("yyyyMMdd")), LocalDate.from(student.getIncomingFileset().getDemFileUploadDate())).getYears() < 18) {
            log.debug("StudentAdultBirthdate-D23: Student must be on the SCCP program. SCCP Completion date not updated. for demographicStudentID :: {}", student.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.BIRTHDATE, DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_ADULT, DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_ADULT.getMessage()));
        }
        return errors;
    }

}
