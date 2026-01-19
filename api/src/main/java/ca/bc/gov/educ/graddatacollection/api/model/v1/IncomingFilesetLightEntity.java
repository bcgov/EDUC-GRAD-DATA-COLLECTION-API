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
@Table(name = "INCOMING_FILESET")
@JsonIgnoreProperties(ignoreUnknown = true)
public class IncomingFilesetLightEntity {
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

    @Column(name = "FILESET_STATUS_CODE")
    private String filesetStatusCode;

    @PastOrPresent
    @Column(name = "CREATE_DATE", updatable = false)
    private LocalDateTime createDate;

}
