package ca.bc.gov.educ.graddatacollection.api.batch.service;

import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.batch.mappers.BatchFileMapper;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.*;
import ca.bc.gov.educ.graddatacollection.api.batch.validation.GradFileValidator;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.DEMBatchFile;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradCollectionStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolStudentStatus;
import ca.bc.gov.educ.graddatacollection.api.mappers.StringMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.service.v1.IncomingFilesetService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import com.nimbusds.jose.util.Pair;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.flatpack.DataSet;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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

@Service("stdcrs")
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
    public IncomingFilesetEntity populateBatchFileAndLoadData(String guid, DataSet ds, final GradFileUpload fileUpload, final String schoolID) throws FileUnProcessableException {
        val batchFile = new GradStudentCourseFile();
        this.populateBatchFile(guid, ds, batchFile, schoolID);
        return this.processLoadedRecordsInBatchFile(guid, batchFile, fileUpload, schoolID);
    }

    public void populateBatchFile(final String guid, final DataSet ds, final GradStudentCourseFile batchFile, final String schoolID) throws FileUnProcessableException {
        long index = 0;
        while (ds.next()) {
            final var mincode = ds.getString(DEMBatchFile.MINCODE.getName());
            gradFileValidator.validateMincode(guid, schoolID, mincode);
            batchFile.getCourseData().add(this.getStudentCourseDetailRecordFromFile(ds, guid, index));
            index++;
        }
    }

    public IncomingFilesetEntity processLoadedRecordsInBatchFile(@NonNull final String guid, @NonNull final GradStudentCourseFile batchFile, @NonNull final GradFileUpload fileUpload, @NonNull final String schoolID) {
        log.debug("Going to persist CRS data for batch :: {}", guid);
        final IncomingFilesetEntity entity = mapper.toIncomingCRSBatchEntity(fileUpload, schoolID); // batch file can be processed further and persisted.
        for (final var student : batchFile.getCourseData()) { // set the object so that PK/FK relationship will be auto established by hibernate.
            final var crsStudentEntity = mapper.toCRSStudentEntity(student, entity);
            entity.getCourseStudentEntities().add(crsStudentEntity);
        }

        return craftStudentSetAndMarkInitialLoadComplete(entity, schoolID);
    }

    public IncomingFilesetEntity craftStudentSetAndMarkInitialLoadComplete(@NonNull final IncomingFilesetEntity incomingFilesetEntity, @NonNull final String schoolID) {
        var fileSetEntity = incomingFilesetRepository.findBySchoolID(UUID.fromString(schoolID));
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
            currentFileset.getCourseStudentEntities().addAll(pairStudentList.getLeft());
            return incomingFilesetService.saveIncomingFilesetRecord(currentFileset);
        } else {
            incomingFilesetEntity.setDemFileStatusCode(String.valueOf(FilesetStatus.NOT_LOADED.getCode()));
            incomingFilesetEntity.setXamFileStatusCode(String.valueOf(FilesetStatus.NOT_LOADED.getCode()));
            incomingFilesetEntity.setCrsFileStatusCode(String.valueOf(FilesetStatus.LOADED.getCode()));
            incomingFilesetEntity.setFilesetStatusCode(String.valueOf(FilesetStatus.LOADED.getCode()));
            return incomingFilesetService.saveIncomingFilesetRecord(incomingFilesetEntity);
        }
    }

    private Pair<List<CourseStudentEntity>, List<UUID>> compareAndShoreUpStudentList(IncomingFilesetEntity currentFileset, IncomingFilesetEntity incomingFileset){
        Map<Integer, CourseStudentEntity> incomingStudentsHashCodes = new HashMap<>();
        Map<Integer,CourseStudentEntity> finalStudentsMap = new HashMap<>();
        List<UUID> removedStudents = new ArrayList<>();
        incomingFileset.getCourseStudentEntities().forEach(student -> incomingStudentsHashCodes.put(student.getUniqueObjectHash(), student));
        log.debug("Found {} current students in CRS file", currentFileset.getDemographicStudentEntities().size());
        log.debug("Found {} incoming students in CRS file", incomingStudentsHashCodes.size());

        currentFileset.getCourseStudentEntities().forEach(currentStudent -> {
            var currentStudentHash = currentStudent.getUniqueObjectHash();
            if(incomingStudentsHashCodes.containsKey(currentStudentHash)  && !currentStudent.getStudentStatusCode().equals(SchoolStudentStatus.DELETED.toString())){
                finalStudentsMap.put(currentStudentHash, currentStudent);
            }else{
                removedStudents.add(currentStudent.getCourseStudentID());
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
        log.debug("Found {} new students for IncomingFilesetID {} in CRS File", newStudCount, currentFileset.getIncomingFilesetID());
        return Pair.of(finalStudentsMap.values().stream().toList(), removedStudents);
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
