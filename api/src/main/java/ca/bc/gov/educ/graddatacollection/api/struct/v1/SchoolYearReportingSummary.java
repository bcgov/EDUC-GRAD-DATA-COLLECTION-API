package ca.bc.gov.educ.graddatacollection.api.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SchoolYearReportingSummary implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String categoryOrFacilityType;
    private String schoolsExpected;
    private String schoolsWithSubmissions;
    private String isSection;
}
