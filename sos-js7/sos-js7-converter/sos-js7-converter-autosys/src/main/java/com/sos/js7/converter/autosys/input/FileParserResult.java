package com.sos.js7.converter.autosys.input;

import java.util.List;

import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;

public class FileParserResult {

    private final List<ACommonJob> allJobs;

    public FileParserResult(List<ACommonJob> allJobs) {
        this.allJobs = allJobs;
    }

    public List<ACommonJob> getAllJobs() {
        return allJobs;
    }

}
