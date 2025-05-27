package ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1;

import ca.bc.gov.educ.graddatacollection.api.struct.v1.BaseRequest;
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
public class GradCourseStudent extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String  pen;

    private String submissionModeCode;

    private String  courseCode;

    private String  courseLevel;

    private String  courseYear;

    private String  courseMonth;

    private String  interimPercentage;

    private String  interimLetterGrade;

    private String  finalPercentage;

    private String  finalLetterGrade;

    private String  courseStatus;

    private String  numberOfCredits;

    private String  relatedCourse;

    private String  relatedLevel;

    private String  courseDescription;

    private String  courseType;

    private String  courseGraduationRequirement;

    private String isSummerCollection;
}
