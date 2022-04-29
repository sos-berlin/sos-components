package com.sos.js7.converter.autosys.common.v12.job;

import com.sos.commons.util.common.SOSArgument;
import com.sos.js7.converter.autosys.common.v12.job.attr.AJobAttributes;
import com.sos.js7.converter.autosys.common.v12.job.attr.annotation.JobAttributeSetter;

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

    public JobFW() {
        super(ConverterJobType.FW);
    }

    public SOSArgument<Long> getWatchInterval() {
        return watchInterval;
    }

    @JobAttributeSetter(name = ATTR_WATCH_INTERVAL)
    public void setWatchInterval(String val) {
        watchInterval.setValue(AJobAttributes.longValue(val));
    }

}
