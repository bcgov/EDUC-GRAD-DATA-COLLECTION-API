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
 * The type Course.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "COURSE")
@DynamicUpdate
public class CourseEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
    @Column(name = "COURSE_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    UUID courseId;

    @NotNull(message = "code cannot be null")
    @Column(name = "COURSE_CODE")
    String courseCode;

    @NotNull(message = "level cannot be null")
    @Column(name = "COURSE_LEVEL")
    String courseLevel;

    @NotNull(message = "name cannot be null")
    @Column(name = "COURSE_NAME")
    String courseName;

    @NotNull(message = "num credits cannot be null")
    @Column(name = "NUM_CREDITS")
    String numCredits;

    @NotNull(message = "language cannot be null")
    @Column(name = "COURSE_LANGUAGE")
    String courseLanguage;

    @PastOrPresent
    @NotNull(message = "start date cannot be null")
    @Column(name = "COURSE_START_DATE")
    LocalDateTime courseStartDate;

    @PastOrPresent
    @NotNull(message = "end date cannot be null")
    @Column(name = "COURSE_END_DATE")
    LocalDateTime courseEndDate;

    @Column(name = "WORK_EXPERIENCE")
    Boolean workExperience;

    @Column(name = "COURSE_TYPE_GENERIC")
    String courseTypeGeneric;

    @PastOrPresent
    @Column(name = "CREATE_DATE", updatable = false)
    LocalDateTime createDate;

    @PastOrPresent
    @Column(name = "UPDATE_DATE")
    LocalDateTime updateDate;
}
