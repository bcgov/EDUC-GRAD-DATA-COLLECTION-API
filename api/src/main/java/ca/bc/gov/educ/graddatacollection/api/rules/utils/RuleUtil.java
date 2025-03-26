package ca.bc.gov.educ.graddatacollection.api.rules.utils;

import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

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
    if(StringUtils.isNotBlank(studentFromAPI.getDob()) && StringUtils.isNotBlank(demStudent.getBirthdate())) {
      var formattedDemBirthdate = LocalDate.parse(demStudent.getBirthdate(), DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT));
      var formattedStudentApiBirthdate = LocalDate.from(LocalDateTime.parse(studentFromAPI.getDob(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
      return formattedStudentApiBirthdate.isEqual(formattedDemBirthdate);
    }
    return false;
  }

}
