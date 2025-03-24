package ca.bc.gov.educ.graddatacollection.api.batch.validation;

import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileError;
import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradCollectionStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolCategoryCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolStudentStatus;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import lombok.extern.slf4j.Slf4j;
import net.sf.flatpack.DataError;
import net.sf.flatpack.DataSet;
import net.sf.flatpack.Record;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.graddatacollection.api.batch.exception.FileError.DISTRICT_MINCODE_MISMATCH;
import static ca.bc.gov.educ.graddatacollection.api.batch.exception.FileError.MINCODE_MISMATCH;

@Component
@Slf4j
public class GradFileValidator {
    public static final String TOO_LONG = "TOO LONG";
    public static final String FILE_TYPE = "dem";
    public static final String MINCODE = "mincode";
    private final IncomingFilesetRepository incomingFilesetRepository;
    private final RestUtils restUtils;

    public GradFileValidator(IncomingFilesetRepository incomingFilesetRepository, RestUtils restUtils) {
        this.incomingFilesetRepository = incomingFilesetRepository;
        this.restUtils = restUtils;
    }

    public byte[] getUploadedFileBytes(@NonNull final String guid, final GradFileUpload fileUpload, String fileType) throws FileUnProcessableException {
        byte[] bytes = Base64.getDecoder().decode(fileUpload.getFileContents());
        if (fileType.equalsIgnoreCase(FILE_TYPE) && bytes.length == 0) {
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
            return "Line " + (error.getLineNo()) + " has too many characters.";
        }
        return "Line " + (error.getLineNo()) + " is missing characters.";
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

    public void validateFileUploadIsNotInProgress(@NonNull final String guid, final String schoolID) throws FileUnProcessableException {
        Optional<IncomingFilesetEntity> inProgressFileset = incomingFilesetRepository
                .findBySchoolIDAndFilesetStatusCodeAndDemFileNameIsNotNullAndXamFileNameIsNotNullAndCrsFileNameIsNotNull(UUID.fromString(schoolID), SchoolStudentStatus.LOADED.getCode());
        if (inProgressFileset.isPresent()) {
            String schoolMincode = getMincode(guid, schoolID);
            throw new FileUnProcessableException(FileError.CONFLICT_FILE_ALREADY_IN_FLIGHT, guid, GradCollectionStatus.LOAD_FAIL, schoolMincode);
        }
    }
    public void validateMincode(@NonNull final String guid, final String schoolID, String fileMincode) throws FileUnProcessableException {
        String schoolMincode = getMincode(guid, schoolID);
        if (StringUtils.isBlank(schoolMincode) || StringUtils.isBlank(fileMincode) || !fileMincode.equals(schoolMincode)) {
            throw new FileUnProcessableException(FileError.MINCODE_MISMATCH, guid, GradCollectionStatus.LOAD_FAIL,schoolMincode);
        }
    }
    public String getMincode(@NonNull final String guid,final String schoolID) throws FileUnProcessableException {
        Optional<SchoolTombstone> schoolOptional = restUtils.getSchoolBySchoolID(schoolID);
        SchoolTombstone school = schoolOptional.orElseThrow(() -> new FileUnProcessableException(FileError.INVALID_SCHOOL, guid, GradCollectionStatus.LOAD_FAIL, schoolID));
        return school.getMincode();
    }

    public SchoolTombstone getSchoolFromFileMincodeField(final String guid, final DataSet ds) throws FileUnProcessableException {
        var mincode = getSchoolMincode(guid, ds);
        var school = getSchoolUsingMincode(mincode);
        return school.orElseThrow(() -> new FileUnProcessableException(FileError.INVALID_SCHOOL, guid, GradCollectionStatus.LOAD_FAIL, mincode));
    }

    public SchoolTombstone getSchoolFromFileName(final String guid, String fileName) throws FileUnProcessableException {
        String mincode = fileName.split("\\.")[0];
        var school = getSchoolUsingMincode(mincode);
        return school.orElseThrow(() -> new FileUnProcessableException(FileError.INVALID_FILENAME, guid, GradCollectionStatus.LOAD_FAIL));
    }

    public String getSchoolMincode(final String guid, @NonNull final DataSet ds) throws FileUnProcessableException{
        ds.goTop();
        ds.next();

        Optional<Record> firstRow = ds.getRecord();
        String mincode = firstRow.map(row -> row.getString(MINCODE)).orElse(null);

        if(StringUtils.isBlank(mincode)){
            throw new FileUnProcessableException(FileError.MISSING_MINCODE, guid, GradCollectionStatus.LOAD_FAIL);
        }
        ds.goTop();
        return mincode;
    }

    public Optional<SchoolTombstone> getSchoolUsingMincode(final String mincode) {
        return restUtils.getSchoolByMincode(mincode);
    }

    public void checkForMincodeMismatch(@NonNull final String guid, String fileMincode, String studentMincode, String schoolID, String districtID) throws FileUnProcessableException {
        if (!Objects.equals(fileMincode, studentMincode)) {
            if (districtID != null) {
                throw new FileUnProcessableException(DISTRICT_MINCODE_MISMATCH, guid, GradCollectionStatus.LOAD_FAIL, districtID);
            } else {
                throw new FileUnProcessableException(MINCODE_MISMATCH, guid, GradCollectionStatus.LOAD_FAIL, schoolID);
            }
        }
    }

    public void validateSchoolIsTranscriptEligibleAndOpen(@NonNull final String guid, @NonNull final SchoolTombstone school, final String instituteID) throws FileUnProcessableException {
        if(Boolean.FALSE.equals(school.getCanIssueTranscripts())) {
            throw new FileUnProcessableException(FileError.INVALID_SCHOOL_FOR_UPLOAD, guid, GradCollectionStatus.LOAD_FAIL, instituteID);
        }

        try {
            LocalDateTime currentDate = LocalDateTime.now();
            LocalDateTime openDate = LocalDateTime.parse(school.getOpenedDate());
            LocalDateTime endOfCloseDateGraceWindow = school.getClosedDate() != null ? LocalDateTime.parse(school.getClosedDate()).plusMonths(3) : null;

            if (currentDate.isBefore(openDate)){
                throw new FileUnProcessableException(FileError.SCHOOL_IS_OPENING, guid, GradCollectionStatus.LOAD_FAIL, instituteID);
            }
            if ((endOfCloseDateGraceWindow != null) && currentDate.isAfter(endOfCloseDateGraceWindow)) {
                throw new FileUnProcessableException(FileError.SCHOOL_IS_CLOSED, guid, GradCollectionStatus.LOAD_FAIL, instituteID);
            }
        } catch (DateTimeParseException e) {
            throw new FileUnProcessableException(FileError.INVALID_SCHOOL_DATES, guid, GradCollectionStatus.LOAD_FAIL, instituteID);
        }

    }

    public void validateSchoolIsOpenAndBelongsToDistrict(@NonNull final String guid, @NonNull final SchoolTombstone school, final String districtID) throws FileUnProcessableException {
        String schoolDistrictID = school.getDistrictId();
        if(!school.getSchoolCategoryCode().equalsIgnoreCase(SchoolCategoryCodes.PUBLIC.getCode()) || StringUtils.compare(schoolDistrictID, districtID) != 0) {
            throw new FileUnProcessableException(
                    FileError.SCHOOL_OUTSIDE_OF_DISTRICT,
                    guid,
                    GradCollectionStatus.LOAD_FAIL
            );
        }

        validateSchoolIsTranscriptEligibleAndOpen(guid, school, school.getSchoolId());
    }

    public SchoolTombstone getSchoolByID(@NonNull final String guid,final String schoolID) throws FileUnProcessableException {
        Optional<SchoolTombstone> schoolOptional = restUtils.getSchoolBySchoolID(schoolID);
        return schoolOptional.orElseThrow(() -> new FileUnProcessableException(FileError.INVALID_SCHOOL, guid, GradCollectionStatus.LOAD_FAIL, schoolID));
    }
}
