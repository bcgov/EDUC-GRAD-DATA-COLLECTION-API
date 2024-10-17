package ca.bc.gov.educ.graddatacollection.api.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@Entity
@Table(name = "INCOMING_FILESET")
@JsonIgnoreProperties(ignoreUnknown = true)
public class IncomingFilesetEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
    @Column(name = "INCOMING_FILESET_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID incomingFilesetID;

    @Getter
    @Basic
    @Column(name = "SCHOOL_ID", columnDefinition = "BINARY(16)")
    private UUID schoolID;

    @Column(name = "DEM_FILE_NAME")
    private String demFileName;

    @Column(name = "DEM_FILE_DATE_UPLOADED")
    private LocalDateTime demFileUploadDate;

    @Column(name = "DEM_FILE_STATUS_CODE")
    private String demFileStatusCode;

    @Column(name = "XAM_FILE_NAME")
    private String xamFileName;

    @Column(name = "XAM_FILE_DATE_UPLOADED")
    private LocalDateTime xamFileUploadDate;

    @Column(name = "XAM_FILE_STATUS_CODE")
    private String xamFileStatusCode;

    @Column(name = "CRS_FILE_NAME")
    private String crsFileName;

    @Column(name = "CRS_FILE_DATE_UPLOADED")
    private LocalDateTime crsFileUploadDate;

    @Column(name = "CRS_FILE_STATUS_CODE")
    private String crsFileStatusCode;

    @Column(name = "FILESET_STATUS_CODE")
    private String filesetStatusCode;

    @Column(name = "CREATE_USER", updatable = false , length = 32)
    private String createUser;

    @PastOrPresent
    @Column(name = "CREATE_DATE", updatable = false)
    private LocalDateTime createDate;

    @Column(name = "UPDATE_USER", length = 32)
    private String updateUser;

    @PastOrPresent
    @Column(name = "UPDATE_DATE")
    private LocalDateTime updateDate;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "incomingFileset", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = DemographicStudentEntity.class)
    Set<DemographicStudentEntity> demographicStudentEntities;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "incomingFileset", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = CourseStudentEntity.class)
    Set<CourseStudentEntity> courseStudentEntities;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "incomingFileset", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = AssessmentStudentEntity.class)
    Set<AssessmentStudentEntity> assessmentStudentEntities;

    public Set<DemographicStudentEntity> getDemographicStudentEntities() {
        if (this.demographicStudentEntities == null) {
            this.demographicStudentEntities = new HashSet<>();
        }
        return this.demographicStudentEntities;
    }

    public Set<CourseStudentEntity> getCourseStudentEntities() {
        if (this.courseStudentEntities == null) {
            this.courseStudentEntities = new HashSet<>();
        }
        return this.courseStudentEntities;
    }

    public Set<AssessmentStudentEntity> getAssessmentStudentEntities() {
        if (this.assessmentStudentEntities == null) {
            this.assessmentStudentEntities = new HashSet<>();
        }
        return this.assessmentStudentEntities;
    }


}
