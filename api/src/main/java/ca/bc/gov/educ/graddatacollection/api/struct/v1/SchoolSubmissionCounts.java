package ca.bc.gov.educ.graddatacollection.api.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SchoolSubmissionCounts implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    String categoryCode;
    List<SchoolSubmissionCount> schoolSubmissions;
    List<SchoolSubmissionCount> summerSubmissions;
}
