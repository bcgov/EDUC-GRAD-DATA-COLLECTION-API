package ca.bc.gov.educ.graddatacollection.api.batch.processor;

import ca.bc.gov.educ.graddatacollection.api.batch.constants.ExcelFileType;
import ca.bc.gov.educ.graddatacollection.api.batch.constants.FileType;
import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileError;
import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradFileExcelProcessor;
import ca.bc.gov.educ.graddatacollection.api.batch.validation.GradFileValidator;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradCollectionStatus;
import ca.bc.gov.educ.graddatacollection.api.exception.InvalidPayloadException;
import ca.bc.gov.educ.graddatacollection.api.exception.errors.ApiError;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SummerStudentData;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SummerStudentDataResponse;
import ca.bc.gov.educ.graddatacollection.api.util.ValidationUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
@Slf4j
public class GradExcelFileProcessor {

    private final Map<String, GradFileExcelProcessor> fileProcessorsMap;
    @Getter(PRIVATE)
    private final GradFileValidator gradFileValidator;
    public static final String INVALID_PAYLOAD_MSG = "Payload contains invalid data.";
    public static final String GRAD_FILE_UPLOAD = "gradFileUpload";

    public GradExcelFileProcessor(final List<GradFileExcelProcessor> fileProcessors, Map<String, GradFileExcelProcessor> fileProcessorsMap, GradFileValidator gradFileValidator) {
        this.fileProcessorsMap = fileProcessorsMap;
                //fileProcessors.stream().collect(Collectors.toMap(GradFileExcelProcessor::getFileType, Function.identity()));
        this.gradFileValidator = gradFileValidator;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SummerStudentDataResponse processSchoolExcelFile(GradFileUpload fileUpload, String schoolID) {
        return processExcelFile(fileUpload, schoolID, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SummerStudentDataResponse processDistrictExcelFile(GradFileUpload fileUpload, String districtID) {
        return processExcelFile(fileUpload, null, districtID);
    }

    public SummerStudentDataResponse processExcelFile(GradFileUpload fileUpload, String schoolID, String districtID) {
        final var guid = UUID.randomUUID().toString();
        try {
            ExcelFileType fileDetails = ExcelFileType.findByCode(fileUpload.getFileType()).orElseThrow(() -> new FileUnProcessableException(FileError.FILE_NOT_ALLOWED, guid, GradCollectionStatus.LOAD_FAIL));
            var fileContent = Base64.getDecoder().decode(fileUpload.getFileContents());
            return fileProcessorsMap.get(fileDetails.getCode()).extractData(guid, fileContent, schoolID, null);
        } catch (final OLE2NotOfficeXmlFileException ole2NotOfficeXmlFileException) {
            log.warn("OLE2NotOfficeXmlFileException during Nominal Roll file processing", ole2NotOfficeXmlFileException);
            ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message(INVALID_PAYLOAD_MSG).status(BAD_REQUEST).build();
            var validationError = ValidationUtil.createFieldError(GRAD_FILE_UPLOAD, districtID != null ? districtID : schoolID, FileError.FILE_ENCRYPTED.getMessage());
            List<FieldError> fieldErrorList = new ArrayList<>();
            fieldErrorList.add(validationError);
            error.addValidationErrors(fieldErrorList);
            throw new InvalidPayloadException(error);
        } catch (final FileUnProcessableException fileUnProcessableException) {
            log.error("File could not be processed exception :: {}", fileUnProcessableException);
            ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message(INVALID_PAYLOAD_MSG).status(BAD_REQUEST).build();
            var validationError = ValidationUtil.createFieldError(GRAD_FILE_UPLOAD, districtID != null ? districtID : schoolID, fileUnProcessableException.getReason());
            List<FieldError> fieldErrorList = new ArrayList<>();
            fieldErrorList.add(validationError);
            error.addValidationErrors(fieldErrorList);
            throw new InvalidPayloadException(error);
        } catch (final Exception e) {
            log.error("Exception while processing the file with guid :: {} :: Exception :: {}", guid, e);
            ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message(INVALID_PAYLOAD_MSG).status(BAD_REQUEST).build();
            var validationError = ValidationUtil.createFieldError(GRAD_FILE_UPLOAD, districtID != null ? districtID : schoolID, FileError.GENERIC_ERROR_MESSAGE.getMessage());
            List<FieldError> fieldErrorList = new ArrayList<>();
            fieldErrorList.add(validationError);
            error.addValidationErrors(fieldErrorList);
            throw new InvalidPayloadException(error);
        }
    }


    }
