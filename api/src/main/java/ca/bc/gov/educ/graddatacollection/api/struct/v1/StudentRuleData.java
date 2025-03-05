package ca.bc.gov.educ.graddatacollection.api.struct.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1.CoregCoursesRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.easapi.v1.AssessmentStudentDetailResponse;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GradStudentCourseRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GradStudentRecord;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentRuleData {
  private AssessmentStudentEntity assessmentStudentEntity;
  private CourseStudentEntity courseStudentEntity;
  private DemographicStudentEntity demographicStudentEntity;
  private SchoolTombstone school;
  private Student studentApiStudent;
  private Boolean studentApiStudentFetched = false;
  private GradStudentRecord gradStudentRecord;
  private Boolean gradStudentRecordFetched = false;
  private AssessmentStudentDetailResponse assessmentStudentDetail;
  private Map<String, CoregCoursesRecord> coregCoursesRecordMap;
  private List<GradStudentCourseRecord> gradStudentCourseRecordList;
}
