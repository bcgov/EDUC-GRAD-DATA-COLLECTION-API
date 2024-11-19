package ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GradStudentRecord {

    private String program;
    private String programCompletionDate;
    private String schoolOfRecord;

}
