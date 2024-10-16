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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "DEMOGRAPHIC_STUDENT")
@DynamicUpdate
public class DemographicStudentEntity {

  @Id
  @UuidGenerator
  @Column(name = "DEMOGRAPHIC_STUDENT_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID demographicStudentID;

  @NotNull(message = "incomingFilesetID cannot be null")
  @Column(name = "INCOMING_FILESET_ID", columnDefinition = "BINARY(16)")
  UUID incomingFilesetID;

  @NotNull(message = "studentStatusCode cannot be null")
  @Column(name = "STUDENT_STATUS_CODE")
  String studentStatusCode;

  @Column(name = "TRANSACTION_ID")
  String transactionID;

  @Column(name = "LOCAL_ID")
  String localID;

  @Column(name = "PEN")
  String pen;

  @Column(name = "LAST_NAME")
  String lastName;

  @Column(name = "FIRST_NAME")
  String firstName;

  @Column(name = "ADDRESS1")
  String addressLine1;

  @Column(name = "ADDRESS2")
  String addressLine2;

  @Column(name = "CITY")
  String city;

  @Column(name = "PROVINCIAL_CODE")
  String provincialCode;

  @Column(name = "COUNTRY_CODE")
  String countryCode;

  @Column(name = "POSTAL_CODE")
  String postalCode;

  @Column(name = "BIRTHDATE")
  String birthdate;

  @Column(name = "GENDER")
  String gender;

  @Column(name = "CITIZENSHIP")
  String citizenship;

  @Column(name = "GRADE")
  String grade;

  @Column(name = "PROGRAM_CODE_1")
  String programCode1;

  @Column(name = "PROGRAM_CODE_2")
  String programCode2;

  @Column(name = "PROGRAM_CODE_3")
  String programCode3;

  @Column(name = "PROGRAM_CODE_4")
  String programCode4;

  @Column(name = "PROGRAM_CODE_5")
  String programCode5;

  @Column(name = "program_cadre_flag")
  String programCadreFlag;

  @Column(name = "grad_requirement_year")
  String gradRequirementYear;

  @Column(name = "school_certificate_completion_date")
  String schoolCertificateCompletionDate;

  @NotNull(message = "create user cannot be null")
  @Column(name = "CREATE_USER", updatable = false)
  @Size(max = 100)
  String createUser;

  @NotNull(message = "update user cannot be null")
  @Column(name = "UPDATE_USER")
  @Size(max = 100)
  String updateUser;

  @PastOrPresent
  @Column(name = "CREATE_DATE", updatable = false)
  LocalDateTime createDate;

  @PastOrPresent
  @Column(name = "UPDATE_DATE")
  LocalDateTime updateDate;

}
