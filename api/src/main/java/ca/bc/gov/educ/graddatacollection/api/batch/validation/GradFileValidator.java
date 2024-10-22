package ca.bc.gov.educ.graddatacollection.api.batch.validation;

import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileError;
import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradCollectionStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolStudentStatus;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.AssessmentStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.CourseStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.DemographicStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import lombok.extern.slf4j.Slf4j;
import net.sf.flatpack.DataError;
import net.sf.flatpack.DataSet;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class GradFileValidator {
    public static final String TOO_LONG = "TOO LONG";

    private final DemographicStudentRepository demographicStudentRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final AssessmentStudentRepository assessmentStudentRepository;
    private final RestUtils restUtils;

    public GradFileValidator(DemographicStudentRepository demographicStudentRepository, CourseStudentRepository courseStudentRepository, AssessmentStudentRepository assessmentStudentRepository, RestUtils restUtils) {
        this.demographicStudentRepository = demographicStudentRepository;
        this.courseStudentRepository = courseStudentRepository;
        this.assessmentStudentRepository = assessmentStudentRepository;
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

    public void validateFileUploadIsNotInProgress(@NonNull final String guid, final String schoolID) throws FileUnProcessableException {
        long inFlightDemCount = demographicStudentRepository.countByIncomingFileset_SchoolIDAndStudentStatusCode(UUID.fromString(schoolID), SchoolStudentStatus.LOADED.getCode());
        long inFlightCrsCount = courseStudentRepository.countByIncomingFileset_SchoolIDAndStudentStatusCode(UUID.fromString(schoolID), SchoolStudentStatus.LOADED.getCode());
        long inFlightXamCount = assessmentStudentRepository.countByIncomingFileset_SchoolIDAndStudentStatusCode(UUID.fromString(schoolID), SchoolStudentStatus.LOADED.getCode());

        if (inFlightDemCount > 0 && inFlightCrsCount > 0 && inFlightXamCount > 0) {
            String schoolMincode = getMincode(guid, schoolID);
            throw new FileUnProcessableException(FileError.CONFLICT_FILE_ALREADY_IN_FLIGHT, guid, GradCollectionStatus.LOAD_FAIL, schoolMincode);
        }
    }
    public void validateMincode(@NonNull final String guid, final String schoolID, String fileMincode) throws FileUnProcessableException {
        String schoolMincode = getMincode(guid, schoolID);
        if (!fileMincode.equals(schoolMincode)) {
            throw new FileUnProcessableException(FileError.MINCODE_MISMATCH, guid, GradCollectionStatus.LOAD_FAIL,schoolMincode);
        }
    }
    public String getMincode(@NonNull final String guid,final String schoolID) throws FileUnProcessableException {
        Optional<SchoolTombstone> schoolOptional = restUtils.getSchoolBySchoolID(schoolID);
        SchoolTombstone school = schoolOptional.orElseThrow(() -> new FileUnProcessableException(FileError.INVALID_SCHOOL, guid, GradCollectionStatus.LOAD_FAIL, schoolID));
        return school.getMincode();
    }
}
