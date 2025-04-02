package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.DemographicRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | D17 | ERROR    | For graduated students, the students' graduation program cannot       |  D03, D05    |
 *  |      |          | be changed                                                            |              |
 */

@Component
@Slf4j
@Order(170)
public class GraduatedStudentProgramRule implements DemographicValidationBaseRule {

    private final DemographicRulesService demographicRulesService;

    public GraduatedStudentProgramRule(DemographicRulesService demographicRulesService) {
        this.demographicRulesService = demographicRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of StudentProgram-D17: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = isValidationDependencyResolved("D17", validationErrorsMap);

        log.debug("In shouldExecute of StudentProgram-D17: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of StudentProgram-D17 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        var gradStudent = demographicRulesService.getGradStudentRecord(studentRuleData, student.getPen());
        String studentProgram = student.getGradRequirementYear();

        if (gradStudent != null &&
            gradStudent.getGraduated().equalsIgnoreCase("true")) {
            log.debug("StudentProgram-D17: Error: The student has already graduated so their program code cannot be changed. The student's DEM file will not be processed. demographicStudentID :: {}", student.getDemographicStudentID());
            String message = "The student has a "+ StringEscapeUtils.escapeHtml4(studentProgram)+" completion date so the program code cannot be changed or removed.";
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.PEN, DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_ALREADY_GRADUATED, message));
        }

        return errors;
    }
}
