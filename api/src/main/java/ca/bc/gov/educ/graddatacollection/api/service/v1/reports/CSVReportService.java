package ca.bc.gov.educ.graddatacollection.api.service.v1.reports;

import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.ErrorFilesetStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.ErrorFilesetStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorFilesetStudent;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorFilesetStudentValidationIssue;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.reports.DownloadableReportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.stereotype.Service;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

import static ca.bc.gov.educ.graddatacollection.api.constants.v1.reports.ErrorReportHeaders.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CSVReportService {
    private final ErrorFilesetStudentRepository errorFilesetStudentRepository;
    private static final ErrorFilesetStudentMapper errorFilesetStudentMapper = ErrorFilesetStudentMapper.mapper;

    public DownloadableReportResponse generateErrorReport(UUID incomingFilesetId) {
        List<ErrorFilesetStudent> results = errorFilesetStudentRepository.findAllByIncomingFileset_IncomingFilesetID(incomingFilesetId)
                .stream()
                .map(errorFilesetStudentMapper::toStructure)
                .toList();

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(PEN.getCode(),LOCAL_ID.getCode(), LAST_NAME.getCode(), FIRST_NAME.getCode(), DATE_OF_BIRTH.getCode(), VALIDATION_ISSUES.getCode())
                .build();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(byteArrayOutputStream));
            CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);

            for (ErrorFilesetStudent result : results) {
                List<String> csvRowData = prepareErrorDataForCsv(result);

                if (csvRowData != null) {
                    csvPrinter.printRecord(csvRowData);
                }
            }

            csvPrinter.flush();

            var downloadableReport = new DownloadableReportResponse();
            downloadableReport.setDocumentData(Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()));
            return downloadableReport;

        } catch (IOException e) {
            throw new GradDataCollectionAPIRuntimeException(e);
        }
    }

    private List<String> prepareErrorDataForCsv(ErrorFilesetStudent result) {
        String validationIssues = processValidationIssuesForField(result.getErrorFilesetStudentValidationIssues());

        return Arrays.asList(
           result.getPen(),
           result.getLocalID(),
           result.getLastName(),
           result.getFirstName(),
           null,
           validationIssues
        );
    }

    private String processValidationIssuesForField(List<ErrorFilesetStudentValidationIssue> issues) {
        if (issues == null || issues.isEmpty()) {
            return "";
        }

        return issues.stream()
                .filter(Objects::nonNull)
                .map(issue -> String.format(
                        "%s %s %s %s",
                        issue.getErrorFilesetValidationIssueTypeCode(),
                        issue.getValidationIssueSeverityCode(),
                        issue.getValidationIssueFieldCode(),
                        issue.getValidationIssueCode()
                ))
                .distinct()
                .reduce((a, b) -> a + ";\n" + b)
                .orElse("");
    }
}
