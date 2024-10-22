package ca.bc.gov.educ.graddatacollection.api.model.v1;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "ASSESSMENT_STUDENT")
@DynamicUpdate
public class AssessmentStudentEntity {

  @Id
  @UuidGenerator
  @Column(name = "ASSESSMENT_STUDENT_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID assessmentStudentID;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @ManyToOne(optional = false, targetEntity = IncomingFilesetEntity.class)
  @JoinColumn(name = "INCOMING_FILESET_ID", referencedColumnName = "INCOMING_FILESET_ID", updatable = false)
  private IncomingFilesetEntity incomingFileset;

  @Column(name = "ASSESSMENT_ID", columnDefinition = "BINARY(16)")
  UUID assessmentID;

  @NotNull(message = "studentStatusCode cannot be null")
  @Column(name = "STUDENT_STATUS_CODE")
  String studentStatusCode;

  @Column(name = "TRANSACTION_ID")
  String transactionID;

  @Column(name = "LOCAL_ID")
  String localID;

  @Column(name = "PEN")
  String pen;

  @Column(name = "COURSE_CODE")
  String courseCode;

  @Column(name = "COURSE_YEAR")
  String courseYear;

  @Column(name = "COURSE_MONTH")
  String courseMonth;

  @Column(name = "IS_ELECTRONIC_EXAM")
  String isElectronicExam;

  @Column(name = "LOCAL_COURSE_ID")
  String localCourseID;

  @Column(name = "PROVINCIAL_SPECIAL_CASE")
  String provincialSpecialCase;

  @Column(name = "COURSE_STATUS")
  String courseStatus;

  @Column(name = "LAST_NAME")
  String lastName;

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

  public int getUniqueObjectHash() {
    return Objects.hash(incomingFileset.getSchoolID(), localID, pen, lastName, courseCode, courseYear, courseMonth, isElectronicExam, localCourseID, provincialSpecialCase, courseStatus, lastName);
  }

}
