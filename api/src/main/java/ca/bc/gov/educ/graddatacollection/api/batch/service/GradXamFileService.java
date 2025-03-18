package ca.bc.gov.educ.graddatacollection.api.batch.service;

import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.batch.mappers.BatchFileMapper;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradFileBatchProcessor;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentAssessmentDetails;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentXamFile;
import ca.bc.gov.educ.graddatacollection.api.batch.validation.GradFileValidator;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradCollectionStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolCategoryCodes;
import ca.bc.gov.educ.graddatacollection.api.mappers.StringMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.graddatacollection.api.batch.exception.FileError.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.CourseBatchFile.LOCAL_STUDENT_ID;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.DEMBatchFile.MINCODE;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.XamBatchFile.*;
import static lombok.AccessLevel.PRIVATE;

@Service("xam")
@RequiredArgsConstructor
@Slf4j
public class GradXamFileService implements GradFileBatchProcessor {
    private static final Set<String> TRANSACTION_CODE_STUDENT_XAM_RECORDS = new HashSet<>(Arrays.asList("D06", "E06"));
    private static final BatchFileMapper mapper = BatchFileMapper.mapper;
    @Getter(PRIVATE)
    private final IncomingFilesetRepository incomingFilesetRepository;
    @Getter(PRIVATE)
    private final IncomingFilesetService incomingFilesetService;
    @Getter(PRIVATE)
    private final GradFileValidator gradFileValidator;
    @Getter(PRIVATE)
    private final RestUtils restUtils;

    @Override
    public IncomingFilesetEntity populateBatchFileAndLoadData(String guid, DataSet ds, final GradFileUpload fileUpload, final String schoolID, final String districtID) throws FileUnProcessableException {
        val batchFile = new GradStudentXamFile();
        String incomingSchoolID = schoolID;
        String incomingDistrictID = districtID;
        if(districtID == null) {
            var schoolTombstone =  gradFileValidator.getSchoolByID(guid, schoolID);
            if(!SchoolCategoryCodes.INDEPENDENTS_AND_OFFSHORE.contains(schoolTombstone.getSchoolCategoryCode())) {
                incomingDistrictID = schoolTombstone.getDistrictId();
            }
            gradFileValidator.validateFileUploadIsNotInProgress(guid, schoolID);
            this.populateSchoolBatchFile(guid, ds, batchFile, schoolID);
        } else {
            var schoolTombstone =  ds.getRowCount() == 0 ? gradFileValidator.getSchoolFromFileName(guid, fileUpload.getFileName()) : gradFileValidator.getSchoolFromFileMincodeField(guid, ds);
            incomingSchoolID = schoolTombstone.getSchoolId();
            gradFileValidator.validateFileUploadIsNotInProgress(guid, schoolTombstone.getSchoolId());
            this.populateDistrictBatchFile(guid, ds, batchFile, schoolTombstone, districtID);
        }
        return this.processLoadedRecordsInBatchFile(guid, batchFile, fileUpload, incomingSchoolID, incomingDistrictID);
    }

    public void populateSchoolBatchFile(final String guid, final DataSet ds, final GradStudentXamFile batchFile, final String schoolID) throws FileUnProcessableException {
        long index = 0;
        while (ds.next()) {
            final var mincode = ds.getString(MINCODE.getName());
            gradFileValidator.validateMincode(guid, schoolID, mincode);
            var schoolTombstone =  gradFileValidator.getSchoolByID(guid, schoolID);
            gradFileValidator.validateSchoolIsTranscriptEligibleAndOpen(guid, schoolTombstone, schoolID);
            batchFile.getAssessmentData().add(this.getStudentCourseDetailRecordFromFile(ds, guid, index));
            index++;
        }
    }

    public void populateDistrictBatchFile(final String guid, final DataSet ds, final GradStudentXamFile batchFile, SchoolTombstone schoolTombstone, final String districtID) throws FileUnProcessableException {
        long index = 0;
        while (ds.next()) {
            gradFileValidator.validateSchoolIsOpenAndBelongsToDistrict(guid, schoolTombstone, districtID);
            batchFile.getAssessmentData().add(this.getStudentCourseDetailRecordFromFile(ds, guid, index));
            index++;
        }
    }

    public IncomingFilesetEntity processLoadedRecordsInBatchFile(@NonNull final String guid, @NonNull final GradStudentXamFile batchFile, @NonNull final GradFileUpload fileUpload, final String schoolID, final String districtID) throws FileUnProcessableException {
        log.debug("Going to persist XAM data for batch :: {}", guid);
        final IncomingFilesetEntity entity = mapper.toIncomingXAMBatchEntity(fileUpload, schoolID); // batch file can be processed further and persisted.
        if(districtID != null) {
            entity.setDistrictID(UUID.fromString(districtID));
        }

        var blankLineSet = new TreeSet<>();
        var mincode = batchFile.getAssessmentData().isEmpty() ? null : batchFile.getAssessmentData().get(0).getMincode();
        for (final var student : batchFile.getAssessmentData()) {
            if(StringUtils.isBlank(student.getPen())){
                blankLineSet.add(Integer.parseInt(student.getLineNumber()));
            }

            if(mincode != null){
                gradFileValidator.checkForMincodeMismatch(guid, mincode, student.getMincode(), schoolID, districtID);
            }
        }

        if(!blankLineSet.isEmpty()){
            String lines = blankLineSet.stream().map(Object::toString).collect(Collectors.joining(","));
            throw new FileUnProcessableException(BLANK_PEN_IN_XAM_FILE, guid, GradCollectionStatus.LOAD_FAIL, lines.length() == 1 ? "line" : "lines", lines);
        }

        for (final var student : batchFile.getAssessmentData()) { // set the object so that PK/FK relationship will be auto established by hibernate.
            final var assessmentStudentEntity = mapper.toXAMStudentEntity(student, entity);
            if(StringUtils.isNotBlank(student.getExamMincode())) {
                var school = restUtils.getSchoolByMincode(student.getExamMincode());
                school.ifPresent(schoolTombstone -> assessmentStudentEntity.setExamSchoolID(UUID.fromString(schoolTombstone.getSchoolId())));
            }
            entity.getAssessmentStudentEntities().add(assessmentStudentEntity);
        }
        return craftStudentSetAndMarkInitialLoadComplete(entity, schoolID);
    }

    @Retryable(retryFor = {Exception.class}, backoff = @Backoff(multiplier = 3, delay = 2000))
    public IncomingFilesetEntity craftStudentSetAndMarkInitialLoadComplete(@NonNull final IncomingFilesetEntity incomingFilesetEntity, @NonNull final String schoolID) {
        var fileSetEntity = incomingFilesetRepository.findBySchoolIDAndFilesetStatusCode(UUID.fromString(schoolID), FilesetStatus.LOADED.getCode());
        if(fileSetEntity.isPresent()) {
            var currentFileset = fileSetEntity.get();
            var pairStudentList = compareAndShoreUpStudentList(currentFileset, incomingFilesetEntity);
            currentFileset.setXamFileUploadDate(incomingFilesetEntity.getXamFileUploadDate());
            currentFileset.setXamFileName(incomingFilesetEntity.getXamFileName());
            currentFileset.setUpdateUser(incomingFilesetEntity.getUpdateUser());
            currentFileset.setUpdateDate(LocalDateTime.now());

            currentFileset.setFilesetStatusCode(String.valueOf(FilesetStatus.LOADED.getCode()));
            currentFileset.getAssessmentStudentEntities().clear();
            currentFileset.getAssessmentStudentEntities().addAll(pairStudentList);
            return incomingFilesetService.saveIncomingFilesetRecord(currentFileset);
        } else {
            incomingFilesetEntity.setFilesetStatusCode(String.valueOf(FilesetStatus.LOADED.getCode()));
            return incomingFilesetService.saveIncomingFilesetRecord(incomingFilesetEntity);
        }
    }

    private List<AssessmentStudentEntity> compareAndShoreUpStudentList(IncomingFilesetEntity currentFileset, IncomingFilesetEntity incomingFileset){
        log.debug("Found {} current students in XAM file", incomingFileset.getAssessmentStudentEntities().size());
        incomingFileset.getAssessmentStudentEntities().forEach(finalStudent -> finalStudent.setIncomingFileset(currentFileset));
        return incomingFileset.getAssessmentStudentEntities().stream().toList();
    }

    private GradStudentAssessmentDetails getStudentCourseDetailRecordFromFile(final DataSet ds, final String guid, final long index) throws FileUnProcessableException {
        final var transactionCode = ds.getString(TRANSACTION_CODE.getName());
        if (!TRANSACTION_CODE_STUDENT_XAM_RECORDS.contains(transactionCode)) {
            throw new FileUnProcessableException(INVALID_TRANSACTION_CODE_STUDENT_DETAILS_XAM, guid, GradCollectionStatus.LOAD_FAIL, String.valueOf(index + 1), ValidationUtil.getValueOrBlank(ds.getString(LOCAL_STUDENT_ID.getName())));
        }

        return GradStudentAssessmentDetails.builder()
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
                .lineNumber(Long.toString(index + 1))
                .build();
    }
}
