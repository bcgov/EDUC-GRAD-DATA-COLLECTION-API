package ca.bc.gov.educ.graddatacollection.api.rules.demographic;

import ca.bc.gov.educ.graddatacollection.api.struct.v1.DemographicStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class DemographicStudentRulesProcessor {
  private final List<DemographicValidationBaseRule> rules;

  @Autowired
  public DemographicStudentRulesProcessor(final List<DemographicValidationBaseRule> rules) {
    this.rules = rules;
  }

  public List<DemographicStudentValidationIssue> processRules(StudentRuleData ruleStudent) {
    final List<DemographicStudentValidationIssue> validationErrorsMap = new ArrayList<>();
    log.debug("Starting validations check for student :: {} with data :: {}", ruleStudent.getDemographicStudentEntity().getDemographicStudentID(), ruleStudent);
    rules.forEach(rule -> {
      if(rule.shouldExecute(ruleStudent, validationErrorsMap)) {
        validationErrorsMap.addAll(rule.executeValidation(ruleStudent));
      }
    });
    return validationErrorsMap;
  }
}
