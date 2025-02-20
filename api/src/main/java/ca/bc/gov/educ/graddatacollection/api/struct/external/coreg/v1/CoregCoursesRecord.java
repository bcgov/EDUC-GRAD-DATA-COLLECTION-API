package ca.bc.gov.educ.graddatacollection.api.struct.external.coreg.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CoregCoursesRecord {
    private String courseID;
    private String sifSubjectCode;
    private String courseTitle;
    private String startDate;
    private String endDate;
    private String completionEndDate;
    private String genericCourseType;
    private String programGuideTitle;
    private String externalIndicator;
    private Set<CourseCodeRecord> courseCode;
    private CourseCharacteristicsRecord courseCharacteristics;
    private CourseCharacteristicsRecord courseCategory;
    private Set<CourseAllowableCreditRecord> courseAllowableCredit;
    private Set<RequiredCourseRecord> requiredCourse;

    // for testing
    public CoregCoursesRecord() {

    }
}
