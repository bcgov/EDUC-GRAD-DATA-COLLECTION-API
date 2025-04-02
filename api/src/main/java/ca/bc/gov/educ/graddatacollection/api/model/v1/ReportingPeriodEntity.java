package ca.bc.gov.educ.graddatacollection.api.model.v1;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "REPORTING_PERIOD")
@DynamicUpdate
public class ReportingPeriodEntity {

    @Id
    @UuidGenerator
    @Column(name = "REPORTING_PERIOD_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID reportingPeriodID;

    @NotNull(message = "schYrStart cannot be null")
    @Column(name = "SCH_YR_START", nullable = false)
    private LocalDateTime schYrStart;

    @NotNull(message = "schYrEnd cannot be null")
    @Column(name = "SCH_YR_END", nullable = false)
    private LocalDateTime schYrEnd;

    @NotNull(message = "summerStart cannot be null")
    @Column(name = "SUMMER_START", nullable = false)
    private LocalDateTime summerStart;

    @NotNull(message = "summerEnd cannot be null")
    @Column(name = "SUMMER_END", nullable = false)
    private LocalDateTime summerEnd;

    @NotNull(message = "create user cannot be null")
    @Size(max = 100)
    @Column(name = "CREATE_USER", nullable = false, updatable = false)
    private String createUser;

    @NotNull(message = "create date cannot be null")
    @PastOrPresent
    @Column(name = "CREATE_DATE", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @NotNull(message = "update user cannot be null")
    @Size(max = 100)
    @Column(name = "UPDATE_USER", nullable = false)
    private String updateUser;

    @NotNull(message = "update date cannot be null")
    @PastOrPresent
    @Column(name = "UPDATE_DATE", nullable = false)
    private LocalDateTime updateDate;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "reportingPeriod", fetch = FetchType.LAZY, targetEntity = IncomingFilesetEntity.class)
    private Set<IncomingFilesetEntity> incomingFilesets;

    public Set<IncomingFilesetEntity> getIncomingFilesets() {
        if (this.incomingFilesets == null) {
            this.incomingFilesets = new HashSet<>();
        }
        return this.incomingFilesets;
    }

}