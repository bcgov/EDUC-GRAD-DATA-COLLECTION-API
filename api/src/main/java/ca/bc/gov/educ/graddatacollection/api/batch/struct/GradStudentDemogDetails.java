package ca.bc.gov.educ.graddatacollection.api.batch.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GradStudentDemogDetails {

    private String transactionCode; //TX_ID 3 0
    private String vendorID; //VENDOR_ID 1 3
    private String verificationFlag;//VERI_FLAG 1 4
    private String filler1;//FILLER1 5 5
    private String mincode;//MINCODE 8 10
    private String localId;//STUD_LOCAL_ID 12 18
    private String pen;//STUD_NO (PEN) 10 30
    private String filler2;//FILLER2 9 40
    private String legalSurname;//STUD_SURNAME 25 49
    private String legalGivenName;//STUD_GIVEN 25 74
    private String legalMiddleName;//STUD_MIDDLE 25 99
    private String addressLine1;//ADDRESS1 40 124
    private String addressLine2;//ADDRESS2 40 164
    private String city;//CITY CHARACTER 30 204
    private String provinceCode;//PROV_CODE 2 234
    private String countryCode;//CNTRY_CODE 3 236
    private String postalCode;//POSTAL 7 239
    private String dob;//BIRTHDATE 8 246
    private String gender;//STUD_SEX 1 254
    private String citizenshipStatus;//STUD_CITIZ 1 255
    private String grade;//STUD_GRADE 2 256
    private String programCode1;//PRGM_CODE1 4 258
    private String programCode2;//PRGM_CODE2 4 262
    private String programCode3;//PRGM_CODE3 4 266
    private String programCode4;//PRGM_CODE4 4 270
    private String programCode5;//PRGM_CODE5 4 274
    private String filler3;//FILLER3 5 278
    private String programCadreFlag;//PROGRAM_CADRE_FLAG 1 283
    private String studentStatus;//STUD_STATUS 1 284
    private String gradRequirementYear;//GRAD_REQT_YEAR 4 285
    private String sscpCompletionDate; //SCCP_COMPLETION_DATE 8 289
}
