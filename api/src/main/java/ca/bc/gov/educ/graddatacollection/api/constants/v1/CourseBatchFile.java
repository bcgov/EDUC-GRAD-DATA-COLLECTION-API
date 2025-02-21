package ca.bc.gov.educ.graddatacollection.api.constants.v1;

import lombok.Getter;

@Getter
public enum CourseBatchFile {
    TRANSACTION_CODE("transactionCode"),
    VENDOR_ID("vendorID"),
    VERIFICATION_FLAG("verificationFlag"),
    MINCODE("mincode"),
    LOCAL_STUDENT_ID("localId"),
    PEN("pen"),
    COURSE_CODE("courseCode"),
    COURSE_LEVEL("courseLevel"),
    COURSE_YEAR("courseYear"),
    COURSE_MONTH("courseMonth"),
    INTERIM_PERCENTAGE("interimPercentage"),
    INTERIM_LETTER_GRADE("interimLetterGrade"),
    FINAL_PERCENTAGE("finalPercentage"),
    FINAL_LETTER_GRADE("finalLetterGrade"),
    COURSE_STATUS("courseStatus"),
    LEGAL_SURNAME("legalSurname"),
    NUM_CREDITS("numCredits"),
    RELATED_COURSE("relatedCourse"),
    RELATED_COURSE_LEVEL("relatedCourseLevel"),
    COURSE_DESC("courseDesc"),
    COURSE_TYPE("courseType"),
    COURSE_GRAD_REQT("courseGradReqt")
    ;
    private final String name;

    CourseBatchFile(String name) {
        this.name = name;
    }
}
