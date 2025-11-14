package ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GradStudentRecordCourses {

    private String courseCode;
    private String courseLevel;
    private String courseSession;
    private String gradReqMet;
}
