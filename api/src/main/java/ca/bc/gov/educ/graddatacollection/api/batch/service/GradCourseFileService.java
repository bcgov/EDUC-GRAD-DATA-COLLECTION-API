package ca.bc.gov.educ.graddatacollection.api.batch.service;

import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.batch.mappers.BatchFileMapper;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.*;
import ca.bc.gov.educ.graddatacollection.api.batch.validation.GradFileValidator;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.DEMBatchFile;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradCollectionStatus;
import ca.bc.gov.educ.graddatacollection.api.mappers.StringMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.service.v1.IncomingFilesetService;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.flatpack.DataSet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static ca.bc.gov.educ.graddatacollection.api.batch.exception.FileError.COURSE_FILE_SESSION_ERROR;
import static ca.bc.gov.educ.graddatacollection.api.batch.exception.FileError.INVALID_TRANSACTION_CODE_STUDENT_DETAILS;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.CourseBatchFile.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.CourseBatchFile.LEGAL_SURNAME;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.CourseBatchFile.LOCAL_STUDENT_ID;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.CourseBatchFile.MINCODE;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.CourseBatchFile.PEN;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.CourseBatchFile.TRANSACTION_CODE;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.CourseBatchFile.VENDOR_ID;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.CourseBatchFile.VERIFICATION_FLAG;
import static lombok.AccessLevel.PRIVATE;

@Service("crs")
@RequiredArgsConstructor
@Slf4j
public class GradCourseFileService implements GradFileBatchProcessor {
    public static final String TRANSACTION_CODE_STUDENT_COURSE_RECORD = "E08";
    private static final BatchFileMapper mapper = BatchFileMapper.mapper;
    @Getter(PRIVATE)
    private final IncomingFilesetRepository incomingFilesetRepository;
    @Getter(PRIVATE)
    private final IncomingFilesetService incomingFilesetService;
    @Getter(PRIVATE)
    private final GradFileValidator gradFileValidator;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public IncomingFilesetEntity populateBatchFileAndLoadData(String guid, DataSet ds, final GradFileUpload fileUpload, final String schoolID, final String districtID) throws FileUnProcessableException {
        val batchFile = new GradStudentCourseFile();
        String incomingSchoolID = schoolID;
        if(districtID == null) {
            this.populateSchoolBatchFile(guid, ds, batchFile, schoolID);
        } else {
            var schoolTombstone =  ds.getRowCount() == 0 ? gradFileValidator.getSchoolFromFileName(guid, fileUpload.getFileName()) : gradFileValidator.getSchoolFromFileMincodeField(guid, ds);
            incomingSchoolID = schoolTombstone.getSchoolId();
            this.populateDistrictBatchFile(guid, ds, batchFile, schoolTombstone, districtID);
            gradFileValidator.validateFileUploadIsNotInProgress(guid, schoolTombstone.getSchoolId());
        }
        return this.processLoadedRecordsInBatchFile(guid, batchFile, fileUpload, incomingSchoolID, districtID);
    }

    public void populateSchoolBatchFile(final String guid, final DataSet ds, final GradStudentCourseFile batchFile, final String schoolID) throws FileUnProcessableException {
        long index = 0;
        while (ds.next()) {
            final var mincode = ds.getString(DEMBatchFile.MINCODE.getName());
            gradFileValidator.validateMincode(guid, schoolID, mincode);
            batchFile.getCourseData().add(this.getStudentCourseDetailRecordFromFile(ds, guid, index));
            index++;
        }
    }

    public void populateDistrictBatchFile(final String guid, final DataSet ds, final GradStudentCourseFile batchFile, SchoolTombstone schoolTombstone, final String districtID) throws FileUnProcessableException {
        long index = 0;
        while (ds.next()) {
            gradFileValidator.validateSchoolIsOpenAndBelongsToDistrict(guid, schoolTombstone, districtID);
            batchFile.getCourseData().add(this.getStudentCourseDetailRecordFromFile(ds, guid, index));
            index++;
        }
    }

    public IncomingFilesetEntity processLoadedRecordsInBatchFile(@NonNull final String guid, @NonNull final GradStudentCourseFile batchFile, @NonNull final GradFileUpload fileUpload, final String schoolID, final String districtID) throws FileUnProcessableException {
        log.debug("Going to persist CRS data for batch :: {}", guid);
        final IncomingFilesetEntity entity = mapper.toIncomingCRSBatchEntity(fileUpload, schoolID); // batch file can be processed further and persisted.
        if(districtID != null) {
            entity.setDistrictID(UUID.fromString(districtID));
        }
        for (final var student : batchFile.getCourseData()) { // set the object so that PK/FK relationship will be auto established by hibernate.
            final var crsStudentEntity = mapper.toCRSStudentEntity(student, entity);
            entity.getCourseStudentEntities().add(crsStudentEntity);
        }

        if(!entity.getCourseStudentEntities().isEmpty()) {
            var hasCurrentOrFutureSession = entity.getCourseStudentEntities().stream().filter(this::validateCourseYearAndMonth).findFirst();
            if(hasCurrentOrFutureSession.isEmpty()) {
                throw new FileUnProcessableException(COURSE_FILE_SESSION_ERROR, guid, GradCollectionStatus.LOAD_FAIL);
            }
        }
        return craftStudentSetAndMarkInitialLoadComplete(entity, schoolID);
    }

    private boolean validateCourseYearAndMonth(CourseStudentEntity courseStudentEntity) {
        if(StringUtils.isNotEmpty(courseStudentEntity.getCourseMonth()) && StringUtils.isNumeric(courseStudentEntity.getCourseMonth())
                && StringUtils.isNotEmpty(courseStudentEntity.getCourseYear()) && StringUtils.isNumeric(courseStudentEntity.getCourseYear())) {
            var courseSessionStart = LocalDate.of(LocalDate.now().getYear(), 9, 1);
            var courseSessionEnd = LocalDate.of(LocalDate.now().getYear() + 1, 8, 31);

            var courseMonth = Integer.parseInt(courseStudentEntity.getCourseMonth());
            var courseYear = Integer.parseInt(courseStudentEntity.getCourseYear());
            var incomingCourseSession = LocalDate.of(courseYear, courseMonth, 1);
            return (incomingCourseSession.isEqual(courseSessionStart) || incomingCourseSession.isAfter(courseSessionStart))
                    && (incomingCourseSession.isEqual(courseSessionEnd) || incomingCourseSession.isBefore(courseSessionEnd));
        }
        return false;
    }

    @Retryable(retryFor = {Exception.class}, backoff = @Backoff(multiplier = 3, delay = 2000))
    public IncomingFilesetEntity craftStudentSetAndMarkInitialLoadComplete(@NonNull final IncomingFilesetEntity incomingFilesetEntity, @NonNull final String schoolID) {
        var fileSetEntity = incomingFilesetRepository.findBySchoolIDAndFilesetStatusCode(UUID.fromString(schoolID), FilesetStatus.LOADED.getCode());
        if(fileSetEntity.isPresent()) {
            var currentFileset = fileSetEntity.get();
            var pairStudentList = compareAndShoreUpStudentList(currentFileset, incomingFilesetEntity);
            currentFileset.setCrsFileUploadDate(incomingFilesetEntity.getCrsFileUploadDate());
            currentFileset.setCrsFileName(incomingFilesetEntity.getCrsFileName());
            currentFileset.setUpdateUser(incomingFilesetEntity.getUpdateUser());
            currentFileset.setUpdateDate(LocalDateTime.now());

            currentFileset.setCrsFileStatusCode(String.valueOf(FilesetStatus.LOADED.getCode()));
            currentFileset.setFilesetStatusCode(String.valueOf(FilesetStatus.LOADED.getCode()));
            currentFileset.getCourseStudentEntities().clear();
            currentFileset.getCourseStudentEntities().addAll(pairStudentList);
            return incomingFilesetService.saveIncomingFilesetRecord(currentFileset);
        } else {
            incomingFilesetEntity.setDemFileStatusCode(String.valueOf(FilesetStatus.NOT_LOADED.getCode()));
            incomingFilesetEntity.setXamFileStatusCode(String.valueOf(FilesetStatus.NOT_LOADED.getCode()));
            incomingFilesetEntity.setCrsFileStatusCode(String.valueOf(FilesetStatus.LOADED.getCode()));
            incomingFilesetEntity.setFilesetStatusCode(String.valueOf(FilesetStatus.LOADED.getCode()));
            return incomingFilesetService.saveIncomingFilesetRecord(incomingFilesetEntity);
        }
    }

    private List<CourseStudentEntity> compareAndShoreUpStudentList(IncomingFilesetEntity currentFileset, IncomingFilesetEntity incomingFileset){
        log.debug("Found {} incoming students in CRS file", incomingFileset.getCourseStudentEntities().size());
        incomingFileset.getCourseStudentEntities().forEach(finalStudent -> finalStudent.setIncomingFileset(currentFileset));
        return incomingFileset.getCourseStudentEntities().stream().toList();
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
