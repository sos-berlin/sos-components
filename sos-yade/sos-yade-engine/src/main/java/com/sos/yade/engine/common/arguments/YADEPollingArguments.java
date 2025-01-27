package com.sos.yade.engine.common.arguments;

import com.sos.commons.util.common.SOSArgument;
import com.sos.yade.engine.common.handler.source.YADESourcePollingHandler;

public class YADEPollingArguments {

    /** - Polling ------- */
    private SOSArgument<Boolean> pollingServer = new SOSArgument<>("polling_server", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> pollingServerPollForever = new SOSArgument<>("polling_server_poll_forever", false, Boolean.valueOf(false));
    // TODO - time - YADE 1 default 0
    private SOSArgument<String> pollingServerDuration = new SOSArgument<>("polling_server_duration", false);
    // seconds
    private SOSArgument<String> pollInterval = new SOSArgument<>("poll_interval", false, String.valueOf(
            YADESourcePollingHandler.DEFAULT_POLL_INTERVAL));
    // private SOSArgument<String> pollingDuration = new SOSArgument<>("pollingduration", false);// ??? is used - can be set in schema

    private SOSArgument<Boolean> pollingWait4SourceFolder = new SOSArgument<>("polling_wait_4_source_folder", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> waitingForLateComers = new SOSArgument<>("waiting_for_late_comers", false, Boolean.valueOf(false));

    private SOSArgument<Integer> pollMinFiles = new SOSArgument<>("poll_minfiles", false);
    // minutes
    private SOSArgument<Integer> pollTimeout = new SOSArgument<>("poll_timeout", false);
    // declared by not used with YADE 1: polling_end_at, pollKeepConnection

    public boolean pollMinFiles() {
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
