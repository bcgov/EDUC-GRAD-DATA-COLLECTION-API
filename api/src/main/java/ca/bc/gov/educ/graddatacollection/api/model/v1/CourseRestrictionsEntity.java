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
 * The type Course Restrictions.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "COURSE_RESTRICTIONS")
@DynamicUpdate
public class CourseRestrictionsEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
    @Column(name = "COURSE_RESTRICTIONS_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    UUID courseRestrictionsId;

    @NotNull(message = "code cannot be null")
    @Column(name = "COURSE_CODE")
    String courseCode;

    @NotNull(message = "level cannot be null")
    @Column(name = "COURSE_LEVEL")
    String courseLevel;

    @NotNull(message = "restricted code cannot be null")
    @Column(name = "RESTRICTED_COURSE_CODE")
    String restrictedCourseCode;

    @NotNull(message = "restricted level cannot be null")
    @Column(name = "RESTRICTED_COURSE_LEVEL")
    String restrictedCourseLevel;

    @PastOrPresent
    @NotNull(message = "restriction start date cannot be null")
    @Column(name = "RESTRICTION_START_DATE")
    LocalDateTime restrictionStartDate;

    @PastOrPresent
    @NotNull(message = "restriction end date cannot be null")
    @Column(name = "RESTRICTION_END_DATE")
    LocalDateTime restrictionEndDate;

    @PastOrPresent
    @Column(name = "CREATE_DATE", updatable = false)
    LocalDateTime createDate;

    @PastOrPresent
    @Column(name = "UPDATE_DATE")
    LocalDateTime updateDate;
}
