package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.model.v1.FinalIncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.*;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorAndWarningSummary;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.FileWarningErrorCounts;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFileset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MetricsService {
    private final FinalIncomingFilesetRepository incomingFilesetRepository;
    private final FinalErrorFilesetStudentRepository errorFilesetStudentRepository;
    private final FinalDemographicStudentRepository demographicStudentRepository;
    private final FinalAssessmentStudentRepository assessmentStudentRepository;
    private final FinalCourseStudentRepository courseStudentRepository;

    public FinalIncomingFilesetEntity getFilesetData(UUID incomingFilesetID) {
        return incomingFilesetRepository.findByIncomingFilesetID(incomingFilesetID)
                .orElseThrow(() -> new EntityNotFoundException(IncomingFileset.class, "incomingFilesetID", incomingFilesetID.toString()));
    }

    public ErrorAndWarningSummary getErrorAndWarningSummary(UUID incomingFilesetID) {
        FinalIncomingFilesetEntity filesetEntity = incomingFilesetRepository.findByIncomingFilesetID(incomingFilesetID)
                .orElseThrow(() -> new EntityNotFoundException(IncomingFileset.class, "incomingFilesetID", incomingFilesetID.toString()));

        ErrorAndWarningSummary summary = ErrorAndWarningSummary.builder()
                .schoolID(filesetEntity.getSchoolID().toString())
                .filesetID(incomingFilesetID.toString())
                .totalStudents(String.valueOf(errorFilesetStudentRepository.countAllByIncomingFileset_IncomingFilesetID(incomingFilesetID)))
                .build();

        String error = "ERROR";
        String warning = "WARNING";

        Map<String, Long> demCountsMap = getCountsMap(demographicStudentRepository.countValidationIssuesBySeverity(incomingFilesetID));
        long demErrorCount = demCountsMap.getOrDefault(error, 0L);
        long demWarningCount = demCountsMap.getOrDefault(warning, 0L);
        summary.setDemCounts(createFileWarningErrorCounts(demErrorCount, demWarningCount));

        Map<String, Long> xamCountsMap = getCountsMap(assessmentStudentRepository.countValidationIssuesBySeverity(incomingFilesetID));
        long xamErrorCount = xamCountsMap.getOrDefault(error, 0L);
        long xamWarningCount = xamCountsMap.getOrDefault(warning, 0L);
        summary.setXamCounts(createFileWarningErrorCounts(xamErrorCount, xamWarningCount));

        Map<String, Long> crsCountsMap = getCountsMap(courseStudentRepository.countValidationIssuesBySeverity(incomingFilesetID));
        long crsErrorCount = crsCountsMap.getOrDefault(error, 0L);
        long crsWarningCount = crsCountsMap.getOrDefault(warning, 0L);
        summary.setCrsCounts(createFileWarningErrorCounts(crsErrorCount, crsWarningCount));

        long totalErrors = demErrorCount + xamErrorCount + crsErrorCount;
        long totalWarnings = demWarningCount + xamWarningCount + crsWarningCount;

        summary.setTotalErrors(String.valueOf(totalErrors));
        summary.setTotalWarnings(String.valueOf(totalWarnings));
        return summary;
    }

    private Map<String, Long> getCountsMap(List<Object[]> results) {
        if (results == null || results.isEmpty()) {
            return Map.of();
        }
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }

    private FileWarningErrorCounts createFileWarningErrorCounts(Long errorCount, Long warningCount) {
        return FileWarningErrorCounts.builder()
                .errorCount(String.valueOf(errorCount))
                .warningCount(String.valueOf(warningCount))
                .build();
    }

}