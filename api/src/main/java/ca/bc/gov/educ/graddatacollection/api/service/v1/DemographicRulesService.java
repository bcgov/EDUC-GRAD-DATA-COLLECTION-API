package ca.bc.gov.educ.graddatacollection.api.service.v1;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DemographicRulesService extends BaseRulesService {
    public DemographicRulesService(RestUtils restUtils) {
        super(restUtils);
    }
}
