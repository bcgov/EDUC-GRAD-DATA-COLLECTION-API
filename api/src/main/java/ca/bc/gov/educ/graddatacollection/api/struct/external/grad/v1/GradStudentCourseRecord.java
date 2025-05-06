package ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GradStudentCourseRecord {
    private String pen;
    private String courseCode;
    private String courseName; // Course
    private Integer originalCredits; // Course
    private String courseLevel;
    private String sessionDate;
    private String customizedCourseName;
    private String gradReqMet;
    private Double completedCoursePercentage;
    private String completedCourseLetterGrade;
    private Double interimPercent;
    private String interimLetterGrade;
    private Double bestSchoolPercent; // Exam
    private Double bestExamPercent; // Exam
    private Double schoolPercent; // Exam
    private Double examPercent; // Exam
    private String equivOrChallenge;
    private String fineArtsAppliedSkills;
    private String metLitNumRequirement;
    private Integer credits;
    private Integer creditsUsedForGrad;
    private String relatedCourse;
    private String relatedCourseName;
    private String relatedLevel;
    private String hasRelatedCourse;
    private String genericCourseType; // Course
    private String language; // Course
    private String workExpFlag; // Course
    private String specialCase; // Exam
    private String toWriteFlag; // Exam
    private String provExamCourse;
    private boolean isNotCompleted;
    private boolean isFailed;
    private boolean isDuplicate;
    private GradCourseRecord courseDetails;

    // for testing
    public GradStudentCourseRecord() {

    }
}
