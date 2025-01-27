package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradRequirementYearCodes;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V128 | ERROR    | Birthdate value must be a valid date		                   	      |              |
 *
 */
@Component
@Slf4j
@Order(50)
public class V128DemographicStudentBirthdate implements DemographicValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentBirthdate-v128: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = true;

        log.debug("In shouldExecute of StudentBirthdate-v128: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentBirthdate-v128 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        DateTimeFormatter format = DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);
        if (StringUtils.isEmpty(student.getBirthdate())) {
            log.debug("StudentBirthdate-v128: Student date of birth is not valid (EMPTY). for demographicStudentID :: {}", student.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, DemographicStudentValidationFieldCode.STUDENT_BIRTHDATE, DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID, DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getMessage()));
        } else {
            try {
                LocalDate.parse(student.getBirthdate(), format);
            } catch (DateTimeParseException ex) {
                log.debug("StudentBirthdate-v128: Student date of birth is not valid (CANNOT PARSE). for demographicStudentID :: {}", student.getDemographicStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, DemographicStudentValidationFieldCode.STUDENT_BIRTHDATE, DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID, DemographicStudentValidationIssueTypeCode.STUDENT_BIRTHDATE_INVALID.getMessage()));
            }
        }

        return errors;
    }

}
