package com.sos.js7.converter.autosys.common.v12.job;

import com.sos.commons.util.common.SOSArgument;
import com.sos.js7.converter.autosys.common.v12.job.attr.AJobAttributes;
import com.sos.js7.converter.autosys.common.v12.job.attr.annotation.JobAttributeSetter;

public abstract class ACommonFileWatcherJob extends ACommonMachineJob {

    private static final String ATTR_WATCH_FILE = "watch_file";

    /** watch_file - This attribute is required for the following job types: FW,FT.<br/>
     * Format: watch_file: file<br/>
     * <br/>
     * JS7 - 100% - File Watching<br/>
     */
    private SOSArgument<String> watchFile = new SOSArgument<>(ATTR_WATCH_FILE, true);

    public ACommonFileWatcherJob(ConverterJobType type) {
        super(type);
    }

    public SOSArgument<String> getWatchFile() {
        return watchFile;
    }

    @JobAttributeSetter(name = ATTR_WATCH_FILE)
    public void setWatchFile(String val) {
        watchFile.setValue(AJobAttributes.stringValue(val));
    }

}
