package ca.bc.gov.educ.graddatacollection.api.batch.constants;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum ExcelFileType {
  XLSX("xlsx", ".xlsx")
  ;

  private final String code;
  private final String allowedExtensions;

  ExcelFileType(final String fileType, final String extension) {
    this.code = fileType;
    this.allowedExtensions = extension;
  }

  public static Optional<ExcelFileType> findByCode(String filetypeCode) {
    return Arrays.stream(values()).filter(filetype -> filetype.code.equalsIgnoreCase(filetypeCode)).findFirst();
  }
}
