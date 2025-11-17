package ca.bc.gov.educ.graddatacollection.api.batch.constants;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum Headers {
  SCHOOL_CODE("School Code", ColumnType.STRING),
  PEN("PEN", ColumnType.STRING),
  LEGAL_SURNAME("Legal Surname", ColumnType.STRING),
  LEGAL_FIRST_NAME("Legal Given Name", ColumnType.STRING),
  LEGAL_MIDDLE_NAME("Legal Middle Name", ColumnType.STRING),
  DOB("Birthdate", ColumnType.DATE),
  COURSE_CODE("Ministry Course Code", ColumnType.STRING),
  COURSE_LEVEL("Ministry Course Level", ColumnType.STRING),
  SESSION_DATE("Session Date", ColumnType.STRING),
  FINAL_PERCENT("Final Percent", ColumnType.STRING),
  FINAL_LETTER_GRADE("Final Letter Grade", ColumnType.STRING),
  NO_OF_CREDITS("Credits", ColumnType.STRING);

  private final String code;
  private final ColumnType type;

  Headers(final String headerName, final ColumnType headerType) {
    this.code = headerName;
    this.type = headerType;
  }

  public static Optional<Headers> fromString(final String headerName) {
    return Arrays.stream(Headers.values()).filter(el -> headerName.equalsIgnoreCase(el.getCode())).findFirst();
  }
}
