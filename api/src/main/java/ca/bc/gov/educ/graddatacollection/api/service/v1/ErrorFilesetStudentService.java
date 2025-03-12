package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ErrorFilesetStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ErrorFilesetStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorFilesetStudent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ErrorFilesetStudentService {
    private final ErrorFilesetStudentRepository errorFilesetStudentRepository;
    private final IncomingFilesetRepository incomingFilesetRepository;

    @Retryable(retryFor = {PSQLException.class}, backoff = @Backoff(multiplier = 3, delay = 2000))
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void flagErrorOnStudent(UUID incomingFilesetID, String pen, boolean isDemLoad, String firstName, String lastName, String localID, String birthDate) {
        Optional<ErrorFilesetStudentEntity> preexisting = errorFilesetStudentRepository.findByIncomingFileset_IncomingFilesetIDAndPen(incomingFilesetID, pen);
        if (preexisting.isPresent() && isDemLoad) {
            var stud =  preexisting.get();
            stud.setLocalID(localID);
            stud.setLastName(lastName);
            stud.setFirstName(firstName);
            stud.setBirthdate(birthDate);
            errorFilesetStudentRepository.save(stud);
        }else if(preexisting.isEmpty()){
            var fileSet = incomingFilesetRepository.findById(incomingFilesetID).orElseThrow(() -> new EntityNotFoundException(IncomingFilesetEntity.class, "incomingFilesetID", incomingFilesetID.toString()));
            ErrorFilesetStudentEntity newErrorFilesetStudent = new ErrorFilesetStudentEntity();
            newErrorFilesetStudent.setIncomingFileset(fileSet);
            newErrorFilesetStudent.setPen(pen);
            if(isDemLoad) {
                newErrorFilesetStudent.setLastName(lastName);
                newErrorFilesetStudent.setFirstName(firstName);
                newErrorFilesetStudent.setLocalID(localID);
                newErrorFilesetStudent.setBirthdate(birthDate);
            }
            errorFilesetStudentRepository.save(newErrorFilesetStudent);
        }
    }

    public ErrorFilesetStudentEntity getErrorFilesetStudent(String pen, UUID incomingFilesetId) {
        Optional<ErrorFilesetStudentEntity> optionalErrorFilesetStudentEntity;
        String incomingFilesetIdString;
        if (incomingFilesetId != null) {
            incomingFilesetIdString = incomingFilesetId.toString();
            optionalErrorFilesetStudentEntity = errorFilesetStudentRepository.findByIncomingFileset_IncomingFilesetIDAndPen(incomingFilesetId, pen);
        } else {
            incomingFilesetIdString = "null";
            optionalErrorFilesetStudentEntity = errorFilesetStudentRepository.findFirstByPenOrderByIncomingFileset_CreateDateDesc(pen);
        }

        return optionalErrorFilesetStudentEntity.orElseThrow(() -> new EntityNotFoundException(ErrorFilesetStudent.class, "pen: ", pen, "incomingFilesetId: ", incomingFilesetIdString));
    }
}
