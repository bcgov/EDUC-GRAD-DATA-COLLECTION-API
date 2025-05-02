package ca.bc.gov.educ.graddatacollection.api.model.v1;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "COURSE_STUDENT")
@DynamicUpdate
public class CourseStudentLightEntity {

  @Id
  @UuidGenerator
  @Column(name = "COURSE_STUDENT_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID courseStudentID;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @ManyToOne(optional = false, targetEntity = IncomingFilesetEntity.class)
  @JoinColumn(name = "INCOMING_FILESET_ID", referencedColumnName = "INCOMING_FILESET_ID", updatable = false)
  private IncomingFilesetEntity incomingFileset;

  @NotNull(message = "studentStatusCode cannot be null")
  @Column(name = "STUDENT_STATUS_CODE")
  String studentStatusCode;

  @Column(name = "TRANSACTION_ID")
  String transactionID;

  @Column(name = "LOCAL_ID")
  String localID;

  @Column(name = "VENDOR_ID")
  String vendorID;

  @Column(name = "PEN")
  String pen;

  @Column(name = "COURSE_CODE")
  String courseCode;

  @Column(name = "COURSE_LEVEL")
  String courseLevel;

  @Column(name = "COURSE_YEAR")
  String courseYear;

  @Column(name = "COURSE_MONTH")
  String courseMonth;

  @Column(name = "INTERIM_PERCENTAGE")
  String interimPercentage;

  @Column(name = "INTERIM_GRADE")
  String interimLetterGrade;

  @Column(name = "FINAL_PERCENTAGE")
  String finalPercentage;

  @Column(name = "FINAL_GRADE")
  String finalLetterGrade;

  @Column(name = "COURSE_STATUS")
  String courseStatus;

  @Column(name = "LAST_NAME")
  String lastName;

  @Column(name = "NUMBER_OF_CREDITS")
  String numberOfCredits;

  @Column(name = "RELATED_COURSE")
  String relatedCourse;

  @Column(name = "RELATED_LEVEL")
  String relatedLevel;

  @Column(name = "COURSE_DESCRIPTION")
  String courseDescription;

  @Column(name = "COURSE_TYPE")
  String courseType;

  @Column(name = "COURSE_GRADUATION_REQUIREMENT")
  String courseGraduationRequirement;

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
