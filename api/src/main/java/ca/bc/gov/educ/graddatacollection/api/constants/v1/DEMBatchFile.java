package ca.bc.gov.educ.graddatacollection.api.constants.v1;

import lombok.Getter;

@Getter
public enum DEMBatchFile {
    TRANSACTION_CODE("transactionCode"),
    VENDOR_ID("vendorID"),
    VERIFICATION_FLAG("verificationFlag"),
    MINCODE("mincode"),
    LOCAL_STUDENT_ID("localId"),
    PEN("pen"),
    LEGAL_SURNAME("legalSurname"),
    LEGAL_GIVEN_NAME("legalGivenName"),
    LEGAL_MIDDLE_NAME("legalMiddleName"),
    ADDRESS_LINE_1("addressLine1"),
    ADDRESS_LINE_2("addressLine2"),
    CITY("city"),
    PROVINCE_CODE("provinceCode"),
    COUNTRY_CODE("countryCode"),
    POSTAL_CODE("postalCode"),
    DOB("dob"),
    GENDER("gender"),
    CITIZENSHIP_STATUS("citizenshipStatus"),
    GRADE("grade"),
    PROGRAM_CODE1("programCode1"),
    PROGRAM_CODE2("programCode2"),
    PROGRAM_CODE3("programCode3"),
    PROGRAM_CODE4("programCode4"),
    PROGRAM_CODE5("programCode5"),
    PROGRAM_CADRE_FLAG("programCadreFlag"),
    STUDENT_STATUS("studentStatus"),
    GRAD_YEAR("gradRequirementYear"),
    SSCP_COMPLETION_DATE("sscpCompletionDate")
    ;
    private final String name;

    DEMBatchFile(String name) {
        this.name = name;
    }
}
