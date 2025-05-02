package ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequiredCourseRecord {
    private String cacID;
    private String creditValue;
    private String courseID;
    private String startDate;
    private String endDate;

    // for testing
    public RequiredCourseRecord() {}
}
