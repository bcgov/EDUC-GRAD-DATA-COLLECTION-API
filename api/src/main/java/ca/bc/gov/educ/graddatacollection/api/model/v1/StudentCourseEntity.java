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
 * The type Student Course.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "STUDENT_COURSE")
@DynamicUpdate
public class StudentCourseEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
    @Column(name = "STUDENT_COURSE_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    UUID studentCourseId;

    @NotNull(message = "student pen cannot be null")
    @Column(name = "STUDENT_PEN")
    String studentPen;

    @NotNull(message = "code cannot be null")
    @Column(name = "COURSE_CODE")
    String courseCode;

    @NotNull(message = "level cannot be null")
    @Column(name = "COURSE_LEVEL")
    String courseLevel;

    @NotNull(message = "name cannot be null")
    @Column(name = "COURSE_NAME")
    String courseName;

    @Column(name = "GRAD_REQ_MET")
    String gradeReqMet;

    @PastOrPresent
    @Column(name = "COURSE_SESSION")
    LocalDateTime courseSession;

    @Column(name = "COURSE_TYPE")
    String courseType;

    @Column(name = "COMPLETED_PERCENT")
    String completePercent;

    @Column(name = "COMPLETED_LETTER_GRADE")
    String completeLetterGrade;

    @Column(name = "INTERIM_LETTER_PERCENT")
    String interimLetterPercent;

    @Column(name = "INTERIM_LETTER_GRADE")
    String interimLetterGrade;

    @Column(name = "COURSE_CREDITS")
    String courseCredits;

    @Column(name = "USED_FOR_GRAD")
    String usedForGrad;

    @Column(name = "RELATED_COURSE")
    String relatedCourse;

    @Column(name = "RELATED_LEVEL")
    String relatedLevel;

    @Column(name = "COURSE_DESCRIPTION")
    String courseDescription;

    @Column(name = "GRAD_REQ_TYPE")
    String gradeReqType;

    @Column(name = "RELATED_COURSE_FLAG")
    Boolean relatedCourseFlag;

    @PastOrPresent
    @Column(name = "CREATE_DATE", updatable = false)
    LocalDateTime createDate;

    @PastOrPresent
    @Column(name = "UPDATE_DATE")
    LocalDateTime updateDate;
}
