package ca.bc.gov.educ.graddatacollection.api.rules.course;

import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CourseStudentRulesProcessor {
  private final List<CourseValidationBaseRule> rules;

  @Autowired
  public CourseStudentRulesProcessor(final List<CourseValidationBaseRule> rules) {
    this.rules = rules;
  }

  public List<CourseStudentValidationIssue> processRules(StudentRuleData ruleStudent) {
    final List<CourseStudentValidationIssue> validationErrorsMap = new ArrayList<>();
    log.debug("Starting validations check for student :: {} with data :: {}", ruleStudent.getCourseStudentEntity().getCourseStudentID(), ruleStudent);
    rules.forEach(rule -> {
      if(rule.shouldExecute(ruleStudent, validationErrorsMap)) {
        validationErrorsMap.addAll(rule.executeValidation(ruleStudent));
      }
    });
    return validationErrorsMap;
  }
}
