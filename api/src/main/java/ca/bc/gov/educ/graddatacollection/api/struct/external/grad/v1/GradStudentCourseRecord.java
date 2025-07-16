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
    private String id; //The property id is used only for update & rendering back on read.
    private String courseID;
    private String courseSession;
    private Integer interimPercent;
    private String interimLetterGrade;
    private Integer finalPercent;
    private String finalLetterGrade;
    private Integer credits;
    private String equivOrChallenge;
    private String fineArtsAppliedSkills;
    private String customizedCourseName;
    private String relatedCourseId;
    private GradStudentCourseExam courseExam;
    private GradCourseCode gradCourseCode;
    //private GradCourseCode courseCode39;

    // for testing
    public GradStudentCourseRecord() {

    }
}
