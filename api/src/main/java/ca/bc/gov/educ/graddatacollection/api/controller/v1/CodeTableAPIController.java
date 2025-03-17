package ca.bc.gov.educ.graddatacollection.api.controller.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.endpoint.v1.CodeTableAPIEndpoint;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GraduationProgramCode;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ValidationIssueFieldCode;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ValidationIssueTypeCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@Slf4j
public class CodeTableAPIController implements CodeTableAPIEndpoint {

    private final RestUtils restUtils;

    public CodeTableAPIController(RestUtils restUtils) {
        this.restUtils = restUtils;
    }

    @Override
    public List<ValidationIssueTypeCode> getValidationIssueTypeCodes() {
        List<ValidationIssueTypeCode> validationIssues = new ArrayList<>();
        Arrays.stream(DemographicStudentValidationIssueTypeCode.values()).forEach(validationCodes -> validationIssues.add(getValidationIssue(validationCodes.getCode(), validationCodes.getMessage())));
        Arrays.stream(CourseStudentValidationIssueTypeCode.values()).forEach(validationCodes -> validationIssues.add(getValidationIssue(validationCodes.getCode(), validationCodes.getMessage())));
        Arrays.stream(AssessmentStudentValidationIssueTypeCode.values()).forEach(validationCodes -> validationIssues.add(getValidationIssue(validationCodes.getCode(), validationCodes.getMessage())));
        return validationIssues;
    }

    @Override
    public List<ValidationIssueFieldCode> getValidationFieldCodes() {
        List<ValidationIssueFieldCode> validationIssues = new ArrayList<>();
        Arrays.stream(ValidationFieldCode.values()).forEach(field -> validationIssues.add(getValidationField(field.getCode(),field.getDescription())));
        return validationIssues;
    }

    @Override
    public List<GraduationProgramCode> getGraduationProgramCodes() {
        return restUtils.getGraduationProgramCodeList();
    }

    private ValidationIssueTypeCode getValidationIssue(String validationIssueCode, String validationIssueDesc){
        ValidationIssueTypeCode issue = new ValidationIssueTypeCode();
        issue.setValidationIssueTypeCode(validationIssueCode);
        issue.setMessage(validationIssueDesc);
        return issue;
    }

    private ValidationIssueFieldCode getValidationField(String code, String description){
        ValidationIssueFieldCode field = new ValidationIssueFieldCode();
        field.setCode(code);
        field.setDescription(description);
        return field;
    }
}
