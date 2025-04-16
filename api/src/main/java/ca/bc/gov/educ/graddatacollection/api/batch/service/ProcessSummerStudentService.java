package ca.bc.gov.educ.graddatacollection.api.batch.service;

import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileError;
import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.batch.mappers.BatchFileMapper;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentCourseDetails;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentCourseFile;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentDemogDetails;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentDemogFile;
import ca.bc.gov.educ.graddatacollection.api.batch.validation.GradFileValidator;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradCollectionStatus;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolCategoryCodes;
import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.exception.InvalidPayloadException;
import ca.bc.gov.educ.graddatacollection.api.exception.errors.ApiError;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ReportingPeriodEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ReportingPeriodRepository;
import ca.bc.gov.educ.graddatacollection.api.service.v1.IncomingFilesetService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SummerFileUpload;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SummerStudentData;
import ca.bc.gov.educ.graddatacollection.api.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessSummerStudentService {
    private final IncomingFilesetService incomingFilesetService;
    private final ReportingPeriodRepository reportingPeriodRepository;
    private final GradFileValidator gradFileValidator;
    public static final String INVALID_PAYLOAD_MSG = "Payload contains invalid data.";
    public static final String GRAD_FILE_UPLOAD = "gradFileUpload";
    private static final BatchFileMapper mapper = BatchFileMapper.mapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(final SummerFileUpload summerUpload, final String schoolID, final String districtID) {
        final var guid = UUID.randomUUID().toString();
        try {
            String incomingSchoolID = schoolID;
            String incomingDistrictID = districtID;
            if (districtID != null) {
                var mincode = summerUpload.getSummerStudents().getFirst();
                var schoolTombstone = gradFileValidator.getSchoolUsingMincode(mincode.getSchoolCode());
                if(schoolTombstone.isPresent()) {
                    incomingSchoolID = schoolTombstone.get().getSchoolId();
                } else {
                    throw new FileUnProcessableException(FileError.INVALID_SCHOOL, guid, GradCollectionStatus.LOAD_FAIL, String.valueOf(mincode));
                }
            } else {
                var schoolTombstone = gradFileValidator.getSchoolByID(guid, schoolID);
                if (!SchoolCategoryCodes.INDEPENDENTS_AND_OFFSHORE.contains(schoolTombstone.getSchoolCategoryCode())) {
                    incomingDistrictID = schoolTombstone.getDistrictId();
                }
            }

            GradStudentDemogFile demFile = createDemFile(summerUpload.getSummerStudents());
            GradStudentCourseFile courseFile = createCourseFile(summerUpload.getSummerStudents());
            createAndSaveIncomingFileset(summerUpload, incomingSchoolID, incomingDistrictID, demFile, courseFile);
        } catch(final FileUnProcessableException fileUnProcessableException) {
            log.error("File could not be processed exception :: {}", fileUnProcessableException);
            ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message(INVALID_PAYLOAD_MSG).status(BAD_REQUEST).build();
            var validationError = ValidationUtil.createFieldError(GRAD_FILE_UPLOAD, schoolID, fileUnProcessableException.getReason());
            List<FieldError> fieldErrorList = new ArrayList<>();
            fieldErrorList.add(validationError);
            error.addValidationErrors(fieldErrorList);
            throw new InvalidPayloadException(error);
        } catch (final Exception e) {
            log.error("Exception while processing the file with guid :: {} :: Exception :: {}", guid, e);
            ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message(INVALID_PAYLOAD_MSG).status(BAD_REQUEST).build();
            var validationError = ValidationUtil.createFieldError(GRAD_FILE_UPLOAD, schoolID, FileError.GENERIC_ERROR_MESSAGE.getMessage());
            List<FieldError> fieldErrorList = new ArrayList<>();
            fieldErrorList.add(validationError);
            error.addValidationErrors(fieldErrorList);
            throw new InvalidPayloadException(error);
        }
    }

    public GradStudentDemogFile createDemFile(final List<SummerStudentData> summerStudents) {
        val batchFile = new GradStudentDemogFile();
        summerStudents.forEach(student -> batchFile.getDemogData().add(this.createStudentDemogDetailRecord(student)));
        return batchFile;
    }

    public GradStudentCourseFile createCourseFile(final List<SummerStudentData> summerStudents) {
        val batchFile = new GradStudentCourseFile();
        summerStudents.forEach(student -> batchFile.getCourseData().add(this.getStudentCourseDetailRecordFromFile(student)));
        return batchFile;
    }

    public void createAndSaveIncomingFileset(final SummerFileUpload summerUpload, final String schoolID, final String districtID, final GradStudentDemogFile demFile, final GradStudentCourseFile courseFile) {
        String incomingFileName = summerUpload.getFileName();
        IncomingFilesetEntity incomingFilesetEntity = mapper.toIncomingSummerBatchEntity(summerUpload);

        ReportingPeriodEntity reportingPeriodEntity = reportingPeriodRepository.findActiveReportingPeriod().orElseThrow(() -> new EntityNotFoundException(ReportingPeriodEntity.class, "currentDate", String.valueOf(LocalDateTime.now())));
        incomingFilesetEntity.setReportingPeriod(reportingPeriodEntity);
        incomingFilesetEntity.setSchoolID(UUID.fromString(schoolID));
        incomingFilesetEntity.setDistrictID(UUID.fromString(districtID));
        incomingFilesetEntity.setDemFileName(incomingFileName.split("\\.")[0] + ".DEM");
        incomingFilesetEntity.setCrsFileName(incomingFileName.split("\\.")[0] + ".CRS");
        incomingFilesetEntity.setXamFileName(incomingFileName.split("\\.")[0] + ".XAM");
        incomingFilesetEntity.setCsvFileName(incomingFileName);

        for (final var student : demFile.getDemogData()) {
            final var demStudentEntity = mapper.toDEMStudentEntity(student, incomingFilesetEntity);
            incomingFilesetEntity.getDemographicStudentEntities().add(demStudentEntity);
        }

        for (final var student : courseFile.getCourseData()) {
            final var crsStudentEntity = mapper.toCRSStudentEntity(student, incomingFilesetEntity);
            incomingFilesetEntity.getCourseStudentEntities().add(crsStudentEntity);
        }
        incomingFilesetEntity.setFilesetStatusCode(String.valueOf(FilesetStatus.LOADED.getCode()));
        incomingFilesetService.saveIncomingFilesetRecord(incomingFilesetEntity);
    }

    private GradStudentDemogDetails createStudentDemogDetailRecord(final SummerStudentData student) {
        return GradStudentDemogDetails.builder()
                .transactionCode("E02")
                .vendorID("3")
                .mincode(student.getSchoolCode())
                .pen(student.getPen())
                .legalSurname(student.getLegalSurname())
                .legalGivenName(student.getLegalFirstName())
                .legalMiddleName(student.getLegalMiddleName())
                .dob(student.getDob())
                .studentStatus("A")
                .build();
    }

    private GradStudentCourseDetails getStudentCourseDetailRecordFromFile(final SummerStudentData student) {
        String course = student.getCourse();
        String session = student.getSessionDate();

        String courseCode = null;
        String courseLevel = null;
        String courseYear = null;
        String courseMonth = null;

        if(StringUtils.isNotBlank(course)) {
            courseCode = course.substring(0, 4);
            courseLevel = course.substring(5);
        }

        if(StringUtils.isNotBlank(session)) {
            courseYear = session.substring(0, 3);
            courseMonth = session.substring(4);
        }

        return GradStudentCourseDetails.builder()
                .transactionCode("E08")
                .vendorID("W")
                .mincode(student.getSchoolCode())
                .pen(student.getPen())
                .courseCode(courseCode)
                .courseLevel(courseLevel)
                .courseYear(courseYear)
                .courseMonth(courseMonth)
                .finalPercentage(student.getFinalPercent())
                .finalLetterGrade(student.getFinalLetterGrade())
                .courseStatus("A")
                .legalSurname(student.getLegalSurname())
                .numCredits(student.getNoOfCredits())
                .build();
    }
}
