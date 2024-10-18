package ca.bc.gov.educ.graddatacollection.api.batch.constants;


import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum FileType {
    DEM("stddem", ".stddem", "demMapper.xml", "SHOULD BE 297"),
    XAM("stdxam", ".stdxam", "assessmentMapper.xml", "SHOULD BE 130"),
    CRS("stdcrs", ".stdcrs", "courseMapper.xml", "SHOULD BE 142")
    ;

    private final String code;
    private final String allowedExtensions;
    private final String mapperFileName;
    private final String detailedRecordSizeError;
    FileType(String code, String extension, String mapperFileName, String detailedRecordSizeError) {
        this.code = code;
        this.allowedExtensions = extension;
        this.mapperFileName = mapperFileName;
        this.detailedRecordSizeError = detailedRecordSizeError;
    }

    public static Optional<FileType> findByCode(String filetypeCode) {
        return Arrays.stream(values()).filter(filetype -> filetype.code.equalsIgnoreCase(filetypeCode)).findFirst();
    }
}
