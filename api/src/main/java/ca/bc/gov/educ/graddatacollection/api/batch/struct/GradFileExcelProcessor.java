package ca.bc.gov.educ.graddatacollection.api.batch.struct;

import ca.bc.gov.educ.graddatacollection.api.batch.constants.ExcelFileType;
import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SummerStudentDataResponse;

import java.util.List;

public interface GradFileExcelProcessor {

    SummerStudentDataResponse extractData(String guid, byte[] fileContents, String schoolID, String districtID) throws FileUnProcessableException;
}
