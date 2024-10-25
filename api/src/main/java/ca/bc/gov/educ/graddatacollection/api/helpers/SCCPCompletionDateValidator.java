package ca.bc.gov.educ.graddatacollection.api.helpers;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.SCCPEffectiveDate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class SCCPCompletionDateValidator {

    private SCCPCompletionDateValidator() {}

    private static final DateTimeFormatter YYYYMMDD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Validates if the given date is in the format YYYYMMDD, represents a valid date,
     * and is on or after the SCCP Effective Date (2006-07-01). Future dates are allowed.
     *
     * @param date the date string in YYYYMMDD format
     * @return true if the date is valid and on or after 2006-07-01, false otherwise
     */
    public static boolean isValidYYYYMMDD(String date) {
        if (date == null || date.length() != 8) {
            return false;
        }

        try {
            LocalDate parsedDate = LocalDate.parse(date, YYYYMMDD_FORMATTER);
            LocalDate effectiveDate = LocalDate.parse(SCCPEffectiveDate.SCCP_EFFECTIVE_DATE.getDate(), YYYYMMDD_FORMATTER);
            return !parsedDate.isBefore(effectiveDate);
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
