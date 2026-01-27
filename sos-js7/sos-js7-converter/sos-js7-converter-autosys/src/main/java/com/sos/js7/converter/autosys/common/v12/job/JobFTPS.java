package com.sos.js7.converter.autosys.common.v12.job;

import java.nio.file.Path;

public class JobFTPS extends JobFTP {

    // ---------------------------------------------------------------------------------------------------------------------
    public JobFTPS(Path source, boolean reference) {
        super(source, ConverterJobType.FTPS, reference);
    }

}
