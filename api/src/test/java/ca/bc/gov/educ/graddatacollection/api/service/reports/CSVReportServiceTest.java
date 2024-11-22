package ca.bc.gov.educ.graddatacollection.api.service.reports;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.service.v1.reports.CSVReportService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorFilesetStudent;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorFilesetStudentValidationIssue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CSVReportServiceTest extends BaseGradDataCollectionAPITest {
    @Autowired
    CSVReportService csvReportService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPrepareErrorDataForCsvEmptyIssues() {
        ErrorFilesetStudent student = new ErrorFilesetStudent();
        student.setPen("123456789");
        student.setLocalID("LOC123");
        student.setLastName("Doe");
        student.setFirstName("John");
        student.setErrorFilesetStudentValidationIssues(new ArrayList<>());

        List<List<String>> result = csvReportService.prepareErrorDataForCsv(student);

        assertEquals(0, result.size());
    }

    @Test
    void testPrepareErrorDataForCsvCourseIssue() {
        ErrorFilesetStudentValidationIssue issue = new ErrorFilesetStudentValidationIssue();
        issue.setErrorFilesetValidationIssueTypeCode("COURSE");
        issue.setValidationIssueSeverityCode("ERROR");
        issue.setValidationIssueCode("DEM_DATA_MISSING");

        ErrorFilesetStudent student = new ErrorFilesetStudent();
        student.setPen("123456789");
        student.setLocalID("LOC123");
        student.setLastName("Doe");
        student.setFirstName("John");
        student.setErrorFilesetStudentValidationIssues(List.of(issue));

        List<List<String>> result = csvReportService.prepareErrorDataForCsv(student);

        assertEquals(1, result.size());
        assertEquals(
                List.of(
                        "123456789",
                        "LOC123",
                        "Doe",
                        "John",
                        "",
                        "COURSE",
                        "ERROR",
                        "This student is missing demographic data based on Student PEN, Surname and Local ID."
                ),
                result.get(0)
        );
    }

    @Test
    void testPrepareErrorDataForCsvAssessmentIssue() {
        ErrorFilesetStudentValidationIssue issue = new ErrorFilesetStudentValidationIssue();
        issue.setErrorFilesetValidationIssueTypeCode("ASSESSMENT");
        issue.setValidationIssueSeverityCode("ERROR");
        issue.setValidationIssueCode("DEM_DATA_MISSING");

        ErrorFilesetStudent student = new ErrorFilesetStudent();
        student.setPen("123456789");
        student.setLocalID("LOC123");
        student.setLastName("Doe");
        student.setFirstName("John");
        student.setErrorFilesetStudentValidationIssues(List.of(issue));

        List<List<String>> result = csvReportService.prepareErrorDataForCsv(student);

        assertEquals(1, result.size());
        assertEquals(
                List.of(
                        "123456789",
                        "LOC123",
                        "Doe",
                        "John",
                        "",
                        "ASSESSMENT",
                        "ERROR",
                        "This student is missing demographic data based on Student PEN, Surname, Mincode and Local ID."
                ),
                result.get(0)
        );
    }

    @Test
    void testPrepareErrorDataForCsvDemographicsIssue() {
        ErrorFilesetStudentValidationIssue issue = new ErrorFilesetStudentValidationIssue();
        issue.setErrorFilesetValidationIssueTypeCode("DEMOGRAPHICS");
        issue.setValidationIssueSeverityCode("ERROR");
        issue.setValidationIssueCode("STUDENTPENBLANK");

        ErrorFilesetStudent student = new ErrorFilesetStudent();
        student.setPen("123456789");
        student.setLocalID("LOC123");
        student.setLastName("Doe");
        student.setFirstName("John");
        student.setErrorFilesetStudentValidationIssues(List.of(issue));

        List<List<String>> result = csvReportService.prepareErrorDataForCsv(student);

        assertEquals(1, result.size());
        assertEquals(
                List.of(
                        "123456789",
                        "LOC123",
                        "Doe",
                        "John",
                        "",
                        "DEMOGRAPHICS",
                        "ERROR",
                        "PEN is blank. Correct PEN in system or through PEN Web."
                ),
                result.get(0)
        );
    }
}
