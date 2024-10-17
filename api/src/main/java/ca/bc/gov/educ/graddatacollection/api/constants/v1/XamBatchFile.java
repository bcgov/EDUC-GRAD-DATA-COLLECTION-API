package ca.bc.gov.educ.graddatacollection.api.constants.v1;

import lombok.Getter;

@Getter
public enum XamBatchFile {
    TRANSACTION_CODE("transactionCode"),
    VENDOR_ID("vendorId"),
    VERIFICATION_FLAG("verificationFlag"),
    MINCODE("mincode"),
    LOCAL_STUDENT_ID("localId"),
    PEN("pen"),
    COURSE_CODE("courseCode"),
    COURSE_LEVEL("courseLevel"),
    COURSE_YEAR("courseYear"),
    COURSE_MONTH("courseMonth"),
    INTERIM_LETTER_GRADE("interimLetterGrade"),
    INTERIM_SCHOOL_PERCENTAGE("interimSchoolPercentage"),
    FINAL_SCHOOL_PERCENTAGE("finalSchoolPercentage"),
    EXAM_PERCENTAGE("examPercentage"),
    FINAL_PERCENTAGE("finalPercentage"),
    FINAL_LETTER_GRADE("finalLetterGrade"),
    E_EXAM_FLAG("eExamFlag"),
    PROV_SPEC_CASE("provSpecCase"),
    LOCAL_COURSE_ID("localCourseId"),
    COURSE_STATUS("courseStatus"),
    LEGAL_SURNAME("legalSurname"),
    NUM_CREDITS("numCredits"),
    COURSE_TYPE("courseType"),
    WRITE_FLAG("writeFlag")
    ;
    private final String name;

    XamBatchFile(String name) {
        this.name = name;
    }
}
