package ca.bc.gov.educ.graddatacollection.api.service.v1.reports;

import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
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
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class CSVReportService {

    public DownloadableReportResponse generateErrorReport() {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().build();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(byteArrayOutputStream));
            CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);

            csvPrinter.flush();

            var downloadableReport = new DownloadableReportResponse();
            downloadableReport.setDocumentData(Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()));
            return downloadableReport;

        } catch (IOException e) {
            throw new GradDataCollectionAPIRuntimeException(e);
        }
    }
}
