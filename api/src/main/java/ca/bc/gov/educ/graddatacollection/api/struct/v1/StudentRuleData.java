package ca.bc.gov.educ.graddatacollection.api.struct.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GradStudentRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentRuleData {
  private static final long serialVersionUID = -2329245910142215178L;
  private AssessmentStudentEntity assessmentStudentEntity;
  private CourseStudentEntity courseStudentEntity;
  private DemographicStudentEntity demographicStudentEntity;
  private SchoolTombstone school;
  private Student studentApiStudent;
  private GradStudentRecord gradStudentRecord;
}
