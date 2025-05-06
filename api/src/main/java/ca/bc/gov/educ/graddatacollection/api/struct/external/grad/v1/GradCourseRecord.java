package ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GradCourseRecord {
    private String courseCode;
    private String courseLevel;
    private String courseName;
    private String language;
    private String startDate;
    private String endDate;
    private String workExpFlag;
    private String genericCourseType;
    private String courseID;
    private Integer numCredits;

    // for testing
    public GradCourseRecord() {

    }
}
