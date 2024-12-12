package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.LetterGrade;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *  | ID   | Severity | Rule                                                                  | Dependent On |
 *  |------|----------|-----------------------------------------------------------------------|--------------|
 *  | V215 | ERROR    | Must be a valid letter grade for the course session provided	      | V212         |
 *
 */
@Component
@Slf4j
@Order(150)
public class V215InterimLetterGrade implements CourseValidationBaseRule {

    private final RestUtils restUtils;

    public V215InterimLetterGrade(RestUtils restUtils) {
        this.restUtils = restUtils;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of V215: for courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("V215", validationErrorsMap);

        log.debug("In shouldExecute of V215: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of V215 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        List<LetterGrade> letterGradeList = restUtils.getLetterGrades();

        if (letterGradeList.stream().noneMatch(letterGrade -> letterGradeMatch(letterGrade, student))) {
            log.debug("V215: Error: Invalid letter grade. This course will not be updated for courseStudentID :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, CourseStudentValidationFieldCode.INTERIM_LETTER_GRADE, CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_INVALID));
        }
        return errors;
    }

    private Boolean letterGradeMatch(LetterGrade letterGrade, CourseStudentEntity student) {
        // expiry dates can be null
        LocalDate effectiveDate = ZonedDateTime.parse(letterGrade.getEffectiveDate()).toLocalDate();
        LocalDate expiryDate = letterGrade.getExpiryDate() != null ? ZonedDateTime.parse(letterGrade.getExpiryDate()).toLocalDate() : null;
        LocalDate currentDate = LocalDate.now();

        boolean isWithinDateRange = currentDate.isAfter(effectiveDate) && (expiryDate == null || currentDate.isBefore(expiryDate));

        return isWithinDateRange && letterGrade.getGrade().equalsIgnoreCase(student.getInterimGrade());
    }
}
