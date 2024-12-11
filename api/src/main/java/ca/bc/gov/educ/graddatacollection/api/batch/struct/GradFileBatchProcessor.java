package ca.bc.gov.educ.graddatacollection.api.batch.struct;

import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import net.sf.flatpack.DataSet;

public interface GradFileBatchProcessor {
    IncomingFilesetEntity populateBatchFileAndLoadData(final String guid, final DataSet ds, final GradFileUpload fileUpload, final String schoolID, final String districtID) throws FileUnProcessableException;
}
