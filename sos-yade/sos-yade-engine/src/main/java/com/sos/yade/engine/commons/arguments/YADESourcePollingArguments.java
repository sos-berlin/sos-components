package com.sos.yade.engine.commons.arguments;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgumentHelper;

public class YADESourcePollingArguments extends ASOSArguments {

    public static final long DEFAULT_POLL_INTERVAL = 60L;// seconds

    /** - Polling ------- */
    private SOSArgument<Boolean> pollingServer = new SOSArgument<>("PollingServer", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> pollingServerPollForever = new SOSArgument<>("PollForever", false, Boolean.valueOf(false));
    // TODO - time - YADE 1 default 0
    private SOSArgument<String> pollingServerDuration = new SOSArgument<>("PollingServerDuration", false);
    // seconds
    private SOSArgument<String> pollInterval = new SOSArgument<>("PollInterval", false, String.valueOf(DEFAULT_POLL_INTERVAL));
    // private SOSArgument<String> pollingDuration = new SOSArgument<>("pollingduration", false);// ??? is used - can be set in schema

    private SOSArgument<Boolean> waitForSourceFolder = new SOSArgument<>("WaitForSourceFolder", false, Boolean.valueOf(false));

    private SOSArgument<Integer> pollMinFiles = new SOSArgument<>("MinFiles", false);
    // minutes
    private SOSArgument<String> pollTimeout = new SOSArgument<>("PollTimeout", false);
    // internal
    private SOSArgument<Long> pollTimeoutAsSeconds = new SOSArgument<>(null, false, 0L);
    // declared by not used with YADE 1: polling_end_at, pollKeepConnection

    public boolean isPollMinFilesEnabled() {
        return pollMinFiles.getValue() != null;
    }

    public SOSArgument<Boolean> getPollingServer() {
        return pollingServer;
    }

    public SOSArgument<Boolean> getPollingServerPollForever() {
        return pollingServerPollForever;
    }

    public SOSArgument<String> getPollingServerDuration() {
        return pollingServerDuration;
    }

    public SOSArgument<String> getPollInterval() {
        return pollInterval;
    }

    public SOSArgument<Boolean> getWaitForSourceFolder() {
        return waitForSourceFolder;
    }

    public SOSArgument<Integer> getPollMinFiles() {
        return pollMinFiles;
    }

    public void setPollTimeoutValue(String val) {
        pollTimeout.setValue(val);
        pollTimeoutAsSeconds.setValue(calculatePollTimeout());
    }

    public String getPollTimeoutName() {
        return pollTimeout.getName();
    }

    public String getPollTimeoutValue() {
        return pollTimeout.getValue();
    }

    public SOSArgument<Long> getPollTimeoutAsSeconds() {
        return pollTimeoutAsSeconds;
    }

    private long calculatePollTimeout() {
        if (pollTimeout.isEmpty()) {
            return 0L;
        } else {
            // capability with previous behaviour - if only 1 <- minutes
            if (SOSString.isNumeric(pollTimeout.getValue())) {
                return Integer.valueOf(pollTimeout.getValue()) * 60;
            } else {
                return SOSArgumentHelper.asSeconds(pollTimeout, 0L);
            }
        }
    }

}
