package ca.bc.gov.educ.graddatacollection.api.batch.service;

import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.batch.mappers.BatchFileMapper;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradFileBatchProcessor;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentAssessmentDetails;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentXamFile;
import ca.bc.gov.educ.graddatacollection.api.batch.validation.GradFileValidator;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradCollectionStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolStudentStatus;
import ca.bc.gov.educ.graddatacollection.api.mappers.StringMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.service.v1.IncomingFilesetService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import com.nimbusds.jose.util.Pair;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.sf.flatpack.DataSet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static ca.bc.gov.educ.graddatacollection.api.batch.exception.FileError.INVALID_TRANSACTION_CODE_STUDENT_DETAILS;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.DEMBatchFile.MINCODE;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.XamBatchFile.*;
import static lombok.AccessLevel.PRIVATE;

@Service("stdxam")
@RequiredArgsConstructor
@Slf4j
public class GradXamFileService implements GradFileBatchProcessor {
    public static final String TRANSACTION_CODE_STUDENT_XAM_RECORD = "E06";
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
    public IncomingFilesetEntity populateBatchFileAndLoadData(String guid, DataSet ds, final GradFileUpload fileUpload, final String schoolID) throws FileUnProcessableException {
        val batchFile = new GradStudentXamFile();
        this.populateBatchFile(guid, ds, batchFile, schoolID);
        return this.processLoadedRecordsInBatchFile(guid, batchFile, fileUpload, schoolID);
    }

    public void populateBatchFile(final String guid, final DataSet ds, final GradStudentXamFile batchFile, final String schoolID) throws FileUnProcessableException {
        long index = 0;
        while (ds.next()) {
            final var mincode = ds.getString(MINCODE.getName());
            gradFileValidator.validateMincode(guid, schoolID, mincode);
            batchFile.getAssessmentData().add(this.getStudentCourseDetailRecordFromFile(ds, guid, index));
            index++;
        }
    }

    public IncomingFilesetEntity processLoadedRecordsInBatchFile(@NonNull final String guid, @NonNull final GradStudentXamFile batchFile, @NonNull final GradFileUpload fileUpload, @NonNull final String schoolID) {
        log.debug("Going to persist XAM data for batch :: {}", guid);
        final IncomingFilesetEntity entity = mapper.toIncomingXAMBatchEntity(fileUpload, schoolID); // batch file can be processed further and persisted.
        for (final var student : batchFile.getAssessmentData()) { // set the object so that PK/FK relationship will be auto established by hibernate.
            final var assessmentStudentEntity = mapper.toXAMStudentEntity(student, entity);
            if(StringUtils.isNotBlank(student.getExamMincode())) {
                var school = restUtils.getSchoolByMincode(student.getExamMincode());
                if(school.isPresent()) {
                    assessmentStudentEntity.setExamSchoolID(UUID.fromString(school.get().getSchoolId()));
                }
            }
            entity.getAssessmentStudentEntities().add(assessmentStudentEntity);
        }
        return craftStudentSetAndMarkInitialLoadComplete(entity, schoolID);
    }
    
    public IncomingFilesetEntity craftStudentSetAndMarkInitialLoadComplete(@NonNull final IncomingFilesetEntity incomingFilesetEntity, @NonNull final String schoolID) {
        var fileSetEntity = incomingFilesetRepository.findBySchoolIDAndFilesetStatusCode(UUID.fromString(schoolID), FilesetStatus.LOADED.getCode());
        if(fileSetEntity.isPresent()) {
            var currentFileset = fileSetEntity.get();
            var pairStudentList = compareAndShoreUpStudentList(currentFileset, incomingFilesetEntity);
            currentFileset.setXamFileUploadDate(incomingFilesetEntity.getXamFileUploadDate());
            currentFileset.setXamFileName(incomingFilesetEntity.getXamFileName());
            currentFileset.setUpdateUser(incomingFilesetEntity.getUpdateUser());
            currentFileset.setUpdateDate(LocalDateTime.now());

            currentFileset.setXamFileStatusCode(String.valueOf(FilesetStatus.LOADED.getCode()));
            currentFileset.setFilesetStatusCode(String.valueOf(FilesetStatus.LOADED.getCode()));
            currentFileset.getAssessmentStudentEntities().clear();
            currentFileset.getAssessmentStudentEntities().addAll(pairStudentList.getLeft());
            return incomingFilesetService.saveIncomingFilesetRecord(currentFileset);
        } else {
            incomingFilesetEntity.setDemFileStatusCode(String.valueOf(FilesetStatus.NOT_LOADED.getCode()));
            incomingFilesetEntity.setCrsFileStatusCode(String.valueOf(FilesetStatus.NOT_LOADED.getCode()));
            incomingFilesetEntity.setXamFileStatusCode(String.valueOf(FilesetStatus.LOADED.getCode()));
            incomingFilesetEntity.setFilesetStatusCode(String.valueOf(FilesetStatus.LOADED.getCode()));
            return incomingFilesetService.saveIncomingFilesetRecord(incomingFilesetEntity);
        }
    }

    private Pair<List<AssessmentStudentEntity>, List<UUID>> compareAndShoreUpStudentList(IncomingFilesetEntity currentFileset, IncomingFilesetEntity incomingFileset){
        Map<Integer, AssessmentStudentEntity> incomingStudentsHashCodes = new HashMap<>();
        Map<Integer,AssessmentStudentEntity> finalStudentsMap = new HashMap<>();
        List<UUID> removedStudents = new ArrayList<>();
        incomingFileset.getAssessmentStudentEntities().forEach(student -> incomingStudentsHashCodes.put(student.getUniqueObjectHash(), student));
        log.debug("Found {} current students in XAM file", currentFileset.getDemographicStudentEntities().size());
        log.debug("Found {} incoming students in XAM file", incomingStudentsHashCodes.size());

        currentFileset.getAssessmentStudentEntities().forEach(currentStudent -> {
            var currentStudentHash = currentStudent.getUniqueObjectHash();
            if(incomingStudentsHashCodes.containsKey(currentStudentHash)  && !currentStudent.getStudentStatusCode().equals(SchoolStudentStatus.DELETED.toString())){
                finalStudentsMap.put(currentStudentHash, currentStudent);
            }else{
                removedStudents.add(currentStudent.getAssessmentStudentID());
            }
        });

        AtomicInteger newStudCount = new AtomicInteger();
        incomingStudentsHashCodes.keySet().forEach(incomingStudentHash -> {
            if(!finalStudentsMap.containsKey(incomingStudentHash)){
                newStudCount.getAndIncrement();
                finalStudentsMap.put(incomingStudentHash, incomingStudentsHashCodes.get(incomingStudentHash));
            }
        });

        finalStudentsMap.values().forEach(finalStudent -> finalStudent.setIncomingFileset(currentFileset));
        log.debug("Found {} new students for IncomingFilesetID {} in XAM File", newStudCount, currentFileset.getIncomingFilesetID());
        return Pair.of(finalStudentsMap.values().stream().toList(), removedStudents);
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
