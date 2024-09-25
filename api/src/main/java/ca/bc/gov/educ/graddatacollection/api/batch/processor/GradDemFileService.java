package ca.bc.gov.educ.graddatacollection.api.batch.processor;

import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradFileBatchProcessor;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentDemogDetails;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentDemogFile;
import ca.bc.gov.educ.graddatacollection.api.batch.validation.GradFileValidator;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradCollectionStatus;
import ca.bc.gov.educ.graddatacollection.api.mappers.StringMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.sf.flatpack.DataSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static ca.bc.gov.educ.graddatacollection.api.batch.exception.FileError.INVALID_TRANSACTION_CODE_STUDENT_DETAILS;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.DEMBatchFile.*;
import static lombok.AccessLevel.PRIVATE;

@Service("dem")
@Slf4j
public class GradDemFileService implements GradFileBatchProcessor {

    @Getter(PRIVATE)
    private final GradFileValidator gradFileValidator;
    public static final String TRANSACTION_CODE_STUDENT_DEMOG_RECORD = "E02";

    public GradDemFileService(GradFileValidator gradFileValidator) {
        this.gradFileValidator = gradFileValidator;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void populateBatchFileAndLoadData(String guid, final DataSet ds) throws FileUnProcessableException {
        val batchFile = new GradStudentDemogFile();
        this.populateBatchFile(guid, ds, batchFile);
//            gradFileValidator.validateStudentCountForMismatchAndSize(guid, batchFile);

//            return this.processLoadedRecordsInBatchFile(guid, batchFile, guid, sdcSchoolCollectionID, false);
    }

    public void populateBatchFile(final String guid, final DataSet ds, final GradStudentDemogFile batchFile) throws FileUnProcessableException {
        long index = 0;
        while (ds.next()) {
//            if (ds.isRecordID(HEADER.getName()) || ds.isRecordID(TRAILER.getName())) {
//                this.setHeaderOrTrailer(ds, batchFile);
//                index++;
//                continue;
//            }
            batchFile.getDemogData().add(this.getStudentDemogDetailRecordFromFile(ds, guid, index));
            index++;
        }

//        if(batchFile.getBatchFileTrailer() == null) {
//            setManualTrailer(guid, ds, batchFile);
//        }
    }

    private GradStudentDemogDetails getStudentDemogDetailRecordFromFile(final DataSet ds, final String guid, final long index) throws FileUnProcessableException {
        final var transactionCode = ds.getString(TRANSACTION_CODE.getName());
        if (!TRANSACTION_CODE_STUDENT_DEMOG_RECORD.equals(transactionCode)) {
            throw new FileUnProcessableException(INVALID_TRANSACTION_CODE_STUDENT_DETAILS, guid, GradCollectionStatus.LOAD_FAIL, String.valueOf(index), ds.getString(LOCAL_STUDENT_ID.getName()));
        }

        return GradStudentDemogDetails.builder()
                .transactionCode(transactionCode)
                .vendorId(ds.getString(VENDOR_ID.getName()))
                .verificationFlag(ds.getString(VERIFICATION_FLAG.getName()))
                .mincode(ds.getString(MINCODE.getName()))
                .localId(StringMapper.trimAndUppercase(ds.getString(LOCAL_STUDENT_ID.getName())))
                .pen(ds.getString(PEN.getName()))
                .legalSurname(StringMapper.trimAndUppercase(ds.getString(LEGAL_SURNAME.getName())))
                .legalGivenName(StringMapper.processGivenName(ds.getString(LEGAL_GIVEN_NAME.getName())))
                .legalMiddleName(StringMapper.trimAndUppercase(ds.getString(LEGAL_MIDDLE_NAME.getName())))
                .addressLine1(StringMapper.trimAndUppercase(ds.getString(ADDRESS_LINE_1.getName())))
                .addressLine2(StringMapper.trimAndUppercase(ds.getString(ADDRESS_LINE_2.getName())))
                .city(StringMapper.trimAndUppercase(ds.getString(CITY.getName())))
                .provinceCode(StringMapper.trimAndUppercase(ds.getString(PROVINCE_CODE.getName())))
                .countryCode(StringMapper.trimAndUppercase(ds.getString(COUNTRY_CODE.getName())))
                .postalCode(StringMapper.trimAndUppercase(ds.getString(POSTAL_CODE.getName())))
                .dob(StringMapper.trimAndUppercase(ds.getString(DOB.getName())))
                .gender(StringMapper.trimAndUppercase(ds.getString(GENDER.getName())))
                .citizenshipStatus(StringMapper.trimAndUppercase(ds.getString(CITIZENSHIP_STATUS.getName())))
                .grade(StringMapper.trimAndUppercase(ds.getString(GRADE.getName())))
                .programCode1(StringMapper.trimAndUppercase(ds.getString(PROGRAM_CODE1.getName())))
                .programCode2(StringMapper.trimAndUppercase(ds.getString(PROGRAM_CODE2.getName())))
                .programCode3(StringMapper.trimAndUppercase(ds.getString(PROGRAM_CODE3.getName())))
                .programCode4(StringMapper.trimAndUppercase(ds.getString(PROGRAM_CODE4.getName())))
                .programCode5(StringMapper.trimAndUppercase(ds.getString(PROGRAM_CODE5.getName())))
                .programCadreFlag(StringMapper.trimAndUppercase(ds.getString(PROGRAM_CADRE_FLAG.getName())))
                .studentStatus(StringMapper.trimAndUppercase(ds.getString(STUDENT_STATUS.getName())))
                .gradRequirementYear(StringMapper.trimAndUppercase(ds.getString(GRAD_YEAR.getName())))
                .sscpCompletionDate(StringMapper.trimAndUppercase(ds.getString(SSCP_COMPLETION_DATE.getName())))
                .build();
    }
}
