package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | C03 | ERROR    | Must exist in CoReg - check for both TRAX and MyEdBC                  |   C02       |
 *  |      |          | If it exists in TRAX validation passes                                |              |
 *                      If it does not exist in either use Error msg 1
 *                      If it exists in MyEdBC but not TRAX use Error msg 2
 */
@Component
@Slf4j
@Order(30)
public class CourseCodeRule implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public CourseCodeRule(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }
    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C03: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C03", validationErrorsMap);

        log.debug("In shouldExecute of C03: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of C03 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        String paddedCourseCode = String.format("%-5s", student.getCourseCode());
        var coursesRecord = courseRulesService.getCoregCoursesRecord(studentRuleData, paddedCourseCode + student.getCourseLevel());

        if (coursesRecord == null || coursesRecord.getCourseCode().isEmpty()) {
            log.debug("C03: Error1: The submitted course code does not exist in the ministry course registry. This course cannot be updated. for courseStudentID :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_CODE, CourseStudentValidationIssueTypeCode.COURSE_CODE_COREG_TRAX_INVALID, CourseStudentValidationIssueTypeCode.COURSE_CODE_COREG_TRAX_INVALID.getMessage()));
        } else {
            boolean hasTRAX = coursesRecord.getCourseCode().stream().anyMatch(code -> "39".equals(code.getOriginatingSystem()));
            boolean hasMyEdBC = coursesRecord.getCourseCode().stream().anyMatch(code -> "38".equals(code.getOriginatingSystem()));
            if (!hasTRAX) {
                if (hasMyEdBC) {
                    log.debug("C03: Error2: The submitted course code is a local course code, not a ministry code. This course cannot be updated. for courseStudentID :: {}", student.getCourseStudentID());
                    errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_CODE, CourseStudentValidationIssueTypeCode.COURSE_CODE_COREG_MYEDBC_INVALID, CourseStudentValidationIssueTypeCode.COURSE_CODE_COREG_MYEDBC_INVALID.getMessage()));
                } else {
                    log.debug("C03: Error1: The submitted course code does not exist in the ministry course registry. This course cannot be updated. for courseStudentID :: {}", student.getCourseStudentID());
                    errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_CODE, CourseStudentValidationIssueTypeCode.COURSE_CODE_COREG_TRAX_INVALID, CourseStudentValidationIssueTypeCode.COURSE_CODE_COREG_TRAX_INVALID.getMessage()));
                }
            }
        }

        return errors;
    }

}
