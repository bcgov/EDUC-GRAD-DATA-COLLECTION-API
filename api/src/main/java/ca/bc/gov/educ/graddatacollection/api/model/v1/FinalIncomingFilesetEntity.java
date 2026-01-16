package ca.bc.gov.educ.graddatacollection.api.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@Entity
@Table(name = "FINAL_INCOMING_FILESET")
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinalIncomingFilesetEntity {
    @Id
    @UuidGenerator
    @Column(name = "INCOMING_FILESET_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID incomingFilesetID;

    @Getter
    @Basic
    @Column(name = "SCHOOL_ID", columnDefinition = "BINARY(16)")
    private UUID schoolID;

    @Column(name = "DISTRICT_ID", columnDefinition = "BINARY(16)")
    private UUID districtID;

    @Column(name = "DEM_FILE_NAME")
    private String demFileName;

    @Column(name = "DEM_FILE_DATE_UPLOADED")
    private LocalDateTime demFileUploadDate;

    @Column(name = "XAM_FILE_NAME")
    private String xamFileName;

    @Column(name = "XAM_FILE_DATE_UPLOADED")
    private LocalDateTime xamFileUploadDate;

    @Column(name = "CRS_FILE_NAME")
    private String crsFileName;

    @Column(name = "CRS_FILE_DATE_UPLOADED")
    private LocalDateTime crsFileUploadDate;

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

    @Column(name = "CSV_FILE_NAME")
    private String csvFileName;

    @Transient
    private int numberOfMissingPENs; 

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "incomingFileset", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = FinalDemographicStudentEntity.class)
    Set<FinalDemographicStudentEntity> demographicStudentEntities;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "incomingFileset", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = FinalCourseStudentEntity.class)
    Set<FinalCourseStudentEntity> courseStudentEntities;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "incomingFileset", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = FinalAssessmentStudentEntity.class)
    Set<FinalAssessmentStudentEntity> assessmentStudentEntities;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "incomingFileset", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = FinalErrorFilesetStudentEntity.class)
    Set<FinalErrorFilesetStudentEntity> errorFilesetStudentEntities;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(optional = false, targetEntity = ReportingPeriodEntity.class)
    @JoinColumn(name = "REPORTING_PERIOD_ID", referencedColumnName = "REPORTING_PERIOD_ID", updatable = false)
    private ReportingPeriodEntity reportingPeriod;

    public Set<FinalDemographicStudentEntity> getDemographicStudentEntities() {
        if (this.demographicStudentEntities == null) {
            this.demographicStudentEntities = new HashSet<>();
        }
        return this.demographicStudentEntities;
    }

    public Set<FinalCourseStudentEntity> getCourseStudentEntities() {
        if (this.courseStudentEntities == null) {
            this.courseStudentEntities = new HashSet<>();
        }
        return this.courseStudentEntities;
    }

    public Set<FinalAssessmentStudentEntity> getAssessmentStudentEntities() {
        if (this.assessmentStudentEntities == null) {
            this.assessmentStudentEntities = new HashSet<>();
        }
        return this.assessmentStudentEntities;
    }

    public Set<FinalErrorFilesetStudentEntity> getErrorFilesetStudentEntities() {
        if (this.errorFilesetStudentEntities == null) {
            this.errorFilesetStudentEntities = new HashSet<>();
        }
        return this.errorFilesetStudentEntities;
    }
}
