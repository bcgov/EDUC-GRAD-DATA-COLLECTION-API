package ca.bc.gov.educ.graddatacollection.api.batch.validation;

import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileError;
import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradCollectionStatus;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import lombok.extern.slf4j.Slf4j;
import net.sf.flatpack.DataError;
import net.sf.flatpack.DataSet;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Optional;

@Component
@Slf4j
public class GradFileValidator {
    public static final String TOO_LONG = "TOO LONG";
    private final RestUtils restUtils;

    public GradFileValidator(RestUtils restUtils) {
        this.restUtils = restUtils;
    }

    public byte[] getUploadedFileBytes(@NonNull final String guid, final GradFileUpload fileUpload) throws FileUnProcessableException {
        byte[] bytes = Base64.getDecoder().decode(fileUpload.getFileContents());
        if (bytes.length == 0) {
            throw new FileUnProcessableException(FileError.EMPTY_FILE, guid, GradCollectionStatus.LOAD_FAIL);
        }
        return bytes;
    }
    public void validateFileForFormatAndLength(@NonNull final String guid, @NonNull final DataSet ds, @NonNull final String lengthError) throws FileUnProcessableException {
        this.processDataSetForRowLengthErrors(guid, ds, lengthError);
    }
    private static boolean isMalformedRowError(DataError error, String lengthError) {
        String description = error.getErrorDesc();
        return description.contains(lengthError);
    }
    private String getMalformedRowMessage(String errorDescription, DataError error, String lengthError) {
        if (errorDescription.contains(lengthError)) {
            return this.getDetailRowLengthIncorrectMessage(error, errorDescription);
        }
        return "The uploaded file contains a malformed row that could not be identified.";
    }

    public void processDataSetForRowLengthErrors(@NonNull final String guid, @NonNull final DataSet ds, @NonNull final String lengthError) throws FileUnProcessableException {
        Optional<DataError> maybeError = ds
                .getErrors()
                .stream()
                .filter(error -> isMalformedRowError(error, lengthError))
                .findFirst();

        // Ignore trailer length errors due to inconsistency in flat files
        if (maybeError.isPresent() && ds.getRowCount() != maybeError.get().getLineNo()) {
            DataError error = maybeError.get();
            String message = this.getMalformedRowMessage(error.getErrorDesc(), error, lengthError);
            throw new FileUnProcessableException(
                    FileError.INVALID_ROW_LENGTH,
                    guid,
                    GradCollectionStatus.LOAD_FAIL,
                    message
            );
        }
    }

    /**
     * Gets detail row length incorrect message.
     * here 1 is subtracted from the line number as line number starts from header record and here header record
     * needs to
     * be  discarded
     *
     * @param errorDescription the {@link DataError} description
     * @param error the error
     * @return the detail row length incorrect message
     */
    public String getDetailRowLengthIncorrectMessage(final DataError error, String errorDescription) {
        if (errorDescription.contains(TOO_LONG)) {
            return "Detail record " + (error.getLineNo() - 1) + " has extraneous characters.";
        }
        return "Detail record " + (error.getLineNo() - 1) + " is missing characters.";
    }

    public void validateFileHasCorrectExtension(@NonNull final String guid, final GradFileUpload fileUpload, String allowedExtension) throws FileUnProcessableException {
        String fileName = fileUpload.getFileName();
        int lastIndex = fileName.lastIndexOf('.');

        if(lastIndex == -1){
            throw new FileUnProcessableException(FileError.NO_FILE_EXTENSION, guid, GradCollectionStatus.LOAD_FAIL);
        }

        String extension = fileName.substring(lastIndex);

        if (!extension.equalsIgnoreCase(allowedExtension)) {
            throw new FileUnProcessableException(FileError.INVALID_FILE_EXTENSION, guid, GradCollectionStatus.LOAD_FAIL);
        }
    }

//    public void validateFileHasCorrectMincode(@NonNull final String guid, @NonNull final DataSet ds, final SdcSchoolCollectionEntity sdcSchoolCollectionEntity) throws FileUnProcessableException {
//        String fileMincode = pluckMincodeFromFile(ds, guid);
//
//        String schoolID = sdcSchoolCollectionEntity.getSchoolID().toString();
//        Optional<SchoolTombstone> school = restUtils.getSchoolBySchoolID(schoolID);
//
//        if (school.isEmpty()) {
//            throw new FileUnProcessableException(FileError.INVALID_SCHOOL, guid, GradCollectionStatus.LOAD_FAIL, fileMincode);
//        }
//
//        String mincode = school.get().getMincode();
//        if (!fileMincode.equals(mincode)) {
//            throw new FileUnProcessableException(
//                    FileError.MINCODE_MISMATCH,
//                    guid,
//                    GradCollectionStatus.LOAD_FAIL,
//                    mincode
//            );
//        }
//
//        ds.goTop();
//    }
//
//    public void validateFileUploadIsNotInProgress(@NonNull final String guid, @NonNull final DataSet ds,final SdcSchoolCollectionEntity sdcSchoolCollectionEntity) throws FileUnProcessableException {
//        long inFlightCount = sdcSchoolCollectionStudentRepository.countBySdcSchoolCollection_SdcSchoolCollectionIDAndSdcSchoolCollectionStudentStatusCode(sdcSchoolCollectionEntity.getSdcSchoolCollectionID(), "LOADED");
//
//        if (inFlightCount > 0) {
//            String fileMincode = pluckMincodeFromFile(ds, guid);
//            throw new FileUnProcessableException(FileError.CONFLICT_FILE_ALREADY_IN_FLIGHT, guid, GradCollectionStatus.LOAD_FAIL, fileMincode);
//        }
//    }
//
//    public void validateSchoolIsOpenAndBelongsToDistrict(@NonNull final String guid, @NonNull final SchoolTombstone school, final String districtID) throws FileUnProcessableException {
//        var currentDate = LocalDateTime.now();
//        LocalDateTime openDate = null;
//        LocalDateTime closeDate = null;
//        try {
//            openDate = LocalDateTime.parse(school.getOpenedDate());
//
//            if (openDate.isAfter(currentDate)){
//                throw new FileUnProcessableException(FileError.SCHOOL_IS_OPENING, guid, GradCollectionStatus.LOAD_FAIL, districtID);
//            }
//
//            if(school.getClosedDate() != null) {
//                closeDate = LocalDateTime.parse(school.getClosedDate());
//            }else{
//                closeDate = LocalDateTime.now().plusDays(5);
//            }
//        } catch (DateTimeParseException e) {
//            throw new FileUnProcessableException(FileError.INVALID_SCHOOL_DATES, guid, GradCollectionStatus.LOAD_FAIL, districtID);
//        }
//
//        if (!(openDate.isBefore(currentDate) && closeDate.isAfter(currentDate))) {
//            throw new FileUnProcessableException(FileError.SCHOOL_IS_CLOSED, guid, GradCollectionStatus.LOAD_FAIL, districtID);
//        }
//
//        String schoolDistrictID = school.getDistrictId();
//
//        if(StringUtils.compare(schoolDistrictID, districtID) != 0) {
//            throw new FileUnProcessableException(
//                    FileError.SCHOOL_OUTSIDE_OF_DISTRICT,
//                    guid,
//                    GradCollectionStatus.LOAD_FAIL
//            );
//        }
//
//    }
}
