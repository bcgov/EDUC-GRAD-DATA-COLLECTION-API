package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.external.scholarships.v1.CitizenshipCode;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V116 | ERROR    | Must be a valid citizenship code                              	      | -            |
 *
 */
@Component
@Slf4j
@Order(1600)
public class V116DemographicStudentCitizenship implements DemographicValidationBaseRule {

    private final RestUtils restUtils;

    public V116DemographicStudentCitizenship(RestUtils restUtils) {
        this.restUtils = restUtils;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentCitizenship-V116: for demographicCitizenshipCode :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = true;

        log.debug("In shouldExecute of StudentCitizenship-V116: Condition returned - {} for demographicCitizenshipCode :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentCitizenship-V116 for demographicCitizenshipCode :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        List<CitizenshipCode> citizenshipCodes = restUtils.getScholarshipsCitizenshipCodes();

        if (citizenshipCodes.stream().noneMatch(code -> Objects.equals(code.getCitizenshipCode(), student.getCitizenship()))) {
            log.debug("StudentCitizenship-V116: Invalid citizenship code - must be C, O or blank for demographicCitizenshipCode :: {}", student.getDemographicStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, DemographicStudentValidationFieldCode.STUDENT_CITIZENSHIP_CODE, DemographicStudentValidationIssueTypeCode.STUDENT_CITIZENSHIP_CODE_INVALID));
        }
        return errors;
    }

}
