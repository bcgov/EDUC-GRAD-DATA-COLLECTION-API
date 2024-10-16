package ca.bc.gov.educ.graddatacollection.api.batch.mapper;

import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradStudentDemogDetails;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolStudentStatus;
import ca.bc.gov.educ.graddatacollection.api.mappers.StringMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

@Slf4j
public abstract class BatchFileDecorator implements DemBatchFileMapper{
    private final DemBatchFileMapper delegate;

    protected BatchFileDecorator(DemBatchFileMapper delegate) {
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
}
