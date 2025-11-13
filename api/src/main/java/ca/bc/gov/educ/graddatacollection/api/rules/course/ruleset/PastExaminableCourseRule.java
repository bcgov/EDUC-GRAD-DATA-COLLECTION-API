package ca.bc.gov.educ.graddatacollection.api.rules.course.ruleset;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.rules.StudentValidationIssueSeverityCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseValidationBaseRule;
import ca.bc.gov.educ.graddatacollection.api.service.v1.CourseRulesService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.StudentRuleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

        var shouldExecute = isValidationDependencyResolved("C15", validationErrorsMap)
                && StringUtils.isNotBlank(studentRuleData.getCourseStudentEntity().getCourseStatus())
                && !studentRuleData.getCourseStudentEntity().getCourseStatus().equalsIgnoreCase("W");

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
        log.debug("in c15 is examinable :: {}", isExaminable);

        if (isExaminable) {
            var studentCourseRecords = courseRulesService.getStudentCourseRecord(studentRuleData, student.getStudentID());
            String externalID = courseRulesService.formatExternalID(courseStudentEntity.getCourseCode(), courseStudentEntity.getCourseLevel());
            String sessionDate = courseStudentEntity.getCourseYear() + "/" + courseStudentEntity.getCourseMonth();

            // Check if course exists for the student
            boolean courseExists = false;
            boolean hasMismatch = false;

            if (studentCourseRecords != null && !studentCourseRecords.isEmpty()) {
                var matchingRecord = studentCourseRecords.stream().filter(record ->
                        { log.debug("Checking record: courseSession={}, gradCourseCode39={}", record.getCourseSession(), record.getGradCourseCode39() != null ? record.getGradCourseCode39().getExternalCode() : "null");
                            boolean courseCodeMatch = (record.getGradCourseCode39() != null && record.getGradCourseCode39().getExternalCode().equalsIgnoreCase(externalID));

                            // Normalize session dates for comparison
                            String normalizedRecordSession = record.getCourseSession().replaceAll("/", "");
                            String normalizedSessionDate = sessionDate.replaceAll("/", "");
                            boolean sessionMatch = normalizedRecordSession.equalsIgnoreCase(normalizedSessionDate);

                            log.debug("Match results: courseCodeMatch={}, sessionMatch={} for externalID={}, sessionDate={}",
                                    courseCodeMatch, sessionMatch, externalID, sessionDate);

                            return courseCodeMatch && sessionMatch;
                        })
                        .findFirst();

                log.debug("Matching record found: {}", matchingRecord.isPresent());

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
                            log.debug("Final percent mismatch: submitted={}, record={}", submittedPercent, record.getFinalPercent());
                        } catch (NumberFormatException e) {
                            finalPercentMismatch = true;
                        }
                    } else if (courseStudentEntity.getFinalPercentage() != null || record.getFinalPercent() != null) {
                        finalPercentMismatch = true;
                        log.debug("Final percent mismatch: finalPercent={}", record.getFinalPercent());
                    }

                    if (courseStudentEntity.getFinalLetterGrade() != null && record.getFinalLetterGrade() != null) {
                        finalLetterGradeMismatch = !courseStudentEntity.getFinalLetterGrade().equalsIgnoreCase(record.getFinalLetterGrade());
                        log.debug("Final letter grade mismatch: submitted={}, record={}", courseStudentEntity.getFinalLetterGrade(), record.getFinalLetterGrade());
                    } else if (courseStudentEntity.getFinalLetterGrade() != null || record.getFinalLetterGrade() != null) {
                        finalLetterGradeMismatch = true;
                        log.debug("Final letter grade mismatch: finalLetterGrade={}", record.getFinalLetterGrade());
                    }
                    log.debug("Final percent mismatch: {}, Final letter grade mismatch: {}", finalPercentMismatch, finalLetterGradeMismatch);
                    hasMismatch = finalPercentMismatch || finalLetterGradeMismatch;
                    log.debug("Final percent mismatch: hasMismatch={}", hasMismatch);
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
