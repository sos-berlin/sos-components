package com.sos.js7.converter.autosys.output.js7.helper.beans;

import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;

public class BoardCrossInstance {

    private final ACommonJob job;
    private final String js7BoardName;

    public BoardCrossInstance(ACommonJob job, String js7BoardName) {
        this.job = job;
        this.js7BoardName = js7BoardName;
    }

    public ACommonJob getJob() {
        return job;
    }

    public String getJS7BoardName() {
        return js7BoardName;
    }

}
