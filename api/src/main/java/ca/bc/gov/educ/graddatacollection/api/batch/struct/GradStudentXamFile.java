package ca.bc.gov.educ.graddatacollection.api.batch.struct;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GradStudentXamFile {

    private List<GradStudentAssessmentDetails> assessmentData;

    public List<GradStudentAssessmentDetails> getAssessmentData() {
        if(assessmentData == null){
            assessmentData = new ArrayList<>();
        }
        return assessmentData;
    }
}
