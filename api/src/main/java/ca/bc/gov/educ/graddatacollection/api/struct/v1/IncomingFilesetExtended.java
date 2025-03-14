package ca.bc.gov.educ.graddatacollection.api.struct.v1;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IncomingFilesetExtended extends IncomingFileset implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<DemographicStudent> demographicStudents;
    private List<CourseStudent> courseStudents;
    private List<AssessmentStudent> assessmentStudents;
}
