package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.ErrorFilesetStudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ErrorFilesetStudentRepository extends JpaRepository<ErrorFilesetStudentEntity, UUID>, JpaSpecificationExecutor<ErrorFilesetStudentEntity> {
    Optional<ErrorFilesetStudentEntity> findByIncomingFileset_IncomingFilesetIDAndPen(UUID incomingFilesetId, String pen);
    List<ErrorFilesetStudentEntity> findAllByIncomingFileset_IncomingFilesetID(UUID incomingFilesetId);

    @Modifying
    @Query(value = """
            INSERT INTO error_fileset_student
                (error_fileset_student_id, incoming_fileset_id, pen, local_id, last_name, first_name, birthdate, create_user, create_date, update_user, update_date)
            VALUES
                (:id, :incomingFilesetId, :pen, :localId, :lastName, :firstName, :birthdate, :createUser, :createDate, :updateUser, :updateDate)
            ON CONFLICT (incoming_fileset_id, pen) DO NOTHING
            """, nativeQuery = true)
    void insertIgnoreConflict(
            @Param("id") UUID id,
            @Param("incomingFilesetId") UUID incomingFilesetId,
            @Param("pen") String pen,
            @Param("localId") String localId,
            @Param("lastName") String lastName,
            @Param("firstName") String firstName,
            @Param("birthdate") String birthdate,
            @Param("createUser") String createUser,
            @Param("createDate") LocalDateTime createDate,
            @Param("updateUser") String updateUser,
            @Param("updateDate") LocalDateTime updateDate
    );
}
