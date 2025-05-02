package ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseCharacteristicsRecord {
    private String id;
    private String courseID;
    private String type;
    private String code;
    private String description;

    // for testing
    public CourseCharacteristicsRecord() {}
}
