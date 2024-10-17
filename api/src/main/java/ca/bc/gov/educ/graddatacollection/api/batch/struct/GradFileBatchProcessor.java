package ca.bc.gov.educ.graddatacollection.api.batch.struct;

import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.GradFileUpload;
import net.sf.flatpack.DataSet;

public interface GradFileBatchProcessor {
    void populateBatchFileAndLoadData(final String guid, final DataSet ds, final GradFileUpload fileUpload, final String schoolID) throws FileUnProcessableException;
}
