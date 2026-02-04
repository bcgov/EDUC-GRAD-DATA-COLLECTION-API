package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.FacilityTypeCodes;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.SchoolCategoryCodes;
import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ReportingPeriodEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.FinalIncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ReportingPeriodRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ReportingCycleSummary;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SchoolSubmissionCount;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SchoolYearReportingSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportingSummaryService {

    private final FinalIncomingFilesetRepository finalIncomingFilesetRepository;
    private final ReportingPeriodRepository reportingPeriodRepository;
    private final RestUtils restUtils;
    private static final String SUMMER_COLLECTION_TYPE = "Summer";

    private static final String STANDARD_TYPE = "Standard";
    private static final String CONT_ED_TYPE = "Continuing Ed";
    private static final String PROVINCIAL_TYPE = "Provincial Online Learning";
    private static final String DIST_LEARN_TYPE = "District Online Learning";
    private static final String ALT_PROGS_TYPE = "Alternate Programs";
    private static final String SHORT_PRP_TYPE = "Short Term PRP";
    private static final String LONG_PRP_TYPE = "Long Term PRP";
    private static final String SUMMER_TYPE = "Summer";
    private static final String YOUTH_TYPE = "Youth Custody/Residential";

    public ReportingCycleSummary getReportingSummary(UUID reportingPeriodID, String type) {
        ReportingPeriodEntity reportingEntity = reportingPeriodRepository.findById(reportingPeriodID).orElseThrow(() -> new EntityNotFoundException(ReportingPeriodEntity.class, "reportingPeriodID", reportingPeriodID.toString()));
        boolean isSummer = type.equalsIgnoreCase(SUMMER_COLLECTION_TYPE);
        return getSchoolYearSummary(reportingEntity, isSummer);
    }

    public ReportingCycleSummary getSchoolYearSummary(ReportingPeriodEntity reportingEntity, boolean isSummer) {
        List<SchoolSubmissionCount> submissionCount;
        List<SchoolSubmissionCount> last30DaysSubmissionCount;

        if(isSummer) {
            submissionCount = finalIncomingFilesetRepository.findSchoolSubmissionsInSummerReportingPeriod(reportingEntity.getReportingPeriodID(), reportingEntity.getSummerStart(), reportingEntity.getSummerEnd());
            last30DaysSubmissionCount = finalIncomingFilesetRepository.findSchoolSubmissionsInLast30Days(reportingEntity.getReportingPeriodID(), reportingEntity.getSummerStart());
        } else {
            submissionCount = finalIncomingFilesetRepository.findSchoolSubmissionsInSchoolReportingPeriod(reportingEntity.getReportingPeriodID(), reportingEntity.getSchYrStart(), reportingEntity.getSchYrEnd());
            last30DaysSubmissionCount = finalIncomingFilesetRepository.findSchoolSubmissionsInLast30Days(reportingEntity.getReportingPeriodID(), reportingEntity.getSchYrStart());
        }

        List<SchoolTombstone> schools = getEligibleSchools(reportingEntity, isSummer);
        ReportingCycleSummary summary = new ReportingCycleSummary();
        summary.setRows(new ArrayList<>());

        List<SchoolYearReportingSummary> rows = new ArrayList<>();

        for (Map.Entry<String, String> title : getSchoolCategoryTitles().entrySet()) {
            List<SchoolYearReportingSummary> rowData = new ArrayList<>();
            var filteredBySchoolCategory = schools.stream().filter(schoolTombstone -> schoolTombstone.getSchoolCategoryCode().equalsIgnoreCase(title.getKey())).map(SchoolTombstone::getSchoolId).toList();
            var totalSchools = filteredBySchoolCategory.size();

            var schoolCategoryInSubmission = submissionCount.stream().filter(submission -> filteredBySchoolCategory.contains(submission.getSchoolID())).toList();
            var totalSubmissionByCategory = schoolCategoryInSubmission.stream().map(SchoolSubmissionCount::getSubmissionCount).mapToInt(Integer::valueOf).sum();

            var schoolCategoryInSubmissionLast30Days = last30DaysSubmissionCount.stream().filter(submission -> filteredBySchoolCategory.contains(submission.getSchoolID())).map(SchoolSubmissionCount::getSchoolID).toList();
            var schoolsWithoutSubmissionLast30Days = totalSchools - schoolCategoryInSubmissionLast30Days.size();

            rowData.add(SchoolYearReportingSummary.builder()
                .categoryOrFacilityType(title.getValue())
                .isSection("true")
                .schoolsExpected(String.valueOf(totalSchools))
                .schoolsWithSubmissions(String.valueOf(totalSubmissionByCategory))
                .schoolsWitSubmissionsInLast30DaysCount(String.valueOf(schoolsWithoutSubmissionLast30Days))
                .build());

            for (Map.Entry<String, String> facilityTitle : getFacilityTitles(title.getKey()).entrySet()) {
                var filteredByCategoryAndFacility = schools.stream().filter(schoolTombstone -> schoolTombstone.getSchoolCategoryCode().equalsIgnoreCase(title.getKey()) && schoolTombstone.getFacilityTypeCode().equalsIgnoreCase(facilityTitle.getKey())).map(SchoolTombstone::getSchoolId).toList();
                var totalFacilityExpected = filteredByCategoryAndFacility.size();

                var schoolFacilityInSubmission = submissionCount.stream().filter(submission -> filteredByCategoryAndFacility.contains(submission.getSchoolID())).toList();
                var totalSubmissionByFacility = schoolFacilityInSubmission.stream().map(SchoolSubmissionCount::getSubmissionCount).mapToInt(Integer::valueOf).sum();

                var schoolFacilityInSubmissionLast30Days = last30DaysSubmissionCount.stream().filter(submission -> filteredByCategoryAndFacility.contains(submission.getSchoolID())).map(SchoolSubmissionCount::getSchoolID).toList();
                var schoolsWithoutSubmissionLast30DaysFacility = totalFacilityExpected - schoolFacilityInSubmissionLast30Days.size();

                rowData.add(SchoolYearReportingSummary.builder()
                    .categoryOrFacilityType(facilityTitle.getValue())
                    .isSection("false")
                    .schoolsExpected(String.valueOf(totalFacilityExpected))
                    .schoolsWithSubmissions(String.valueOf(totalSubmissionByFacility))
                    .schoolsWitSubmissionsInLast30DaysCount(String.valueOf(schoolsWithoutSubmissionLast30DaysFacility))
                    .build());
            }
            rows.addAll(rowData);
        }
        summary.setRows(rows);
        return summary;
    }

    public List<SchoolSubmissionCount> getSchoolSubmissionCounts(UUID reportingPeriodID, String categoryCode, Boolean isSummer) {
        ReportingPeriodEntity reportingEntity = reportingPeriodRepository.findById(reportingPeriodID).orElseThrow(() -> new EntityNotFoundException(ReportingPeriodEntity.class, "reportingPeriodID", reportingPeriodID.toString()));

        List<SchoolSubmissionCount> submissionCount;
        if (Boolean.TRUE.equals(isSummer)) {
            submissionCount = finalIncomingFilesetRepository.findSchoolSubmissionsInSummerReportingPeriod(reportingPeriodID, reportingEntity.getSummerStart(), reportingEntity.getSummerEnd());
        } else {
            submissionCount = finalIncomingFilesetRepository.findSchoolSubmissionsInSchoolReportingPeriod(reportingPeriodID, reportingEntity.getSchYrStart(), reportingEntity.getSchYrEnd());
        }

        List<SchoolTombstone> schools = getEligibleSchools(reportingEntity, isSummer);
        
        if (categoryCode != null && !categoryCode.isEmpty()) {
            Map<String, String> schoolToFacilityTypeMap = schools.stream()
                .collect(Collectors.toMap(
                    SchoolTombstone::getSchoolId,
                    SchoolTombstone::getSchoolCategoryCode,
                    (existing, replacement) -> existing
                ));
            
            submissionCount = submissionCount.stream()
                .filter(submission -> {
                    String schoolFacilityType = schoolToFacilityTypeMap.get(submission.getSchoolID());
                    return schoolFacilityType != null && schoolFacilityType.equalsIgnoreCase(categoryCode);
                })
                .toList();
        }

        return submissionCount;
    }

    private Map<String, String> getSchoolCategoryTitles() {
        Map<String, String> rowTitles = new LinkedHashMap<>();
        rowTitles.put(SchoolCategoryCodes.PUBLIC.getCode(), "Public");
        rowTitles.put(SchoolCategoryCodes.INDEPEND.getCode(), "Independent");
        rowTitles.put(SchoolCategoryCodes.INDP_FNS.getCode(), "Ind. First Nations");
        rowTitles.put(SchoolCategoryCodes.FED_BAND.getCode(), "Non-Ind. First Nations");
        rowTitles.put(SchoolCategoryCodes.OFFSHORE.getCode(), "Offshore");
        rowTitles.put(SchoolCategoryCodes.YUKON.getCode(), "Yukon");
        return rowTitles;
    }

    private Map<String, String> getFacilityTitles(String schoolCategory) {
        return switch (schoolCategory) {
            case "PUBLIC" -> getPublicFacilityTitles();
            case "INDEPEND" -> getIndpFacilityTitles();
            case "INDP_FNS" -> getIndpFnsFacilityTitles();
            case "FED_BAND" -> getFedBandFacilityTitles();
            case "OFFSHORE" -> getOffshoreFacilityTitles();
            case "YUKON" -> getYukonFacilityTitles();
            default -> {
                log.error("Unexpected header title.  This cannot happen::" + schoolCategory);
                throw new GradDataCollectionAPIRuntimeException("Unexpected header title.  This cannot happen::" + schoolCategory);
            }
        };
    }

    private Map<String, String> getPublicFacilityTitles() {
        Map<String, String> rowTitles = new LinkedHashMap<>();
        rowTitles.put(FacilityTypeCodes.STANDARD.getCode(), STANDARD_TYPE);
        rowTitles.put(FacilityTypeCodes.CONT_ED.getCode(), CONT_ED_TYPE);
        rowTitles.put(FacilityTypeCodes.DIST_LEARN.getCode(), PROVINCIAL_TYPE);
        rowTitles.put(FacilityTypeCodes.DISTONLINE.getCode(), DIST_LEARN_TYPE);
        rowTitles.put(FacilityTypeCodes.ALT_PROGS.getCode(), ALT_PROGS_TYPE);
        rowTitles.put(FacilityTypeCodes.SHORT_PRP.getCode(), SHORT_PRP_TYPE);
        rowTitles.put(FacilityTypeCodes.LONG_PRP.getCode(), LONG_PRP_TYPE);
        rowTitles.put(FacilityTypeCodes.SUMMER.getCode(), SUMMER_TYPE);
        rowTitles.put(FacilityTypeCodes.YOUTH.getCode(), YOUTH_TYPE);
        return rowTitles;
    }
    private Map<String, String> getIndpFacilityTitles() {
        Map<String, String> rowTitles = new LinkedHashMap<>();
        rowTitles.put(FacilityTypeCodes.STANDARD.getCode(), STANDARD_TYPE);
        rowTitles.put(FacilityTypeCodes.DIST_LEARN.getCode(), PROVINCIAL_TYPE);
        return rowTitles;
    }
    private Map<String, String> getIndpFnsFacilityTitles() {
        Map<String, String> rowTitles = new LinkedHashMap<>();
        rowTitles.put(FacilityTypeCodes.STANDARD.getCode(), STANDARD_TYPE);
        return rowTitles;
    }
    private Map<String, String> getFedBandFacilityTitles() {
        Map<String, String> rowTitles = new LinkedHashMap<>();
        rowTitles.put(FacilityTypeCodes.STANDARD.getCode(), STANDARD_TYPE);
        return rowTitles;
    }
    private Map<String, String> getOffshoreFacilityTitles() {
        Map<String, String> rowTitles = new LinkedHashMap<>();
        rowTitles.put(FacilityTypeCodes.STANDARD.getCode(), STANDARD_TYPE);
        return rowTitles;
    }
    private Map<String, String> getYukonFacilityTitles() {
        Map<String, String> rowTitles = new LinkedHashMap<>();
        rowTitles.put(FacilityTypeCodes.STANDARD.getCode(), STANDARD_TYPE);
        rowTitles.put(FacilityTypeCodes.DIST_LEARN.getCode(), PROVINCIAL_TYPE);
        rowTitles.put(FacilityTypeCodes.SUMMER.getCode(), SUMMER_TYPE);
        return rowTitles;
    }

    private List<SchoolTombstone> getEligibleSchools(ReportingPeriodEntity reportingPeriodEntity, boolean isSummer) {
        LocalDateTime cutoffDate;
        if (isSummer && reportingPeriodEntity.getSummerEnd() != null) {
            cutoffDate = reportingPeriodEntity.getSummerEnd();
        } else if (!isSummer && reportingPeriodEntity.getSchYrEnd() != null) {
            cutoffDate = reportingPeriodEntity.getSchYrEnd();
        } else {
            cutoffDate = LocalDateTime.now();
        }

        return restUtils.getAllSchools().stream()
            .filter(school -> school.getOpenedDate() != null)
            .filter(school -> {
                var gradSchool = restUtils.getGradSchoolBySchoolID(school.getSchoolId());
                return gradSchool.isPresent() && "Y".equalsIgnoreCase(gradSchool.get().getCanIssueTranscripts());
            })
            .filter(school -> {
                try {
                    LocalDateTime openDate = LocalDateTime.parse(school.getOpenedDate());
                    LocalDateTime endOfCloseDateGraceWindow = null;
                    if (school.getClosedDate() != null) {
                        endOfCloseDateGraceWindow = LocalDateTime.parse(school.getClosedDate()).plusMonths(3);
                    }
                    if (cutoffDate.isBefore(openDate)) return false;
                    return endOfCloseDateGraceWindow == null || !cutoffDate.isAfter(endOfCloseDateGraceWindow);
                } catch (Exception e) {
                    return false;
                }
            })
            .toList();
    }
}
