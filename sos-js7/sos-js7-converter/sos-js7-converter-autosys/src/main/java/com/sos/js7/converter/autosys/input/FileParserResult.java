package com.sos.js7.converter.autosys.input;

import java.util.List;

import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;

public class FileParserResult {

    private final List<ACommonJob> allJobs;
    private final List<JobBOX> boxJobsCopy;

    public FileParserResult(List<ACommonJob> allJobs, List<JobBOX> boxJobsCopy) {
        this.allJobs = allJobs;
        this.boxJobsCopy = boxJobsCopy;
    }

    public List<ACommonJob> getAllJobs() {
        return allJobs;
    }

    public List<JobBOX> getBoxJobsCopy() {
        return boxJobsCopy;
    }

}
