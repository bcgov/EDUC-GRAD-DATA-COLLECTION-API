package ca.bc.gov.educ.graddatacollection.api.batch.mappers;

import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentAssessmentDetails;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentCourseDetails;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentDemogDetails;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolStudentStatus;
import ca.bc.gov.educ.graddatacollection.api.mappers.StringMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public abstract class BatchFileDecorator implements BatchFileMapper {
    private final BatchFileMapper delegate;

    protected BatchFileDecorator(BatchFileMapper delegate) {
        this.delegate = delegate;
    }

    @Override
    public IncomingFilesetEntity toIncomingDEMBatchEntity(final GradFileUpload upload, final String schoolID) {
        final var entity = this.delegate.toIncomingDEMBatchEntity(upload, schoolID);
        entity.setSchoolID(UUID.fromString(schoolID));
        entity.setDemFileName(upload.getFileName());
        return entity;
    }

    @Override
    public IncomingFilesetEntity toIncomingCRSBatchEntity(final GradFileUpload upload, final String schoolID) {
        final var entity = this.delegate.toIncomingCRSBatchEntity(upload, schoolID);
        entity.setSchoolID(UUID.fromString(schoolID));
        entity.setCrsFileName(upload.getFileName());
        return entity;
    }

    @Override
    public IncomingFilesetEntity toIncomingXAMBatchEntity(final GradFileUpload upload, final String schoolID) {
        final var entity = this.delegate.toIncomingXAMBatchEntity(upload, schoolID);
        entity.setSchoolID(UUID.fromString(schoolID));
        entity.setXamFileName(upload.getFileName());
        return entity;
    }

    @Override
    public DemographicStudentEntity toDEMStudentEntity(GradStudentDemogDetails studentDetails, IncomingFilesetEntity incomingFilesetEntity) {
        final var entity = this.delegate.toDEMStudentEntity(studentDetails, incomingFilesetEntity);
        entity.setIncomingFileset(incomingFilesetEntity); // add thePK/FK relationship
        entity.setStudentStatusCode(SchoolStudentStatus.LOADED.getCode());

        entity.setPen(StringMapper.trimAndUppercase(studentDetails.getPen()));
        entity.setGender(StringMapper.trimAndUppercase(studentDetails.getGender()));
        entity.setBirthdate(StringMapper.trimAndUppercase(studentDetails.getDob()));
        entity.setGrade(StringMapper.trimAndUppercase(studentDetails.getGrade()));
        entity.setLocalID(StringMapper.trimAndUppercase(studentDetails.getLocalId()));
        entity.setFirstName(StringMapper.trimAndUppercase(studentDetails.getLegalGivenName()));
        entity.setLastName(StringMapper.trimAndUppercase(studentDetails.getLegalSurname()));
        entity.setMiddleName(StringMapper.trimAndUppercase(studentDetails.getLegalMiddleName()));
        entity.setAddressLine1(StringMapper.trimAndUppercase(studentDetails.getAddressLine1()));
        entity.setAddressLine2(StringMapper.trimAndUppercase(studentDetails.getAddressLine2()));
        entity.setCity(StringMapper.trimAndUppercase(studentDetails.getCity()));
        entity.setProvincialCode(StringMapper.trimAndUppercase(studentDetails.getProvinceCode()));
        entity.setCountryCode(StringMapper.trimAndUppercase(studentDetails.getCountryCode()));
        entity.setPostalCode(StringMapper.trimAndUppercase(studentDetails.getPostalCode()));

        entity.setCitizenship(StringMapper.trimAndUppercase(studentDetails.getCitizenshipStatus()));
        entity.setProgramCode1(StringMapper.trimAndUppercase(studentDetails.getProgramCode1()));
        entity.setProgramCode2(StringMapper.trimAndUppercase(studentDetails.getProgramCode2()));
        entity.setProgramCode3(StringMapper.trimAndUppercase(studentDetails.getProgramCode3()));
        entity.setProgramCode4(StringMapper.trimAndUppercase(studentDetails.getProgramCode4()));
        entity.setProgramCode5(StringMapper.trimAndUppercase(studentDetails.getProgramCode5()));
        entity.setProgramCadreFlag(StringMapper.trimAndUppercase(studentDetails.getProgramCadreFlag()));
        entity.setGradRequirementYear(StringMapper.trimAndUppercase(studentDetails.getGradRequirementYear()));
        entity.setSchoolCertificateCompletionDate(StringMapper.trimAndUppercase(studentDetails.getSscpCompletionDate()));

        return entity;
    }

    @Override
    public CourseStudentEntity toCRSStudentEntity(GradStudentCourseDetails courseDetails, IncomingFilesetEntity incomingFilesetEntity) {
        final var entity = this.delegate.toCRSStudentEntity(courseDetails, incomingFilesetEntity);
        entity.setIncomingFileset(incomingFilesetEntity); // add thePK/FK relationship
        entity.setStudentStatusCode(SchoolStudentStatus.LOADED.getCode());

        entity.setPen(StringMapper.trimAndUppercase(courseDetails.getPen()));
        entity.setTransactionID(StringMapper.trimAndUppercase(courseDetails.getTransactionCode()));
        entity.setLocalID(StringMapper.trimAndUppercase(courseDetails.getLocalId()));
        entity.setCourseCode(StringMapper.trimAndUppercase(courseDetails.getCourseCode()));
        entity.setCourseLevel(StringMapper.trimAndUppercase(courseDetails.getCourseLevel()));
        entity.setCourseYear(StringMapper.trimAndUppercase(courseDetails.getCourseYear()));
        entity.setCourseMonth(StringMapper.trimAndUppercase(courseDetails.getCourseMonth()));
        entity.setInterimPercentage(StringMapper.trimAndUppercase(courseDetails.getInterimPercentage()));
        entity.setFinalPercentage(StringMapper.trimAndUppercase(courseDetails.getFinalPercentage()));

        entity.setFinalGrade(StringMapper.trimAndUppercase(courseDetails.getFinalLetterGrade()));
        entity.setCourseStatus(StringMapper.trimAndUppercase(courseDetails.getCourseStatus()));
        entity.setLastName(StringMapper.trimAndUppercase(courseDetails.getLegalSurname()));
        entity.setRelatedCourse(StringMapper.trimAndUppercase(courseDetails.getRelatedCourse()));
        entity.setRelatedLevel(StringMapper.trimAndUppercase(courseDetails.getRelatedCourseLevel()));
        entity.setCourseDescription(StringMapper.trimAndUppercase(courseDetails.getCourseDesc()));
        entity.setCourseType(StringMapper.trimAndUppercase(courseDetails.getCourseType()));
        entity.setCourseGraduationRequirement(StringMapper.trimAndUppercase(courseDetails.getCourseGradReqt()));

        return entity;
    }

    @Override
    public AssessmentStudentEntity toXAMStudentEntity(GradStudentAssessmentDetails assessmentDetails, IncomingFilesetEntity incomingFilesetEntity) {
        final var entity = this.delegate.toXAMStudentEntity(assessmentDetails, incomingFilesetEntity);
        entity.setIncomingFileset(incomingFilesetEntity); // add thePK/FK relationship
        entity.setStudentStatusCode(SchoolStudentStatus.LOADED.getCode());

        entity.setPen(StringMapper.trimAndUppercase(assessmentDetails.getPen()));
        entity.setTransactionID(StringMapper.trimAndUppercase(assessmentDetails.getTransactionCode()));
        entity.setLocalID(StringMapper.trimAndUppercase(assessmentDetails.getLocalId()));
        entity.setCourseCode(StringMapper.trimAndUppercase(assessmentDetails.getCourseCode()));
        entity.setCourseLevel(StringMapper.trimAndUppercase(assessmentDetails.getCourseLevel()));
        entity.setCourseYear(StringMapper.trimAndUppercase(assessmentDetails.getCourseYear()));
        entity.setCourseMonth(StringMapper.trimAndUppercase(assessmentDetails.getCourseMonth()));
        entity.setInterimLetterGrade(StringMapper.trimAndUppercase(assessmentDetails.getInterimLetterGrade()));
        entity.setInterimSchoolPercent(StringMapper.trimAndUppercase(assessmentDetails.getInterimSchoolPercentage()));
        entity.setFinalSchoolPercent(StringMapper.trimAndUppercase(assessmentDetails.getFinalSchoolPercentage()));
        entity.setExamPercent(StringMapper.trimAndUppercase(assessmentDetails.getExamPercentage()));
        entity.setFinalPercent(StringMapper.trimAndUppercase(assessmentDetails.getFinalPercentage()));
        entity.setFinalLetterGrade(StringMapper.trimAndUppercase(assessmentDetails.getFinalLetterGrade()));
        entity.setIsElectronicExam(StringMapper.trimAndUppercase(assessmentDetails.getEExamFlag()));
        entity.setLocalCourseID(StringMapper.trimAndUppercase(assessmentDetails.getLocalCourseId()));
        entity.setProvincialSpecialCase(StringMapper.trimAndUppercase(assessmentDetails.getProvSpecCase()));
        entity.setCourseStatus(StringMapper.trimAndUppercase(assessmentDetails.getCourseStatus()));
        entity.setLastName(StringMapper.trimAndUppercase(assessmentDetails.getLegalSurname()));
        entity.setNumberOfCredits(StringMapper.trimAndUppercase(assessmentDetails.getNumCredits()));
        entity.setCourseType(StringMapper.trimAndUppercase(assessmentDetails.getCourseType()));
        entity.setToWriteFlag(StringMapper.trimAndUppercase(assessmentDetails.getWriteFlag()));

        return entity;
    }

}
