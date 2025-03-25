package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.FilesetStatus;
import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.*;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.School;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorAndWarningSummary;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.FileWarningErrorCounts;
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
    private final IncomingFilesetRepository incomingFilesetRepository;
    private final ErrorFilesetStudentRepository errorFilesetStudentRepository;
    private final DemographicStudentRepository demographicStudentRepository;
    private final AssessmentStudentRepository assessmentStudentRepository;
    private final CourseStudentRepository courseStudentRepository;

    public IncomingFilesetEntity getFilesetData(UUID schoolID) {
        return incomingFilesetRepository.findBySchoolIDAndFilesetStatusCode(schoolID, FilesetStatus.LOADED.getCode())
                .orElseThrow(() -> new EntityNotFoundException(School.class, "schoolID", schoolID.toString()));
    }

    public ErrorAndWarningSummary getErrorAndWarningSummary(UUID schoolID) {
        IncomingFilesetEntity filesetEntity = incomingFilesetRepository.findBySchoolIDAndFilesetStatusCode(schoolID, FilesetStatus.LOADED.getCode())
                .orElseThrow(() -> new EntityNotFoundException(School.class, "schoolID", schoolID.toString()));

        UUID filesetID = filesetEntity.getIncomingFilesetID();

        ErrorAndWarningSummary summary = ErrorAndWarningSummary.builder()
                .schoolID(schoolID.toString())
                .filesetID(filesetID.toString())
                .totalStudents(String.valueOf(errorFilesetStudentRepository.countAllByIncomingFileset_IncomingFilesetID(filesetID)))
                .build();

        Map<String, Long> demCountsMap = getCountsMap(demographicStudentRepository.countValidationIssuesBySeverity(filesetID));
        long demErrorCount = demCountsMap.getOrDefault("ERROR", 0L);
        long demWarningCount = demCountsMap.getOrDefault("WARNING", 0L);
        summary.setDemCounts(createFileWarningErrorCounts(demErrorCount, demWarningCount));

        Map<String, Long> xamCountsMap = getCountsMap(assessmentStudentRepository.countValidationIssuesBySeverity(filesetID));
        long xamErrorCount = xamCountsMap.getOrDefault("ERROR", 0L);
        long xamWarningCount = xamCountsMap.getOrDefault("WARNING", 0L);
        summary.setXamCounts(createFileWarningErrorCounts(xamErrorCount, xamWarningCount));

        Map<String, Long> crsCountsMap = getCountsMap(courseStudentRepository.countValidationIssuesBySeverity(filesetID));
        long crsErrorCount = crsCountsMap.getOrDefault("ERROR", 0L);
        long crsWarningCount = crsCountsMap.getOrDefault("WARNING", 0L);
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