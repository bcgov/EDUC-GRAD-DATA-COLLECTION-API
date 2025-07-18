package ca.bc.gov.educ.graddatacollection.api.batch.constants;


import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum FileType {
    DEM("dem", ".dem", "demMapper.xml", "SHOULD BE 297", 297),
    XAM("xam", ".xam", "assessmentMapper.xml", "SHOULD BE 130", 130),
    CRS("crs", ".crs", "courseMapper.xml", "SHOULD BE 142", 142)
    ;

    private final String code;
    private final String allowedExtensions;
    private final String mapperFileName;
    private final String detailedRecordSizeError;
    private final int actualFileSize;
    
    FileType(String code, String extension, String mapperFileName, String detailedRecordSizeError, int actualFileSize) {
        this.code = code;
        this.allowedExtensions = extension;
        this.mapperFileName = mapperFileName;
        this.detailedRecordSizeError = detailedRecordSizeError;
        this.actualFileSize = actualFileSize;
    }

    public static Optional<FileType> findByCode(String filetypeCode) {
        return Arrays.stream(values()).filter(filetype -> filetype.code.equalsIgnoreCase(filetypeCode)).findFirst();
    }
}
