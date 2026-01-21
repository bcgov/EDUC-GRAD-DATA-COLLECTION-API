package ca.bc.gov.educ.graddatacollection.api.service.v1.reports;

import ca.bc.gov.educ.graddatacollection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.ErrorFilesetStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.FinalIncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.FinalErrorFilesetStudentRepository;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.FinalIncomingFilesetRepository;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.struct.external.institute.v1.SchoolTombstone;
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
import java.time.format.DateTimeFormatter;
import java.util.*;

import static ca.bc.gov.educ.graddatacollection.api.constants.v1.reports.ErrorReportHeaders.*;
import static ca.bc.gov.educ.graddatacollection.api.constants.v1.reports.ReportTypeCodes.STUDENT_ERROR_REPORT;

@Service
@Slf4j
public class CSVReportService {
    private final FinalErrorFilesetStudentRepository errorFilesetStudentRepository;
    private final FinalIncomingFilesetRepository incomingFilesetRepository;
    private static final ErrorFilesetStudentMapper errorFilesetStudentMapper = ErrorFilesetStudentMapper.mapper;
    private final RestUtils restUtils;

    @Autowired
    public CSVReportService(FinalErrorFilesetStudentRepository errorFilesetStudentRepository, FinalIncomingFilesetRepository incomingFilesetRepository, RestUtils restUtils) {
        this.errorFilesetStudentRepository = errorFilesetStudentRepository;
        this.incomingFilesetRepository = incomingFilesetRepository;
        this.restUtils = restUtils;
    }

    public DownloadableReportResponse generateErrorReport(UUID incomingFilesetId) {
        Optional<FinalIncomingFilesetEntity> optionalIncomingFilesetEntity =  incomingFilesetRepository.findById(incomingFilesetId);
        FinalIncomingFilesetEntity incomingFileset = optionalIncomingFilesetEntity.orElseThrow(() -> new EntityNotFoundException(FinalIncomingFilesetEntity.class, "incomingFilesetID", incomingFilesetId.toString()));
        Optional<SchoolTombstone> optionalSchoolTombstones = restUtils.getSchoolBySchoolID(incomingFileset.getSchoolID().toString());
        SchoolTombstone schoolTombstone = optionalSchoolTombstones.orElseThrow(() -> new EntityNotFoundException(SchoolTombstone.class,"incomingFilesetSchoolId", incomingFileset.getSchoolID().toString()));
        List<ErrorFilesetStudent> results = errorFilesetStudentRepository.findAllByIncomingFileset_IncomingFilesetID(incomingFilesetId)
                .stream()
                .map(errorFilesetStudentMapper::toStructure)
                .toList();

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(PEN.getCode(),LOCAL_ID.getCode(), LAST_NAME.getCode(), FIRST_NAME.getCode(), DATE_OF_BIRTH.getCode(), FILE_TYPE.getCode(), SEVERITY_CODE.getCode(), ERROR_CONTEXT.getCode(), FIELD.getCode(), DESCRIPTION.getCode())
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
            downloadableReport.setReportName(String.format("%s - Graduation Data Error Report - %s", schoolTombstone.getMincode(), incomingFileset.getCreateDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
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
                        result.getBirthdate(),
                        issue.getErrorFilesetValidationIssueTypeCode(),
                        issue.getValidationIssueSeverityCode(),
                        issue.getErrorContext(),
                        issue.getValidationIssueFieldCode(),
                        issue.getValidationIssueDescription()
                ))
                .toList();
    }
}
