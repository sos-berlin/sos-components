package com.sos.js7.converter.autosys.common.v12.job;

import java.nio.file.Path;

public class JobNotSupported extends ACommonJob {

    public JobNotSupported(Path source) {
        super(source,ConverterJobType.NOT_SUPPORTED);
    }

}
