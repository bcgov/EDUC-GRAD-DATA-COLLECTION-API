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
 *  | C15 | ERROR    | Error if the course code, level, and session exists for the student,   | C03, C07, C08|
 *                      and the course has an associated exam.
 *                      Else, error if the course was examinable for the course code, session,
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

        var studentCourseRecords = courseRulesService.getStudentCourseRecord(studentRuleData, student.getStudentID());
        String externalID = courseRulesService.formatExternalID(courseStudentEntity.getCourseCode(), courseStudentEntity.getCourseLevel());
        String sessionDate = courseStudentEntity.getCourseYear() + "/" + courseStudentEntity.getCourseMonth();

        // First check: Error if course exists with an associated exam
        if (studentCourseRecords != null && !studentCourseRecords.isEmpty()) {
            var matchingRecordWithExam = studentCourseRecords.stream().filter(courseRecord ->
                    { log.debug("Checking record for exam: courseSession={}, gradCourseCode39={}", courseRecord.getCourseSession(), courseRecord.getGradCourseCode39() != null ? courseRecord.getGradCourseCode39().getExternalCode() : "null");
                        boolean courseCodeMatch = (courseRecord.getGradCourseCode39() != null && courseRecord.getGradCourseCode39().getExternalCode().equalsIgnoreCase(externalID));

                        // Normalize session dates for comparison
                        String normalizedRecordSession = courseRecord.getCourseSession().replace("/", "");
                        String normalizedSessionDate = sessionDate.replace("/", "");
                        boolean sessionMatch = normalizedRecordSession.equalsIgnoreCase(normalizedSessionDate);

                        log.debug("Match results for exam check: courseCodeMatch={}, sessionMatch={}, hasExam={} for externalID={}, sessionDate={}",
                                courseCodeMatch, sessionMatch, courseRecord.getCourseExam() != null, externalID, sessionDate);

                        return courseCodeMatch && sessionMatch && courseRecord.getCourseExam() != null;
                    })
                    .findFirst();

            if (matchingRecordWithExam.isPresent()) {
                log.debug("C15: Error - course exists with exam for course student id :: {}", courseStudentEntity.getCourseStudentID());
                errors.add(createValidationIssue(
                        StudentValidationIssueSeverityCode.ERROR,
                        ValidationFieldCode.COURSE_CODE,
                        CourseStudentValidationIssueTypeCode.EXAMINABLE_COURSES_DISCONTINUED,
                        CourseStudentValidationIssueTypeCode.EXAMINABLE_COURSES_DISCONTINUED.getMessage()
                ));
                return errors;
            }
        }

        // Second check: Check if course is examinable and sub validations
        boolean isExaminable = courseRulesService.courseExaminableAtCourseSessionDate(studentRuleData);
        log.debug("in c15 is examinable :: {}", isExaminable);

        if (isExaminable) {
            // Check if course exists for the student
            boolean courseExists = false;
            boolean hasMismatch = false;

            if (studentCourseRecords != null && !studentCourseRecords.isEmpty()) {
                var matchingRecord = studentCourseRecords.stream().filter(courseRecord ->
                        { log.debug("Checking record: courseSession={}, gradCourseCode39={}", courseRecord.getCourseSession(), courseRecord.getGradCourseCode39() != null ? courseRecord.getGradCourseCode39().getExternalCode() : "null");
                            boolean courseCodeMatch = (courseRecord.getGradCourseCode39() != null && courseRecord.getGradCourseCode39().getExternalCode().equalsIgnoreCase(externalID));

                            // Normalize session dates for comparison
                            String normalizedRecordSession = courseRecord.getCourseSession().replace("/", "");
                            String normalizedSessionDate = sessionDate.replace("/", "");
                            boolean sessionMatch = normalizedRecordSession.equalsIgnoreCase(normalizedSessionDate);

                            log.debug("Match results: courseCodeMatch={}, sessionMatch={} for externalID={}, sessionDate={}",
                                    courseCodeMatch, sessionMatch, externalID, sessionDate);

                            return courseCodeMatch && sessionMatch;
                        })
                        .findFirst();

                log.debug("Matching record found: {}", matchingRecord.isPresent());

                if (matchingRecord.isPresent()) {
                    courseExists = true;
                    var courseRecord = matchingRecord.get();

                    // Check for mismatches in Final Percent or Final Letter Grade
                    boolean finalPercentMismatch = false;
                    boolean finalLetterGradeMismatch = false;

                    if (courseStudentEntity.getFinalPercentage() != null && courseRecord.getFinalPercent() != null) {
                        try {
                            Integer submittedPercent = Integer.valueOf(courseStudentEntity.getFinalPercentage());
                            finalPercentMismatch = !submittedPercent.equals(courseRecord.getFinalPercent());
                            log.debug("Final percent mismatch: submitted={}, record={}", submittedPercent, courseRecord.getFinalPercent());
                        } catch (NumberFormatException e) {
                            finalPercentMismatch = true;
                        }
                    } else if (courseStudentEntity.getFinalPercentage() != null || courseRecord.getFinalPercent() != null) {
                        finalPercentMismatch = true;
                        log.debug("Final percent mismatch: finalPercent={}", courseRecord.getFinalPercent());
                    }

                    if (courseStudentEntity.getFinalLetterGrade() != null && courseRecord.getFinalLetterGrade() != null) {
                        finalLetterGradeMismatch = !courseStudentEntity.getFinalLetterGrade().equalsIgnoreCase(courseRecord.getFinalLetterGrade());
                        log.debug("Final letter grade mismatch: submitted={}, record={}", courseStudentEntity.getFinalLetterGrade(), courseRecord.getFinalLetterGrade());
                    } else if (courseStudentEntity.getFinalLetterGrade() != null || courseRecord.getFinalLetterGrade() != null) {
                        finalLetterGradeMismatch = true;
                        log.debug("Final letter grade mismatch: finalLetterGrade={}", courseRecord.getFinalLetterGrade());
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
