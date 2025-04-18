package ca.bc.gov.educ.graddatacollection.api.service.reports;

import ca.bc.gov.educ.graddatacollection.api.BaseGradDataCollectionAPITest;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.StudentStatusCodes;
import ca.bc.gov.educ.graddatacollection.api.rest.RestUtils;
import ca.bc.gov.educ.graddatacollection.api.service.v1.reports.CSVReportService;
import ca.bc.gov.educ.graddatacollection.api.struct.external.studentapi.v1.Student;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorFilesetStudent;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorFilesetStudentValidationIssue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CSVReportServiceTest extends BaseGradDataCollectionAPITest {
    @Autowired
    CSVReportService csvReportService;

    @MockBean
    private RestUtils restUtils;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        Student studentApiStudent = new Student();
        studentApiStudent.setStudentID(UUID.randomUUID().toString());
        studentApiStudent.setPen("123456789");
        studentApiStudent.setLocalID("8887555");
        studentApiStudent.setStatusCode(StudentStatusCodes.A.getCode());
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentApiStudent);
    }

    @Test
    void testPrepareErrorDataForCsvEmptyIssues() {
        ErrorFilesetStudent student = new ErrorFilesetStudent();
        student.setPen("123456789");
        student.setLocalID("8887555");
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
        issue.setValidationIssueFieldCode("PEN");
        issue.setValidationIssueDescription("This student appears in the CRS file but is missing from the DEM file. No course records for this student will be updated.");
        issue.setErrorContext("Test Context");

        ErrorFilesetStudent student = new ErrorFilesetStudent();
        student.setPen("123456789");
        student.setLocalID("8887555");
        student.setLastName("Doe");
        student.setFirstName("John");
        student.setBirthdate("20000101");
        student.setErrorFilesetStudentValidationIssues(List.of(issue));

        List<List<String>> result = csvReportService.prepareErrorDataForCsv(student);

        assertEquals(1, result.size());
        assertEquals(
                List.of(
                        "123456789",
                        "8887555",
                        "Doe",
                        "John",
                        "20000101",
                        "COURSE",
                        "ERROR",
                        "Test Context",
                        "PEN",
                        "This student appears in the CRS file but is missing from the DEM file. No course records for this student will be updated."
                ),
                result.get(0)
        );
    }

    @Test
    void testPrepareErrorDataForCsvAssessmentIssue() {
        ErrorFilesetStudentValidationIssue issue = new ErrorFilesetStudentValidationIssue();
        issue.setErrorFilesetValidationIssueTypeCode("ASSESSMENT");
        issue.setValidationIssueSeverityCode("ERROR");
        issue.setValidationIssueFieldCode("PEN");
        issue.setValidationIssueDescription("This student is missing demographic data based on Student PEN, Surname, Mincode and Local ID.");
        issue.setErrorContext("Test Context");

        ErrorFilesetStudent student = new ErrorFilesetStudent();
        student.setPen("123456789");
        student.setLocalID("8887555");
        student.setLastName("Doe");
        student.setFirstName("John");
        student.setBirthdate("20000101");
        student.setErrorFilesetStudentValidationIssues(List.of(issue));

        List<List<String>> result = csvReportService.prepareErrorDataForCsv(student);

        assertEquals(1, result.size());
        assertEquals(
                List.of(
                        "123456789",
                        "8887555",
                        "Doe",
                        "John",
                        "20000101",
                        "ASSESSMENT",
                        "ERROR",
                        "Test Context",
                        "PEN",
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
        issue.setValidationIssueFieldCode("PEN");
        issue.setValidationIssueDescription("PEN is blank so the student record cannot be updated. Ensure the correct PEN appears in system data file extracts.");
        issue.setErrorContext("");

        ErrorFilesetStudent student = new ErrorFilesetStudent();
        student.setPen("123456789");
        student.setLocalID("8887555");
        student.setLastName("Doe");
        student.setFirstName("John");
        student.setBirthdate("20000101");
        student.setErrorFilesetStudentValidationIssues(List.of(issue));

        List<List<String>> result = csvReportService.prepareErrorDataForCsv(student);

        assertEquals(1, result.size());
        assertEquals(
                List.of(
                        "123456789",
                        "8887555",
                        "Doe",
                        "John",
                        "20000101",
                        "DEMOGRAPHICS",
                        "ERROR",
                        "",
                        "PEN",
                        "PEN is blank so the student record cannot be updated. Ensure the correct PEN appears in system data file extracts."
                ),
                result.get(0)
        );
    }
}
