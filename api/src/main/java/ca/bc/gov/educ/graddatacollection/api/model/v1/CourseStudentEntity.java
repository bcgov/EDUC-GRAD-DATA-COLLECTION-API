package ca.bc.gov.educ.graddatacollection.api.model.v1;

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
@Table(name = "COURSE_STUDENT")
@DynamicUpdate
public class CourseStudentEntity {

  @Id
  @UuidGenerator
  @Column(name = "COURSE_STUDENT_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID courseStudentID;

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

  @Column(name = "FINAL_PERCENTAGE")
  String finalPercentage;

  @Column(name = "FINAL_GRADE")
  String finalGrade;

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