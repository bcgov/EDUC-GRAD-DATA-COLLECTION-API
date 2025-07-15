package ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GradBaseCourse {
    private String courseCode;
    private String courseLevel;
    private String courseName;
    private String language;
    private String startDate;
    private String endDate;
    private String completionEndDate;
    private String genericCourseType;
    private String courseID;
    private Integer numCredits;

    public String getCourseCode() {
        return courseCode != null ? courseCode.trim(): null;
    }
    public String getCourseLevel() {
        return courseLevel != null ? courseLevel.trim(): null;
    }

    // for testing
    public GradBaseCourse() {

    }
}
