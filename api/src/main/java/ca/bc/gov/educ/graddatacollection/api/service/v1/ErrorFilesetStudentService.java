package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ErrorFilesetStudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ErrorFilesetStudentService {
    private final ErrorFilesetStudentRepository errorFilesetStudentRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void flagErrorOnStudent(UUID incomingFilesetID, String pen, DemographicStudentEntity demStudent, String createUser, LocalDateTime createDate, String updateUser, LocalDateTime updateDate) {
        errorFilesetStudentRepository.insertIgnoreConflict(
                UUID.randomUUID(),
                incomingFilesetID,
                pen,
                demStudent != null ? demStudent.getLocalID() : null,
                demStudent != null ? demStudent.getLastName() : null,
                demStudent != null ? demStudent.getFirstName() : null,
                demStudent != null ? demStudent.getBirthdate() : null,
                createUser,
                createDate,
                updateUser,
                updateDate
        );
    }
}
