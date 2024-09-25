package ca.bc.gov.educ.graddatacollection.api.batch.struct;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GradStudentCourseFile {

    private List<GradStudentCourseDetails> courseData;

    public List<GradStudentCourseDetails> getCourseData() {
        if(courseData == null){
            courseData = new ArrayList<>();
        }
        return courseData;
    }
}
