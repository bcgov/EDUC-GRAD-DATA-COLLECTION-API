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

import static ca.bc.gov.educ.graddatacollection.api.batch.exception.FileError.INVALID_TRANSACTION_CODE_STUDENT_DETAILS;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.XamBatchFile.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.XamBatchFile.FINAL_PERCENTAGE;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.XamBatchFile.VENDOR_ID;

@Service("stdxam")
@NoArgsConstructor
@Slf4j
public class GradXamFileService implements GradFileBatchProcessor {
    // TOD0: QuesUse E06 for transactions sent to the Ministry. The Ministry will use E07 for transactions sent to the school.

    public static final String TRANSACTION_CODE_STUDENT_XAM_RECORD = "E06";

    @Override
    public void populateBatchFileAndLoadData(String guid, DataSet ds, final GradFileUpload fileUpload, final String schoolID) throws FileUnProcessableException {
        val batchFile = new GradStudentXamFile();
        this.populateBatchFile(guid, ds, batchFile);

    }

    public void populateBatchFile(final String guid, final DataSet ds, final GradStudentXamFile batchFile) throws FileUnProcessableException {
        long index = 0;
        while (ds.next()) {
//            if (ds.isRecordID(HEADER.getName()) || ds.isRecordID(TRAILER.getName())) {
//                this.setHeaderOrTrailer(ds, batchFile);
//                index++;
//                continue;
//            }
            batchFile.getAssessmentData().add(this.getStudentCourseDetailRecordFromFile(ds, guid, index));
            index++;
        }

//        if(batchFile.getBatchFileTrailer() == null) {
//            setManualTrailer(guid, ds, batchFile);
//        }
    }

    private GradStudentAssessmentDetails getStudentCourseDetailRecordFromFile(final DataSet ds, final String guid, final long index) throws FileUnProcessableException {
        final var transactionCode = ds.getString(TRANSACTION_CODE.getName());
        if (!TRANSACTION_CODE_STUDENT_XAM_RECORD.equals(transactionCode)) {
            throw new FileUnProcessableException(INVALID_TRANSACTION_CODE_STUDENT_DETAILS, guid, GradCollectionStatus.LOAD_FAIL, String.valueOf(index), ds.getString(LOCAL_STUDENT_ID.getName()));
        }

        return GradStudentAssessmentDetails.builder()
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
                .interimLetterGrade(StringMapper.trimAndUppercase(ds.getString(INTERIM_LETTER_GRADE.getName())))
                .interimSchoolPercentage(StringMapper.trimAndUppercase(ds.getString(INTERIM_SCHOOL_PERCENTAGE.getName())))
                .finalSchoolPercentage(StringMapper.trimAndUppercase(ds.getString(FINAL_SCHOOL_PERCENTAGE.getName())))
                .examPercentage(StringMapper.trimAndUppercase(ds.getString(EXAM_PERCENTAGE.getName())))
                .finalPercentage(StringMapper.trimAndUppercase(ds.getString(FINAL_PERCENTAGE.getName())))
                .finalLetterGrade(StringMapper.trimAndUppercase(ds.getString(FINAL_LETTER_GRADE.getName())))
                .eExamFlag(StringMapper.trimAndUppercase(ds.getString(E_EXAM_FLAG.getName())))
                .provSpecCase(StringMapper.trimAndUppercase(ds.getString(PROV_SPEC_CASE.getName())))
                .localCourseId(StringMapper.trimAndUppercase(ds.getString(LOCAL_COURSE_ID.getName())))
                .courseStatus(StringMapper.trimAndUppercase(ds.getString(COURSE_STATUS.getName())))
                .legalSurname(StringMapper.trimAndUppercase(ds.getString(LEGAL_SURNAME.getName())))
                .numCredits(StringMapper.trimAndUppercase(ds.getString(NUM_CREDITS.getName())))
                .courseType(StringMapper.trimAndUppercase(ds.getString(COURSE_TYPE.getName())))
                .writeFlag(StringMapper.trimAndUppercase(ds.getString(WRITE_FLAG.getName())))
                .build();
    }
}
