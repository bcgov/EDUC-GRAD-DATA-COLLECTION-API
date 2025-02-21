package ca.bc.gov.educ.graddatacollection.api.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AssessmentStudent extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String assessmentStudentID;

    @NotNull(message = "incomingFilesetID cannot be null")
    private String incomingFilesetID;

    private String assessmentID;

    private String studentStatusCode;

    private String transactionID;

    private String localID;

    private String vendorID;

    private String pen;

    private String courseCode;

    private String courseYear;

    private String courseMonth;

    private String isElectronicExam;

    private String localCourseID;

    private String provincialSpecialCase;

    private String courseStatus;

    private String lastName;

    private String courseLevel;

    private String interimLetterGrade;

    private String interimSchoolPercent;

    private String finalSchoolPercent;

    private String examPercent;

    private String finalPercent;

    private String finalLetterGrade;

    private String numberOfCredits;

    private String courseType;

    private String toWriteFlag;
}
