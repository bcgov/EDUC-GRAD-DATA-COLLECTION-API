package ca.bc.gov.educ.graddatacollection.api.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@Entity
@Table(name = "DEMOGRAPHIC_STUDENT_VALIDATION_ISSUE")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DemographicStudentValidationIssueEntity {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
      @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "DEMOGRAPHIC_STUDENT_VALIDATION_ISSUE_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID demographicStudentValidationIssueID;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @ManyToOne(optional = false, targetEntity = DemographicStudentEntity.class)
  @JoinColumn(name = "DEMOGRAPHIC_STUDENT_ID", referencedColumnName = "DEMOGRAPHIC_STUDENT_ID", updatable = false)
  DemographicStudentEntity demographicStudent;

  @Column(name = "VALIDATION_ISSUE_SEVERITY_CODE", nullable = false)
  private String validationIssueSeverityCode;

  @Column(name = "VALIDATION_ISSUE_CODE", nullable = false)
  private String validationIssueCode;

  @Column(name = "VALIDATION_ISSUE_FIELD_CODE", nullable = false)
  private String validationIssueFieldCode;

  @Column(name = "VALIDATION_ISSUE_DESCRIPTION", nullable = false, length = 4000)
  private String validationIssueDescription;

  @Column(name = "CREATE_USER", updatable = false , length = 32)
  private String createUser;

  @PastOrPresent
  @Column(name = "CREATE_DATE", updatable = false)
  private LocalDateTime createDate;

  @Column(name = "UPDATE_USER", length = 32)
  private String updateUser;

  @PastOrPresent
  @Column(name = "UPDATE_DATE")
  private LocalDateTime updateDate;

}
