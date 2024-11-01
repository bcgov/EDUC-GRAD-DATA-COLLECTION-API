package ca.bc.gov.educ.graddatacollection.api.rules.utils;

import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class RuleUtil {
  private RuleUtil() {
  }

  public static boolean validateStudentRecordExists(Student studentFromAPI) {
    return studentFromAPI != null;
  }

  public static boolean validateStudentSurnameMatches(DemographicStudentEntity demStudent, Student studentFromAPI) {
    return StringUtils.isNotBlank(studentFromAPI.getLegalLastName()) && StringUtils.isNotBlank(demStudent.getLastName()) && studentFromAPI.getLegalLastName().equalsIgnoreCase(demStudent.getLastName());
  }

  public static boolean validateStudentGivenNameMatches(DemographicStudentEntity demStudent, Student studentFromAPI) {
    return (StringUtils.isNotBlank(studentFromAPI.getLegalFirstName()) && StringUtils.isNotBlank(demStudent.getFirstName()) && studentFromAPI.getLegalFirstName().equalsIgnoreCase(demStudent.getFirstName())) ||
            (StringUtils.isBlank(studentFromAPI.getLegalFirstName()) && StringUtils.isBlank(demStudent.getFirstName()));
  }

  public static boolean validateStudentMiddleNameMatches(DemographicStudentEntity demStudent, Student studentFromAPI) {
    return (StringUtils.isNotBlank(studentFromAPI.getLegalMiddleNames()) && StringUtils.isNotBlank(demStudent.getMiddleName()) && studentFromAPI.getLegalMiddleNames().equalsIgnoreCase(demStudent.getMiddleName())) ||
            (StringUtils.isBlank(studentFromAPI.getLegalMiddleNames()) && StringUtils.isBlank(demStudent.getMiddleName()));
  }

  public static boolean validateStudentDOBMatches(DemographicStudentEntity demStudent, Student studentFromAPI) {
    return (StringUtils.isNotBlank(studentFromAPI.getDob()) && StringUtils.isNotBlank(demStudent.getBirthdate()) && studentFromAPI.getDob().equalsIgnoreCase(demStudent.getBirthdate()));
  }

}
