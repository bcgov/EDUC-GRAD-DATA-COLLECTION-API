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
public class GradStudentCourseExam {
    private Integer schoolPercentage;
    private Integer bestSchoolPercentage;
    private Integer bestExamPercentage;
    private String specialCase;

    private UUID id;
    private Integer examPercentage;
    private String toWriteFlag;
    private String wroteFlag;

    // for testing
    public GradStudentCourseExam() {

    }
}
