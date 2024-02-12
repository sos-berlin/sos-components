package com.sos.js7.converter.autosys.output.js7;

import java.util.List;

import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.commons.JS7ConverterResult;

public class ConverterResult {

    private final JS7ConverterResult result;
    private final List<ACommonJob> standaloneJobs;
    private final List<ACommonJob> boxJobs;

    public ConverterResult(JS7ConverterResult result, List<ACommonJob> standaloneJobs, List<ACommonJob> boxJobs) {
        this.result = result;
        this.standaloneJobs = standaloneJobs;
        this.boxJobs = boxJobs;
    }

    public JS7ConverterResult getResult() {
        return result;
    }

    public List<ACommonJob> getStandaloneJobs() {
        return standaloneJobs;
    }

    public List<ACommonJob> getBoxJobs() {
        return boxJobs;
    }

}
