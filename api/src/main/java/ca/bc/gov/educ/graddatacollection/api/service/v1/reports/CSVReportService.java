package ca.bc.gov.educ.graddatacollection.api.service.v1.reports;

import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.ErrorFilesetStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ErrorFilesetStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.rules.assessment.AssessmentStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.course.CourseStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.rules.demographic.DemographicStudentValidationIssueTypeCode;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorFilesetStudent;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.reports.DownloadableReportResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

import static ca.bc.gov.educ.graddatacollection.api.constants.v1.reports.ErrorReportHeaders.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.reports.ReportTypeCodes.*;

@Service
@Slf4j
public class CSVReportService {
    private final ErrorFilesetStudentRepository errorFilesetStudentRepository;
    private static final ErrorFilesetStudentMapper errorFilesetStudentMapper = ErrorFilesetStudentMapper.mapper;

    @Autowired
    public CSVReportService(ErrorFilesetStudentRepository errorFilesetStudentRepository) {
        this.errorFilesetStudentRepository = errorFilesetStudentRepository;
    }

    public DownloadableReportResponse generateErrorReport(UUID incomingFilesetId) {
        List<ErrorFilesetStudent> results = errorFilesetStudentRepository.findAllByIncomingFileset_IncomingFilesetID(incomingFilesetId)
                .stream()
                .map(errorFilesetStudentMapper::toStructure)
                .toList();

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(PEN.getCode(),LOCAL_ID.getCode(), LAST_NAME.getCode(), FIRST_NAME.getCode(), DATE_OF_BIRTH.getCode(), FILE_TYPE.getCode(), SEVERITY_CODE.getCode(), DESCRIPTION.getCode())
                .build();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(byteArrayOutputStream));
            CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);

            for (ErrorFilesetStudent student : results) {
                List<List<String>> rows = prepareErrorDataForCsv(student);
                for (List<String> row : rows) {
                    csvPrinter.printRecord(row);
                }
            }

            csvPrinter.flush();

            var downloadableReport = new DownloadableReportResponse();
            downloadableReport.setReportType(STUDENT_ERROR_REPORT.getCode());
            downloadableReport.setDocumentData(Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()));
            return downloadableReport;

        } catch (IOException e) {
            throw new GradDataCollectionAPIRuntimeException(e);
        }
    }

    public List<List<String>> prepareErrorDataForCsv(ErrorFilesetStudent result) {
        return result.getErrorFilesetStudentValidationIssues().stream()
                .map(issue -> Arrays.asList(
                        result.getPen(),
                        result.getLocalID(),
                        result.getLastName(),
                        result.getFirstName(),
                        "",
                        issue.getErrorFilesetValidationIssueTypeCode(),
                        issue.getValidationIssueSeverityCode(),
                        switch (issue.getErrorFilesetValidationIssueTypeCode()) {
                            case "ASSESSMENT" -> AssessmentStudentValidationIssueTypeCode.findByValue(issue.getValidationIssueCode()).getMessage();
                            case "COURSE" -> CourseStudentValidationIssueTypeCode.findByValue(issue.getValidationIssueCode()).getMessage();
                            case "DEMOGRAPHICS" -> DemographicStudentValidationIssueTypeCode.findByValue(issue.getValidationIssueCode()).getMessage();
                            default -> "";
                        }
                ))
                .toList();
    }
}
