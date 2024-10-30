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
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(callSuper = true)
public class CourseStudent extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String  courseStudentID;

    @NotNull(message = "incomingFilesetID cannot be null")
    private String incomingFilesetID;

    private String  studentStatusCode;

    private String  transactionID;

    private String  localID;

    private String  pen;

    private String  courseCode;

    private String  courseLevel;

    private String  courseYear;

    private String  courseMonth;

    private String  interimPercentage;

    private String  finalPercentage;

    private String  finalGrade;

    private String  courseStatus;

    private String  lastName;

    private String  numberOfCredits;

    private String  relatedCourse;

    private String  relatedLevel;

    private String  courseDescription;

    private String  courseType;

    private String  courseGraduationRequirement;
}
