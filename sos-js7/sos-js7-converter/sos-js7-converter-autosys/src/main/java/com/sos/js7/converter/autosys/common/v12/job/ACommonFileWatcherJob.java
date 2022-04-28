package com.sos.js7.converter.autosys.common.v12.job;

import com.sos.commons.util.common.SOSArgument;
import com.sos.js7.converter.autosys.common.v12.job.attributes.AJobArguments;

public abstract class ACommonFileWatcherJob extends ACommonMachineJob {

    /** watch_file - This attribute is required for the following job types: FW,FT.<br/>
     * Format: watch_file: file<br/>
     * <br/>
     * JS7 - 100% - File Watching<br/>
     */
    private SOSArgument<String> watchFile = new SOSArgument<>("watch_file", true);

    public ACommonFileWatcherJob(JobType jobType) {
        super(jobType);
    }

    public SOSArgument<String> getWatchFile() {
        return watchFile;
    }

    public void setWatchFile(String val) {
        watchFile.setValue(AJobArguments.stringValue(val));
    }

}
