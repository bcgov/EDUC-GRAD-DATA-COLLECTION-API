package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ErrorFilesetStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ErrorFilesetStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ErrorFilesetStudentService {
    private final ErrorFilesetStudentRepository errorFilesetStudentRepository;
    private final IncomingFilesetRepository incomingFilesetRepository;

    public void flagErrorOnStudent(UUID incomingFilesetID, String pen, boolean isDemLoad, String firstName, String lastName, String localID, String birthDate) {
        Optional<ErrorFilesetStudentEntity> preexisting = errorFilesetStudentRepository.findByIncomingFileset_IncomingFilesetIDAndPen(incomingFilesetID, pen);
        if (preexisting.isPresent() && isDemLoad) {
            var stud =  preexisting.get();
            stud.setLocalID(localID);
            stud.setLastName(lastName);
            stud.setFirstName(firstName);
            stud.setBirthDate(birthDate);
            errorFilesetStudentRepository.save(stud);
        }else{
            var fileSet = incomingFilesetRepository.findById(incomingFilesetID).orElseThrow(() -> new EntityNotFoundException(IncomingFilesetEntity.class, "incomingFilesetID", incomingFilesetID.toString()));
            ErrorFilesetStudentEntity newErrorFilesetStudent = new ErrorFilesetStudentEntity();
            newErrorFilesetStudent.setIncomingFileset(fileSet);
            newErrorFilesetStudent.setPen(pen);
            if(isDemLoad) {
                newErrorFilesetStudent.setLastName(lastName);
                newErrorFilesetStudent.setFirstName(firstName);
                newErrorFilesetStudent.setLocalID(localID);
                newErrorFilesetStudent.setBirthDate(birthDate);
            }
            errorFilesetStudentRepository.save(newErrorFilesetStudent);
        }
    }
}
