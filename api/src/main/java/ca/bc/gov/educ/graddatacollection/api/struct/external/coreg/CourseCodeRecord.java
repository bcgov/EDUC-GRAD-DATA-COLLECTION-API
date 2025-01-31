package ca.bc.gov.educ.graddatacollection.api.struct.external.coreg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CourseCodeRecord {
    private String courseID;
    private String externalCode;
    private String originatingSystem;

    // for testing
    public CourseCodeRecord() {

    }
}
