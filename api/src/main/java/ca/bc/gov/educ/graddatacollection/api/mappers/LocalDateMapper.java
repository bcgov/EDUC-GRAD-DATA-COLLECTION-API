package ca.bc.gov.educ.graddatacollection.api.mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The type Local date time mapper.
 */
public class LocalDateMapper {

  /**
   * Map string.
   *
   * @param date the date
   * @return the string
   */
  public String map(LocalDate date) {
    if (date == null) {
      return null;
    }
    return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
  }

  /**
   * Map local date time.
   *
   * @param dateTime the date time
   * @return the local date time
   */
  public LocalDateTime map(String dateTime) {
    if (dateTime == null) {
      return null;
    }
    return LocalDateTime.parse(dateTime);
  }

}
