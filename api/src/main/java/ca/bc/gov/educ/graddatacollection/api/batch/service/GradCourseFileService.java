package ca.bc.gov.educ.graddatacollection.api.batch.service;

import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.batch.mappers.BatchFileMapper;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradFileBatchProcessor;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentCourseDetails;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentCourseFile;
import ca.bc.gov.educ.graddatacollection.api.batch.validation.GradFileValidator;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.DEMBatchFile;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradCollectionStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolCategoryCodes;
import ca.bc.gov.educ.graddatacollection.api.exception.ConfirmationRequiredException;
import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.exception.errors.ApiError;
import ca.bc.gov.educ.graddatacollection.api.mappers.StringMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ReportingPeriodEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ReportingPeriodRepository;
import ca.bc.gov.educ.graddatacollection.api.service.v1.IncomingFilesetService;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import ca.bc.gov.educ.graddatacollection.api.util.ValidationUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
import java.util.stream.Collectors;

import static ca.bc.gov.educ.graddatacollection.api.batch.exception.FileError.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.CourseBatchFile.*;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.http.HttpStatus.PRECONDITION_REQUIRED;

@Service("crs")
@RequiredArgsConstructor
@Slf4j
public class GradCourseFileService implements GradFileBatchProcessor {
    private static final Set<String> TRANSACTION_CODE_STUDENT_COURSE_RECORDS = new HashSet<>(Arrays.asList("D08", "E08"));
    private static final BatchFileMapper mapper = BatchFileMapper.mapper;
    @Getter(PRIVATE)
    private final IncomingFilesetRepository incomingFilesetRepository;
    @Getter(PRIVATE)
    private final ReportingPeriodRepository reportingPeriodRepository;
    @Getter(PRIVATE)
    private final IncomingFilesetService incomingFilesetService;
    @Getter(PRIVATE)
    private final GradFileValidator gradFileValidator;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public IncomingFilesetEntity populateBatchFileAndLoadData(String guid, DataSet ds, final GradFileUpload fileUpload, final String schoolID, final String districtID) throws FileUnProcessableException {
        val batchFile = new GradStudentCourseFile();
        String incomingSchoolID = schoolID;
        String incomingDistrictID = districtID;
        if(districtID == null) {
            var schoolTombstone =  gradFileValidator.getSchoolByID(guid, schoolID);
            if(!SchoolCategoryCodes.INDEPENDENTS_AND_OFFSHORE.contains(schoolTombstone.getSchoolCategoryCode())) {
                incomingDistrictID = schoolTombstone.getDistrictId();
            }
            gradFileValidator.validateFileUploadIsNotInProgress(guid, schoolID);
            this.populateSchoolBatchFile(guid, ds, batchFile, schoolTombstone, schoolID);
        } else {
            var schoolTombstone =  ds.getRowCount() == 0 ? gradFileValidator.getSchoolFromFileName(guid, fileUpload.getFileName()) : gradFileValidator.getSchoolFromFileMincodeField(guid, ds);
            incomingSchoolID = schoolTombstone.getSchoolId();
            gradFileValidator.validateFileUploadIsNotInProgress(guid, incomingSchoolID);
            this.populateDistrictBatchFile(guid, ds, batchFile, schoolTombstone, districtID);
        }
        return this.processLoadedRecordsInBatchFile(guid, batchFile, fileUpload, incomingSchoolID, incomingDistrictID);
    }

    public void populateSchoolBatchFile(final String guid, final DataSet ds, final GradStudentCourseFile batchFile, SchoolTombstone schoolTombstone, final String schoolID) throws FileUnProcessableException {
        long index = 0;
        while (ds.next()) {
            gradFileValidator.validateSchoolIsTranscriptEligibleAndOpen(guid, schoolTombstone, schoolID);
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

        var blankLineSet = new TreeSet<>();
        var mincode = batchFile.getCourseData().isEmpty() ? null : batchFile.getCourseData().get(0).getMincode();

        for (final var student : batchFile.getCourseData()) {
            if(StringUtils.isBlank(student.getPen())){
                blankLineSet.add(Integer.parseInt(student.getLineNumber()));
            }
            if (mincode != null){
                gradFileValidator.checkForMincodeMismatch(guid, mincode, student.getMincode(), schoolID, districtID);
            }
        }

        if(!blankLineSet.isEmpty()){
            String lines = blankLineSet.stream().map(Object::toString).collect(Collectors.joining(","));
            throw new FileUnProcessableException(BLANK_PEN_IN_CRS_FILE, guid, GradCollectionStatus.LOAD_FAIL, lines.length() == 1 ? "line" : "lines", lines);
        }

        var courseLineSet = new HashMap<String, CourseStudentEntity>();
        for (final var student : batchFile.getCourseData()) { // set the object so that PK/FK relationship will be auto established by hibernate.
            final var crsStudentEntity = mapper.toCRSStudentEntity(student, entity);
            courseLineSet.put(student.getLineNumber(), crsStudentEntity);
            entity.getCourseStudentEntities().add(crsStudentEntity);
        }

        boolean foundAnyFutureSessions = false;
        if(!entity.getCourseStudentEntities().isEmpty()) {
            var hasCurrentOrFutureSession = true;

            for(String lineNumberKey: courseLineSet.keySet()){
                hasCurrentOrFutureSession = validateCourseYearAndMonth(courseLineSet.get(lineNumberKey), guid, lineNumberKey);
                if(hasCurrentOrFutureSession){
                    foundAnyFutureSessions = true;
                }
            }
        }

        if(!foundAnyFutureSessions && !fileUpload.isCourseSessionOverride()) {
            throw new ConfirmationRequiredException(new ApiError(PRECONDITION_REQUIRED));
        }
        return craftStudentSetAndMarkInitialLoadComplete(entity, schoolID);
    }

    private boolean validateCourseYearAndMonth(CourseStudentEntity courseStudentEntity, String guid, String lineNumber) throws FileUnProcessableException {
        if(StringUtils.isNotEmpty(courseStudentEntity.getCourseMonth()) && StringUtils.isNumeric(courseStudentEntity.getCourseMonth())
                && StringUtils.isNotEmpty(courseStudentEntity.getCourseYear()) && StringUtils.isNumeric(courseStudentEntity.getCourseYear())) {

            ReportingPeriodEntity reportingPeriodEntity = reportingPeriodRepository.findActiveReportingPeriod().orElseThrow(() -> new EntityNotFoundException(ReportingPeriodEntity.class, "currentDate", String.valueOf(LocalDateTime.now())));
            LocalDate courseSessionStart = LocalDate.now().withDayOfMonth(1);
            LocalDate courseSessionEnd = reportingPeriodEntity.getSummerEnd().toLocalDate();

            LocalDate incomingCourseSession = null;
            try {
                var courseMonth = Integer.parseInt(courseStudentEntity.getCourseMonth());
                var courseYear = Integer.parseInt(courseStudentEntity.getCourseYear());
                incomingCourseSession = LocalDate.of(courseYear, courseMonth, 1);
            } catch (Exception e) {
                return false;
            }
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

            currentFileset.setFilesetStatusCode(String.valueOf(FilesetStatus.LOADED.getCode()));
            currentFileset.getCourseStudentEntities().clear();
            currentFileset.getCourseStudentEntities().addAll(pairStudentList);

            return incomingFilesetService.saveIncomingFilesetRecord(currentFileset);
        } else {
            incomingFilesetEntity.setFilesetStatusCode(String.valueOf(FilesetStatus.LOADED.getCode()));

            ReportingPeriodEntity reportingPeriodEntity = reportingPeriodRepository.findActiveReportingPeriod().orElseThrow(() -> new EntityNotFoundException(ReportingPeriodEntity.class, "currentDate", String.valueOf(LocalDateTime.now())));
            incomingFilesetEntity.setReportingPeriod(reportingPeriodEntity);

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
        if (!TRANSACTION_CODE_STUDENT_COURSE_RECORDS.contains(transactionCode)) {
            throw new FileUnProcessableException(INVALID_TRANSACTION_CODE_STUDENT_DETAILS_CRS, guid, GradCollectionStatus.LOAD_FAIL, String.valueOf(index + 1), ValidationUtil.getValueOrBlank(ds.getString(LOCAL_STUDENT_ID.getName())));
        }

        return GradStudentCourseDetails.builder()
                .transactionCode(transactionCode)
                .vendorID(ds.getString(VENDOR_ID.getName()))
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
                .lineNumber(Long.toString(index + 1))
                .build();
    }
}
