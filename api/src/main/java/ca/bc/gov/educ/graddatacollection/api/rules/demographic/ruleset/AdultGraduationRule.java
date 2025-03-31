package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradRequirementYearCodes;
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

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | D23 | ERROR    | If this is a new student to GRAD and they are submitted on the Adult  | D04, D05     |
 *  |      |          | program, students must be at least 18 years old at the time of data   |              |
 *  |      |          | submission		                           	                          |              |
 *
 */
@Component
@Slf4j
@Order(230)
public class AdultGraduationRule implements DemographicValidationBaseRule {

    private final DemographicRulesService demographicRulesService;

    public AdultGraduationRule(DemographicRulesService demographicRulesService) {
        this.demographicRulesService = demographicRulesService;
    }

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
        var gradStudent = demographicRulesService.getGradStudentRecord(studentRuleData, student.getPen());

        if (gradStudent == null &&
            GradRequirementYearCodes.getAdultGraduationProgramYearCodes().stream().anyMatch(code -> code.equalsIgnoreCase(student.getGradRequirementYear())) &&
            Period.between(LocalDate.parse(student.getBirthdate(), DateTimeFormatter.ofPattern("yyyyMMdd")), LocalDate.from(student.getIncomingFileset().getDemFileUploadDate())).getYears() < 18) {
            log.debug("StudentAdultBirthdate-D23: Student must be on the SCCP program. SCCP Completion date not updated. for demographicStudentID :: {}", student.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.BIRTHDATE, DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_ADULT, DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_ADULT.getMessage()));
        }
        return errors;
    }

}
