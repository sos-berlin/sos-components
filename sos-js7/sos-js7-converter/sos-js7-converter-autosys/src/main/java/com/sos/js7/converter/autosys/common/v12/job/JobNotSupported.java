package com.sos.js7.converter.autosys.common.v12.job;

import java.nio.file.Path;

public class JobNotSupported extends ACommonMachineJob {

    public JobNotSupported(Path source, boolean reference) {
        super(source, ConverterJobType.NOT_SUPPORTED, reference);
    }

}
