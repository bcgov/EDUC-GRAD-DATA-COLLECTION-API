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

    @NotNull(message = "student local id cannot be null")
    @Column(name = "STUDENT_LOCAL_ID")
    String studentLocalId;

    @NotNull(message = "student pen cannot be null")
    @Column(name = "STUDENT_PEN")
    String studentPen;

    @NotNull(message = "status cannot be null")
    @Column(name = "STATUS")
    String status;

    @NotNull(message = "code cannot be null")
    @Column(name = "CODE")
    String code;

    @NotNull(message = "level cannot be null")
    @Column(name = "LEVEL")
    String level;

    @NotNull(message = "name cannot be null")
    @Column(name = "NAME")
    String name;

    @Column(name = "GRAD_REQ_MET")
    String gradeReqMet;

    @PastOrPresent
    @Column(name = "SESSION")
    LocalDateTime session;

    @Column(name = "TYPE")
    String type;

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

    @Column(name = "DESCRIPTION")
    String description;

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
