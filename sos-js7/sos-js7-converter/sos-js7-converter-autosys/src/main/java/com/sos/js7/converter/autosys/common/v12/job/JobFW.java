package com.sos.js7.converter.autosys.common.v12.job;

import java.nio.file.Path;

import com.sos.commons.util.common.SOSArgument;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.annotation.ArgumentSetter;

public class JobFW extends ACommonFileWatcherJob {

    private static final String ATTR_WATCH_INTERVAL = "watch_interval";

    /** watch_interval - Specify the Frequency to Monitor a File<br/>
     * This attribute is optional for the FW job type.<br/>
     * 
     * Format: watch_interval: seconds<br/>
     * seconds - Specifies the frequency (in seconds) the File Watcher job checks for the existence and size of the watched file.<br/>
     * Default: 60<br/>
     * Limits: Up to 2147483647<br/>
     * <br/>
     * JS7 - 0% - For Windows JS7 Agents use the Windows API and receive information about incoming files in near real-time.<br/>
     * For Linux inotify is used that similarly provides instant information.<br/>
     * For other Unixes including MacOS the polling interval is 2s.<br/>
     * This behavior is not configurable. We have doubts if a development for configurability makes sense.<br/>
     */
    private SOSArgument<Long> watchInterval = new SOSArgument<>(ATTR_WATCH_INTERVAL, false);

    public JobFW(Path source) {
        super(source, ConverterJobType.FW);
    }

    public SOSArgument<Long> getWatchInterval() {
        return watchInterval;
    }

    @ArgumentSetter(name = ATTR_WATCH_INTERVAL)
    public void setWatchInterval(String val) {
        watchInterval.setValue(JS7ConverterHelper.longValue(val));
    }

}
