package ca.bc.gov.educ.graddatacollection.api.rules;

import ca.bc.gov.educ.graddatacollection.api.struct.StudentRuleData;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SdcSchoolCollectionStudentValidationIssue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RulesProcessor {
    private final List<ValidationBaseRule> rules;

    @Autowired
    public RulesProcessor(final List<ValidationBaseRule> rules) {
        this.rules = rules;
    }

    public List<SdcSchoolCollectionStudentValidationIssue> processRules(StudentRuleData ruleStudent) {
        final List<SdcSchoolCollectionStudentValidationIssue> validationErrorsMap = new ArrayList<>();
        log.debug("Starting validations check with data :: {}",  ruleStudent);
        rules.forEach(rule -> {
            if(rule.shouldExecute(ruleStudent, validationErrorsMap)) {
                validationErrorsMap.addAll(rule.executeValidation(ruleStudent));
            }
        });
        return validationErrorsMap;
    }
}
