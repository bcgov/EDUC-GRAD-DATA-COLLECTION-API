package ca.bc.gov.educ.graddatacollection.api.rules.demographic.ruleset;

import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationFieldCode;
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

import java.util.*;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V114 | ERROR    | Invalid Career Program code / Invalid Optional Program code           |              |
 *  |      |          | Validate against GRAD Career and Optional Programs	                  |              |
 *
 */

@Component
@Slf4j
@Order(1400)
public class V114DemographicProgramCode15 implements DemographicValidationBaseRule {

    private final RestUtils restUtils;

    public V114DemographicProgramCode15(RestUtils restUtils) {
        this.restUtils = restUtils;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of ProgramCode15-V114: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = true;

        log.debug("In shouldExecute of ProgramCode15-V114: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of ProgramCode15-V114 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        List<CareerProgramCode> careerProgramCodes = restUtils.getCareerPrograms();
        List<OptionalProgramCode> optionalProgramCodes = restUtils.getOptionalPrograms();

        Set<String> careerProgramCodeSet = new HashSet<>();
        Set<String> optionalProgramCodeSet = new HashSet<>();

        careerProgramCodes.forEach(c -> careerProgramCodeSet.add(c.getCode()));
        optionalProgramCodes.forEach(o -> optionalProgramCodeSet.add(o.getOptProgramCode()));

        List<String> studentProgramCodes = new ArrayList<>();
        Collections.addAll(studentProgramCodes,
                student.getProgramCode1(),
                student.getProgramCode2(),
                student.getProgramCode3(),
                student.getProgramCode4(),
                student.getProgramCode5()
        );

        for (String programCode : studentProgramCodes) {
            if (StringUtils.isNotEmpty(programCode) &&
                    !careerProgramCodeSet.contains(programCode) && !optionalProgramCodeSet.contains(programCode)) {
                log.debug("ProgramCode15-V114:Invalid Career Program code / Invalid Optional Program code {} for demographicStudentID :: {}", programCode, student.getDemographicStudentID());
                errors.add(createValidationIssue(
                        StudentValidationIssueSeverityCode.ERROR,
                        DemographicStudentValidationFieldCode.STUDENT_PROGRAM_CODE,
                        DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID
                ));
                break;
            }
        }
        return errors;
    }
}
