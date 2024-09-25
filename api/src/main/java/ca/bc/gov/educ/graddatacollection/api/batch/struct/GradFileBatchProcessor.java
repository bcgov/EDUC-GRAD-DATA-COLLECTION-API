package ca.bc.gov.educ.graddatacollection.api.batch.struct;

import ca.bc.gov.educ.graddatacollection.api.batch.exception.FileUnProcessableException;
import net.sf.flatpack.DataSet;

public interface GradFileBatchProcessor {
    void populateBatchFileAndLoadData(final String guid, final DataSet ds) throws FileUnProcessableException;
}
