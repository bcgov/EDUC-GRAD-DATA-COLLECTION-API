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
 *  | C15 | ERROR    | Error if the course was examinable for the course code, session,       | C03, C07, C08|
 *                      and grad program submitted AND either of the following are true:
 *                      1. The course does not exist for the student
 *                      2. The course exists for the student and there is a mismatch between
 *                      the submitted course and the existing course on Final Percent or Final Letter Grade
 *
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
        var courseStudentEntity = studentRuleData.getCourseStudentEntity();
        var student = courseRulesService.getStudentApiStudent(studentRuleData, courseStudentEntity.getPen());

        log.debug("In executeValidation of C15 for courseStudentID :: {}", student.getStudentID());
        final List<CourseStudentValidationIssue> errors = new ArrayList<>();

        boolean isExaminable = courseRulesService.courseExaminableAtCourseSessionDate(studentRuleData);

        if (isExaminable) {
            var studentCourseRecords = courseRulesService.getStudentCourseRecord(studentRuleData, courseStudentEntity.getPen());
            String externalID = courseRulesService.formatExternalID(courseStudentEntity.getCourseCode(), courseStudentEntity.getCourseLevel());
            String sessionDate = courseStudentEntity.getCourseYear() + "/" + courseStudentEntity.getCourseMonth();

            // Check if course exists for the student
            boolean courseExists = false;
            boolean hasMismatch = false;

            if (studentCourseRecords != null && !studentCourseRecords.isEmpty()) {
                var matchingRecord = studentCourseRecords.stream()
                    .filter(record ->
                        (record.getGradCourseCode38() != null && record.getGradCourseCode38().getExternalCode().equalsIgnoreCase(externalID) ||
                         record.getGradCourseCode39() != null && record.getGradCourseCode39().getExternalCode().equalsIgnoreCase(externalID))
                        && record.getCourseSession().equalsIgnoreCase(sessionDate)
                    )
                    .findFirst();

                if (matchingRecord.isPresent()) {
                    courseExists = true;
                    var record = matchingRecord.get();

                    // Check for mismatches in Final Percent or Final Letter Grade
                    boolean finalPercentMismatch = false;
                    boolean finalLetterGradeMismatch = false;

                    if (courseStudentEntity.getFinalPercentage() != null && record.getFinalPercent() != null) {
                        try {
                            Integer submittedPercent = Integer.valueOf(courseStudentEntity.getFinalPercentage());
                            finalPercentMismatch = !submittedPercent.equals(record.getFinalPercent());
                        } catch (NumberFormatException e) {
                            finalPercentMismatch = true;
                        }
                    } else if (courseStudentEntity.getFinalPercentage() != null || record.getFinalPercent() != null) {
                        finalPercentMismatch = true;
                    }

                    if (courseStudentEntity.getFinalLetterGrade() != null && record.getFinalLetterGrade() != null) {
                        finalLetterGradeMismatch = !courseStudentEntity.getFinalLetterGrade().equalsIgnoreCase(record.getFinalLetterGrade());
                    } else if (courseStudentEntity.getFinalLetterGrade() != null || record.getFinalLetterGrade() != null) {
                        finalLetterGradeMismatch = true;
                    }

                    hasMismatch = finalPercentMismatch || finalLetterGradeMismatch;
                }
            }

            // Error if course doesn't exist OR if there's a mismatch
            if (!courseExists || hasMismatch) {
                log.debug("C15: Error for course student id :: {}", courseStudentEntity.getCourseStudentID());
                errors.add(createValidationIssue(
                        StudentValidationIssueSeverityCode.ERROR,
                        ValidationFieldCode.COURSE_CODE,
                        CourseStudentValidationIssueTypeCode.EXAMINABLE_COURSES_DISCONTINUED,
                        CourseStudentValidationIssueTypeCode.EXAMINABLE_COURSES_DISCONTINUED.getMessage()
                ));
            }
        }
        return errors;
    }

}
