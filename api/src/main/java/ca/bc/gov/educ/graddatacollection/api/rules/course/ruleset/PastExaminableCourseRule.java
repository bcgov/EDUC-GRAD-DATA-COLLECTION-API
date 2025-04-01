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
 *  | ID   | Severity | Rule                                                                  | Dependent On  |
 *  |------|----------|-----------------------------------------------------------------------|---------------|
 *  | C15 | ERROR    | Courses that were examinable at the time of the course session date   | C03, C07, C08|
 *                      cannot be submitted through the CRS file unless the record exists for
 *                      the same Course Code/Level/Session in Student Course and the Student
 *                      Course record has no associated Student Exam record and the Student
 *                      Course record has a Final Letter Grade
 */
@Component
@Slf4j
@Order(150)
public class PastExaminableCourseRule implements CourseValidationBaseRule {

    private final CourseRulesService courseRulesService;

    public PastExaminableCourseRule(CourseRulesService courseRulesService) {
        this.courseRulesService = courseRulesService;
    }

    @Override
    public boolean shouldExecute(StudentRuleData studentRuleData, List<CourseStudentValidationIssue> validationErrorsMap) {
        log.debug("In shouldExecute of C15: for course {} and courseStudentID :: {}", studentRuleData.getCourseStudentEntity().getCourseStudentID() ,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        var shouldExecute = isValidationDependencyResolved("C15", validationErrorsMap);

        log.debug("In shouldExecute of C15: Condition returned - {} for courseStudentID :: {}" ,
                shouldExecute,
                studentRuleData.getCourseStudentEntity().getCourseStudentID());

        return  shouldExecute;
    }

    @Override
    public List<CourseStudentValidationIssue> executeValidation(StudentRuleData studentRuleData) {
        var student = studentRuleData.getCourseStudentEntity();
        log.debug("In executeValidation of C15 for courseStudentID :: {}", student.getCourseStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        var studentCourseRecord = courseRulesService.getStudentCourseRecord(studentRuleData, student.getPen());

        if (studentCourseRecord != null
            && studentCourseRecord.stream().anyMatch(record ->
                record.getCourseCode().equalsIgnoreCase(student.getCourseCode())
                    && record.getCourseLevel().equalsIgnoreCase(student.getCourseLevel())
                    && record.getSessionDate().equalsIgnoreCase(student.getCourseYear() + "/" + student.getCourseMonth()) // yyyy/mm
                    && record.getCompletedCourseLetterGrade() != null
                    && record.getExamPercent() != null) // this might also change there are a few exam values that could be checked
            // TODO If the course does not already exist, check to see if the course was examinable for the course session provided.  Check the  New GRAD table: Examinable_Courses
            // pending response - where/when is this new table expected
        ) {
            log.debug("C15: Error: Examinable courses were discontinued in 2019/2020. To add a past examinable course to a student record, please submit a GRAD Change Form. for course student id :: {}", student.getCourseStudentID());
            errors.add(createValidationIssue(StudentValidationIssueSeverityCode.ERROR, ValidationFieldCode.COURSE_CODE, CourseStudentValidationIssueTypeCode.EXAMINABLE_COURSES_DISCONTINUED, CourseStudentValidationIssueTypeCode.EXAMINABLE_COURSES_DISCONTINUED.getMessage()));
        }
        return errors;
    }

}
