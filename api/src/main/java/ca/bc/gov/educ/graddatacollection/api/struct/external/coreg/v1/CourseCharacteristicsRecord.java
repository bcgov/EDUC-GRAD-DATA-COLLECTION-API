package ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CourseCharacteristicsRecord {
    private String id;
    private String courseID;
    private String type;
    private String code;
    private String description;

    // for testing
    public CourseCharacteristicsRecord() {}
}
