package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

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
import java.util.Objects;
import java.util.regex.Pattern;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V01  | WARNING  | Student address must exist for students in grades 12 or AD	 	      | -            |
 *
 */
@Component
@Slf4j
@Order(10)
public class Grade12ADAddressRule implements DemographicValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentAddress-v01: for demographicStudentAddress :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = true;

        log.debug("In shouldExecute of StudentAddress-v01: Condition returned - {} for demographicStudentAddress :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentAddress-v01 for demographicStudentAddress :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        if (StringUtils.isNotBlank(student.getGrade()) && SchoolGradeCodes.getGrades12AndAD().stream().anyMatch(grade -> grade.equalsIgnoreCase(student.getGrade()))) {
            log.debug("StudentAddress-v01: Student address must exist for students in grades 12 or AD. for demographicStudentAddress :: {}", student.getDemographicStudentID());

            if (StringUtils.isBlank(student.getAddressLine1())) {
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, ValidationFieldCode.ADDRESS1, DemographicStudentValidationIssueTypeCode.STUDENT_ADDRESS_BLANK, DemographicStudentValidationIssueTypeCode.STUDENT_ADDRESS_BLANK.getMessage()));
            }
            if (StringUtils.isBlank(student.getCity())) {
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, ValidationFieldCode.CITY, DemographicStudentValidationIssueTypeCode.STUDENT_CITY_BLANK, DemographicStudentValidationIssueTypeCode.STUDENT_CITY_BLANK.getMessage()));
            }
            Pattern pattern = Pattern.compile("^(?:[A-Za-z]\\d[A-Za-z]\\d[A-Za-z]\\d|\\d{5}|\\d{7})$");
            String postalCode = student.getPostalCode();

            if(StringUtils.isBlank(postalCode) || !pattern.matcher(student.getPostalCode()).matches()) {
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, ValidationFieldCode.POSTAL_CODE, DemographicStudentValidationIssueTypeCode.STUDENT_POSTAL_CODE_INVALID, DemographicStudentValidationIssueTypeCode.STUDENT_POSTAL_CODE_INVALID.getMessage()));
            }
            if (!StringUtils.equalsIgnoreCase("BC", student.getProvincialCode())) {
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, ValidationFieldCode.PROVINCIAL_CODE, DemographicStudentValidationIssueTypeCode.STUDENT_PROVINCE_CODE_INVALID, DemographicStudentValidationIssueTypeCode.STUDENT_PROVINCE_CODE_INVALID.getMessage()));
            }
            if (!StringUtils.equalsIgnoreCase("CA", student.getCountryCode())) {
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.WARNING, ValidationFieldCode.COUNTRY_CODE, DemographicStudentValidationIssueTypeCode.STUDENT_COUNTRY_CODE_INVALID, DemographicStudentValidationIssueTypeCode.STUDENT_COUNTRY_CODE_INVALID.getMessage()));
            }
        }
        return errors;
    }

}
