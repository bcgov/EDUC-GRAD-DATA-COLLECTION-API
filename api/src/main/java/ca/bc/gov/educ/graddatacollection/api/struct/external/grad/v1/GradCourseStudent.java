package ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1;

import ca.bc.gov.educ.graddatacollection.api.struct.v1.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(callSuper = true)
public class GradCourseStudent extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String  pen;

    private String submissionModeCode;

    private String isSummerCollection;

    List<GradCourseStudentDetail> studentDetails;

    public List<GradCourseStudentDetail> getStudentDetails() {
        if (studentDetails == null) {
           studentDetails = new ArrayList<>();
        }
        return studentDetails;
    }
}
