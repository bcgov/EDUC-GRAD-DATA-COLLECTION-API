package ca.bc.gov.educ.graddatacollection.api.struct.v1;

import java.time.LocalDateTime;

public interface SchoolSubmissionCount {
    String getSchoolID();
    String getSubmissionCount();
    LocalDateTime getLastSubmissionDate();
}
