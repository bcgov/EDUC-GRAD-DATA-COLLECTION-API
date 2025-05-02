package ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GradStudentRecord {

    private String studentID;
    private String exception;
    private String program;
    private String programCompletionDate;
    private String schoolOfRecordId;
    private String studentStatusCode;
    private String graduated;

    public GradStudentRecord() {
        //for testing
    }
}
