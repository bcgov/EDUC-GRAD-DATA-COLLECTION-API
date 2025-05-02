package ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GradCourseRecord {
    private String courseCode;
    private String courseLevel;
    private String courseName;
    private String language;
    private String startDate;
    private String endDate;
    private String completionEndDate;
    private String workExpFlag;
    private String genericCourseType;
    private String courseID;
    private Integer numCredits;

    // for testing
    public GradCourseRecord() {

    }
}
