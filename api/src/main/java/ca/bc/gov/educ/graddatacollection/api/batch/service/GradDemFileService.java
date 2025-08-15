package ca.bc.gov.educ.graddatacollection.api.batch.service;

import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.batch.mappers.BatchFileMapper;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradFileBatchProcessor;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentDemogDetails;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentDemogFile;
import ca.bc.gov.educ.graddatacollection.api.batch.validation.GradFileValidator;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradCollectionStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolCategoryCodes;
import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.mappers.StringMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.graddatacollection.api.batch.exception.FileError.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.CourseBatchFile.LOCAL_STUDENT_ID;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.DEMBatchFile.*;
import static lombok.AccessLevel.PRIVATE;

@Service("dem")
@Slf4j
@RequiredArgsConstructor
public class GradDemFileService implements GradFileBatchProcessor {

    @Getter(PRIVATE)
    private final GradFileValidator gradFileValidator;
    private static final Set<String> TRANSACTION_CODE_STUDENT_DEMOG_RECORDS = new HashSet<>(Arrays.asList("D02", "E02"));
    private static final BatchFileMapper mapper = BatchFileMapper.mapper;
    @Getter(PRIVATE)
    private final IncomingFilesetRepository incomingFilesetRepository;
    @Getter(PRIVATE)
    private final ReportingPeriodRepository reportingPeriodRepository;
    @Getter(PRIVATE)
    private final IncomingFilesetService incomingFilesetService;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public IncomingFilesetEntity populateBatchFileAndLoadData(String guid, final DataSet ds, final GradFileUpload fileUpload, final String schoolID, final String districtID) throws FileUnProcessableException {
        val batchFile = new GradStudentDemogFile();
        String incomingSchoolID = schoolID;
        String incomingDistrictID = districtID;
        if(districtID != null) {
          var schoolTombstone = gradFileValidator.getSchoolFromFileMincodeField(guid, ds);
          incomingSchoolID = schoolTombstone.getSchoolId();
          gradFileValidator.validateFileUploadIsNotInProgress(guid, incomingSchoolID);
          this.populateDistrictBatchFile(guid, ds, batchFile, schoolTombstone, districtID);
        } else {
           var schoolTombstone =  gradFileValidator.getSchoolByID(guid, schoolID);
            if(!SchoolCategoryCodes.INDEPENDENTS_AND_OFFSHORE.contains(schoolTombstone.getSchoolCategoryCode())) {
                incomingDistrictID = schoolTombstone.getDistrictId();
            }
           gradFileValidator.validateFileUploadIsNotInProgress(guid, incomingSchoolID);
           this.populateSchoolBatchFile(guid, ds, batchFile, incomingSchoolID);
        }
        return this.processLoadedRecordsInBatchFile(guid, batchFile, fileUpload, incomingSchoolID, incomingDistrictID);
    }

    public void populateSchoolBatchFile(final String guid, final DataSet ds, final GradStudentDemogFile batchFile, final String schoolID) throws FileUnProcessableException {
        long index = 0;
        while (ds.next()) {
            final var mincode = ds.getString(MINCODE.getName());
            gradFileValidator.validateMincode(guid, schoolID, mincode);
            var schoolTombstone =  gradFileValidator.getSchoolByID(guid, schoolID);
            gradFileValidator.validateSchoolIsTranscriptEligibleAndOpen(guid, schoolTombstone, schoolID);
            batchFile.getDemogData().add(this.getStudentDemogDetailRecordFromFile(ds, guid, index));
            index++;
        }
    }

    public void populateDistrictBatchFile(final String guid, final DataSet ds, final GradStudentDemogFile batchFile, SchoolTombstone schoolTombstone, final String districtID) throws FileUnProcessableException {
        long index = 0;
        while (ds.next()) {
            gradFileValidator.validateSchoolIsOpenAndBelongsToDistrict(guid, schoolTombstone, districtID);
            batchFile.getDemogData().add(this.getStudentDemogDetailRecordFromFile(ds, guid, index));
            index++;
        }
    }

    public IncomingFilesetEntity processLoadedRecordsInBatchFile(@NonNull final String guid, @NonNull final GradStudentDemogFile batchFile, @NonNull final GradFileUpload fileUpload, final String schoolID, final String districtID) throws FileUnProcessableException {
        log.debug("Going to persist DEM data for batch :: {}", guid);
        final IncomingFilesetEntity entity = mapper.toIncomingDEMBatchEntity(fileUpload, schoolID); // batch file can be processed further and persisted.
        if(districtID != null) {
            entity.setDistrictID(UUID.fromString(districtID));
        }

        var mincode = batchFile.getDemogData().isEmpty() ? null : batchFile.getDemogData().get(0).getMincode();
        for (final var student : batchFile.getDemogData()) {
            if(mincode != null){
                gradFileValidator.checkForMincodeMismatch(guid, mincode, student.getMincode(), schoolID, districtID);
            }
        }

        var listOfPENs = new HashSet<>();
        String foundDupePEN = null;
        entity.setNumberOfMissingPENs(0);
        for (final var student : batchFile.getDemogData()) {
            if(StringUtils.isNotBlank(student.getPen())) {
                if (!listOfPENs.contains(student.getPen())) {
                    listOfPENs.add(student.getPen());
                } else {
                    foundDupePEN = student.getPen();
                    break;
                }
                final var demStudentEntity = mapper.toDEMStudentEntity(student, entity);
                entity.getDemographicStudentEntities().add(demStudentEntity);
            }else{
                entity.setNumberOfMissingPENs(entity.getNumberOfMissingPENs() + 1);
            }
        }

        if(StringUtils.isNotBlank(foundDupePEN)){
            var dupePENLines = new TreeSet<>();
            for (final var student : batchFile.getDemogData()) {
                if(StringUtils.isNotBlank(student.getPen()) && student.getPen().equals(foundDupePEN)){
                    dupePENLines.add(Integer.parseInt(student.getLineNumber()));
                }
            }
            String lines = dupePENLines.stream().map(Object::toString).collect(Collectors.joining(","));
            throw new FileUnProcessableException(DUPLICATE_PEN_IN_DEM_FILE, guid, GradCollectionStatus.LOAD_FAIL, foundDupePEN, lines.length() == 1 ? "line" : "lines", lines);
        }

        return craftStudentSetAndMarkInitialLoadComplete(entity, schoolID);
    }

    @Retryable(retryFor = {Exception.class}, backoff = @Backoff(multiplier = 3, delay = 2000))
    public IncomingFilesetEntity craftStudentSetAndMarkInitialLoadComplete(@NonNull final IncomingFilesetEntity incomingFilesetEntity, @NonNull final String schoolID) {
        var fileSetEntity = incomingFilesetRepository.findBySchoolIDAndFilesetStatusCode(UUID.fromString(schoolID), FilesetStatus.LOADED.getCode());
        if(fileSetEntity.isPresent()) {
            var currentFileset = fileSetEntity.get();
            var pairStudentList = compareAndShoreUpStudentList(currentFileset, incomingFilesetEntity);
            currentFileset.setDemFileUploadDate(incomingFilesetEntity.getDemFileUploadDate());
            currentFileset.setDemFileName(incomingFilesetEntity.getDemFileName());
            currentFileset.setUpdateUser(incomingFilesetEntity.getUpdateUser());
            currentFileset.setUpdateDate(LocalDateTime.now());
            currentFileset.setNumberOfMissingPENs(incomingFilesetEntity.getNumberOfMissingPENs());
            currentFileset.setFilesetStatusCode(String.valueOf(FilesetStatus.LOADED.getCode()));
            currentFileset.getDemographicStudentEntities().clear();
            currentFileset.getDemographicStudentEntities().addAll(pairStudentList );
            return incomingFilesetService.saveIncomingFilesetRecord(currentFileset);
        } else {
            incomingFilesetEntity.setFilesetStatusCode(String.valueOf(FilesetStatus.LOADED.getCode()));

            ReportingPeriodEntity reportingPeriodEntity = reportingPeriodRepository.findActiveReportingPeriod().orElseThrow(() -> new EntityNotFoundException(ReportingPeriodEntity.class, "currentDate", String.valueOf(LocalDateTime.now())));
            incomingFilesetEntity.setReportingPeriod(reportingPeriodEntity);
            return incomingFilesetService.saveIncomingFilesetRecord(incomingFilesetEntity);
        }
    }

    private List<DemographicStudentEntity> compareAndShoreUpStudentList(IncomingFilesetEntity currentFileset, IncomingFilesetEntity incomingFileset){
        log.debug("Found {} incoming students in DEM File", incomingFileset.getDemographicStudentEntities().size());
        incomingFileset.getDemographicStudentEntities().forEach(finalStudent -> finalStudent.setIncomingFileset(currentFileset));
        return incomingFileset.getDemographicStudentEntities().stream().toList();
    }

    private GradStudentDemogDetails getStudentDemogDetailRecordFromFile(final DataSet ds, final String guid, final long index) throws FileUnProcessableException {
        final var transactionCode = ds.getString(TRANSACTION_CODE.getName());
        if (!TRANSACTION_CODE_STUDENT_DEMOG_RECORDS.contains(transactionCode)) {
            throw new FileUnProcessableException(INVALID_TRANSACTION_CODE_STUDENT_DETAILS_DEM, guid, GradCollectionStatus.LOAD_FAIL, String.valueOf(index + 1), ValidationUtil.getValueOrBlank(ds.getString(LOCAL_STUDENT_ID.getName())));
        }

        return GradStudentDemogDetails.builder()
                .transactionCode(transactionCode)
                .vendorID(ds.getString(VENDOR_ID.getName()))
                .verificationFlag(ds.getString(VERIFICATION_FLAG.getName()))
                .mincode(ds.getString(MINCODE.getName()))
                .localId(StringMapper.trimAndUppercase(ds.getString(LOCAL_STUDENT_ID.getName())))
                .pen(ds.getString(PEN.getName()))
                .legalSurname(StringMapper.trimAndUppercase(ds.getString(LEGAL_SURNAME.getName())))
                .legalGivenName(StringMapper.processGivenName(ds.getString(LEGAL_GIVEN_NAME.getName())))
                .legalMiddleName(StringMapper.trimAndUppercase(ds.getString(LEGAL_MIDDLE_NAME.getName())))
                .addressLine1(StringMapper.trimAndUppercase(ds.getString(ADDRESS_LINE_1.getName())))
                .addressLine2(StringMapper.trimAndUppercase(ds.getString(ADDRESS_LINE_2.getName())))
                .city(StringMapper.trimAndUppercase(ds.getString(CITY.getName())))
                .provinceCode(StringMapper.trimAndUppercase(ds.getString(PROVINCE_CODE.getName())))
                .countryCode(StringMapper.trimAndUppercase(ds.getString(COUNTRY_CODE.getName())))
                .postalCode(StringMapper.trimAndUppercase(ds.getString(POSTAL_CODE.getName())))
                .dob(StringMapper.trimAndUppercase(ds.getString(DOB.getName())))
                .gender(StringMapper.trimAndUppercase(ds.getString(GENDER.getName())))
                .citizenshipStatus(StringMapper.trimAndUppercase(ds.getString(CITIZENSHIP_STATUS.getName())))
                .grade(StringMapper.trimAndUppercase(ds.getString(GRADE.getName())))
                .programCode1(StringMapper.trimAndUppercase(ds.getString(PROGRAM_CODE1.getName())))
                .programCode2(StringMapper.trimAndUppercase(ds.getString(PROGRAM_CODE2.getName())))
                .programCode3(StringMapper.trimAndUppercase(ds.getString(PROGRAM_CODE3.getName())))
                .programCode4(StringMapper.trimAndUppercase(ds.getString(PROGRAM_CODE4.getName())))
                .programCode5(StringMapper.trimAndUppercase(ds.getString(PROGRAM_CODE5.getName())))
                .programCadreFlag(StringMapper.trimAndUppercase(ds.getString(PROGRAM_CADRE_FLAG.getName())))
                .studentStatus(StringMapper.trimAndUppercase(ds.getString(STUDENT_STATUS.getName())))
                .gradRequirementYear(StringMapper.trimAndUppercase(ds.getString(GRAD_YEAR.getName())))
                .sscpCompletionDate(StringMapper.trimAndUppercase(ds.getString(SSCP_COMPLETION_DATE.getName())))
                .lineNumber(Long.toString(index + 1))
                .build();
    }
}
