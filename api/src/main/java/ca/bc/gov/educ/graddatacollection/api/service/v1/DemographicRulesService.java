package ca.bc.gov.educ.graddatacollection.api.service.v1;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.external.grad.v1.GradStudentRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DemographicRulesService {

    private final RestUtils restUtils;

    public GradStudentRecord getGradStudentRecord(String studentID){
        return restUtils.getGradStudentRecordByStudentID(UUID.randomUUID(), studentID);
    }
}
