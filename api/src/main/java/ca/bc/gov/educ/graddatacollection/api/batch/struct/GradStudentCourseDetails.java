package ca.bc.gov.educ.graddatacollection.api.batch.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GradStudentCourseDetails {
    private String transactionCode;//    TX_ID CHARACTER 3 0
    private String vendorId;//    VENDOR_ID CHARACTER 1 3
    private String verificationFlag;//    VERI_FLAG CHARACTER 1 4
    private String filler1;//    FILLER1 CHARACTER 5 5
    private String mincode;//    MINCODE CHARACTER 8 10
    private String localId;//    STUD_LOCAL_ID CHARACTER 12 18
    private String pen;//    STUD_NO CHARACTER 10 30
    private String courseCode;//    CRSE_CODE CHARACTER 5 40
    private String courseLevel;//    CRSE_LEVEL CHARACTER 3 45
    private String courseYear;//    CRSE_YEAR CHARACTER 4 48
    private String courseMonth;//    CRSE_MONTH CHARACTER 2 52
    private String interimPercentage;//    INTERIM_PERCENT CHARACTER 3 54
    private String interimLetterGrade;//    INTERIM_LG CHARACTER 2 57
    private String finalPercentage;//    FINAL_PERCENT CHARACTER 3 59
    private String finalLetterGrade;//    FINAL_LG CHARACTER 2 62
    private String courseStatus;//    CRSE_STATUS CHARACTER 1 64
    private String legalSurname;//    STUD_SURNAME CHARACTER 25 65
    private String numCredits;//    NUM_CREDITS CHARACTER 2 90
    private String relatedCourse;//    RELATED_CRSE CHARACTER 5 92
    private String relatedCourseLevel;//    RELATED_LEVEL CHARACTER 3 97
    private String courseDesc;//    CRSE_DESC CHARACTER 40 100
    private String courseType;//    CRSE_TYPE CHARACTER 1 140
    private String courseGradReqt;//    CRSE_GRAD_REQT CHARACTER 1 141
}
