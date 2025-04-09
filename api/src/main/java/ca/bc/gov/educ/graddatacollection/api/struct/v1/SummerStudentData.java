package ca.bc.gov.educ.graddatacollection.api.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SummerStudentData implements Serializable {
    private static final SummerStudentData EMPTY = new SummerStudentData();
    private static final long serialVersionUID = 1L;
    String schoolCode;
    String pen;
    String legalSurname;
    String legalMiddleName;
    String legalFirstName;
    String dob;
    String studentGrade;
    String course;
    String sessionDate;
    String finalPercent;
    String finalLetterGrade;
    String noOfCredits;

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }
}
