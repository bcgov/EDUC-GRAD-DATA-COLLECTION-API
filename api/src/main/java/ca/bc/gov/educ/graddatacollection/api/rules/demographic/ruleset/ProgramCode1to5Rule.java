package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.CareerProgramCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.OptionalProgramCode;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V14 | ERROR    | Invalid Career Program code / Invalid Optional Program code           |       V05    |
 *  |      |          | Validate against GRAD Career and Optional Programs	                  |              |
 *
 */

@Component
@Slf4j
@Order(140)
public class ProgramCode1to5Rule implements DemographicValidationBaseRule {

    private final RestUtils restUtils;

    private static final String DEBUG_MSG = "ProgramCode15-V14:Invalid Career Program code / Invalid Optional Program code {} for demographicStudentID :: {}";

    public ProgramCode1to5Rule(RestUtils restUtils) {
        this.restUtils = restUtils;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of ProgramCode15-V14: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = isValidationDependencyResolved("V14", validationErrorsMap);

        log.debug("In shouldExecute of ProgramCode15-V14: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of ProgramCode15-V14 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        List<CareerProgramCode> careerProgramCodes = restUtils.getCareerProgramCodeList();
        List<OptionalProgramCode> optionalProgramCodes = restUtils.getOptionalProgramCodeList();

        Set<String> careerProgramCodeSet = new HashSet<>();
        Set<String> optionalProgramCodeSet = new HashSet<>();

        careerProgramCodes.forEach(c -> careerProgramCodeSet.add(c.getCode()));
        optionalProgramCodes.forEach(o -> optionalProgramCodeSet.add(o.getOptProgramCode()));

        if (StringUtils.isNotBlank(student.getProgramCode1()) &&
                (!careerProgramCodeSet.contains(student.getProgramCode1()) || !optionalProgramCodeSet.contains(student.getProgramCode1()))) {
            log.debug(DEBUG_MSG, student.getProgramCode1(), student.getDemographicStudentID());
            errors.add(createValidationIssue(
                    StudentValidationIssueSeverityCode.ERROR,
                    ValidationFieldCode.PROGRAM_CODE_1,
                    DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID,
                    DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getMessage()
            ));
        }

        if (StringUtils.isNotBlank(student.getProgramCode2()) &&
                (!careerProgramCodeSet.contains(student.getProgramCode2()) || !optionalProgramCodeSet.contains(student.getProgramCode2()))) {
            log.debug(DEBUG_MSG, student.getProgramCode2(), student.getDemographicStudentID());
            errors.add(createValidationIssue(
                    StudentValidationIssueSeverityCode.ERROR,
                    ValidationFieldCode.PROGRAM_CODE_2,
                    DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID,
                    DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getMessage()
            ));
        }

        if (StringUtils.isNotBlank(student.getProgramCode3()) &&
                (!careerProgramCodeSet.contains(student.getProgramCode3()) || !optionalProgramCodeSet.contains(student.getProgramCode3()))) {
            log.debug(DEBUG_MSG, student.getProgramCode3(), student.getDemographicStudentID());
            errors.add(createValidationIssue(
                    StudentValidationIssueSeverityCode.ERROR,
                    ValidationFieldCode.PROGRAM_CODE_3,
                    DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID,
                    DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getMessage()
            ));
        }

        if (StringUtils.isNotBlank(student.getProgramCode4()) &&
                (!careerProgramCodeSet.contains(student.getProgramCode4()) || !optionalProgramCodeSet.contains(student.getProgramCode4()))) {
            log.debug(DEBUG_MSG, student.getProgramCode4(), student.getDemographicStudentID());
            errors.add(createValidationIssue(
                    StudentValidationIssueSeverityCode.ERROR,
                    ValidationFieldCode.PROGRAM_CODE_4,
                    DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID,
                    DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getMessage()
            ));
        }

        if (StringUtils.isNotBlank(student.getProgramCode5()) &&
                (!careerProgramCodeSet.contains(student.getProgramCode5()) || !optionalProgramCodeSet.contains(student.getProgramCode5()))) {
            log.debug(DEBUG_MSG, student.getProgramCode5(), student.getDemographicStudentID());
            errors.add(createValidationIssue(
                    StudentValidationIssueSeverityCode.ERROR,
                    ValidationFieldCode.PROGRAM_CODE_5,
                    DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID,
                    DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getMessage()
            ));
        }

        return errors;
    }
}
