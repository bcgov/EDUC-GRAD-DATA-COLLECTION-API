package ca.bc.gov.educ.graddatacollection.api.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@Entity
@Table(name = "ERROR_FILESET_STUDENT")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorFilesetStudentEntity {
    @Id
    @UuidGenerator
    @Column(name = "ERROR_FILESET_STUDENT_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    UUID errorFilesetStudentId;

    @Column(name = "PEN", unique = true, updatable = false)
    String pen;

    @Column(name = "LOCAL_ID")
    String localID;

    @Column(name = "LAST_NAME")
    String lastName;

    @Column(name = "FIRST_NAME")
    String firstName;

    @Column(name = "BIRTHDATE")
    String birthdate;

    @Column(name = "CREATE_USER", updatable = false , length = 32)
    String createUser;

    @PastOrPresent
    @Column(name = "CREATE_DATE", updatable = false)
    LocalDateTime createDate;

    @Column(name = "UPDATE_USER", length = 32)
    String updateUser;

    @PastOrPresent
    @Column(name = "UPDATE_DATE")
    LocalDateTime updateDate;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(optional = false, targetEntity = IncomingFilesetEntity.class)
    @JoinColumn(name = "INCOMING_FILESET_ID", referencedColumnName = "INCOMING_FILESET_ID", updatable = false)
    IncomingFilesetEntity incomingFileset;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, targetEntity = DemographicStudentEntity.class)
    @JoinColumn(name = "INCOMING_FILESET_ID", referencedColumnName = "INCOMING_FILESET_ID")
    @JoinColumn(name = "PEN", referencedColumnName = "PEN")
    Set<DemographicStudentEntity> demographicStudentEntities;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, targetEntity = CourseStudentEntity.class)
    @JoinColumn(name = "INCOMING_FILESET_ID", referencedColumnName = "INCOMING_FILESET_ID")
    @JoinColumn(name = "PEN", referencedColumnName = "PEN")
    Set<CourseStudentEntity> courseStudentEntities;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, targetEntity = AssessmentStudentEntity.class)
    @JoinColumn(name = "INCOMING_FILESET_ID", referencedColumnName = "INCOMING_FILESET_ID")
    @JoinColumn(name = "PEN", referencedColumnName = "PEN")
    Set<AssessmentStudentEntity> assessmentStudentEntities;
}
