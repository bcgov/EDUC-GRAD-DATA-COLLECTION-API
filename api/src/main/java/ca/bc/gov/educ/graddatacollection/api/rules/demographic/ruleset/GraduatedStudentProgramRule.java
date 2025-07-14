package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.DemographicRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

        boolean isSummer = demographicRulesService.isSummerCollection(student.getIncomingFileset());
        var gradStudent = demographicRulesService.getGradStudentRecord(studentRuleData, student.getPen());
        String studentProgram = student.getGradRequirementYear();

        // Prog Completion
        // If you have a prog completion date in GRAD, you can't change it unless it's SCCP and there's another one incoming

        if (gradStudent != null && StringUtils.isNotBlank(gradStudent.getProgramCompletionDate()) && StringUtils.isNotBlank(gradStudent.getProgram())) {
            var program = gradStudent.getProgram().length() >= 4 ? gradStudent.getProgram().substring(0, 4) : "";
            if (!isSummer &&
                    (!program.equalsIgnoreCase("SCCP") &&
                        (StringUtils.isBlank(studentProgram) || !studentProgram.equalsIgnoreCase(program))) ||
                        (program.equalsIgnoreCase("SCCP") && StringUtils.isBlank(studentProgram)))
            {
                String errorMessage = DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_ALREADY_GRADUATED.getMessage().formatted(StringEscapeUtils.escapeHtml4(gradStudent.getProgram().replaceAll("-EN","").replaceAll("-PF","")));
                log.debug("StudentProgram-D17: {} for demographicStudentID :: {}", errorMessage, student.getDemographicStudentID());
                errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.PEN, DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_ALREADY_GRADUATED, errorMessage));
            }
        }

        return errors;
    }
}
