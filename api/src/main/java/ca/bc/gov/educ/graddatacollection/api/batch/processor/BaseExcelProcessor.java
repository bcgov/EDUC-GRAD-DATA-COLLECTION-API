package ca.bc.gov.educ.graddatacollection.api.batch.processor;

import ca.bc.gov.educ.graddatacollection.api.batch.constants.ColumnType;
import ca.bc.gov.educ.graddatacollection.api.batch.constants.Headers;
import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileError;
import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradFileExcelProcessor;
import ca.bc.gov.educ.graddatacollection.api.batch.validation.GradFileValidator;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradCollectionStatus;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ReportingPeriodEntity;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.service.v1.ReportingPeriodService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ReportingPeriod;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SummerStudentData;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SummerStudentDataResponse;
import ca.bc.gov.educ.graddatacollection.api.util.PenUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.*;

@Slf4j
public abstract class BaseExcelProcessor implements GradFileExcelProcessor {
    protected final ApplicationProperties applicationProperties;
    private static final List<String> MANDATORY_HEADERS = Arrays.stream(Headers.values()).map(Headers::getCode).toList();
    private static final String STRING_TYPE = "String type :: {}";
    private static final String DATE_TYPE = "Date type :: {}";
    private static final String NUMBER_TYPE = "Number type :: {}";
    private final GradFileValidator gradFileValidator;
    private final ReportingPeriodService reportingPeriodService;

    protected BaseExcelProcessor(ApplicationProperties applicationProperties, GradFileValidator gradFileValidator, ReportingPeriodService reportingPeriodService) {
        this.applicationProperties = applicationProperties;
        this.gradFileValidator = gradFileValidator;
        this.reportingPeriodService = reportingPeriodService;
    }

    protected File getFile(final byte[] fileContents, final String code) throws IOException {
        final Path path = Files.createTempFile(Paths.get(applicationProperties.getFolderBasePath()), "grad-", code);
        Files.write(path, fileContents);
        final File outputFile = path.toFile();
        outputFile.deleteOnExit();
        return outputFile;
    }

    protected SummerStudentDataResponse processSheet(final Sheet sheet, final String schoolID, final String districtID, final String guid) throws FileUnProcessableException {
        final Map<Integer, String> headersMap = new HashMap<>();
        final int rowEnd = sheet.getLastRowNum();
        final List<SummerStudentData> summerStudents = new ArrayList<>();
        var reportingPeriod = reportingPeriodService.getActiveReportingPeriod();
        if(sheet.getRow(0) == null) {
            throw new FileUnProcessableException(FileError.EMPTY_EXCEL_NOT_ALLOWED, guid, GradCollectionStatus.LOAD_FAIL);
        }
        for (int rowNum = 0; rowNum <= rowEnd; rowNum++) {
            final Row r = sheet.getRow(rowNum);
            if (rowNum == 0 && r == null) {
                throw new FileUnProcessableException(FileError.FILE_NOT_ALLOWED, guid, GradCollectionStatus.LOAD_FAIL);
            } else if (r == null) {
                log.warn("empty row at :: {}", rowNum);
                continue;
            }
            final SummerStudentData summerStudent = SummerStudentData.builder().build();
            final int lastColumn = r.getLastCellNum();
            for (int cn = 0; cn < lastColumn; cn++) {
                this.processEachColumn(guid, headersMap, rowNum, r, summerStudent, cn, schoolID, districtID, reportingPeriod);
            }
            this.populateRowData(guid, headersMap, summerStudents, rowNum, summerStudent);
        }

        return SummerStudentDataResponse.builder().headers(new ArrayList<>(headersMap.values())).summerStudents(summerStudents).build();
    }

    private void populateRowData(final String guid, final Map<Integer, String> headersMap, final List<SummerStudentData> summerStudents, final int rowNum, final SummerStudentData summerStudent) throws FileUnProcessableException {
        if (rowNum == 0) {
            log.debug("Headers Map is populated as :: {}", headersMap);
            this.checkForValidHeaders(guid, headersMap);
        } else {
            if (summerStudent != null && !summerStudent.isEmpty() && StringUtils.isNotBlank(summerStudent.getPen())) {
                summerStudents.add(summerStudent);
            }
        }
    }

    private void checkForValidHeaders(final String guid, final Map<Integer, String> headersMap) throws FileUnProcessableException {
        val headerNames = headersMap.values();
        for (val headerName : MANDATORY_HEADERS) {
            if (!headerNames.contains(headerName)) {
                throw new FileUnProcessableException(FileError.MISSING_MANDATORY_HEADER, guid, GradCollectionStatus.LOAD_FAIL, headerName);
            }
        }
    }

    private void processEachColumn(final String guid, final Map<Integer, String> headersMap, final int rowNum, final Row r, SummerStudentData summerStudent, final int cn, final String schoolID, final String districtID, final ReportingPeriodEntity reportingPeriod) throws FileUnProcessableException {
        if (rowNum == 0) {
            this.handleHeaderRow(r, cn, guid, headersMap);
        } else if (StringUtils.isNotBlank(headersMap.get(cn))) {
            this.handleEachCell(r, cn, rowNum, headersMap, summerStudent, schoolID, districtID, guid, reportingPeriod);
        }
    }

    private void handleHeaderRow(final Row r, final int cn, final String guid, final Map<Integer, String> headersMap) throws FileUnProcessableException {
        final Cell cell = r.getCell(cn, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            throw new FileUnProcessableException(FileError.BLANK_CELL_IN_HEADING_ROW, guid, GradCollectionStatus.LOAD_FAIL, String.valueOf(cn));
        }
        val headerNameFromFile = StringUtils.trim(cell.getStringCellValue());
        val headerOptional = Headers.fromString(headerNameFromFile);
        headerOptional.ifPresent(header -> headersMap.put(cn, StringUtils.trim(header.getCode())));
    }

    private void handleEachCell(final Row r, final int cn, final int rowNum, final Map<Integer, String> headersMap, final SummerStudentData summerStudent, final String schoolID, final String districtID, final String guid, final ReportingPeriodEntity reportingPeriod) throws FileUnProcessableException {
        final Cell cell = r.getCell(cn, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        val headerNamesOptional = Headers.fromString(headersMap.get(cn));
        if (headerNamesOptional.isPresent()) {
            final Headers header = headerNamesOptional.get();
            switch (header) {
                case SCHOOL_CODE:
                    this.setSchoolCode(summerStudent, cell, header.getType(), schoolID, districtID, guid);
                    break;
                case PEN:
                    this.setPen(summerStudent, rowNum, cell, header.getType(), guid);
                    break;
                case LEGAL_SURNAME:
                    this.setLegalSurname(summerStudent, rowNum, cell, header.getType(), guid);
                    break;
                case LEGAL_MIDDLE_NAME:
                    this.setMiddleName(summerStudent, rowNum, cell, header.getType(), guid);
                    break;
                case LEGAL_FIRST_NAME:
                    this.setFirstName(summerStudent, rowNum, cell, header.getType(), guid);
                    break;
                case DOB:
                    this.setDOB(summerStudent, rowNum, cell, header.getType(), guid);
                    break;
                case COURSE:
                    this.setCourse(summerStudent, rowNum, cell, header.getType(), guid);
                    break;
                case SESSION_DATE:
                    this.setSessionDate(summerStudent, rowNum, cell, header.getType(), guid, reportingPeriod);
                    break;
                case FINAL_PERCENT:
                    this.setFinalPercent(summerStudent, rowNum, cell, header.getType(), guid);
                    break;
                case FINAL_LETTER_GRADE:
                    this.setFinalLetterGrade(summerStudent, rowNum, cell, header.getType(), guid);
                    break;
                case NO_OF_CREDITS:
                    this.setNoOfCredits(summerStudent, rowNum, cell, header.getType(), guid);
                    break;
            }
        } else {
            log.debug("Header :: '{}' is not configured.", headersMap.get(cn));
        }
    }

    private void setSchoolCode(final SummerStudentData summerStudent, final Cell cell, final ColumnType columnType, final String schoolID, final String districtID, final String guid) throws FileUnProcessableException {
        val fieldValue = this.getCellValueString(cell, columnType);
        validateSchoolCode(schoolID, districtID, fieldValue, guid);
        summerStudent.setSchoolCode(fieldValue);
    }

    private void setPen(final SummerStudentData summerStudent, final int rowNum, final Cell cell, final ColumnType columnType, final String guid) throws FileUnProcessableException {
        val fieldValue = this.getCellValueString(cell, columnType);
        if(StringUtils.isNotBlank(fieldValue)) {
            if(fieldValue.length() != 9) {
                throw new FileUnProcessableException(FileError.PEN_LENGTH_IN_EXCEL, guid, GradCollectionStatus.LOAD_FAIL, String.valueOf(rowNum));
            }else if(!PenUtil.validCheckDigit(fieldValue)) {
                throw new FileUnProcessableException(FileError.PEN_INVALID_IN_EXCEL, guid, GradCollectionStatus.LOAD_FAIL, String.valueOf(rowNum));
            }
        }
        summerStudent.setPen(fieldValue);
    }

    private void setLegalSurname(final SummerStudentData summerStudent, final int rowNum, final Cell cell,  final ColumnType columnType, final String guid) throws FileUnProcessableException {
        val fieldValue = this.getCellValueString(cell, columnType);
        if(StringUtils.isNotBlank(fieldValue) && fieldValue.length() > 25) {
            throw new FileUnProcessableException(FileError.LEGAL_SURNAME_IN_EXCEL, guid, GradCollectionStatus.LOAD_FAIL, String.valueOf(rowNum));
        }
        summerStudent.setLegalSurname(fieldValue);
    }

    private void setFirstName(final SummerStudentData summerStudent, final int rowNum, final Cell cell,  final ColumnType columnType, final String guid) throws FileUnProcessableException {
        val fieldValue = this.getCellValueString(cell, columnType);
        if(StringUtils.isNotBlank(fieldValue) && fieldValue.length() > 25) {
            throw new FileUnProcessableException(FileError.LEGAL_FIRST_NAME_IN_EXCEL, guid, GradCollectionStatus.LOAD_FAIL, String.valueOf(rowNum));
        }
        summerStudent.setLegalFirstName(fieldValue);
    }

    private void setCourse(final SummerStudentData summerStudent, final int rowNum, final Cell cell, final ColumnType columnType, final String guid) throws FileUnProcessableException {
        val fieldValue = this.getCellValueString(cell, columnType);
        if(StringUtils.isNotBlank(fieldValue) && fieldValue.length() > 8) {
            throw new FileUnProcessableException(FileError.COURSE_IN_EXCEL, guid, GradCollectionStatus.LOAD_FAIL, String.valueOf(rowNum));
        }
        summerStudent.setCourse(fieldValue);
    }
    private void setSessionDate(final SummerStudentData summerStudent, final int rowNum, final Cell cell, final ColumnType columnType, final String guid, final ReportingPeriodEntity reportingPeriod) throws FileUnProcessableException {
        val fieldValue = this.getCellValueString(cell, columnType);
        if(StringUtils.isNotBlank(fieldValue)) {
            try {
                var sessionDate = YearMonth.parse(fieldValue, DateTimeFormatter.ofPattern("yyyyMM"));
                if(!isValidSessionDate(sessionDate, reportingPeriod)) {
                    throw new FileUnProcessableException(FileError.SESSION_DATE_FORMAT_EXCEL, guid, GradCollectionStatus.LOAD_FAIL, String.valueOf(rowNum));
                }
                summerStudent.setSessionDate(String.valueOf(sessionDate));
            } catch (DateTimeParseException ex) {
                throw new FileUnProcessableException(FileError.SESSION_DATE_FORMAT_EXCEL, guid, GradCollectionStatus.LOAD_FAIL, String.valueOf(rowNum));
            }
        } else {
            summerStudent.setSessionDate(null);
        }
        summerStudent.setSessionDate(fieldValue);
    }

    private boolean isValidSessionDate(YearMonth sessionDate, final ReportingPeriodEntity reportingPeriod) {
        var startReportingPeriod = reportingPeriod.getPeriodStart();
        var endReportingPeriod = reportingPeriod.getPeriodEnd();

        var sessionLocalDate = sessionDate.atDay(1);

        return (sessionLocalDate.isEqual(endReportingPeriod.toLocalDate()) || sessionLocalDate.isBefore(endReportingPeriod.toLocalDate()))
                && (sessionLocalDate.isEqual(startReportingPeriod.toLocalDate()) || sessionLocalDate.isAfter(startReportingPeriod.toLocalDate()));
    }

    private void setFinalPercent(final SummerStudentData summerStudent, final int rowNum, final Cell cell, final ColumnType columnType, final String guid) throws FileUnProcessableException {
        val fieldValue = this.getCellValueString(cell, columnType);
        if(StringUtils.isNotBlank(fieldValue) && fieldValue.length() > 3) {
            throw new FileUnProcessableException(FileError.FINAL_SCH_PERCENT_EXCEL, guid, GradCollectionStatus.LOAD_FAIL, String.valueOf(rowNum));
        }
        summerStudent.setFinalPercent(fieldValue);
    }
    private void setFinalLetterGrade(final SummerStudentData summerStudent, final int rowNum, final Cell cell, final ColumnType columnType, final String guid) throws FileUnProcessableException {
        val fieldValue = this.getCellValueString(cell, columnType);
        if(StringUtils.isNotBlank(fieldValue) && fieldValue.length() > 2) {
            throw new FileUnProcessableException(FileError.FINAL_LETTER_GRADE_EXCEL, guid, GradCollectionStatus.LOAD_FAIL, String.valueOf(rowNum));
        }
        summerStudent.setFinalLetterGrade(fieldValue);
    }
    private void setNoOfCredits(final SummerStudentData summerStudent, final int rowNum, final Cell cell, final ColumnType columnType, final String guid) throws FileUnProcessableException {
        val fieldValue = this.getCellValueString(cell, columnType);
        if(StringUtils.isNotBlank(fieldValue) && fieldValue.length() > 1) {
            throw new FileUnProcessableException(FileError.NO_OF_CREDITS_EXCEL, guid, GradCollectionStatus.LOAD_FAIL, String.valueOf(rowNum));
        }
        summerStudent.setNoOfCredits(fieldValue);
    }
    private void setMiddleName(final SummerStudentData summerStudent, final int rowNum, final Cell cell, final ColumnType columnType, final String guid) throws FileUnProcessableException {
        val fieldValue = this.getCellValueString(cell, columnType);
        if(StringUtils.isNotBlank(fieldValue) && fieldValue.length() > 25) {
            throw new FileUnProcessableException(FileError.LEGAL_MIDDLE_NAME_IN_EXCEL, guid, GradCollectionStatus.LOAD_FAIL, String.valueOf(rowNum));
        }
        summerStudent.setLegalMiddleName(fieldValue);
    }

    private void setDOB(final SummerStudentData summerStudent, final int rowNum, final Cell cell, final ColumnType columnType, final String guid) throws FileUnProcessableException {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);
        val fieldValue = this.getCellValueString(cell, columnType);
        if(StringUtils.isNotBlank(fieldValue)) {
            try {
                LocalDate.parse(fieldValue, format);
            } catch (DateTimeParseException ex) {
                throw new FileUnProcessableException(FileError.BIRTHDATE_FORMAT_EXCEL, guid, GradCollectionStatus.LOAD_FAIL, String.valueOf(rowNum));
            }
        } else {
            summerStudent.setDob(null);
        }
        summerStudent.setDob(fieldValue);
    }

    private void validateSchoolCode(final String schoolID, final String districtID, final String mincodeFromFile, final String guid) throws FileUnProcessableException {
        if(districtID != null) {
            var schoolTombstone = gradFileValidator.getSchoolUsingMincode(mincodeFromFile);
            if(schoolTombstone.isPresent()) {
                gradFileValidator.validateFileUploadIsNotInProgress(guid, schoolTombstone.get().getSchoolId());
                gradFileValidator.validateSchoolIsOpenAndBelongsToDistrict(guid, schoolTombstone.get(), districtID);
            } else {
                throw new FileUnProcessableException(FileError.INVALID_SCHOOL, guid, GradCollectionStatus.LOAD_FAIL, mincodeFromFile);
            }

        } else {
            gradFileValidator.validateFileUploadIsNotInProgress(guid, schoolID);
            gradFileValidator.validateMincode(guid, schoolID, mincodeFromFile);
            var schoolTombstone =  gradFileValidator.getSchoolByID(guid, schoolID);
            gradFileValidator.validateSchoolIsTranscriptEligibleAndOpen(guid, schoolTombstone, schoolID);
        }
    }

    private String getCellValueString(final Cell cell,final ColumnType columnType) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                log.debug(STRING_TYPE, cell.getRichStringCellValue().getString());
                return cell.getRichStringCellValue().getString();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    val dateValue = cell.getDateCellValue();
                    log.debug(DATE_TYPE, dateValue);
                    return new SimpleDateFormat("yyyy-MM-dd").format(dateValue);
                }
                log.debug(NUMBER_TYPE, cell.getNumericCellValue());

                if(columnType.equals(ColumnType.DOUBLE)) {
                    return String.valueOf(cell.getNumericCellValue());
                }else{
                    Double value = cell.getNumericCellValue();
                    return String.valueOf(value.intValue());
                }
            default:
                log.debug("Default");
                return "";
        }
    }
}
