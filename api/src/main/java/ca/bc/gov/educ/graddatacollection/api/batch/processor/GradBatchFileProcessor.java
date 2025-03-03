package ca.bc.gov.educ.graddatacollection.api.batch.processor;

import ca.bc.gov.educ.graddatacollection.api.batch.constants.FileType;
import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileError;
import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradFileBatchProcessor;
import ca.bc.gov.educ.graddatacollection.api.batch.validation.GradFileValidator;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradCollectionStatus;
import ca.bc.gov.educ.graddatacollection.api.exception.ConfirmationRequiredException;
import ca.bc.gov.educ.graddatacollection.api.exception.InvalidPayloadException;
import ca.bc.gov.educ.graddatacollection.api.exception.errors.ApiError;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import ca.bc.gov.educ.graddatacollection.api.util.ValidationUtil;
import com.google.common.base.Stopwatch;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.sf.flatpack.DataSet;
import net.sf.flatpack.DefaultParserFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.FieldError;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static lombok.AccessLevel.PRIVATE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.PRECONDITION_REQUIRED;

@Component
@Slf4j
public class GradBatchFileProcessor {

    private final Map<String, GradFileBatchProcessor> studentDetailsMap;
    @Getter(PRIVATE)
    private final GradFileValidator gradFileValidator;
    public static final String INVALID_PAYLOAD_MSG = "Payload contains invalid data.";
    public static final String GRAD_FILE_UPLOAD = "gradFileUpload";

    public GradBatchFileProcessor(Map<String, GradFileBatchProcessor> studentDetailsMap, GradFileValidator gradFileValidator) {
        this.studentDetailsMap = studentDetailsMap;
        this.gradFileValidator = gradFileValidator;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IncomingFilesetEntity processSchoolBatchFile(GradFileUpload fileUpload, String schoolID) {
        return processFile(fileUpload, schoolID, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IncomingFilesetEntity processDistrictBatchFile(GradFileUpload fileUpload, String districtID) {
        return processFile(fileUpload, null, districtID);
    }

    public IncomingFilesetEntity processFile(GradFileUpload fileUpload, String schoolID, String districtID) {
        val stopwatch = Stopwatch.createStarted();
        final var guid = UUID.randomUUID().toString();
        Optional<Reader> batchFileReaderOptional = Optional.empty();
        try {
            FileType fileDetails = FileType.findByCode(fileUpload.getFileType()).orElseThrow(() -> new FileUnProcessableException(FileError.FILE_NOT_ALLOWED, guid, GradCollectionStatus.LOAD_FAIL));
            final Reader mapperReader = new FileReader(Objects.requireNonNull(this.getClass().getClassLoader().getResource(fileDetails.getMapperFileName())).getFile());
            var byteArrayOutputStream = new ByteArrayInputStream(gradFileValidator.getUploadedFileBytes(guid, fileUpload, fileDetails.getCode()));
            batchFileReaderOptional = Optional.of(new InputStreamReader(byteArrayOutputStream));
            final DataSet ds = DefaultParserFactory.getInstance().newFixedLengthParser(mapperReader, batchFileReaderOptional.get()).setStoreRawDataToDataError(true).setStoreRawDataToDataSet(true).setNullEmptyStrings(true).parse();

            gradFileValidator.validateFileHasCorrectExtension(guid, fileUpload, fileDetails.getAllowedExtensions());
            gradFileValidator.validateFileForFormatAndLength(guid, ds, fileDetails.getDetailedRecordSizeError());
            return studentDetailsMap.get(fileDetails.getCode()).populateBatchFileAndLoadData(guid, ds, fileUpload, schoolID, districtID);
        } catch (final FileUnProcessableException fileUnProcessableException) {
            log.error("File could not be processed exception :: {}", fileUnProcessableException);
            ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message(INVALID_PAYLOAD_MSG).status(BAD_REQUEST).build();
            var validationError = ValidationUtil.createFieldError(GRAD_FILE_UPLOAD, districtID != null ? districtID : schoolID, fileUnProcessableException.getReason());
            List<FieldError> fieldErrorList = new ArrayList<>();
            fieldErrorList.add(validationError);
            error.addValidationErrors(fieldErrorList);
            throw new InvalidPayloadException(error);
        } catch (final ConfirmationRequiredException e) {
            log.warn("Confirmation required while processing the file with guid :: {}", guid);
            throw new ConfirmationRequiredException(new ApiError(PRECONDITION_REQUIRED));
        } catch (final Exception e) {
            log.error("Exception while processing the file with guid :: {} :: Exception :: {}", guid, e);
            ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message(INVALID_PAYLOAD_MSG).status(BAD_REQUEST).build();
            var validationError = ValidationUtil.createFieldError(GRAD_FILE_UPLOAD, districtID != null ? districtID : schoolID , FileError.GENERIC_ERROR_MESSAGE.getMessage());
            List<FieldError> fieldErrorList = new ArrayList<>();
            fieldErrorList.add(validationError);
            error.addValidationErrors(fieldErrorList);
            throw new InvalidPayloadException(error);
        } finally {
            batchFileReaderOptional.ifPresent(this::closeBatchFileReader);
            stopwatch.stop();
            log.info("Time taken for batch processed is :: {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    private void closeBatchFileReader(final Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (final IOException e) {
            log.warn("Error closing the batch file :: ", e);
        }
    }
}
