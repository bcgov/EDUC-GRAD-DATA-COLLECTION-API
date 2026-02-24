package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ErrorFilesetStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ErrorFilesetStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.IncomingFilesetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ErrorFilesetStudentService {
    private final ErrorFilesetStudentRepository errorFilesetStudentRepository;
    private final IncomingFilesetRepository incomingFilesetRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void flagErrorOnStudent(UUID incomingFilesetID, String pen, DemographicStudentEntity demStudent, String createUser, LocalDateTime createDate, String updateUser, LocalDateTime updateDate) {
        Optional<ErrorFilesetStudentEntity> preexisting = errorFilesetStudentRepository.findByIncomingFileset_IncomingFilesetIDAndPen(incomingFilesetID, pen);
        if (preexisting.isEmpty()) {
            var fileSet = incomingFilesetRepository.findById(incomingFilesetID).orElseThrow(() -> new EntityNotFoundException(IncomingFilesetEntity.class, "incomingFilesetID", incomingFilesetID.toString()));
            ErrorFilesetStudentEntity newErrorFilesetStudent = new ErrorFilesetStudentEntity();
            newErrorFilesetStudent.setIncomingFileset(fileSet);
            newErrorFilesetStudent.setPen(pen);
            if(demStudent != null) {
                newErrorFilesetStudent.setLocalID(demStudent.getLocalID());
                newErrorFilesetStudent.setLastName(demStudent.getLastName());
                newErrorFilesetStudent.setFirstName(demStudent.getFirstName());
                newErrorFilesetStudent.setBirthdate(demStudent.getBirthdate());
            }
            newErrorFilesetStudent.setCreateUser(createUser);
            newErrorFilesetStudent.setCreateDate(createDate);
            newErrorFilesetStudent.setUpdateUser(updateUser);
            newErrorFilesetStudent.setUpdateDate(updateDate);
            try {
                errorFilesetStudentRepository.save(newErrorFilesetStudent);
            } catch (DataIntegrityViolationException e) {
                if (isUniqueConstraintViolation(e)) {
                    log.debug("Error fileset student already exists for pen {} and incomingFilesetID {}, ignoring duplicate insert.", pen, incomingFilesetID);
                } else {
                    throw e;
                }
            }
        }
    }

    private boolean isUniqueConstraintViolation(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof PSQLException psql && "23505".equals(psql.getSQLState())) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
