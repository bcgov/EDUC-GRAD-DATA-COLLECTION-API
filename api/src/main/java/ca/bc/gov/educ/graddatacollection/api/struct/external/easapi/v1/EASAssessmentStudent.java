package ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1;

import ca.bc.gov.educ.graddatacollection.api.struct.v1.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EASAssessmentStudent extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String assessmentStudentID;

    private String assessmentID;

    private String studentID;

    private String givenName;

    private String surname;

    private String pen;

    private String localID;

    private String gradeAtRegistration;

    private String proficiencyScore;

    private String provincialSpecialCaseCode;

    private String courseStatusCode;

    private String numberOfAttempts;

    private String schoolAtWriteSchoolID;

    private String assessmentCenterSchoolID;

    private String schoolOfRecordSchoolID;

    private String assessmentFormID;

    private String adaptedAssessmentIndicator;

    private String irtScore;

    private String rawScore;

    private String localAssessmentID;

    private String isElectronicAssessment;

    private String assessmentStudentStatusCode;

    private String markingSession;

    private String mcTotal;

    private String oeTotal;

    private String vendorID;
}
