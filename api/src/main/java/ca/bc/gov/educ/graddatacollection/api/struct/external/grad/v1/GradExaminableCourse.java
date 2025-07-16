package ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GradExaminableCourse {

    private UUID examinableCourseID;
    private String programYear;
    private String courseCode;
    private String courseLevel;
    private String courseTitle;
    private Integer schoolWeightPercent;
    private Integer examWeightPercent;
    private Double schoolWeightPercentPre1989;
    private Double examWeightPercentPre1989;
    private String examinableStart;
    private String examinableEnd;

    // for testing
    public GradExaminableCourse() {

    }
}
