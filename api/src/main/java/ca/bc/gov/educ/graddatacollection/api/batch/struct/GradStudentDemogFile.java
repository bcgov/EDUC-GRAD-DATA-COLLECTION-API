package ca.bc.gov.educ.graddatacollection.api.batch.struct;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GradStudentDemogFile {

    private List<GradStudentDemogDetails> demogData;

    public List<GradStudentDemogDetails> getDemogData() {
        if(demogData == null){
            demogData = new ArrayList<>();
        }
        return demogData;
    }
}
