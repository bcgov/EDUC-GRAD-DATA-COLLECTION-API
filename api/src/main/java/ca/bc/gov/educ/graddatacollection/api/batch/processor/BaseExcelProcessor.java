package ca.bc.gov.educ.graddatacollection.api.batch.processor;

import ca.bc.gov.educ.graddatacollection.api.batch.constants.ColumnType;
import ca.bc.gov.educ.graddatacollection.api.batch.constants.Headers;
import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileError;
import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.batch.struct.GradFileExcelProcessor;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.GradCollectionStatus;
import ca.bc.gov.educ.graddatacollection.api.mappers.LocalDateMapper;
import ca.bc.gov.educ.graddatacollection.api.properties.ApplicationProperties;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SummerStudentData;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SummerStudentDataResponse;
import ca.bc.gov.educ.graddatacollection.api.util.DOBUtil;
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
import java.util.*;

@Slf4j
public abstract class BaseExcelProcessor implements GradFileExcelProcessor {
    protected final ApplicationProperties applicationProperties;
    private static final List<String> MANDATORY_HEADERS = Arrays.stream(Headers.values()).map(Headers::getCode).toList();
    private static final String STRING_TYPE = "String type :: {}";
    private static final String DATE_TYPE = "Date type :: {}";
    private static final String NUMBER_TYPE = "Number type :: {}";

    protected BaseExcelProcessor(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    protected File getFile(final byte[] fileContents, final String code) throws IOException {
        final Path path = Files.createTempFile(Paths.get(applicationProperties.getFolderBasePath()), "grad-", code);
        Files.write(path, fileContents);
        final File outputFile = path.toFile();
        outputFile.deleteOnExit();
        return outputFile;
    }

    protected SummerStudentDataResponse processSheet(final Sheet sheet, final String guid) throws FileUnProcessableException {
        final Map<Integer, String> headersMap = new HashMap<>();
        final int rowEnd = sheet.getLastRowNum();
        final List<SummerStudentData> summerStudents = new ArrayList<>();
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
                this.processEachColumn(guid, headersMap, rowNum, r, summerStudent, cn);
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
            if (summerStudent != null && !summerStudent.isEmpty()) {
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

    private void processEachColumn(final String correlationID, final Map<Integer, String> headersMap, final int rowNum, final Row r, SummerStudentData summerStudent, final int cn) throws FileUnProcessableException {
        if (rowNum == 0) {
            this.handleHeaderRow(r, cn, correlationID, headersMap);
        } else if (StringUtils.isNotBlank(headersMap.get(cn))) {
            this.handleEachCell(r, cn, headersMap, summerStudent);
        }
    }

    private void handleHeaderRow(final Row r, final int cn, final String correlationID, final Map<Integer, String> headersMap) throws FileUnProcessableException {
        final Cell cell = r.getCell(cn, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            throw new FileUnProcessableException(FileError.BLANK_CELL_IN_HEADING_ROW, correlationID, GradCollectionStatus.LOAD_FAIL, String.valueOf(cn));
        }
        val headerNameFromFile = StringUtils.trim(cell.getStringCellValue());
        val headerOptional = Headers.fromString(headerNameFromFile);
        headerOptional.ifPresent(header -> headersMap.put(cn, StringUtils.trim(header.getCode())));
    }

    private void handleEachCell(final Row r, final int cn, final Map<Integer, String> headersMap, final SummerStudentData summerStudent) {
        final Cell cell = r.getCell(cn, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        val headerNamesOptional = Headers.fromString(headersMap.get(cn));
        if (headerNamesOptional.isPresent()) {
            final Headers header = headerNamesOptional.get();
            switch (header) {
                case SCHOOL_CODE:
                    this.setSchoolCode(summerStudent, cell, header.getType());
                    break;
                case PEN:
                    this.setPen(summerStudent, cell, header.getType());
                    break;
                case LEGAL_SURNAME:
                    this.setLegalSurname(summerStudent, cell, header.getType());
                    break;
                case LEGAL_MIDDLE_NAME:
                    this.setMiddleName(summerStudent, cell, header.getType());
                    break;
                case LEGAL_FIRST_NAME:
                    this.setFirstName(summerStudent, cell, header.getType());
                    break;
                case DOB:
                    this.setDOB(summerStudent, cell, header.getType());
                    break;
                case COURSE:
                    this.setCourse(summerStudent, cell, header.getType());
                    break;
                case SESSION_DATE:
                    this.setSessionDate(summerStudent, cell, header.getType());
                    break;
                case FINAL_PERCENT:
                    this.setFinalPercent(summerStudent, cell, header.getType());
                    break;
                case FINAL_LETTER_GRADE:
                    this.setFinalLetterGrade(summerStudent, cell, header.getType());
                    break;
                case NO_OF_CREDITS:
                    this.setNoOfCredits(summerStudent, cell, header.getType());
                    break;
            }
        } else {
            log.debug("Header :: '{}' is not configured.", headersMap.get(cn));
        }
    }

    private void setSchoolCode(final SummerStudentData summerStudent, final Cell cell, final ColumnType columnType) {
        val fieldValue = this.getCellValueString(cell, columnType);
        summerStudent.setSchoolCode(fieldValue);
    }

    private void setPen(final SummerStudentData summerStudent, final Cell cell, final ColumnType columnType) {
        val fieldValue = this.getCellValueString(cell, columnType);
        summerStudent.setPen(fieldValue);
    }

    private void setLegalSurname(final SummerStudentData summerStudent, final Cell cell,  final ColumnType columnType) {
        val fieldValue = this.getCellValueString(cell, columnType);
        summerStudent.setLegalSurname(fieldValue);
    }

    private void setFirstName(final SummerStudentData summerStudent, final Cell cell,  final ColumnType columnType) {
        val fieldValue = this.getCellValueString(cell, columnType);
        summerStudent.setLegalFirstName(fieldValue);
    }

    private void setCourse(final SummerStudentData summerStudent, final Cell cell, final ColumnType columnType) {
        val fieldValue = this.getCellValueString(cell, columnType);
        summerStudent.setCourse(fieldValue);
    }
    private void setSessionDate(final SummerStudentData summerStudent, final Cell cell, final ColumnType columnType) {
        val fieldValue = this.getCellValueString(cell, columnType);
        summerStudent.setSessionDate(fieldValue);
    }
    private void setFinalPercent(final SummerStudentData summerStudent, final Cell cell, final ColumnType columnType) {
        val fieldValue = this.getCellValueString(cell, columnType);
        summerStudent.setFinalPercent(fieldValue);
    }
    private void setFinalLetterGrade(final SummerStudentData summerStudent, final Cell cell, final ColumnType columnType) {
        val fieldValue = this.getCellValueString(cell, columnType);
        summerStudent.setFinalLetterGrade(fieldValue);
    }
    private void setNoOfCredits(final SummerStudentData summerStudent, final Cell cell, final ColumnType columnType) {
        val fieldValue = this.getCellValueString(cell, columnType);
        summerStudent.setNoOfCredits(fieldValue);
    }
    private void setMiddleName(final SummerStudentData summerStudent, final Cell cell, final ColumnType columnType) {
        val fieldValue = this.getCellValueString(cell, columnType);
        summerStudent.setLegalMiddleName(fieldValue);
    }

    private void setDOB(final SummerStudentData summerStudent, final Cell cell, final ColumnType columnType) {
        val fieldValue = this.getCellValueString(cell, columnType);
        val birthDate = DOBUtil.getBirthDateFromString(fieldValue);
        if (birthDate.isPresent()) {
            summerStudent.setDob(new LocalDateMapper().map(birthDate.get()));
        } else {
            summerStudent.setDob(null);
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
