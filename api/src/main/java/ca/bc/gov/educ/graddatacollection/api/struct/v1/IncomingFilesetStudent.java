package ca.bc.gov.educ.graddatacollection.api.struct.v1;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IncomingFilesetStudent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID incomingFilesetID;
    private String pen;
    private DemographicStudent demographicStudent;
    private List<CourseStudent> courseStudents;
    private List<AssessmentStudent> assessmentStudents;
}
