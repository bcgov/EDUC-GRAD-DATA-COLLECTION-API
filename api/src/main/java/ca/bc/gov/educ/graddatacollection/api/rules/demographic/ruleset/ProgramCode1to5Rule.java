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
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | D14 | ERROR    | Invalid Career Program code / Invalid Optional Program code           |       D05    |
 *  |      |          | Validate against GRAD Career and Optional Programs	                  |              |
 *
 */

@Component
@Slf4j
@Order(140)
public class ProgramCode1to5Rule implements DemographicValidationBaseRule {

    private final RestUtils restUtils;

    public ProgramCode1to5Rule(RestUtils restUtils) {
        this.restUtils = restUtils;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<DemographicStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of ProgramCode15-D14: for demographicStudentID :: {}", studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        var shouldExecute = isValidationDependencyResolved("D14", validationErrorsMap);

        log.debug("In shouldExecute of ProgramCode15-D14: Condition returned - {} for demographicStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getDemographicStudentEntity().getDemographicStudentID());

        return  shouldExecute;
    }

    @Override
    public List<DemographicStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getDemographicStudentEntity();
        log.debug("In executeValidation of ProgramCode15-D14 for demographicStudentID :: {}", student.getDemographicStudentID());
        final List<DemographicStudentValidationIssue> errors = new ArrayList<>();

        List<CareerProgramCode> careerProgramCodes = restUtils.getCareerProgramCodeList();
        List<OptionalProgramCode> optionalProgramCodes = restUtils.getOptionalProgramCodeList();

        if (StringUtils.isNotBlank(student.getProgramCode1())) {
            var programCode = extractProgramCode(student.getProgramCode1());
            var isValidCareerProgram = careerProgramCodes.stream().anyMatch(careerProgramCode -> careerProgramCode.getCode().equalsIgnoreCase(programCode));
            var isValidOptionalProgram = optionalProgramCodes.stream().anyMatch(optionalProgramCode -> optionalProgramCode.getOptProgramCode().equalsIgnoreCase(programCode));

            if (!isValidCareerProgram && !isValidOptionalProgram) {
                String programCode1ErrorMessage = DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getMessage().formatted(StringEscapeUtils.escapeHtml4(student.getProgramCode1()));
                logDebugStatement(programCode1ErrorMessage, student.getDemographicStudentID());
                errors.add(createValidationIssue(
                        StudentValidationIssueSeverityCode.ERROR,
                        ValidationFieldCode.PROGRAM_CODE_1,
                        DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID,
                        programCode1ErrorMessage
                ));
            }
        }

        if (StringUtils.isNotBlank(student.getProgramCode2())) {
            var programCode = extractProgramCode(student.getProgramCode2());
            var isValidCareerProgram = careerProgramCodes.stream().anyMatch(careerProgramCode -> careerProgramCode.getCode().equalsIgnoreCase(programCode));
            var isValidOptionalProgram = optionalProgramCodes.stream().anyMatch(optionalProgramCode -> optionalProgramCode.getOptProgramCode().equalsIgnoreCase(programCode));

            if (!isValidCareerProgram && !isValidOptionalProgram) {
                String programCode2ErrorMessage = DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getMessage().formatted(StringEscapeUtils.escapeHtml4(student.getProgramCode2()));
                logDebugStatement(programCode2ErrorMessage, student.getDemographicStudentID());
                errors.add(createValidationIssue(
                        StudentValidationIssueSeverityCode.ERROR,
                        ValidationFieldCode.PROGRAM_CODE_2,
                        DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID,
                        programCode2ErrorMessage
                ));
            }
        }

        if (StringUtils.isNotBlank(student.getProgramCode3())) {
            var programCode = extractProgramCode(student.getProgramCode3());
            var isValidCareerProgram = careerProgramCodes.stream().anyMatch(careerProgramCode -> careerProgramCode.getCode().equalsIgnoreCase(programCode));
            var isValidOptionalProgram = optionalProgramCodes.stream().anyMatch(optionalProgramCode -> optionalProgramCode.getOptProgramCode().equalsIgnoreCase(programCode));

            if (!isValidCareerProgram && !isValidOptionalProgram) {
                String programCode3ErrorMessage = DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getMessage().formatted(StringEscapeUtils.escapeHtml4(student.getProgramCode3()));
                logDebugStatement(programCode3ErrorMessage, student.getDemographicStudentID());
                errors.add(createValidationIssue(
                        StudentValidationIssueSeverityCode.ERROR,
                        ValidationFieldCode.PROGRAM_CODE_3,
                        DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID,
                        programCode3ErrorMessage
                ));
            }
        }

        if (StringUtils.isNotBlank(student.getProgramCode4())) {
            var programCode = extractProgramCode(student.getProgramCode4());
            var isValidCareerProgram = careerProgramCodes.stream().anyMatch(careerProgramCode -> careerProgramCode.getCode().equalsIgnoreCase(programCode));
            var isValidOptionalProgram = optionalProgramCodes.stream().anyMatch(optionalProgramCode -> optionalProgramCode.getOptProgramCode().equalsIgnoreCase(programCode));

            if (!isValidCareerProgram && !isValidOptionalProgram) {
                String programCode4ErrorMessage = DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getMessage().formatted(StringEscapeUtils.escapeHtml4(student.getProgramCode4()));
                logDebugStatement(programCode4ErrorMessage, student.getDemographicStudentID());
                errors.add(createValidationIssue(
                        StudentValidationIssueSeverityCode.ERROR,
                        ValidationFieldCode.PROGRAM_CODE_4,
                        DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID,
                        programCode4ErrorMessage
                ));
            }
        }

        if (StringUtils.isNotBlank(student.getProgramCode5())) {
            var programCode = extractProgramCode(student.getProgramCode5());
            var isValidCareerProgram = careerProgramCodes.stream().anyMatch(careerProgramCode -> careerProgramCode.getCode().equalsIgnoreCase(programCode));
            var isValidOptionalProgram = optionalProgramCodes.stream().anyMatch(optionalProgramCode -> optionalProgramCode.getOptProgramCode().equalsIgnoreCase(programCode));

            if(!isValidCareerProgram && !isValidOptionalProgram) {
                String programCode4ErrorMessage = DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID.getMessage().formatted(StringEscapeUtils.escapeHtml4(student.getProgramCode5()));
                logDebugStatement(programCode4ErrorMessage, student.getDemographicStudentID());
                errors.add(createValidationIssue(
                        StudentValidationIssueSeverityCode.ERROR,
                        ValidationFieldCode.PROGRAM_CODE_5,
                        DemographicStudentValidationIssueTypeCode.STUDENT_PROGRAM_CODE_INVALID,
                        programCode4ErrorMessage
                ));
            }
        }

        return errors;
    }

    private String extractProgramCode(String incomingProgramCode) {
        if(incomingProgramCode.length() == 3) {
            return incomingProgramCode.substring(1);
        } else if(incomingProgramCode.length() == 4) {
            return incomingProgramCode.substring(2);
        }
        return incomingProgramCode;
    }

    private void logDebugStatement(String errorMessage, java.util.UUID demographicStudentID) {
        log.debug("ProgramCode15-D14: {} for demographicStudentID :: {}", errorMessage, demographicStudentID);
    }
}
