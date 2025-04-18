package ca.bc.gov.educ.graddatacollection.api.util;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.*;

public class DOBUtil {

    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);

    private DOBUtil() {
    }

    public static boolean isValidDate(String dob) {
        if (StringUtils.isEmpty(dob)) {
            return false;
        }
        try {
            LocalDate.parse(dob, format);
        } catch (DateTimeParseException ex) {
            return false;
        }
        return true;
    }
}
