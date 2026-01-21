package ca.bc.gov.educ.graddatacollection.api.model.v1;

import ca.bc.gov.educ.graddatacollection.api.util.UpperCase;
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
public class DemographicStudentLightEntity {

  @Id
  @UuidGenerator
  @Column(name = "DEMOGRAPHIC_STUDENT_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID demographicStudentID;

  @NotNull(message = "incomingFilesetID cannot be null")
  @Column(name = "INCOMING_FILESET_ID")
  UUID incomingFilesetID;

  @NotNull(message = "studentStatusCode cannot be null")
  @Column(name = "STUDENT_STATUS_CODE")
  String studentStatusCode;

  @Column(name = "TRANSACTION_ID")
  String transactionID;

  @Column(name = "VENDOR_ID")
  String vendorID;

  @Column(name = "LOCAL_ID")
  String localID;

  @Column(name = "PEN")
  String pen;

  @Column(name = "LAST_NAME")
  @UpperCase
  String lastName;

  @Column(name = "MIDDLE_NAME")
  @UpperCase
  String middleName;

  @Column(name = "FIRST_NAME")
  @UpperCase
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

  @Column(name = "STUDENT_STATUS")
  String studentStatus;

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
