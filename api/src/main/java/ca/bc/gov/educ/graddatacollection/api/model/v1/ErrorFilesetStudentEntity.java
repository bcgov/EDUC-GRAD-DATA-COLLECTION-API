package ca.bc.gov.educ.graddatacollection.api.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;

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
    @Column(name = "ERROR_FILESET_STUDENT_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    UUID errorFilesetStudentId;

    @Column(name = "INCOMING_FILESET_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    UUID incomingFilesetId;

    @Column(name = "PEN")
    String pen;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(optional = false, targetEntity = IncomingFilesetEntity.class)
    @JoinColumn(name = "INCOMING_FILESET_ID", referencedColumnName = "INCOMING_FILESET_ID", updatable = false)
    IncomingFilesetEntity incomingFileset;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, targetEntity = DemographicStudentEntity.class)
    @JoinColumns({
        @JoinColumn(name = "INCOMING_FILESET_ID", referencedColumnName = "INCOMING_FILESET_ID"),
        @JoinColumn(name = "PEN", referencedColumnName = "PEN")
    })
    Set<DemographicStudentEntity> demographicStudentEntities;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, targetEntity = CourseStudentEntity.class)
    @JoinColumns({
        @JoinColumn(name = "INCOMING_FILESET_ID", referencedColumnName = "INCOMING_FILESET_ID"),
        @JoinColumn(name = "PEN", referencedColumnName = "PEN")
    })
    Set<CourseStudentEntity> courseStudentEntities;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, targetEntity = AssessmentStudentEntity.class)
    @JoinColumns({
        @JoinColumn(name = "INCOMING_FILESET_ID", referencedColumnName = "INCOMING_FILESET_ID"),
        @JoinColumn(name = "PEN", referencedColumnName = "PEN")
    })
    Set<AssessmentStudentEntity> assessmentStudentEntities;
}
