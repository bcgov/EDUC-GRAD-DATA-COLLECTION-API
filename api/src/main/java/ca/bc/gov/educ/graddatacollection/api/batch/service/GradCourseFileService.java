package ca.bc.gov.educ.graddatacollection.api.batch.service;

import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.*;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradCollectionStatus;
import ca.bc.gov.educ.graddatacollection.api.mappers.StringMapper;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.sf.flatpack.DataSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static ca.bc.gov.educ.graddatacollection.api.batch.exception.FileError.INVALID_TRANSACTION_CODE_STUDENT_DETAILS;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.CourseBatchFile.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.CourseBatchFile.LEGAL_SURNAME;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.CourseBatchFile.LOCAL_STUDENT_ID;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.CourseBatchFile.MINCODE;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.CourseBatchFile.PEN;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.CourseBatchFile.TRANSACTION_CODE;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.CourseBatchFile.VENDOR_ID;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.CourseBatchFile.VERIFICATION_FLAG;

@Service("crs")
@NoArgsConstructor
@Slf4j
public class GradCourseFileService implements GradFileBatchProcessor {
    public static final String TRANSACTION_CODE_STUDENT_COURSE_RECORD = "E08";
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void populateBatchFileAndLoadData(String guid, DataSet ds, final GradFileUpload fileUpload, final String schoolID) throws FileUnProcessableException {
        val batchFile = new GradStudentCourseFile();
        this.populateBatchFile(guid, ds, batchFile);

    }

    public void populateBatchFile(final String guid, final DataSet ds, final GradStudentCourseFile batchFile) throws FileUnProcessableException {
        long index = 0;
        while (ds.next()) {
//            if (ds.isRecordID(HEADER.getName()) || ds.isRecordID(TRAILER.getName())) {
//                this.setHeaderOrTrailer(ds, batchFile);
//                index++;
//                continue;
//            }
            batchFile.getCourseData().add(this.getStudentCourseDetailRecordFromFile(ds, guid, index));
            index++;
        }

//        if(batchFile.getBatchFileTrailer() == null) {
//            setManualTrailer(guid, ds, batchFile);
//        }
    }

    private GradStudentCourseDetails getStudentCourseDetailRecordFromFile(final DataSet ds, final String guid, final long index) throws FileUnProcessableException {
        final var transactionCode = ds.getString(TRANSACTION_CODE.getName());
        if (!TRANSACTION_CODE_STUDENT_COURSE_RECORD.equals(transactionCode)) {
            throw new FileUnProcessableException(INVALID_TRANSACTION_CODE_STUDENT_DETAILS, guid, GradCollectionStatus.LOAD_FAIL, String.valueOf(index), ds.getString(LOCAL_STUDENT_ID.getName()));
        }

        return GradStudentCourseDetails.builder()
                .transactionCode(transactionCode)
                .vendorId(ds.getString(VENDOR_ID.getName()))
                .verificationFlag(ds.getString(VERIFICATION_FLAG.getName()))
                .mincode(ds.getString(MINCODE.getName()))
                .localId(StringMapper.trimAndUppercase(ds.getString(LOCAL_STUDENT_ID.getName())))
                .pen(ds.getString(PEN.getName()))
                .courseCode(StringMapper.trimAndUppercase(ds.getString(COURSE_CODE.getName())))
                .courseLevel(StringMapper.processGivenName(ds.getString(COURSE_LEVEL.getName())))
                .courseYear(StringMapper.trimAndUppercase(ds.getString(COURSE_YEAR.getName())))
                .courseMonth(StringMapper.trimAndUppercase(ds.getString(COURSE_MONTH.getName())))
                .interimPercentage(StringMapper.trimAndUppercase(ds.getString(INTERIM_PERCENTAGE.getName())))
                .interimLetterGrade(StringMapper.trimAndUppercase(ds.getString(INTERIM_LETTER_GRADE.getName())))
                .finalPercentage(StringMapper.trimAndUppercase(ds.getString(FINAL_PERCENTAGE.getName())))
                .finalLetterGrade(StringMapper.trimAndUppercase(ds.getString(FINAL_LETTER_GRADE.getName())))
                .courseStatus(StringMapper.trimAndUppercase(ds.getString(COURSE_STATUS.getName())))
                .legalSurname(StringMapper.trimAndUppercase(ds.getString(LEGAL_SURNAME.getName())))
                .numCredits(StringMapper.trimAndUppercase(ds.getString(NUM_CREDITS.getName())))
                .relatedCourse(StringMapper.trimAndUppercase(ds.getString(RELATED_COURSE.getName())))
                .relatedCourseLevel(StringMapper.trimAndUppercase(ds.getString(RELATED_COURSE_LEVEL.getName())))
                .courseDesc(StringMapper.trimAndUppercase(ds.getString(COURSE_DESC.getName())))
                .courseType(StringMapper.trimAndUppercase(ds.getString(COURSE_TYPE.getName())))
                .courseGradReqt(StringMapper.trimAndUppercase(ds.getString(COURSE_GRAD_REQT.getName())))
                .build();
    }
}
