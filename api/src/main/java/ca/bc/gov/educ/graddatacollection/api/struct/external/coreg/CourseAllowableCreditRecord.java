package ca.bc.gov.educ.graddatacollection.api.struct.external.coreg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CourseAllowableCreditRecord {
    private String cacID;
    private String creditValue;
    private String courseID;
    private String startDate;
    private String endDate;

    // for testing
    public CourseAllowableCreditRecord(){
    }
}
