package com.sos.js7.converter.autosys.common.v12.job;

import java.nio.file.Path;

public class JobFT extends ACommonFileWatcherJob {

    public JobFT(Path source, boolean reference) {
        super(source, ConverterJobType.FT, reference);
    }

}
