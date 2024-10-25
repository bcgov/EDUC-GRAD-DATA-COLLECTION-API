package ca.bc.gov.educ.graddatacollection.api.helpers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateValidator {

    private static final DateTimeFormatter YYYYMMDD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static boolean isValidYYYYMMDD(String date) {
        if (date == null || date.length() != 8) {
            return false;
        }

        try {
            LocalDate.parse(date, YYYYMMDD_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
