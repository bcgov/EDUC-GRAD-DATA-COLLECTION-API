package ca.bc.gov.educ.graddatacollection.api.batch.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GradStudentAssessmentDetails {

    private String transactionCode;//    TX_ID 3 0
    private String vendorId;//    VENDOR_ID 1 3
    private String verificationFlag;//    VERI_FLAG 1 4
    private String filler1;//    FILLER1 5 5
    private String mincode;//    MINCODE 8 10
    private String localId;//    STUD_LOCAL_ID 12 18
    private String pen;//    STUD_NO (PEN) 10 30
    private String courseCode;//    CRSE_CODE 5 40
    private String courseLevel;//    CRSE_LEVEL 3 45
    private String courseYear;//    CRSE_YEAR 4 48
    private String courseMonth;//    CRSE_MONTH 2 52
    private String interimLetterGrade;//    INTERIM_LETTER_GRADE 2 54
    private String interimSchoolPercentage;//    INTERIM_SCHOOL_PERCENT 3 56
    private String finalSchoolPercentage;//    FINAL_SCHOOL_PERCENT 3 59
    private String examPercentage;//    EXAM_PERCENT 3 62
    private String finalPercentage;//    FINAL_PERCENT 3 65
    private String finalLetterGrade;//    FINAL_LETTER_GRADE 2 68
    private String eExamFlag;//    E-EXAM FLAG 1 70
    private String provSpecCase;//    PROV_SPEC_CASE 1 71
    private String localCourseId;//    LOCAL_CRSE_ID 20 72
    private String courseStatus;//    CRSE_STATUS 1 92
    private String legalSurname;//    STUD_SURNAME 25 93
    private String numCredits;//    NUM_CREDITS 2 118
    private String courseType;//    CRSE_TYPE 1 120
    private String writeFlag;//    TO_WRITE_FLAG 1 121
    private String examMincode;//    TO_WRITE_FLAG 8 130
}
