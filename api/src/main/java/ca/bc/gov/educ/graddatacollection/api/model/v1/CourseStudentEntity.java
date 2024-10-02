package ca.bc.gov.educ.graddatacollection.api.model.v1;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The type Course Student.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "COURSE_STUDENT")
@DynamicUpdate
public class CourseStudentEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
    @Column(name = "COURSE_STUDENT_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    UUID courseStudentId;

    @Column(name = "INCOMING_FILESET_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    UUID incomingFilesetId;

    @NotNull(message = "student status cannot be null")
    @Column(name = "STUDENT_STATUS")
    String studentStatus;

    @NotNull(message = "student local id cannot be null")
    @Column(name = "STUDENT_LOCAL_ID")
    String studentLocalId;

    @NotNull(message = "pen cannot be null")
    @Column(name = "PEN")
    String pen;

    @NotNull(message = "course code cannot be null")
    @Column(name = "COURSE_CODE")
    String courseCode;

    @NotNull(message = "course year cannot be null")
    @Column(name = "COURSE_YEAR")
    String courseYear;

    @NotNull(message = "course month cannot be null")
    @Column(name = "COURSE_MONTH")
    String courseMonth;

    @Column(name = "INTERIM_PERCENTAGE")
    String interimPercentage;

    @Column(name = "INTERIM_GRADE")
    String interimGrade;

    @Column(name = "FINAL_PERCENTAGE")
    String finalPercentage;

    @Column(name = "FINAL_GRADE")
    String finalGrade;

    @Column(name = "COURSE_STATUS")
    String courseStatus;

    @Column(name = "SURNAME")
    String surname;

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

    @PastOrPresent
    @Column(name = "CREATE_DATE", updatable = false)
    LocalDateTime createDate;

    @PastOrPresent
    @Column(name = "UPDATE_DATE")
    LocalDateTime updateDate;
}
