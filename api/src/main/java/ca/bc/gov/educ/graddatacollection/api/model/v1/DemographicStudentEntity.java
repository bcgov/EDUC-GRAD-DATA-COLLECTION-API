package ca.bc.gov.educ.graddatacollection.api.model.v1;

import ca.bc.gov.educ.graddatacollection.api.util.UpperCase;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@Entity
@Table(name = "DEMOGRAPHIC_STUDENT")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DemographicStudentEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
    @Column(name = "DEMOGRAPHIC_STUDENT_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID demographicStudentID;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(optional = false, targetEntity = IncomingFilesetEntity.class)
    @JoinColumn(name = "INCOMING_FILESET_ID", referencedColumnName = "INCOMING_FILESET_ID", updatable = false)
    private IncomingFilesetEntity incomingFileset;

    @Column(name = "STUDENT_STATUS_CODE")
    private String studentStatusCode;

    @Column(name = "LOCAL_ID")
    private String localID;

    @Column(name = "PEN")
    private String pen;

    @Column(name = "FIRST_NAME")
    @UpperCase
    private String firstName;

    @Column(name = "MIDDLE_NAME")
    @UpperCase
    private String middleName;

    @Column(name = "LAST_NAME")
    @UpperCase
    private String lastName;

    @Column(name = "ADDRESS1")
    private String address1;

    @Column(name = "ADDRESS2")
    private String address2;

    @Column(name = "CITY")
    @UpperCase
    private String city;

    @Column(name = "PROVINCIAL_CODE")
    @UpperCase
    private String provincialCode;

    @Column(name = "COUNTRY_CODE")
    @UpperCase
    private String countryCode;

    @Column(name = "POSTAL_CODE")
    @UpperCase
    private String postalCode;

    @Column(name = "BIRTHDATE")
    private String birthdate;

    @Column(name = "GENDER_CODE", length = 1)
    private String gender;

    @Column(name = "CITIZENSHIP")
    private String citizenship;

    @Column(name = "GRADE")
    private String grade;

    @Column(name = "PROGRAM_CODE_1")
    private String programCode1;

    @Column(name = "PROGRAM_CODE_2")
    private String programCode2;

    @Column(name = "PROGRAM_CODE_3")
    private String programCode3;

    @Column(name = "PROGRAM_CODE_4")
    private String programCode4;

    @Column(name = "PROGRAM_CODE_5")
    private String programCode5;

    @Column(name = "PROGRAM_CADRE_FLAG")
    private String programCadreFlag;

    @Column(name = "GRAD_REQUIREMENT_YEAR")
    private String gradRequirementYear;

    @Column(name= "SCHOOL_CERTIFICATE_COMPLETION_DATE")
    private String schoolCertificateCompletionDate;

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

    public int getUniqueObjectHash() {
        return Objects.hash(incomingFileset.getSchoolID(), localID, pen, lastName, middleName, firstName, birthdate, gender, address1, address2, city, provincialCode, countryCode, citizenship,
                grade, programCadreFlag, gradRequirementYear, schoolCertificateCompletionDate, postalCode, programCode1, programCode2, programCode3, programCode4, programCode5);
    }
}
