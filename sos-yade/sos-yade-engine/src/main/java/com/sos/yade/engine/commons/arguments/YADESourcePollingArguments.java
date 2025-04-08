package com.sos.yade.engine.commons.arguments;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;

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

    private SOSArgument<Boolean> pollingWait4SourceFolder = new SOSArgument<>("WaitForSourceFolder", false, Boolean.valueOf(false));

    // TODO check -if can be set ...
    private SOSArgument<Boolean> waitingForLateComers = new SOSArgument<>("WaitingForLateComers", false, Boolean.valueOf(false));

    private SOSArgument<Integer> pollMinFiles = new SOSArgument<>("MinFiles", false);
    // minutes
    private SOSArgument<Integer> pollTimeout = new SOSArgument<>("PollTimeout", false);
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

    public SOSArgument<Boolean> getPollingWait4SourceFolder() {
        return pollingWait4SourceFolder;
    }

    public SOSArgument<Boolean> getWaitingForLateComers() {
        return waitingForLateComers;
    }

    public SOSArgument<Integer> getPollMinFiles() {
        return pollMinFiles;
    }

    public SOSArgument<Integer> getPollTimeout() {
        return pollTimeout;
    }

}
