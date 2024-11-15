package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.ErrorFilesetStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ErrorFilesetStudentRepository;
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

    public void flagErrorOnStudent(UUID incomingFilesetID, String pen) {
        Optional<ErrorFilesetStudentEntity> preexisting = errorFilesetStudentRepository.findByIncomingFilesetIdAndPen(incomingFilesetID, pen);
        if (preexisting.isPresent()) {
            return;
        }
        ErrorFilesetStudentEntity newErrorFilesetStudent = new ErrorFilesetStudentEntity();
        newErrorFilesetStudent.setIncomingFilesetId(incomingFilesetID);
        newErrorFilesetStudent.setPen(pen);
        errorFilesetStudentRepository.save(newErrorFilesetStudent);
    }
}
