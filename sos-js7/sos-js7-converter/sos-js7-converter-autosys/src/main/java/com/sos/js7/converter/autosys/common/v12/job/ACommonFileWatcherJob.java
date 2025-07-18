package com.sos.js7.converter.autosys.common.v12.job;

import java.nio.file.Path;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.annotation.ArgumentSetter;

public abstract class ACommonFileWatcherJob extends ACommonMachineJob {

    private static final String ATTR_WATCH_FILE = "watch_file";

    /** watch_file - This attribute is required for the following job types: FW,FT.<br/>
     * Format: watch_file: file<br/>
     * <br/>
     * JS7 - 100% - File Watching<br/>
     */
    private SOSArgument<String> watchFile = new SOSArgument<>(ATTR_WATCH_FILE, true);

    public ACommonFileWatcherJob(Path source, ConverterJobType type, boolean reference) {
        super(source, type, reference);
    }

    public SOSArgument<String> getWatchFile() {
        return watchFile;
    }

    @ArgumentSetter(name = ATTR_WATCH_FILE)
    public void setWatchFile(String val) {
        watchFile.setValue(JS7ConverterHelper.stringValue(val));
    }

}
